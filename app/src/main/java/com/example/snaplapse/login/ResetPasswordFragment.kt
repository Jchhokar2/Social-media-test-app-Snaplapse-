package com.example.snaplapse.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.snaplapse.R

class ResetPasswordFragment : Fragment() {
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString(resources.getString(R.string.username_key))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view:View = inflater.inflate(R.layout.fragment_reset_password, container, false)
        val usernameView = view.findViewById<TextView>(R.id.reset_user)
        val password = view.findViewById<TextView>(R.id.new_password)
        val confirmPassword = view.findViewById<TextView>(R.id.reset_re_enter_password)
        val backButton = view.findViewById<ImageButton>(R.id.reset_back_button)
        val resetButton = view.findViewById<Button>(R.id.reset_button)
        val fragmentManager = parentFragmentManager
        val sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)

        usernameView.text = username

        backButton?.setOnClickListener{
            fragmentManager.popBackStack()
        }

        resetButton?.setOnClickListener{
            val usernameText = usernameView.text.toString()
            val passwordText = password?.text.toString()
            val confirmPasswordText = confirmPassword?.text.toString()
            if (passwordText == resources.getString(R.string.empty_string)) {
                password?.error = resources.getString(R.string.empty_password_error)
            }
            else if (passwordText != confirmPasswordText) {
                confirmPassword?.error = resources.getString(R.string.mismatch_passwords_error)
            }
            else {
                with(sharedPref?.edit()) {
                    this?.putString(usernameText, passwordText)
                    this?.apply()
                }

                Toast.makeText(requireContext(), resources.getString(R.string.password_changed_toast), Toast.LENGTH_SHORT).show()
                fragmentManager.popBackStack()
            }
        }

        return view
    }
}
