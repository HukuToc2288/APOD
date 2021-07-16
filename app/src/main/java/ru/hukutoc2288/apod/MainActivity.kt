package ru.hukutoc2288.apod

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.text.format.DateUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.hukutoc2288.apod.api.ApodEntry
import ru.hukutoc2288.apod.api.MediaTypes
import java.text.SimpleDateFormat
import java.util.*

const val APOD_ENTRY_EXTRA_KEY = "apodEntryKey"


class MainActivity : AppCompatActivity() {
    private lateinit var fab: FloatingActionButton
    private lateinit var nestedScroll: NestedScrollView
    private lateinit var pictureView: ImageView
    private lateinit var videoPlayButton: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var model: MainModel
    private lateinit var pictureLoader: ProgressBar
    private lateinit var descriptionTextView: TextView
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var toolbar: Toolbar

    // for debugging purposes as internet connection is limited
    // 09.07.2021 huku
    private var shouldLoadImages = true
    private var currentDisplayingEntry: ApodEntry? = null

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
        videoPlayButton = findViewById(R.id.youtube_play_button)

        dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        val savedEntry = savedInstanceState?.getParcelable<ApodEntry>(APOD_ENTRY_EXTRA_KEY)
        if (savedEntry != null) {
            inflateViewsWithResponse(savedEntry)
        } else {
            apodApi.getToday().enqueue(object : Callback<ApodEntry> {
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
        pictureView.visibility = View.GONE
        videoPlayButton.visibility = View.GONE
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
            if (entry.mediaType == MediaTypes.IMAGE) {
                Picasso.get().load(entry.url).into(pictureView, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        pictureView.visibility = View.VISIBLE
                        pictureLoader.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {

                    }
                })
            } else if (entry.mediaType == MediaTypes.VIDEO) {
                val videoName = Uri.parse(entry.url).lastPathSegment
                Picasso.get().load(String.format(getString(R.string.youtube_thumbnail_base_url), videoName)).into(
                    pictureView,
                    object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            pictureView.visibility = View.VISIBLE
                            pictureLoader.visibility = View.GONE
                            videoPlayButton.visibility = View.VISIBLE
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
}