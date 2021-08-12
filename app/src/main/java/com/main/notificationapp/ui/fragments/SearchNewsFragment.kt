package com.main.notificationapp.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.main.notificationapp.R
import com.main.notificationapp.databinding.FragmentSearchNewsBinding
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.NewsResponse
import com.main.notificationapp.repositories.MainRepository
import com.main.notificationapp.ui.activities.MainActivity
import com.main.notificationapp.ui.adapters.NewsAdapter
import com.main.notificationapp.ui.adapters.NewsItemClickListener
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.Constants
import com.main.notificationapp.utils.Resources
import com.main.notificationapp.utils.SharedResources
import com.main.notificationapp.utils.SharedResources.observeAndExecute
import com.main.notificationapp.utils.search
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val TAG = "SearchNewsFragment"

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news), NewsItemClickListener {

    private lateinit var viewModel: MainViewModel
    lateinit var adapter: NewsAdapter
    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!
    var searchString = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.hide()

        _binding = FragmentSearchNewsBinding.bind(view)

        viewModel = (activity as MainActivity).viewModel
        adapter = NewsAdapter(isSavedFragment = true, lifecycleOwner = viewLifecycleOwner, viewModel)
        setUpRecyclerView()
        adapter.itemClickListener = this
        binding.lifecycleOwner = this

        binding.buttonOk.setOnClickListener {
            viewModel.safeSearchArticleCall(searchString, 1)
        }

        var job: Job? = null
        binding.searchField.search {
            job?.cancel()
            if (it != ""){
                job = lifecycleScope.launch {
                    searchString = it
                    delay(1000L)
                    viewModel.safeSearchArticleCall(it, 1)
                }
            }
        }

        viewModel.searchedArticles.observe(
            viewLifecycleOwner
        ){ resources ->
            when(resources) {
                is Resources.Loading -> {
                    Log.d(TAG, "onViewCreated: loading")
                    viewModel.searchState = Constants.LOADING
                    onProgress()
                }
                is Resources.Success -> {
                    viewModel.searchState = Constants.SUCCESS
                    Log.d(TAG, "onViewCreated: ${resources.data.toString()}")
                    onSuccess(resources.data)
                }
                is Resources.Error -> {
                    viewModel.searchState = Constants.ERROR
                    Log.d(TAG, "onViewCreated: error ${resources}")
                    onFailure(resources.message ?: "")
                }
                is Resources.InitialState -> {
                    Log.d(TAG, "onViewCreated: initialState")
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerViewSaved.adapter = adapter
        binding.recyclerViewSaved.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onPause() {
        super.onPause()
        viewModel.initState()
    }


    private fun onProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.refreshLayout.visibility = View.GONE
    }

    private fun onSuccess(response: NewsResponse) {
        adapter.diffList.submitList(response.articles)
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.visibility = View.GONE
    }

    private fun onFailure(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.visibility = View.VISIBLE
        val builder = AlertDialog.Builder(requireContext())
        when(message){
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
            "Network Failure" ->{
                builder.setMessage(message)
                    .setPositiveButton("Ok"){dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("Go Offline"){dialogInterface, i ->
                        dialogInterface.dismiss()
                    }.setCancelable(false)
                    .show()
            }
            "Conversion Failed"->{
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
            else->{
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onItemClick(article: Article) {
        val bundle = Bundle()
        bundle.putSerializable("article", article)
        val action = SearchNewsFragmentDirections.actionSearchNewsFragmentToArticlesFragment(article, null)
        findNavController().navigate(action)
    }
}