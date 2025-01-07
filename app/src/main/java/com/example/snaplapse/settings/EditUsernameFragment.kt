package com.example.snaplapse.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.snaplapse.R
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.routes.UsersApi
import com.example.snaplapse.api.data.user.UserCredentialsRequest

class EditUsernameFragment(private val usernameView: TextView, private val profileUsernameView: TextView) : Fragment() {

    private val usersApi = RetrofitHelper.getInstance().create(UsersApi::class.java)

    private var userID: Int = 0
    private var sharedPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_username, container, false)

        val username: TextView = view.findViewById(R.id.edit_username_input)
        val backButton: ImageButton = view.findViewById(R.id.edit_username_back_button)
        val confirmButton: Button = view.findViewById(R.id.edit_username_confirm_button)
        sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)

        userID = sharedPref?.getInt("id", 0)!!

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        confirmButton.setOnClickListener {
            val usernameText = username?.text.toString().trim()
            var hasErrors = false
            if (usernameText == "") {
                username?.error = resources.getString(R.string.empty_username_error)
                hasErrors = true
            }
            else if (usernameText.length > 16 || usernameText.length < 4) {
                username?.error = resources.getString(R.string.username_length_error)
                hasErrors = true
            }

            if (!hasErrors) {
                changeUsername(userID, usernameText)
            }
        }

        return view
    }

    private fun changeUsername(id: Int, username: String) {
        lifecycleScope.launchWhenCreated {
            try {
                val requestBody = UserCredentialsRequest(username=username, secret=null)
                val response = usersApi.edit(id, requestBody)
                if (response.isSuccessful) {
                    with(sharedPref?.edit()) {
                        this?.putString("session", username)
                        this?.apply()
                    }
                    usernameView.text = username
                    profileUsernameView.text = username
                    parentFragmentManager.popBackStack()
                }
                else {
                    // TODO: username validation code
                }
            } catch (e: Exception) {
                Log.e("ChangeUsernameError", e.toString())
            }
        }
    }
}
