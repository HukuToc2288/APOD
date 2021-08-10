package ru.hukutoc2288.apod

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.hukutoc2288.apod.api.ApodEntry
import ru.hukutoc2288.apod.api.MediaTypes
import ru.hukutoc2288.apod.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

const val APOD_ENTRY_EXTRA_KEY = "apodEntryKey"


class MainActivity : AppCompatActivity() {

    private lateinit var model: MainModel
    private lateinit var dateFormat: SimpleDateFormat

    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    // for debugging purposes as internet connection is limited
    // 09.07.2021 huku
    private var shouldLoadImages = true
    private var currentDisplayingEntry: ApodEntry? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model = ViewModelProvider(this).get(MainModel::class.java)

        dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        val savedEntry = savedInstanceState?.getParcelable<ApodEntry>(APOD_ENTRY_EXTRA_KEY)
        if (savedEntry != null) {
            inflateViewsWithResponse(savedEntry)
        } else {
            loadByDate()
        }

        initSpeedDial()
    }

    fun onFabClick(view: View) {
        apodApi.getRandom().enqueue(object : Callback<List<ApodEntry>> {
            override fun onResponse(call: Call<List<ApodEntry>>, response: Response<List<ApodEntry>>) {
                if (!response.isSuccessful)
                    return
                val entry = response.body()
                if (entry.isNotEmpty())
                    inflateViewsWithResponse(entry[0])
            }

            override fun onFailure(call: Call<List<ApodEntry>>?, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun inflateViewsWithResponse(entry: ApodEntry) {
        currentDisplayingEntry = entry
        if (entry.date == null) {
            binding.date.text = ""
        } else {
            if (DateUtils.isToday(entry.date.time)) {
                binding.date.text = getString(R.string.date_today)
            } else {
                binding.date.text = dateFormat.format(entry.date)
            }
        }
        binding.description.visibility = View.VISIBLE
        binding.title.text = entry.title
        binding.description.text = entry.explanation

        if (shouldLoadImages) {
            if (entry.mediaType == MediaTypes.IMAGE) {
                Picasso.get().load(entry.url).into(binding.picture, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        binding.picture.visibility = View.VISIBLE
                        binding.pictureLoader.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {

                    }
                })
            } else if (entry.mediaType == MediaTypes.VIDEO) {
                val videoName = Uri.parse(entry.url).lastPathSegment
                Picasso.get().load(String.format(getString(R.string.youtube_thumbnail_base_url), videoName)).into(
                    binding.picture,
                    object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            binding.picture.visibility = View.VISIBLE
                            binding.pictureLoader.visibility = View.GONE
                            binding.youtubePlayButton.visibility = View.VISIBLE
                        }

                        override fun onError(e: Exception?) {

                        }
                    })
            }
        }
    }

    fun onPictureClick(view: View) {
        currentDisplayingEntry?.let {
            if (it.mediaType == MediaTypes.IMAGE) {
                val intent = Intent(this, ImageViewActivity::class.java)
                intent.putExtra(APOD_ENTRY_EXTRA_KEY, currentDisplayingEntry)
                startActivity(intent)
            } else if (it.mediaType == MediaTypes.VIDEO) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
                startActivity(browserIntent)
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putParcelable(APOD_ENTRY_EXTRA_KEY, currentDisplayingEntry)
        }
        super.onSaveInstanceState(outState)
    }

    private fun initSpeedDial() {
        // today
        // random
        // by date
        binding.fab.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_action_today, R.drawable.ic_fab_today)
                .setFabBackgroundColor(getColor(R.color.color_fab_subbutton))
                .setLabel(getString(R.string.action_today))
                .setFabImageTintColor(Color.WHITE)
                .create()
        )
        binding.fab.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_action_random, R.drawable.ic_fab_random)
                .setFabBackgroundColor(getColor(R.color.color_fab_subbutton))
                .setLabel(getString(R.string.action_random))
                .setFabImageTintColor(Color.WHITE)
                .create()
        )
        binding.fab.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_action_date, R.drawable.ic_fab_date)
                .setFabBackgroundColor(getColor(R.color.color_fab_subbutton))
                .setLabel(getString(R.string.action_by_date))
                .setFabImageTintColor(Color.WHITE)
                .create()
        )

        binding.fab.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_action_today -> {
                    loadByDate()
                }
                R.id.fab_action_random -> {
                    loadRandom()
                }
                R.id.fab_action_date -> {
                    showDatePickerDialog()
                }
            }

            binding.fab.close() // To close the Speed Dial with animation
            return@OnActionSelectedListener true // false will close it without animation
        })
    }

    fun loadByDate(date: String? = null) {
        binding.picture.visibility = View.GONE
        binding.youtubePlayButton.visibility = View.GONE
        binding.pictureLoader.visibility = View.VISIBLE
        binding.description.visibility = View.GONE
        apodApi.getByDate(date = date).enqueue(object : Callback<ApodEntry> {
            override fun onResponse(call: Call<ApodEntry>, response: Response<ApodEntry>) {
                if (!response.isSuccessful)
                    return
                val entry = response.body()
                inflateViewsWithResponse(entry)
            }

            override fun onFailure(call: Call<ApodEntry>?, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    fun loadRandom() {
        binding.picture.visibility = View.GONE
        binding.youtubePlayButton.visibility = View.GONE
        binding.pictureLoader.visibility = View.VISIBLE
        binding.description.visibility = View.GONE
        apodApi.getRandom().enqueue(object : Callback<List<ApodEntry>> {
            override fun onResponse(call: Call<List<ApodEntry>>, response: Response<List<ApodEntry>>) {
                if (!response.isSuccessful)
                    return
                val entry = response.body()
                if (entry.isNotEmpty())
                    inflateViewsWithResponse(entry[0])
            }

            override fun onFailure(call: Call<List<ApodEntry>>?, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(this)
        datePickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            // The first day an APOD picture was posted
            // 19.07.2021 huku
            calendar.set(year, month, dayOfMonth)
            Log.d("selectedDate", apiDateFormat.format(calendar.time))
            loadByDate(apiDateFormat.format(calendar.time))
        }

        val calendar = Calendar.getInstance()
        // The first day an APOD picture was posted
        // 19.07.2021 huku
        calendar.set(1995, 6, 16)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}