package ru.hukutoc2288.simplepermissionsdispatcher

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

class SimpleNeverAskDialogFragment(): DialogFragment() {
    private lateinit var viewModel: SimpleRationaleDialogModel

    private lateinit var dispatcher: SimplePermissionsDispatcher
    private lateinit var title: String
    private lateinit var message: String

    constructor(dispatcher: SimplePermissionsDispatcher, title: String, message: String): this(){
        this.dispatcher = dispatcher
        this.title = title
        this.message = message
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // There are no request codes
        if (ContextCompat.checkSelfPermission(requireContext(), dispatcher.permission) == PackageManager.PERMISSION_GRANTED) {
            dismiss()
            dispatcher.currentRunnable.run()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("DialogFragment", "onAttach")
        viewModel = ViewModelProvider(requireActivity()).get(SimpleRationaleDialogModel::class.java)
        if (!viewModel.initialized){
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
                .setPositiveButton(getString(R.string.never_again_dialog_go_settings)) { _, _ ->

                }
                .setNegativeButton(getString(R.string.never_again_dialog_go_dismiss)){ dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart() //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton: Button = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            positiveButton.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                resultLauncher.launch(intent)
            }
        }
    }
}