package ru.hukutoc2288.apod

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso
import ru.hukutoc2288.apod.api.ApodEntry
import ru.hukutoc2288.simplepermissionsdispatcher.SimpleNeverAskDialogFragment
import ru.hukutoc2288.simplepermissionsdispatcher.SimplePermissionsDispatcher
import ru.hukutoc2288.simplepermissionsdispatcher.SimpleRationaleDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class ImageViewActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: FrameLayout
    private lateinit var fullscreenContentControls: ConstraintLayout
    private lateinit var descriptionTextView: TextView
    private lateinit var pictureView: PhotoView
    private lateinit var pictureLoader: ProgressBar
    private lateinit var toolbar: Toolbar

    private val humanDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    private val urlDateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
    private lateinit var entry: ApodEntry

    private val hideHandler = Handler()

    private val storagePermissionDispatcher =
        object : SimplePermissionsDispatcher(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            override fun onPermissionDenied() {
                Toast.makeText(applicationContext, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show()
            }

            override fun onNeverAskAgain() {
                //Toast.makeText(applicationContext, R.string.permission_storage_never_again, Toast.LENGTH_SHORT).show()
                SimpleNeverAskDialogFragment(
                    this,
                    getString(R.string.permission_storage_dialog_title),
                    getString(R.string.permission_storage_never_again)
                ).show(supportFragmentManager, "neverAgainDialog")
            }

            override fun onShowRationale() {
                SimpleRationaleDialogFragment(
                    this,
                    getString(R.string.permission_storage_dialog_title),
                    getString(R.string.permission_storage_dialog_text)
                ).show(supportFragmentManager, "rationaleDialog")
            }

        }

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> {
                view.performClick()
            }
            else -> {

            }
        }
        super.onTouchEvent(motionEvent)
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        fullscreenContent = findViewById(R.id.fullscreen_content)
        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)
        descriptionTextView = findViewById(R.id.description)
        pictureView = findViewById(R.id.picture)
        pictureLoader = findViewById(R.id.image_loader)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        pictureView.setOnClickListener {
            toggle()
        }
        entry = intent.getParcelableExtra<ApodEntry>(APOD_ENTRY_EXTRA_KEY)!!
        //val titleString = if (entry.title != null) entry.title + "\n" else ""
        val dateString = if (entry.date != null) humanDateFormat.format(entry.date!!) + "\n" else ""
        val copyrightString = entry.copyright ?: ""
        descriptionTextView.text = dateString + copyrightString
        supportActionBar?.title = entry.title

        Picasso.get().load(entry.url).into(pictureView, object : com.squareup.picasso.Callback {
            override fun onSuccess() {
                pictureView.visibility = View.VISIBLE
                pictureLoader.visibility = View.GONE
                delayedHide(1000)
            }

            override fun onError(e: Exception?) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
// Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.picture_view, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> ShareCompat.IntentBuilder(this)
                .setType("text/plain")
                .setChooserTitle(getString(R.string.share_picture_title))
                .setText(
                    getString(R.string.share_picture_message).format(
                        entry.title,
                        urlDateFormat.format(entry.date!!)
                    )
                )
                .startChooser()
            R.id.action_download ->
                // TODO: huku 12.07.2021 process storage permission without this dumb library
                if (entry.url != null)
                    storagePermissionDispatcher.executeWithPermission(this) {
                        downloadImage(entry.url!!)
                    }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        // delayedHide(AUTO_HIDE_DELAY_MILLIS)
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        storagePermissionDispatcher.onRequestPermissionsResult(this, grantResults)
    }

    fun downloadImage(url: String) {
        val downloadReference: Long

        // Create request for android download manager
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(url))
        //Set the title of this download, to be displayed in notifications (if enabled).
        request.setTitle("Downloading")
        //Set a description of this download, to be displayed in notifications (if enabled)
        request.setDescription("Downloading File")
        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            url.substring(url.lastIndexOf('/') + 1)
        )
        request.setShowRunningNotification(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadReference = downloadManager.enqueue(request)
    }
}