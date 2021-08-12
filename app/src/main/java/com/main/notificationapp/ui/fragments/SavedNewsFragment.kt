package com.main.notificationapp.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.main.notificationapp.BaseApplication
import com.main.notificationapp.R
import com.main.notificationapp.databinding.FragmentSavedNewsBinding
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.EntityArticle
import com.main.notificationapp.models.RemoteToCacheMappers
import com.main.notificationapp.ui.activities.MainActivity
import com.main.notificationapp.ui.adapters.NewsAdapter
import com.main.notificationapp.ui.adapters.NewsItemClickListener
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.Constants
import com.main.notificationapp.utils.NewsCacheOperations
import com.main.notificationapp.utils.Resources
import com.main.notificationapp.utils.SharedResources
import com.main.notificationapp.utils.SharedResources.observeAndExecute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "SavedNewsFragment"

@AndroidEntryPoint
class SavedNewsFragment : Fragment(R.layout.fragment_saved_news), NewsItemClickListener {
    // TODO: Rename and change types of parameters

    private lateinit var viewModel: MainViewModel
    lateinit var adapter: NewsAdapter
    private var _binding: FragmentSavedNewsBinding? = null
    private val binding: FragmentSavedNewsBinding get() = _binding!!

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSavedNewsBinding.bind(view)

        viewModel = (activity as MainActivity).viewModel
        adapter = NewsAdapter(isSavedFragment = true)
        adapter.itemClickListener = this
        setUpRecyclerView()

        viewModel.getAllCacheArticles()

        binding.browseArticles.setOnClickListener {

        }

        viewModel.cacheArticles.observe(
            viewLifecycleOwner
        ){ resources ->
            when(resources) {
                is Resources.Loading -> {
                    viewModel.savedNewsState = Constants.LOADING
                    Log.d(TAG, "onViewCreated: loading")
                    onProgress()
                }
                is Resources.Success -> {
                    viewModel.savedNewsState = Constants.SUCCESS
                    Log.d(TAG, "onViewCreated: success ${resources.toString()}")
                    onSuccess(resources.data)
                }
                is Resources.Error -> {
                    viewModel.savedNewsState = Constants.ERROR
                    Log.d(TAG, "onViewCreated: error ${resources.toString()}")
                    onFailure(resources.message ?: "")
                }
                is Resources.InitialState -> {
                    Log.d(TAG, "onViewCreated: initialState")
                }
            }
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP  or ItemTouchHelper.DOWN,
        ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = adapter.diffList.currentList[position]
                viewModel.deleteCacheArticle(RemoteToCacheMappers.remoteToCacheConverter(article))

                Snackbar.make(view, "Article Deleted Successfully", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo"){
                        viewModel.saveCacheArticle(RemoteToCacheMappers.remoteToCacheConverter(article))
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.recyclerViewSaved)
        }

        lifecycleScope.launchWhenStarted {
            lifecycleScope.launchWhenStarted {
                viewModel.cacheOperations.collect {
                    when(it) {
                        is NewsCacheOperations.Success -> {
                            Snackbar.make(view, it.message, Snackbar.LENGTH_LONG).show()
                        }
                        is NewsCacheOperations.Error -> {
                            Snackbar.make(view, it.message ?: "", Snackbar.LENGTH_LONG).show()
                        }
                        NewsCacheOperations.Loading -> {

                        }
                    }
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        viewModel.initState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("state", viewModel.savedNewsState)
    }

    private fun setUpRecyclerView() {
        binding.recyclerViewSaved.adapter = adapter
        binding.recyclerViewSaved.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun onProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.refreshLayout.visibility = View.GONE
    }

    private fun onSuccess(data: List<EntityArticle>) {
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.visibility = View.GONE
        val list = data.map {
            RemoteToCacheMappers.cacheToRemoteConverter(it)
        }.toList()
        adapter.diffList.submitList(list)
    }

    private fun onFailure(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.visibility = View.VISIBLE
        val builder = AlertDialog.Builder(requireContext())
        when (message) {
            "No Internet Connection" -> {
                builder.setMessage(message)
                    .setPositiveButton("Connect") { dialog, i ->
                        requireContext().startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                    .setNegativeButton("Go Offline") { dialog, i ->
                        dialog.dismiss()
                    }.setCancelable(false)
                    .show()
            }
            "Network Failure" -> {
                builder.setMessage(message)
                    .setPositiveButton("Ok") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("Go Offline") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }.setCancelable(false)
                    .show()
            }
            "Conversion Failed" -> {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(article: Article) {
        val bundle = Bundle()
        bundle.putSerializable("article", article)
        if (SharedResources.hasInternetConnection((activity as MainActivity).application as BaseApplication)){
            val action = SavedNewsFragmentDirections.actionSavedNewsFragmentToArticlesFragment(article, null)
            findNavController().navigate(action)
        }else{
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Are you sure you want to view in offline mode?")
                .setPositiveButton("Connect") { dialog, i ->
                    requireContext().startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
                .setNegativeButton("Go Offline") { dialog, i ->
                    val action = SavedNewsFragmentDirections.actionSavedNewsFragmentToOfflineNewsFragment(article)
                    findNavController().navigate(action)
                    dialog.dismiss()
                }.setCancelable(false)
                .show()
        }
    }
}