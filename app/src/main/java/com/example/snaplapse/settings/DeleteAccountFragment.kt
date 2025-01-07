package com.example.snaplapse.settings

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
import android.widget.EditText
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.example.snaplapse.R
import com.example.snaplapse.api.RetrofitHelper
import com.example.snaplapse.api.routes.UsersApi
import com.example.snaplapse.login.LoginActivity

class DeleteAccountFragment : Fragment() {

    private val usersApi = RetrofitHelper.getInstance().create(UsersApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var sharedPref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_delete_account, container, false)
        sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)

        val backButton: ImageButton = view.findViewById(R.id.delete_account_back_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val username = sharedPref?.getString("session", "")
        val id = sharedPref?.getInt("id", 0)!!
        val deleteConfirm: Button = view.findViewById(R.id.delete_account_button_confirm)
        val textInput: EditText = view.findViewById(R.id.delete_account_text_input)
        textInput.hint = username

       textInput.setOnKeyListener { view, i, keyEvent ->
           if (textInput.text.toString() == username) {
               deleteConfirm.isEnabled = true
           }
           else {
               deleteConfirm.isEnabled = false
           }
           false
       }

        deleteConfirm.setOnClickListener {
            deleteAccount(id)
        }

        return view
    }

    private fun deleteAccount(id: Int) {
        lifecycleScope.launchWhenCreated {
            try {
                val response = usersApi.delete(id.toString())
                if (response.isSuccessful) {
                    with(sharedPref?.edit()) {
                        this?.remove("session")
                        this?.remove("id")
                        this?.apply()
                    }
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e("DeleteError", e.toString())
            }
        }
    }
}
