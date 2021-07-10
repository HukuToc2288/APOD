package ru.hukutoc2288.apod

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.transition.Visibility
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

const val APOD_ENTRY_EXTRA_KEY = "apodEntryKey"


class MainActivity : AppCompatActivity() {
    lateinit var fab: FloatingActionButton
    lateinit var nestedScroll: NestedScrollView
    lateinit var pictureView: ImageView
    lateinit var titleTextView: TextView
    lateinit var dateTextView: TextView
    lateinit var model: MainModel
    lateinit var pictureLoader: ProgressBar
    lateinit var descriptionTextView: TextView
    lateinit var dateFormat: SimpleDateFormat
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    // for debugging purposes as internet connection is limited
// 09.07.2021 huku
    var shouldLoadImages = true
    lateinit var currentDisplayingEntry: ApodEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        model = ViewModelProvider(this).get(MainModel::class.java)

        nestedScroll = findViewById(R.id.nested_scroll)
        fab = findViewById(R.id.fab)
        pictureView = findViewById(R.id.picture)
        titleTextView = findViewById(R.id.title)
        dateTextView = findViewById(R.id.date)
        pictureLoader = findViewById(R.id.image_loader)
        descriptionTextView = findViewById(R.id.description)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        var response = apodApi.getToday().enqueue(object : Callback<ApodEntry> {
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

    fun onFabClick(view: View) {
        var response = apodApi.getRandom().enqueue(object : Callback<List<ApodEntry>> {
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
        pictureView.visibility = View.GONE
        pictureLoader.visibility = View.VISIBLE
        if (entry.date == null) {
            dateTextView.text = ""
        } else {
            if (DateUtils.isToday(entry.date.time)) {
                dateTextView.text = getString(R.string.date_today)
            } else {
                dateTextView.text = dateFormat.format(entry.date)
            }
        }
        titleTextView.text = entry.title
        descriptionTextView.text = entry.explanation

        if (shouldLoadImages) {
            Picasso.get().load(entry.url).into(pictureView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    pictureView.visibility = View.VISIBLE
                    pictureLoader.visibility = View.GONE
                }

                override fun onError(e: Exception?) {

                }
            })
        }
    }

    fun onPictureClick(view: View) {
        val intent = Intent(this,ImageViewActivity::class.java)
        intent.putExtra(APOD_ENTRY_EXTRA_KEY, currentDisplayingEntry)
        startActivity(intent)
    }
}