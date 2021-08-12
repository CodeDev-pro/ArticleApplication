package com.main.notificationapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.main.notificationapp.R
import com.main.notificationapp.databinding.ActivityMainBinding
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.Source
import com.main.notificationapp.ui.Services
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.Constants.FIRST_SERVICE_RUN
import com.main.notificationapp.utils.Constants.SENDING_ARTICLE
import com.main.notificationapp.utils.Constants.UPDATED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.topHeadlinesFragment, R.id.savedNewsFragment, R.id.searchNewsFragment, R.id.sourcesFragment, R.id.profileFragment)
        )
        
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        setupSmoothBottomMenu()

        val intent = intent
        if(intent.action == SENDING_ARTICLE) {
            val bundle = intent.extras
            val action = MainActivityDirections.globalAction(article = Article(
                author = "Us",
                content = bundle?.get("content") as String,
                description = "",
                publishedAt = "",
                source = Source("2", ""),
                title = bundle["title"] as String,
                url = bundle["url"] as String,
                urlToImage = ""
            )
            )
            navController.navigate(action)
        }

        lifecycleScope.launchWhenStarted {
            Log.d(TAG, "onCreate: courotine started")
            viewModel.notificationArticle.collect {
                Log.d(TAG, "onCreate: ${it.title}")
                updateService(it)
            }
        }

    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu)
        val menu = popupMenu.menu
        binding.bottomBar.setupWithNavController(menu, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun updateService(article: Article) {
        val intent = Intent(this, Services::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.action = UPDATED
        intent.putExtras(bundleOf("content" to article.content, "url" to article.url, "content" to article.content))

        startService(intent)
    }

    private fun startService(){
        val intent = Intent(this, Services::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = FIRST_SERVICE_RUN
        }
        startService(intent)
    }
}