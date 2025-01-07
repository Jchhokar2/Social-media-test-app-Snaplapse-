package com.example.snaplapse.for_you

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.snaplapse.R

class RangeDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            val sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
            val view = inflater.inflate(R.layout.fragment_range_dialog, null)

            val rangeText = view.findViewById<EditText>(R.id.set_fyp_range)
            if (sharedPref!!.contains("range")) {
                rangeText.setText(sharedPref?.getInt("range", 0)!!.div(1000).toString())
            }

            builder.setView(view)
                .setTitle("Change Range")

                // Add action buttons
                .setPositiveButton("Confirm",
                    DialogInterface.OnClickListener { dialog, id ->
                        with(sharedPref?.edit()) {
                            if (rangeText.text.isNotEmpty()) {
                                this?.putInt("range", rangeText.text.toString().toInt() * 1000)
                                this?.apply()
                            } else {
                                this?.remove("range")
                                this?.apply()
                            }

                            val f: ForYouFragment = parentFragment as ForYouFragment
                            f.view?.let { it1 -> f.handleRecommendations(it1) }
                        }
                    })
                .setNegativeButton("Cancel", null)

            builder.create()
        }  ?: throw IllegalStateException("Activity cannot be null")
    }
}