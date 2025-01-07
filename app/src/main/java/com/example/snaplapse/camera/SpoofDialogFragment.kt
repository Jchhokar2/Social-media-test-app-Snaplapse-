package com.example.snaplapse.camera

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.snaplapse.R

class SpoofDialogFragment: DialogFragment() {

    private var latitude = 0F
    private var longitude = 0F
    private var enableSpoof = false

    fun setLatitude(lat: Float) {
        latitude = lat
    }

    fun setLongitude(long: Float) {
        longitude = long
    }

    fun setCheck(b: Boolean) {
        enableSpoof = b
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_spoof_dialog, null)

            val latitudeText = view.findViewById<EditText>(R.id.setLatitude)
            val longitudeText = view.findViewById<EditText>(R.id.setLongitude)
            val checkBox = view.findViewById<CheckBox>(R.id.enableSpoof)

            latitudeText.setText(latitude.toString())
            longitudeText.setText(longitude.toString())
            checkBox.isChecked = enableSpoof

            builder.setView(view)
                .setTitle("Location Spoofing")

                // Add action buttons
                .setPositiveButton("Confirm",
                    DialogInterface.OnClickListener { dialog, id ->
                        latitude = latitudeText.text.toString().toFloat()
                        longitude = longitudeText.text.toString().toFloat()
                        setFragmentResult("spoofDialog", bundleOf("latitude" to latitude, "longitude" to longitude, "spoof" to checkBox.isChecked))
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->

                    })

            builder.create()
        }  ?: throw IllegalStateException("Activity cannot be null")
    }
}