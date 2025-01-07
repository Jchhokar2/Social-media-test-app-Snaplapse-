package com.example.snaplapse.image_details

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.snaplapse.R

class FlagDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            builder.setTitle(R.string.flag_dialog_title)
            builder.setView(inflater.inflate(R.layout.flag_dialog, null))
                .setPositiveButton(R.string.flag_dialog_confirm, DialogInterface.OnClickListener { dialog, id ->
                    Toast.makeText(activity, resources.getString(R.string.flag_toast), Toast.LENGTH_SHORT).show()
                })
                .setNegativeButton(R.string.flag_dialog_cancel, DialogInterface.OnClickListener { dialog, id ->

                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
