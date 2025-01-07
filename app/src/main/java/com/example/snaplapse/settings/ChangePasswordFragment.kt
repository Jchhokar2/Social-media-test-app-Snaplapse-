package com.example.snaplapse.settings

import android.content.Context
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

class ChangePasswordFragment : Fragment() {

    private val usersApi = RetrofitHelper.getInstance().create(UsersApi::class.java)

    private var userID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)
        val password = view.findViewById<TextView>(R.id.change_password_input)
        val confirmPassword = view.findViewById<TextView>(R.id.change_password_input_confirm)
        val backButton = view.findViewById<ImageButton>(R.id.change_password_back_button)
        val confirmButton = view.findViewById<Button>(R.id.change_password_confirm)

        userID = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)?.getInt("id", 0)!!

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        confirmButton.setOnClickListener {
            var hasErrors = false

            val passwordText = password?.text.toString()
            val confirmPasswordText = confirmPassword?.text.toString()
            if (passwordText == resources.getString(R.string.empty_string)) {
                password?.error = resources.getString(R.string.empty_password_error)
                hasErrors = true
            }
            else if (passwordText != confirmPasswordText) {
                confirmPassword?.error = resources.getString(R.string.mismatch_passwords_error)
                hasErrors = true
            }

            if (!hasErrors) {
                changePassword(userID, passwordText)
            }
        }
        return view
    }

    private fun changePassword(id: Int, password: String) {
        lifecycleScope.launchWhenCreated {
            try {
                val requestBody = UserCredentialsRequest(username=null, secret=password)
                val response = usersApi.edit(id, requestBody)
                if (response.isSuccessful) {
                    parentFragmentManager.popBackStack()
                }
                else {
                    // TODO: password validation code
                }
            } catch (e: Exception) {
                Log.e("ChangePasswordError", e.toString())
            }
        }
    }
}
