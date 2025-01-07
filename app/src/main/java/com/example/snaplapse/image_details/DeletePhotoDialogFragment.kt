package com.example.snaplapse.image_details

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.snaplapse.R
import com.example.snaplapse.login.LoginActivity

class DeletePhotoDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete This Photo?")
            builder
                .setPositiveButton("Confirm", DialogInterface.OnClickListener { dialog, id ->
                    setFragmentResult("deletePhoto", bundleOf())
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->

                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
