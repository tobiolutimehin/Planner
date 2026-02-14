package com.planner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.planner.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appBarConfiguration = AppBarConfiguration
            .Builder(
                com.planner.feature.tasks.R.id.taskManagerListFragment,
                com.planner.feature.trips.R.id.listTripFragment,
                R.id.homeFragment,
            )
            .build()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        showBottomNav()
        setUpBottomNav()
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp() || navController.navigateUp()
    }

    /** Setup bottom action bar for bottom navigation support */
    private fun setUpBottomNav() {
        with(binding) {
            setSupportActionBar(this@with.toolbar)
            this@with.bottomMenu.setupWithNavController(navController)
            this@with.bottomMenu.setOnItemSelectedListener { item ->
                val options = NavOptions.Builder()
                    .setPopUpTo(
                        R.id.nav_graph,
                        true,
                    ) // Set the start destination of your graph here
                    .build()
                navController.navigate(item.itemId, null, options)
                true
            }
        }
    }

    /**
     * Sets up a listener to show or hide the bottom navigation menu based on the current destination.
     * When the user navigates to certain destinations (e.g. listTripFragment, taskManagerListFragment, or homeFragment),
     * the bottom navigation menu will be shown. For all other destinations, the bottom navigation menu will be hidden.
     */
    private fun showBottomNav() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                com.planner.feature.trips.R.id.listTripFragment,
                com.planner.feature.tasks.R.id.taskManagerListFragment,
                R.id.homeFragment,
                -> {
                    binding.bottomMenu.isVisible = true
                }

                else -> {
                    binding.bottomMenu.isVisible = false
                }
            }
        }
    }
}
