package com.main.notificationapp.ui.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.main.notificationapp.R
import com.main.notificationapp.databinding.FragmentProfileBinding
import com.main.notificationapp.ui.activities.MainActivity
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.DatastoreOperations
import com.main.notificationapp.utils.NewsCacheOperations
import com.main.notificationapp.utils.Resources
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ProfileFragment"

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var viewModel: MainViewModel
    //private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProfileBinding.bind(view)

        progressDialog = ProgressDialog(requireContext()).apply {
            setTitle("Updating Profile...")
            setCanceledOnTouchOutside(false)
        }

        viewModel = (activity as MainActivity).viewModel
        //val builder = AlertDialog.Builder(requireContext())
        /*val customLayout = (activity as MainActivity).layoutInflater.inflate(R.layout.custom_layout, null)
        builder.setView(customLayout)
        builder.setCancelable(false)*/
        //alertDialog = builder.create()

        binding.buttonOk.setOnClickListener{
            val text = binding.countryText.text.toString()
            if (text == ""){
                Toast.makeText(requireContext(), "Please Input a valid value", Toast.LENGTH_SHORT).show()
            }else{
                viewModel.updateUserInfo(text)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.profileEvents.collect {
                when(it) {
                    is NewsCacheOperations.Loading -> onProgress()
                    is NewsCacheOperations.Error -> onFailure(it.message!!)
                    is NewsCacheOperations.Success -> onSuccess()
                }
            }
        }
    }

    private fun onProgress() {
        progressDialog.show()
    }

    private fun onSuccess() {
        view?.let { Snackbar.make(it, "Successfully Updated Location", Snackbar.LENGTH_SHORT).show() }
        if(viewModel.isFirstLogIn) {
            val action = ProfileFragmentDirections.actionProfileFragmentToTopHeadlinesFragment(binding.countryText.text.toString())
            viewModel.isFirstLogIn = false
            findNavController().navigate(action)
        }

    }

    private fun onFailure(message: String) {
        progressDialog.dismiss()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        viewModel.initState()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}