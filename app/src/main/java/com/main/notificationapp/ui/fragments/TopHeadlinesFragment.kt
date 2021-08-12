package com.main.notificationapp.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.main.notificationapp.R
import com.main.notificationapp.databinding.FragmentTopHeadlinesBinding
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.NewsResponse
import com.main.notificationapp.ui.activities.MainActivity
import com.main.notificationapp.ui.adapters.NewsAdapter
import com.main.notificationapp.ui.adapters.NewsItemClickListener
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.Constants.ERROR
import com.main.notificationapp.utils.Constants.LOADING
import com.main.notificationapp.utils.Constants.SUCCESS
import com.main.notificationapp.utils.DatastoreOperations
import com.main.notificationapp.utils.Resources
import com.main.notificationapp.utils.SharedResources
import com.main.notificationapp.utils.SharedResources.observeAndExecute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "TopHeadlinesFragment"

@AndroidEntryPoint
class TopHeadlinesFragment : Fragment(R.layout.fragment_top_headlines), NewsItemClickListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: NewsAdapter

    private val args: TopHeadlinesFragmentArgs by navArgs()

    private var _binding: FragmentTopHeadlinesBinding? = null
    private val binding get() = _binding!!

    private lateinit var layoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel

        _binding = FragmentTopHeadlinesBinding.bind(view)

        adapter = NewsAdapter(lifecycleOwner = viewLifecycleOwner, viewModel = viewModel)

        adapter.itemClickListener = this
        setUpRecyclerView()

        viewModel.safeTopHeadlinesCall(1)

        binding.buttonOk.setOnClickListener {
            viewModel.safeTopHeadlinesCall(1)
        }

        //viewModel.updateUserInfo(args.country ?: "us", false)

        viewModel.topHeadlinesArticles.observe(
            viewLifecycleOwner
        ){ resources ->
            when(resources) {
                is Resources.Loading -> {
                    Log.d(TAG, "onViewCreated: loading")
                    viewModel.topHeadlinesState = LOADING
                    onProgress()
                }
                is Resources.Success -> {
                    Log.d(TAG, "onViewCreated: ${resources.data.toString()}")
                    viewModel.topHeadlinesState = SUCCESS
                    onSuccess(resources.data)
                }
                is Resources.Error -> {
                    Log.d(TAG, "onViewCreated: error ${resources.toString()}")
                    viewModel.topHeadlinesState = ERROR
                    onFailure(resources.message ?: "")
                }
                is Resources.InitialState -> {
                    Log.d(TAG, "onViewCreated: initialState")
                }
            }
        }

    }


    private fun setUpRecyclerView() {
        binding.recyclerView.adapter = adapter
        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager

        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val currentPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                if(currentPosition == (adapter.diffList.currentList.size - 1)) {
                    viewModel.getMoreArticles()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.initState()
    }


    private fun onProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.refreshLayout.visibility = View.GONE
    }

    private fun onSuccess(data: NewsResponse) {
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.visibility = View.GONE
        adapter.diffList.submitList(data.articles)
    }

    private fun onFailure(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.visibility = View.VISIBLE
        binding.topMainLayout.visibility = View.GONE
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
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onItemClick(article: Article) {
        val bundle = Bundle()
        bundle.putSerializable("article", article)
        val action = TopHeadlinesFragmentDirections.actionTopHeadlinesFragmentToArticlesFragment(article, null)
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
