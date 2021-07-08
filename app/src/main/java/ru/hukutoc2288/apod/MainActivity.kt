package ru.hukutoc2288.apod

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
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

lateinit var fab: FloatingActionButton
lateinit var nestedScroll: NestedScrollView
lateinit var pictureView: ImageView
lateinit var titleTextView: TextView
lateinit var dateTextView: TextView
lateinit var model: MainModel
lateinit var pictureLoader: ProgressBar
lateinit var descriptionTextView: TextView

class MainActivity : AppCompatActivity() {
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

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        var response = apodApi.getToday().enqueue(object : Callback<ApodEntry> {
            override fun onResponse(call: Call<ApodEntry>?, response: Response<ApodEntry>?) {
                val entry = response!!.body()
                if (DateUtils.isToday(entry.date.time)) {
                    dateTextView.text = getString(R.string.date_today)
                } else {
                    dateTextView.text = dateFormat.format(entry.date)
                }
                titleTextView.text = entry.title
                descriptionTextView.text = entry.explanation

                Picasso.get().load(entry.url).into(pictureView, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        pictureView.visibility = View.VISIBLE
                        pictureLoader.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {

                    }
                })
            }

            override fun onFailure(call: Call<ApodEntry>?, t: Throwable) {
                t.printStackTrace()
            }

        })
    }
}