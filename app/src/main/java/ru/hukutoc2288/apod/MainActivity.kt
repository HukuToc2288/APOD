package ru.hukutoc2288.apod

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Retrofit

lateinit var fab: FloatingActionButton
lateinit var nestedScroll: NestedScrollView
lateinit var pictureView: ImageView
lateinit var titleTextView: TextView
lateinit var dateTextView: TextView
lateinit var model: MainModel

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
        var response = apodApi.getData(API_KEY)
    }
}