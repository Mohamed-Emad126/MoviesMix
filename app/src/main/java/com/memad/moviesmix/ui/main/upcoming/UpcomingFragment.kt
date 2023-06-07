package com.memad.moviesmix.ui.main.upcoming

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.flaviofaria.kenburnsview.KenBurnsView
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFadeThrough
import com.google.gson.Gson
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.FragmentUpcomingBinding
import com.memad.moviesmix.utils.CenterZoomLayoutManager
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.getSnapPosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingFragment : Fragment(),
    UpcomingAdapter.OnMoviesClickListener {
    @Volatile
    private var lastSize: Int = 0
    private var error: String = ""
    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var upcomingAdapter: UpcomingAdapter
    private lateinit var snapHelper: LinearSnapHelper


    private val upcomingViewModel by viewModels<UpcomingViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.main_nav_host_fragment
            scrimColor = Color.TRANSPARENT
        }
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

        binding.upcomingRecyclerView.layoutManager =
            CenterZoomLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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
        binding.upcomingRecyclerView.adapter = upcomingAdapter
        upcomingAdapter.upcomingMovieClickListener = this
    }

    private fun snapPositionChange(snapPosition: Int) {
        Log.d("snapPositionChange", snapPosition.toString())
        if (upcomingAdapter.currentList.size == 0) {
            return
        }
        if (upcomingAdapter.currentList[snapPosition].moviePage == -122) {
            upcomingViewModel.loadNextPage()
            loading()
            return
        }
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val date =
            inputFormat.parse(upcomingAdapter.currentList[snapPosition].movie?.release_date!!)
        val formattedDate = outputFormat.format(date!!)
        "🍿 $formattedDate".also { binding.releaseDate.text = it }
    }

    private fun networkCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                upcomingViewModel.networkStatus.collectLatest {
                    when (it) {
                        is NetworkStatus.Disconnected -> {
                            error = getString(R.string.no_network)
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        networkCheck()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                upcomingViewModel.moviesResource.collect {
                    when (it) {
                        is Resource.Loading -> loading()
                        else -> {}
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                upcomingViewModel.moviesList.collectLatest {
                    lastSize = upcomingAdapter.currentList.size
                    upcomingAdapter.submitList(it)
                    if (upcomingAdapter.currentList.size > 20) {
                        snapPositionChange(upcomingAdapter.currentList.size - 21)
                    } else {
                        snapPositionChange(0)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                upcomingViewModel.checkFavouritesFlow.collect { booleans ->
                    upcomingAdapter.submitFavouritesList(booleans)
                }
            }
        }

    }


    private fun loading() {
        binding.releaseDate.text = getString(R.string.loading)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onMovieClicked(position: Int, imageView: KenBurnsView) {
        val extras = FragmentNavigatorExtras(
            imageView to position.toString() + "poster"
        )
        imageView.pause()
        val movieResult = upcomingAdapter.currentList[position]
        findNavController().navigate(
            UpcomingFragmentDirections.actionUpcomingFragmentToMovieDescriptionFragment(
                Gson().toJson(movieResult),
                position.toString()
            ),
            extras
        )
    }

    override fun onMovieDoubleClicked(position: Int) {
        if (upcomingAdapter.favouriteList.isNotEmpty() && !upcomingAdapter.favouriteList[position]) {
            binding.buttonFavoriteUpcoming.visibility = View.VISIBLE
            binding.buttonFavoriteUpcoming.isChecked = true
            binding.buttonFavoriteUpcoming.isActivated = true
            upcomingViewModel.addToFavourites(upcomingAdapter.currentList[position]!!)
            binding.buttonFavoriteUpcoming.playAnimation()
        } else {
            binding.buttonFavoriteUpcoming.visibility = View.VISIBLE
            binding.buttonFavoriteUpcoming.isChecked = false
            binding.buttonFavoriteUpcoming.isActivated = false
            upcomingViewModel.removeFromFavourites(upcomingAdapter.currentList[position].movieId!!)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding.buttonFavoriteUpcoming.visibility = View.GONE
        }, 1000)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }
}