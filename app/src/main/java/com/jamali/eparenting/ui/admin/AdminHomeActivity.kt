package com.jamali.eparenting.ui.admin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityAdminHomeBinding
import com.jamali.eparenting.databinding.NavHeaderAdminHomeBinding
import com.jamali.eparenting.ui.customer.fragments.profile.LogOutFragment
import com.jamali.eparenting.utils.Utility

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarAdminHome.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_admin_home)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_monitor, R.id.nav_reports, R.id.nav_management_doctor, R.id.nav_management_module_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        headerSetup(navView)
    }

    private fun headerSetup(navView: NavigationView) {
        val headerBinding = NavHeaderAdminHomeBinding.bind(navView.getHeaderView(0))

        /**
         * Get user data from firebase realtime database
         */
        Utility.database.reference.child("users").child(Utility.auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                headerBinding.headerUsername.text = it.child("username").value.toString()
                headerBinding.headerEmail.text = it.child("email").value.toString()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                val logoutFragment =
                    LogOutFragment()
                logoutFragment.show(supportFragmentManager, logoutFragment.tag)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_admin_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}