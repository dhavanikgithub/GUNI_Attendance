package com.example.guniattendance.authorization


import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import com.example.guniattendance.R
import com.example.guniattendance.utils.LiveNetworkMonitor
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var snackbar:Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        snackbar=Snackbar.make(
            findViewById(android.R.id.content),
            "",
            Snackbar.LENGTH_INDEFINITE
        )
        val connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val liveNetworkMonitor = LiveNetworkMonitor(connectivityManager)
        liveNetworkMonitor.observe(this,{showNetworkMessage(it)})
        this.supportActionBar?.setDisplayHomeAsUpEnabled(false)


        /*val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container_auth) as NavHostFragment

        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                 R.id.launcherScreenFragment,
                R.id.studentRegisterFragment,
                R.id.settingFragment
            )
        ).build()

        navController = navHostFragment.findNavController()
        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)*/

    }

    private fun showNetworkMessage(isConnected:Boolean)
    {
        if(!isConnected)
        {
            snackbar=Snackbar.make(
                findViewById(android.R.id.content),
                "No internet connection",
                Snackbar.LENGTH_LONG
            )
            snackbar.view.setBackgroundColor(Color.parseColor("#FF3939"))
            snackbar.setTextColor(Color.WHITE)
            snackbar?.duration =BaseTransientBottomBar.LENGTH_INDEFINITE
            snackbar?.show()
        }
        else{
            snackbar?.dismiss()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}