package com.example.snaplapse.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.snaplapse.MainActivity
import com.example.snaplapse.R
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = this.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
        if (sharedPref.contains("session")) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        else {
            setContentView(R.layout.activity_login)

            val fragmentManager = supportFragmentManager
            val loginFragment = LoginFragment()
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.login_layout, loginFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}
