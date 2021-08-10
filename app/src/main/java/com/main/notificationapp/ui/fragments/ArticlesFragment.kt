package com.main.notificationapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.main.notificationapp.R
import com.main.notificationapp.databinding.FragmentArticlesBinding
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.RemoteToCacheMappers
import com.main.notificationapp.models.SourceX
import com.main.notificationapp.ui.activities.MainActivity
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.NewsCacheOperations
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ArticlesFragment : Fragment(R.layout.fragment_articles) {

    val args: ArticlesFragmentArgs by navArgs()
    private lateinit var viewModel: MainViewModel

    private var _binding: FragmentArticlesBinding? = null
    val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentArticlesBinding.bind(view)

        viewModel = (activity as MainActivity).viewModel
        lifecycleScope.launchWhenStarted {
            cacheOperationObserver()
        }

        val article: Article? = args.article
        val source: SourceX? = args.source

        lifecycleScope.launchWhenStarted {
            viewModel.cacheOperations.collect {
                when(it) {
                    is NewsCacheOperations.Success -> {
                        Snackbar.make(view, it.message, Snackbar.LENGTH_LONG)
                            .show()
                        binding.fabIcon.show()
                    }
                    is NewsCacheOperations.Error -> {
                        Snackbar.make(view, it.message ?: "", Snackbar.LENGTH_LONG)
                            .apply{
                                setAction("Try Again") {
                                    viewModel.saveCacheArticle(RemoteToCacheMappers.remoteToCacheConverter(article!!))
                                }
                            }
                            .show()
                        binding.fabIcon.show()
                    }
                    NewsCacheOperations.Loading -> {
                        binding.fabIcon.hide()
                    }
                }
            }
        }

        if(article!=null) {
            binding.searchField.setText(article.url)
            binding.webView.apply {
                webViewClient = WebViewClient()
                Toast.makeText(requireContext(), article.url, Toast.LENGTH_SHORT).show()
                loadUrl(article.url)
            }

            binding.fabIcon.setOnClickListener{
                viewModel.saveCacheArticle(RemoteToCacheMappers.remoteToCacheConverter(article))
            }
        }else{
            binding.searchField.setText(source?.url)
            binding.webView.apply {
                webViewClient = WebViewClient()
                loadUrl(source?.url.toString())
            }
            viewModel.articleProgress.postValue(binding.webView.progress)
        }
    }

    private suspend fun cacheOperationObserver() {
        viewModel.cacheOperations.collect {

        }
    }

}