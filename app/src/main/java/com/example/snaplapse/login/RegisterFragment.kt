package com.example.snaplapse.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.snaplapse.MainActivity
import com.example.snaplapse.R
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.routes.UsersApi
import com.example.snaplapse.api.data.user.UserCredentialsRequest

class RegisterFragment : Fragment() {

    private val usersApi = RetrofitHelper.getInstance().create(UsersApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var usernameView: TextView? = null
    var passwordView: TextView? = null
    var sharedPref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_register, container, false)

        usernameView = view.findViewById(R.id.register_username)
        passwordView = view.findViewById(R.id.register_password)
        val confirmPassword = view.findViewById<TextView>(R.id.re_enter_password)
        val backButton = view.findViewById<ImageButton>(R.id.register_back_button)
        val signupButton = view.findViewById<Button>(R.id.sign_up_button)
        val fragmentManager = parentFragmentManager
        sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)

        backButton?.setOnClickListener{
            fragmentManager.popBackStack()
        }

        signupButton?.setOnClickListener{
            val usernameText = usernameView?.text.toString().trim()
            var hasErrors = false
            if (usernameText == "") {
                usernameView?.error = resources.getString(R.string.empty_username_error)
                hasErrors = true
            }
            else if (usernameText.length > 16 || usernameText.length < 4) {
                usernameView?.error = resources.getString(R.string.username_length_error)
                hasErrors = true
            }

            val passwordText = passwordView?.text.toString()
            val confirmPasswordText = confirmPassword?.text.toString()
            if (passwordText == resources.getString(R.string.empty_string)) {
                passwordView?.error = resources.getString(R.string.empty_password_error)
                hasErrors = true
            }
            else if (passwordText != confirmPasswordText) {
                confirmPassword?.error = resources.getString(R.string.mismatch_passwords_error)
                hasErrors = true
            }

            if (!hasErrors) {
                userRegister(usernameText, passwordText)
            }
        }

        return view
    }

    private fun userRegister(username: String, password: String) {
        lifecycleScope.launchWhenCreated {
            try {
                val requestBody = UserCredentialsRequest(username=username, secret=password)
                val response = usersApi.register(requestBody)
                if (response.isSuccessful) {
                    with(sharedPref?.edit()) {
                        this?.putString("session", response.body()?.username)
                        this?.putInt("id", response.body()!!.id!!.toInt())
                        this?.putString("joined", response.body()?.created)
                        this?.apply()
                    }

                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                }
                else {
                    usernameView?.error = "Username already exists"
                }
            } catch (e: Exception) {
                Log.e("RegisterError", e.toString())
            }
        }
    }
}
