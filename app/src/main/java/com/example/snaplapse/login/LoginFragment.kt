package com.example.snaplapse.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.snaplapse.MainActivity
import com.example.snaplapse.R
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.routes.UsersApi
import com.example.snaplapse.api.data.user.UserCredentialsRequest
import org.json.JSONArray
import org.json.JSONObject

class LoginFragment : Fragment() {

    private val userListApi = RetrofitHelper.getInstance().create(UsersApi::class.java)

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
        val view:View = inflater.inflate(R.layout.fragment_login, container, false)
        usernameView = view.findViewById(R.id.username)
        passwordView = view.findViewById(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val registerButton = view.findViewById<Button>(R.id.register_button)
        val forgotPasswordButton = view.findViewById<TextView>(R.id.forgot_password_button)
        val fragmentManager = parentFragmentManager
        sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)

        loginButton?.setOnClickListener{
            val usernameText = usernameView?.text.toString()
            val passwordText = passwordView?.text.toString()

            userLogin(usernameText, passwordText)
        }

        registerButton?.setOnClickListener{
            resetFields()

            val registerFragment = RegisterFragment()
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.login_layout, registerFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        forgotPasswordButton?.setOnClickListener{
            val usernameText = usernameView?.text.toString()
            if (sharedPref?.contains(usernameText) == false) {
                Toast.makeText(requireContext(), resources.getString(R.string.user_not_found_error), Toast.LENGTH_SHORT).show()
            }
            else {
                resetFields()

                var args = Bundle()
                args.putString(resources.getString(R.string.username_key), usernameText)

                val resetPasswordFragment = ResetPasswordFragment()
                resetPasswordFragment.arguments= args
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.login_layout, resetPasswordFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }

        return view
    }

    private fun userLogin(username: String, password: String) {
        lifecycleScope.launchWhenCreated {
           try {
               val requestBody = UserCredentialsRequest(username=username, secret=password)
               val response = userListApi.login(requestBody)
               if (response.isSuccessful) {
                   // this line is so bad loool
                   val extractedUsername = JSONObject(JSONObject(JSONArray(response.body()?.data.toString())[0].toString()).get("fields").toString()).get("username").toString()
                   val extractedPk = JSONObject(JSONArray(response.body()?.data.toString())[0].toString()).get("pk").toString()
                   val extractedJoinDate = JSONObject(JSONObject(JSONArray(response.body()?.data.toString())[0].toString()).get("fields").toString()).get("created").toString()

                    with(sharedPref?.edit()) {
                        this?.putString("session", extractedUsername)
                        this?.putInt("id", extractedPk.toInt())
                        this?.putString("joined", extractedJoinDate)
                        this?.apply()
                    }

                   startMainActivity()
               }
               else {
                   resetFields()
                   val message = response.body()?.message
                   setErrorMessage(message)
               }
           } catch (e: Exception) {
                Log.e("LoginError", e.toString())
           }
        }
    }

    private fun resetFields() {
        usernameView?.text = ""
        passwordView?.text = ""
        passwordView?.error = null
    }

    private fun setErrorMessage(message: String?) {
        passwordView?.error = message
    }

    private fun startMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }
}
