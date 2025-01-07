package com.example.snaplapse.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.snaplapse.BuildConfig
import com.example.snaplapse.R
import com.example.snaplapse.timeline.TimelineFragment
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.routes.LocationsApi
import com.example.snaplapse.api.routes.PhotosApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var safeContext: Context

    private lateinit var map: GoogleMap
    private var cameraPosition: CameraPosition? = null

    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    private var marker: Marker? = null

    private var lastKnownLocation: Location? = null

    private val locationsApi = RetrofitHelper.getInstance().create(LocationsApi::class.java)
    private val photosApi = RetrofitHelper.getInstance().create(PhotosApi::class.java)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        Places.initialize(safeContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(safeContext)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(safeContext)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    requireActivity().findViewById<FrameLayout>(R.id.map), false)
                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet
                return infoWindow
            }
        })

        // Set a listener for marker click.
        map.setOnMarkerClickListener(this)

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
        showPlaceMarkers()
    }

    /** Called when the user clicks a marker.  */
    override fun onMarkerClick(marker: Marker): Boolean {
        val locationId = marker.tag as Int
        val transaction = parentFragmentManager.beginTransaction()
        transaction.hide(this)
        transaction.add(R.id.fragmentContainerView, TimelineFragment(locationId))
        transaction.addToBackStack(null)
        transaction.commit()

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur
        return false
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (locationPermissionGranted) {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                    }
                } else {
                    map.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                    map.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        }
    }

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

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES, Place.Field.ID)

            // Use the builder to create a FindCurrentPlaceRequest.
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlace = task.result.placeLikelihoods[0].place
                    var markerSnippet = likelyPlace.address
                    if (likelyPlace.attributions != null) {
                        markerSnippet = """
                        $markerSnippet
                        ${likelyPlace.attributions}
                        """.trimIndent()
                    }

                    // Add a marker for the selected place, with an info window
                    // showing information about that place.
                    marker?.remove()
                    marker = map.addMarker(
                        MarkerOptions()
                            .title(likelyPlace.name)
                            .position(likelyPlace.latLng!!)
                            .snippet(markerSnippet)
                    )

                    // Position the map's camera at the location of the marker.
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            likelyPlace.latLng!!,
                            DEFAULT_ZOOM.toFloat()
                        )
                    )
                }
            }
        } else {
            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(MarkerOptions()
                .title("Default Location")
                .position(defaultLocation)
                .snippet("No places found, because location permission is disabled."))

            // Prompt the user for permission.
            getLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showPlaceMarkers() {
        lifecycleScope.launchWhenCreated {
            try {
                var page = 1 //paginate for all 20 recommendations
                while (true) {
                    //                val coordinates = "43.4723,-80.5449" // UWaterloo coordinates for testing

                    val locResponse = locationsApi.getLocations(page=page)

                    if (locResponse.isSuccessful) {
                        for (location in locResponse.body()!!.results) {
                            val photosResponse = photosApi.getPhotoCount(location.id, visible=true)
                            if (photosResponse.isSuccessful) {
                                if (photosResponse.body()!!.count > 0) {
                                    val marker1 = LatLng(location.latitude, location.longitude)
                                    var place = map.addMarker(
                                        MarkerOptions()
                                            .position(marker1)
                                            .title(location.name)
                                    )
                                    place?.tag = location.id // use the location id here to reference the location when clicked

                                }
                            }
                        }
                        page += 1
                    } else {
                        break
                    }
                }
            }catch (e: Exception) {
                Log.e("MapError", e.toString())
            }
        }
    }

        @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (locationPermissionGranted) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        } else {
            map.isMyLocationEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false
            lastKnownLocation = null
            getLocationPermission()
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}
