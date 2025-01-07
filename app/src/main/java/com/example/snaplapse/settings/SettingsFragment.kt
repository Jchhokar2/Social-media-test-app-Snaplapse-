package com.example.snaplapse.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.snaplapse.R
import com.example.snaplapse.timeline.TimelineFragment
import java.time.format.DateTimeFormatter

class SettingsFragment(private val profileUsernameView: TextView) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
        val usernameView: TextView = view.findViewById(R.id.settings_username)
        usernameView.text = sharedPref?.getString("session", "")

        val joinedView: TextView = view.findViewById(R.id.date_joined_text)
        joinedView.text = sharedPref?.getString("joined", "")

        val editUsernameButton: Button = view.findViewById(R.id.edit_username_button)
        editUsernameButton.setOnClickListener {
            changeFragment(EditUsernameFragment(usernameView, profileUsernameView))
        }

        val changePasswordButton: Button = view.findViewById(R.id.change_password_button)
        changePasswordButton.setOnClickListener {
            changeFragment(ChangePasswordFragment())
        }

        val aboutButton: Button = view.findViewById(R.id.about_button)
        aboutButton.setOnClickListener {
            changeFragment(AboutFragment())
        }

        val logOutButton: Button = view.findViewById(R.id.log_out_button)
        logOutButton.setOnClickListener {
            LogOutDialogFragment().show(childFragmentManager, "")
        }

        val deleteAccountButton: Button = view.findViewById(R.id.delete_account_button)
        deleteAccountButton.setOnClickListener {
            changeFragment(DeleteAccountFragment())
        }

        val backButton: ImageButton = view.findViewById(R.id.settings_back_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun changeFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.hide(this)
        transaction.add(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun isPhotoPickerAvailable() {
        val intent: Intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
        } else {
            intent = Intent(Intent.ACTION_GET_CONTENT)
        }

        intent.type = "image/*"
        startActivityForResult(intent, 3645)
    }
}
