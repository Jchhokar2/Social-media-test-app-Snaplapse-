package com.example.snaplapse.for_you

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snaplapse.BuildConfig
import com.example.snaplapse.MainActivity
import com.example.snaplapse.R
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.routes.LocationsApi
import com.example.snaplapse.api.routes.PhotosApi
import com.example.snaplapse.databinding.FragmentForYouBinding
import com.example.snaplapse.view_models.ForYouViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class ForYouFragment : Fragment() {
    private var fragment = this
    private lateinit var safeContext: Context

    private lateinit var adapter: ForYouAdapter
    private lateinit var _binding: FragmentForYouBinding
    private val binding get() = _binding

    private lateinit var placesClient: PlacesClient
    private var locationPermissionGranted = false
    private var sharedPref: SharedPreferences? = null

    private val locationsApi = RetrofitHelper.getInstance().create(LocationsApi::class.java)
    private val photosApi = RetrofitHelper.getInstance().create(PhotosApi::class.java)
    private var userID: Int = 0
    private var cards: ArrayList<ForYouViewModel> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getLocationPermission()

        _binding = FragmentForYouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)

        userID = sharedPref?.getInt("id", 0)!!
        binding.rangeButton.setOnClickListener { RangeDialogFragment().show(childFragmentManager, "") }

        handleRecommendations(view)
    }

    @SuppressLint("MissingPermission")
    fun handleRecommendations (view: View) {
        if (this::adapter.isInitialized) {
            adapter.notifyItemRangeRemoved(0, cards.size)
        }
        cards.clear()

        var range = if (sharedPref!!.contains("range")) {
            sharedPref?.getInt("range", 0)!!
        } else {
            5000 // default radius 5km
        }
        var currentCoordinates: String

        if (locationPermissionGranted) {
            Places.initialize(safeContext, BuildConfig.MAPS_API_KEY)
            placesClient = Places.createClient(safeContext)
            // Use fields to define the data types to return.
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES, Place.Field.ID)

            // Use the builder to create a FindCurrentPlaceRequest.
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlace = task.result.placeLikelihoods[0].place
                    currentCoordinates = likelyPlace.latLng.latitude.toString() + "," + likelyPlace.latLng.longitude.toString()

                    recommendations(view, currentCoordinates, range)
                }
            }
        } else {
            getLocationPermission()
        }
    }

    private fun recommendations(view: View, currentCoordinates: String, range: Int) {
        lifecycleScope.launchWhenCreated {
            try {
                val recyclerView = view.findViewById<RecyclerView>(R.id.for_you_view)
                recyclerView.layoutManager = LinearLayoutManager(MainActivity())

                var page = 1 //paginate for all 20 recommendations
                var quickLoadCounter = 1
                while (true) {
    //                val coordinates = "43.4723,-80.5449" // UWaterloo coordinates for testing
                    val recResponse = locationsApi.getRecommendations(userID, currentCoordinates, range, 20, page=page)
                    if (recResponse.isSuccessful) {
                        for (location in recResponse.body()!!.results) {
                            val photosResponse = photosApi.getPhotosByLocation(location.id, "-created", 1, visible = true)
                            val photos = photosResponse.body()?.results
                            if (photos?.isNotEmpty() == true) {
                                val mostRecentPhoto = photos[0]
                                val bmpRaw = mostRecentPhoto?.bitmap
                                val imageBytes = Base64.decode(bmpRaw, 0)
                                var image =
                                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                                val card = ForYouViewModel(image, location.name, location.id)
                                cards.add(card)
                                if (quickLoadCounter == 3) {
                                    val adapter = ForYouAdapter(cards, parentFragmentManager, fragment)
                                    recyclerView.adapter = adapter
                                }
                                quickLoadCounter += 1
                            }
                        }
                    } else {
                        break
                    }

                    val adapter = ForYouAdapter(cards, parentFragmentManager, fragment)
                    recyclerView.adapter = adapter
                    page += 1
                }

            } catch (e: Exception) {
                Log.e("ForYouPageError", e.toString())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(safeContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}
