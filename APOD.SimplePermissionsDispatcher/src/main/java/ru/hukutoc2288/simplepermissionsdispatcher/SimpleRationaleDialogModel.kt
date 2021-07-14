package ru.hukutoc2288.simplepermissionsdispatcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SimpleRationaleDialogModel(application: Application) : AndroidViewModel(application) {
    var initialized = false
    lateinit var title: String
    lateinit var message: String
    lateinit var dispatcher: SimplePermissionsDispatcher

    fun init(dispatcher: SimplePermissionsDispatcher, title: String, message: String){
        initialized = true
        this.dispatcher = dispatcher
        this.title = title
        this.message = message
    }
}