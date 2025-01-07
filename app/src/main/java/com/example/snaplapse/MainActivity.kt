package com.example.snaplapse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val bundle = Bundle()
        bundle.putString("username", intent.extras?.getString("username"))
        bundle.putString("joined", intent.extras?.getString("joined"))
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnItemSelectedListener {
            navController.navigate(it.itemId, bundle)
            false
        }
    }
}
