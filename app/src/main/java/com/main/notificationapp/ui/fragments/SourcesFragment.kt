package com.main.notificationapp.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.main.notificationapp.R
import com.main.notificationapp.databinding.FragmentSearchNewsBinding
import com.main.notificationapp.databinding.FragmentSourcesBinding
import com.main.notificationapp.models.SourceX
import com.main.notificationapp.models.SourcesResponse
import com.main.notificationapp.ui.activities.MainActivity
import com.main.notificationapp.ui.adapters.NewsAdapter
import com.main.notificationapp.ui.adapters.SourceItemClickListener
import com.main.notificationapp.ui.adapters.SourcesAdapter
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.Resources
import com.main.notificationapp.utils.SharedResources
import com.main.notificationapp.utils.SharedResources.observeAndExecute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "SourcesFragment"

@AndroidEntryPoint
class SourcesFragment : Fragment(R.layout.fragment_sources), SourceItemClickListener {
    private lateinit var viewModel: MainViewModel
    lateinit var adapter: SourcesAdapter
    private var _binding: FragmentSourcesBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSourcesBinding.bind(view)

        viewModel = (activity as MainActivity).viewModel
        adapter = SourcesAdapter()
        adapter.itemClickListener = this

        setUpRecyclerView()

        viewModel.sources.observe(
            viewLifecycleOwner
        ){ resources ->
            when(resources) {
                is Resources.Loading -> {
                    Log.d(TAG, "onViewCreated: loading")
                    onProgress()
                }
                is Resources.Success -> {
                    Log.d(TAG, "onViewCreated: success ${resources.data.toString()}")
                    onSuccess(resources.data)
                }
                is Resources.Error -> {
                    Log.d(TAG, "onViewCreated: error ${resources.toString()}")
                    onFailure(resources.message ?: "")
                }
                is Resources.InitialState -> {
                    Log.d(TAG, "onViewCreated: initialState")
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerViewSources.adapter = adapter
        binding.recyclerViewSources.layoutManager = LinearLayoutManager(requireContext())
    }


    private fun onProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.refreshLayout.visibility = View.GONE
        binding.recyclerViewSources.visibility = View.VISIBLE
    }

    private fun onSuccess(response: SourcesResponse) {
        adapter.differ.submitList(response.sources)
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.visibility = View.GONE
        binding.recyclerViewSources.visibility = View.VISIBLE
    }

    private fun onFailure(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.visibility = View.VISIBLE
        binding.recyclerViewSources.visibility = View.GONE
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

    override fun onPause() {
        super.onPause()
        viewModel.initState()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        viewModel.sourcesFirstLaunch = true
    }

    override fun onItemClick(source: SourceX) {
        val bundle = Bundle()
        bundle.putSerializable("source", source)
        val action = SourcesFragmentDirections.actionSourcesFragmentToArticlesFragment(null, source)
        findNavController().navigate(action)
    }
}