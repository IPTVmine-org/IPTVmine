package com.samyak2403.iptvmine

import android.os.Bundle
import android.text.TextUtils.replace
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.samyak2403.iptvmine.screens.AboutFragment
import com.samyak2403.iptvmine.screens.HomeFragment

import me.ibrahimsn.lib.OnItemSelectedListener
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomBar = findViewById<SmoothBottomBar>(R.id.bottomBar)

        // Initialize with HomeFragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Set up SmoothBottomBar item selection listener
        bottomBar.setOnItemSelectedListener {

                when (it) {
                    0 -> replaceFragment(HomeFragment()) // First menu item for Home
                    1 -> replaceFragment(AboutFragment()) // Second menu item for About
                }


        }
    }

    // Function to replace the current fragment
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerView, fragment)
        }
    }
}
