package com.example.snaplapse.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.snaplapse.BuildConfig
import com.example.snaplapse.R
import com.example.snaplapse.api.MapHelper
import com.example.snaplapse.api.routes.MapsApi
import com.example.snaplapse.databinding.FragmentCameraBinding
import com.example.snaplapse.view_models.CameraViewModel
import com.example.snaplapse.view_models.CurrentPlaceViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.json.JSONObject

class CameraFragment : Fragment() {
    private lateinit var safeContext: Context

    private lateinit var _binding: FragmentCameraBinding
    private val binding get() = _binding

    private val viewModel: CameraViewModel by activityViewModels()
    private val mapsApi = MapHelper.getInstance().create(MapsApi::class.java)

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var placesClient: PlacesClient
    private var locationPermissionGranted = false
    private var sharedPref: SharedPreferences? = null

    private var latitude = 0F
    private var longitude = 0F
    private var spoof = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getLocationPermission()

        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            val requestMultiplePermissions = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions.containsValue(false)) {
                    Toast.makeText(safeContext, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                } else {
                    startCamera()
                }
            }
            requestMultiplePermissions.launch(REQUIRED_PERMISSIONS)
        }
        sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
//        binding.spoofButton.visibility = View.VISIBLE
        binding.spoofButton.setOnClickListener { openSpoofDialogFragment() }
        binding.shutterButton.setOnClickListener { takePhoto() }
        cameraExecutor = Executors.newSingleThreadExecutor()

        childFragmentManager.setFragmentResultListener("spoofDialog", viewLifecycleOwner) { key, bundle ->
            latitude = bundle.getFloat("latitude")
            longitude = bundle.getFloat("longitude")
            spoof = bundle.getBoolean("spoof")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(safeContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun openSpoofDialogFragment() {
        val spoofDialogFragment = SpoofDialogFragment()
        spoofDialogFragment.setLatitude(latitude)
        spoofDialogFragment.setLongitude(longitude)
        spoofDialogFragment.setCheck(spoof)
        spoofDialogFragment.show(childFragmentManager, "")
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(safeContext)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build()
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(safeContext))
    }

    @SuppressLint("MissingPermission")
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(ContextCompat.getMainExecutor(safeContext), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer = image.planes[0].buffer
                buffer.rewind()
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                var imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageBitmap = Bitmap.createBitmap(
                    imageBitmap,
                    0,
                    0,
                    imageBitmap.width,
                    imageBitmap.height,
                    Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) },
                    true
                )
                viewModel.setImageBitmap(imageBitmap)

                if (binding.spoofButton.visibility != View.GONE && spoof) {
                    val sb = StringBuilder()
                    sb.append(latitude).append(",").append(longitude)
                    callNearbySearch(sb.toString(), image)
                }
                else {
                    useCurrentPlace(image)
                }
            }
            override fun onError(exception: ImageCaptureException) {}
        })
    }

    private fun callNearbySearch(coordsString: String, image: ImageProxy) {
        lifecycleScope.launchWhenCreated {
            try {
                val params = HashMap<String, String>()
                params["key"] = BuildConfig.MAPS_API_KEY
                params["location"] = coordsString
                params["rankby"] = "distance"

                val mapsResponse = mapsApi.getNearbyPlaces(params)
                if (mapsResponse.isSuccessful) {
                    val json = JSONObject(mapsResponse.body().toString())
                    val likelyPlacesJSON = json.getJSONArray("results")

                    val viewModels = ArrayList<CurrentPlaceViewModel>()

                    for (i in 0 until likelyPlacesJSON.length()) {
                        val place = JSONObject(likelyPlacesJSON[i].toString())

                        val types = ArrayList<String>()
                        for (i in 0 until place.getJSONArray("types").length()) {
                            types.add(place.getJSONArray("types")[i].toString())
                        }

                        viewModels.add(CurrentPlaceViewModel
                            (
                            place.getString("name"),
                            place.getString("vicinity"),
                            place.getString("place_id"),
                            place.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                            place.getJSONObject("geometry").getJSONObject("location").getDouble("lng"),
                            types
                        )
                        )
                    }

                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.add(R.id.fragmentContainerView,
                        PhotoEditFragment(viewModels))
                    transaction.commit()
                    image.close()
                }
                else {
                    Log.i("mapsResponseError", "")
                }

            } catch (e: Exception) {
                Log.i("NearbySearchError", e.toString())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun useCurrentPlace(image: ImageProxy) {
        if (locationPermissionGranted) {
            Places.initialize(safeContext, BuildConfig.MAPS_API_KEY)
            placesClient = Places.createClient(safeContext)

            // Use fields to define the data types to return.
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES, Place.Field.ID)

            // Use the builder to create a FindCurrentPlaceRequest.
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.

            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result.placeLikelihoods

                    val viewModels = ArrayList<CurrentPlaceViewModel>()

                    for (i in likelyPlaces) {
                        val types = ArrayList<String>()
                        for (j in i.place.types!!) {
                            types.add(j.toString())
                        }

                        viewModels.add(CurrentPlaceViewModel
                            (
                                i.place.name,
                                i.place.address,
                                i.place.id,
                                i.place.latLng.latitude,
                                i.place.latLng.longitude,
                                types
                            )
                        )
                    }

                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.add(R.id.fragmentContainerView,
                        PhotoEditFragment(viewModels)
                    )
                    transaction.commit()
                    image.close()
                }
            }
        } else {
            getLocationPermission()
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

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}
