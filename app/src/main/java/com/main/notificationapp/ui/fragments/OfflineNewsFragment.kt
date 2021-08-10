package com.main.notificationapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.main.notificationapp.R
import com.main.notificationapp.databinding.FragmentOfflineNewsBinding
import com.main.notificationapp.ui.activities.MainActivity
import com.main.notificationapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfflineNewsFragment : Fragment(R.layout.fragment_offline_news) {
    private lateinit var viewModel: MainViewModel
    private val args: OfflineNewsFragmentArgs by navArgs()

    private var _binding: FragmentOfflineNewsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentOfflineNewsBinding.bind(view)

        viewModel = (activity as MainActivity).viewModel
        val article = args.article
        binding.offlineTextView.setText(article.content)
        binding.titleText.text = article.title

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}