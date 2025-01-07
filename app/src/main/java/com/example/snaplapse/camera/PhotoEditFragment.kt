package com.example.snaplapse.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.snaplapse.R
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.data.category.CategoryRequest
import com.example.snaplapse.api.data.location.LocationRequest
import com.example.snaplapse.api.data.photo.PhotoRequest
import com.example.snaplapse.api.routes.CategoriesApi
import com.example.snaplapse.api.routes.LocationsApi
import com.example.snaplapse.api.routes.PhotosApi
import com.example.snaplapse.databinding.FragmentPhotoEditBinding
import com.example.snaplapse.view_models.CameraViewModel
import com.example.snaplapse.view_models.CurrentPlaceViewModel
import com.example.snaplapse.view_models.ItemsViewModel2
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PhotoEditFragment(var currentPlaceViewModel: List<CurrentPlaceViewModel>) : Fragment() {
    private lateinit var safeContext: Context

    private lateinit var _binding: FragmentPhotoEditBinding
    private val binding get() = _binding

    private val viewModel: CameraViewModel by activityViewModels()

    private lateinit var imageBitmap: Bitmap

    private var userID: Int = 0
    private val photosApi = RetrofitHelper.getInstance().create(PhotosApi::class.java)
    private val categoriesApi = RetrofitHelper.getInstance().create(CategoriesApi::class.java)
    private val locationsApi = RetrofitHelper.getInstance().create(LocationsApi::class.java)

    private var selectedLocationIndex = 0
    private var description = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
        userID = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)?.getInt("id", 0)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.add(R.id.fragmentContainerView, CameraFragment())
                transaction.commit()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        _binding = FragmentPhotoEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.imageBitmap.observe(viewLifecycleOwner) { imageBitmap ->
            this.imageBitmap = imageBitmap
            binding.imageView.setImageBitmap(imageBitmap)
        }
        binding.locationButton.setOnClickListener { openSetLocationDialog() }
        binding.descriptionButton.setOnClickListener { openSetDescriptionDialog() }
        binding.uploadButton.setOnClickListener { uploadPhoto() }
        binding.deleteButton.setOnClickListener { deletePhoto() }

        childFragmentManager.setFragmentResultListener("descriptionDialog", viewLifecycleOwner) { key, bundle ->
            description = bundle.getString("descriptionKey").toString()
        }

        childFragmentManager.setFragmentResultListener("locationDialog", viewLifecycleOwner) { key, bundle ->
            selectedLocationIndex = bundle.getInt("locationKey")
        }
    }

    private fun openSetLocationDialog() {
        val locationDialogFragment = LocationDialogFragment()
        locationDialogFragment.setItem(selectedLocationIndex)
        locationDialogFragment.setList(currentPlaceViewModel)
        locationDialogFragment.show(childFragmentManager, "")
    }

    private fun openSetDescriptionDialog() {
        val descriptionDialogFragment = DescriptionDialogFragment()
        descriptionDialogFragment.setDesc(description)
        descriptionDialogFragment.show(childFragmentManager, "")
    }

    private fun uploadPhoto() {
        if (description.isBlank()) {
            Toast.makeText(safeContext, "Please provide a description.", Toast.LENGTH_SHORT).show()
        } else {
            val byteArrayOutputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val encodedBitmap: String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            uploadToServer(description, encodedBitmap)
        }
    }

    private fun deletePhoto() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentContainerView, CameraFragment())
        transaction.commit()
    }

    @SuppressLint("NewApi")
    private fun uploadToServer(description: String, encodedBitmap: String) {
        lifecycleScope.launchWhenCreated {
            try {
                var locationId = 0

                val locationGetResponse = locationsApi.getLocationByGoogleId(currentPlaceViewModel[selectedLocationIndex].id)
                if (locationGetResponse.isSuccessful) {
                    locationId = locationGetResponse.body()!!.id
                }
                else {
                    val categories: MutableList<Int> = ArrayList()
                    for (item in currentPlaceViewModel[selectedLocationIndex].types) {
                        val categoryGetResponse = categoriesApi.getCategoryByName(item)
                        if (categoryGetResponse.isSuccessful) {
                            categories.add(categoryGetResponse.body()!!.id)
                        }
                        else {
                            if (categoryGetResponse.code() == 404) {
                                val categoryRequestBody = CategoryRequest(name=item)
                                val categoryPostResponse = categoriesApi.createCategory(categoryRequestBody)
                                if (categoryPostResponse.isSuccessful) {
                                    categories.add(categoryPostResponse.body()!!.id)
                                }
                                else {
                                    Log.i("CategoryPostError", "error")
                                }
                            }
                            else {
                                Log.i("CategoryGetError", "error")
                            }
                        }
                    }

                    val locationRequestBody = LocationRequest(name=currentPlaceViewModel[selectedLocationIndex].name, longitude=currentPlaceViewModel[selectedLocationIndex].longitude, latitude=currentPlaceViewModel[selectedLocationIndex].latitude, categories=categories, google_id=currentPlaceViewModel[selectedLocationIndex].id)
                    val locationPostResponse = locationsApi.createLocation(locationRequestBody)
                    if (locationPostResponse.isSuccessful) {
                        locationId = locationPostResponse.body()!!.id
                    }
                    else {
                        Log.i("LocationPostError", "")
                    }
                }

                val requestBody = PhotoRequest(user=userID, location=locationId, description=description, bitmap=encodedBitmap)
                val response = photosApi.upload(requestBody)
                if (response.isSuccessful) {
                    Toast.makeText(
                        safeContext,
                        "Uploaded photo: " + description,
                        Toast.LENGTH_SHORT
                    ).show()
                    val current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val text = current.toString() + "\n" + description
                    val parseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val printFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                    val date = LocalDate.parse(response.body()!!.created.substring(0, 10), parseFormat)
                    viewModel.appendProfilePhotos(ItemsViewModel2(response.body()?.id ?: 0, userID, imageBitmap, text, date.format(printFormat).toString(), true))
                    val imm = requireActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE
                    ) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.add(R.id.fragmentContainerView, CameraFragment())
                    transaction.commit()
                }
                else {
                    // TODO: Error handling
                }
            } catch (e: Exception) {
                Log.e("UploadPhotoError", e.toString())
            }
        }
    }
}
