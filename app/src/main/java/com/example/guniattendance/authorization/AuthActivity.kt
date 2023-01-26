package com.example.guniattendance.authorization

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.guniattendance.R
import com.example.guniattendance.utils.showProgress
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.concurrent.Executors


@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container_auth) as NavHostFragment

        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.splashScreenFragment,
                R.id.appPinFragment,
                R.id.launcherScreenFragment
            )
        ).build()

        navController = navHostFragment.findNavController()
        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}