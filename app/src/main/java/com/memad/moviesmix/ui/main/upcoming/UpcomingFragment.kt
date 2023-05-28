package com.memad.moviesmix.ui.main.upcoming

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentUpcomingBinding
import com.memad.moviesmix.ui.main.trending.LoadingAdapter
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.NetworkStatusHelper
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.getSnapPosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingFragment : Fragment(), LoadingAdapter.OnLoadingAdapterClickListener,
    UpcomingAdapter.OnMoviesClickListener {
    private var error: String = ""
    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var upcomingLoadingAdapter: UpcomingLoadingAdapter

    @Inject
    lateinit var upcomingAdapter: UpcomingAdapter
    private lateinit var snapHelper: LinearSnapHelper
    private lateinit var concatAdapter: ConcatAdapter

    @Inject
    lateinit var networkStatusHelper: NetworkStatusHelper
    private lateinit var layoutManager: CenterZoomLayoutManager

    private val upcomingViewModel by viewModels<UpcomingViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpcomingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupObservers()
        return binding.root
    }

    private fun setupRecyclerView() {
        layoutManager =
            CenterZoomLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
        binding.upcomingRecyclerView.layoutManager = layoutManager
        concatAdapter = ConcatAdapter(upcomingAdapter, upcomingLoadingAdapter)
        upcomingAdapter.popularMovieClickListener = this
        upcomingLoadingAdapter.errorClickListener = this
        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.upcomingRecyclerView)
        binding.upcomingRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    snapPositionChange(snapHelper.getSnapPosition(recyclerView))
                }
            }
        })
    }

    private fun snapPositionChange(snapPosition: Int) {
        if (snapPosition == upcomingAdapter.upcomingMoviesList.size - 1) {
            upcomingViewModel.loadNextPage()
        } else {
            binding.releaseDate.text =
                upcomingAdapter.upcomingMoviesList[snapPosition].movie?.release_date
        }
    }

    private fun setupObservers() {
        networkStatusHelper.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkStatus.Unavailable -> {
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
        lifecycleScope.launch {
            upcomingViewModel.moviesResource.collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        is Resource.Loading -> loading()
                        is Resource.Error -> error(it.data!!)
                        is Resource.Success -> {}
                    }
                }
            }
        }
        lifecycleScope.launch {
            upcomingViewModel.moviesList.collectLatest {
                upcomingAdapter.upcomingMoviesList = it
            }
        }
    }


    private fun loading() {
        upcomingLoadingAdapter.loading()
        binding.releaseDate.text = getString(R.string.loading)
    }

    private fun error(list: List<MovieEntity>) {
        if (list.isEmpty()) {
            upcomingLoadingAdapter.error()
            binding.releaseDate.text = error
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMovieErrorStateClicked() {
        upcomingViewModel.reload()
    }

    override fun onMovieClicked(position: Int, imageView: ImageView?) {
        Toast.makeText(context, "$position Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onMovieDoubleClicked(position: Int, imageView: ImageView?) {
        Toast.makeText(context, "$position Double Clicked", Toast.LENGTH_SHORT).show()
    }

}