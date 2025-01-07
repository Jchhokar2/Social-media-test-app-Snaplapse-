package com.example.snaplapse.camera

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.snaplapse.R

class DescriptionDialogFragment: DialogFragment() {
    var description: String = ""

    fun setDesc(desc: String) {
        description = desc
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_description_dialog, null)

            val descriptionText = view.findViewById<EditText>(R.id.descriptionTextArea)
            descriptionText.setText(description)

            builder.setView(view)
                .setTitle("Add Description")
                .setPositiveButton("Confirm", DialogInterface.OnClickListener { dialog, id ->
                    description = descriptionText.text.toString()
                    setFragmentResult("descriptionDialog", bundleOf("descriptionKey" to description))
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->

                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}