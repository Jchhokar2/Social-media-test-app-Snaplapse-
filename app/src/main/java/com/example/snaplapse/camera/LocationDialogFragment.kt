package com.example.snaplapse.camera

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snaplapse.MainActivity
import com.example.snaplapse.R
import com.example.snaplapse.view_models.CurrentPlaceViewModel

class LocationDialogFragment: DialogFragment(), LocationAdapter.RadioButtonCallback {
    var currentItem: Int = 0
    var items = mutableListOf<CurrentPlaceViewModel>()

    fun setItem(index: Int) {
        currentItem = index
    }

    fun setList(list: List<CurrentPlaceViewModel>) {
        for (i in list) {
            items.add(i)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_location_dialog, null)

            val recyclerview = view.findViewById<RecyclerView>(R.id.location_recycler_view)
            recyclerview.layoutManager = LinearLayoutManager(MainActivity())
            val adapter = LocationAdapter(items, currentItem, this)
            recyclerview.adapter = adapter

            builder.setView(view)
                .setTitle("Select Location")

                .setPositiveButton("Confirm", DialogInterface.OnClickListener { dialog, id ->
                    setFragmentResult("locationDialog", bundleOf("locationKey" to currentItem))
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun changeLocation(location: Int) {
        currentItem = location
        Log.i("location", location.toString())
    }
}