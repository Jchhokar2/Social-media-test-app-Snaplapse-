package com.example.snaplapse.profile

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snaplapse.MainActivity
import com.example.snaplapse.R
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.routes.PhotosApi
import com.example.snaplapse.settings.SettingsFragment
import com.example.snaplapse.view_models.CameraViewModel
import com.example.snaplapse.view_models.ItemsViewModel2
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ProfileFragment : Fragment() {
    private var usernameText: String? = null
    private var userId: Int = 0
    private val fragment = this

    private val photosApi = RetrofitHelper.getInstance().create(PhotosApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            usernameText = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)?.getString("session", "")
            userId = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)?.getInt("id", 0)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)
        val recyclerview = view.findViewById<RecyclerView>(R.id.profile_recycler_view)
        recyclerview.layoutManager = GridLayoutManager(MainActivity(), 3)

        val usernameView = view.findViewById<TextView>(R.id.profile_username)

        val data = mutableListOf<ItemsViewModel2>()
        lifecycleScope.launchWhenCreated {
            try {
                val response = photosApi.getPhotosByUser(userId, "-created")
                if (response.isSuccessful) {
                    for (photo in response.body()!!.results) {
                        if (!photo.bitmap.isEmpty()) {
                            val decodedBitmap = Base64.decode(photo.bitmap, Base64.DEFAULT)
                            val bitmap =
                                BitmapFactory.decodeByteArray(decodedBitmap, 0, decodedBitmap.size)
                            val parseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val printFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                            val date = LocalDate.parse(photo.created.substring(0, 10), parseFormat)
                            data.add(ItemsViewModel2(photo.id, userId, bitmap, photo.description, date.format(printFormat).toString(), photo.visible))
                        }
                    }
                    usernameView.text = usernameText
                    val adapter = ProfileRecyclerViewAdapter(data, parentFragmentManager, fragment)
                    recyclerview.adapter = adapter
                }
                else {
                    // TODO: Error handling
                }
            } catch (e: Exception) {
                Log.e("GetPhotoError", e.toString())
            }
        }

        val settingsButton: ImageButton = view.findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.hide(this)
            transaction.add(R.id.fragmentContainerView, SettingsFragment(usernameView))
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }
}
