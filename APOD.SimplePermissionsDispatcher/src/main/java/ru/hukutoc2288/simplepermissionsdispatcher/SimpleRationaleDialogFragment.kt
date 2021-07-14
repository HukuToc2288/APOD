package ru.hukutoc2288.simplepermissionsdispatcher

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

class SimpleRationaleDialogFragment() : DialogFragment() {
    private lateinit var viewModel: SimpleRationaleDialogModel

    private lateinit var dispatcher: SimplePermissionsDispatcher
    private lateinit var title: String
    private lateinit var message: String

    constructor(dispatcher: SimplePermissionsDispatcher, title: String, message: String) : this() {
        this.dispatcher = dispatcher
        this.title = title
        this.message = message
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("DialogFragment", "onAttach")
        viewModel = ViewModelProvider(requireActivity()).get(SimpleRationaleDialogModel::class.java)
        if (!viewModel.initialized) {
            Log.d("DialogFragment", "ViewModel initialized")
            viewModel.init(dispatcher, title, message)
        } else {
            Log.d("DialogFragment", "ViewModel already exist")
            dispatcher = viewModel.dispatcher
            title = viewModel.title
            message = viewModel.message
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.rationale_dialog_confirm)
                ){ dialog, _ ->
                    dispatcher.proceedAfterRationale(it)
                    dialog.dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dispatcher.proceedAfterRationale(requireActivity())
    }
}