package com.memad.moviesmix.ui.main.trending

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.MaterialFadeThrough
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentTrendingBinding
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.NetworkStatusHelper
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class TrendingFragment : Fragment() {

    private var error: String = ""

    @Inject
    lateinit var networkStatusHelper: NetworkStatusHelper
    @Inject
    lateinit var trendingAdapter: TrendingAdapter
    private val trendingViewModel by viewModels<TrendingViewModel>()
    private var _binding: FragmentTrendingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrendingBinding.inflate(inflater, container, false)


        return binding.root
    }


    private fun setupObservables() {
        networkStatusHelper.observe(viewLifecycleOwner, {
            this.error = when (it) {
                is NetworkStatus.Available -> context!!.resources.getString(
                    R.string.something_went_wrong
                )
                is NetworkStatus.Unavailable -> context!!.resources.getString(
                    R.string.no_network
                )
            }
        })
        lifecycleScope.launchWhenStarted {
            trendingViewModel.isFirstLoading.collect {
                if(it){
                    firstLoadingHandle()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            trendingViewModel.moviesResource.collect {
                when (it) {
                    is Resource.Loading -> {
                        loading()
                    }
                    is Resource.Error -> error(trendingViewModel.moviesListLiveData.value)
                    is Resource.Success -> success()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            trendingViewModel.moviesList.collectLatest {
                trendingAdapter.trendingMoviesList = it
            }
        }
    }

    private fun error(list: MutableList<MovieEntity>?) {
        TODO("Not yet implemented")
    }

    private fun success() {

    }

    private fun loading() {

    }

    private fun firstLoadingHandle() {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}