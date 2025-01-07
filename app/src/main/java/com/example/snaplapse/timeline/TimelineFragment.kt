package com.example.snaplapse.timeline

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snaplapse.MainActivity
import com.example.snaplapse.R
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.routes.LocationsApi
import com.example.snaplapse.api.routes.PhotosApi
import com.example.snaplapse.api.routes.UsersApi
import com.example.snaplapse.view_models.ItemsViewModel2
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class TimelineFragment(val locationId: Int) : Fragment() {
    private val photosApi = RetrofitHelper.getInstance().create(PhotosApi::class.java)
    private val locationsApi = RetrofitHelper.getInstance().create(LocationsApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_timeline, container, false)

        // getting the recyclerview by its id
        val recyclerview = view.findViewById<RecyclerView>(R.id.timeline_recycler_view)
        val backButton = view.findViewById<ImageButton>(R.id.timeline_back_button)
        val locationName = view.findViewById<TextView>(R.id.timeline)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(MainActivity())

        val data = mutableListOf<ItemsViewModel2>()
        val fragment = this
        lifecycleScope.launchWhenCreated {
            try {
                var page = 1

                while (true) {
                    val response = photosApi.getPhotosByLocation(locationId, "-created", null, page, visible = true)
                    if (response.isSuccessful) {
                        for (photo in response.body()!!.results) {
                            if (!photo.bitmap.isEmpty()) {
                                val decodedBitmap = Base64.decode(photo.bitmap, Base64.DEFAULT)
                                val bitmap =
                                    BitmapFactory.decodeByteArray(decodedBitmap, 0, decodedBitmap.size)
                                val parseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val printFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                                val date = LocalDate.parse(photo.created.substring(0, 10), parseFormat)
                                data.add(ItemsViewModel2(photo.id, photo.user, bitmap, photo.description, date.format(printFormat).toString(), photo.visible))
                            }
                        }
                    } else {
                        break
                    }
                    val adapter = CustomAdapter(data, parentFragmentManager, fragment)
                    recyclerview.adapter = adapter
                    page += 1
                }

                val locationResponse = locationsApi.getLocation(locationId)
                if (locationResponse.isSuccessful) {
                    locationName.text = locationResponse.body()!!.name
                }
            } catch (e: Exception) {
                Log.e("GetPhotoError", e.toString())
            }
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }
}
