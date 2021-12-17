package com.memad.moviesmix.ui.main.trending

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.BlurTransformation
import com.google.android.material.transition.MaterialFadeThrough
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.FragmentTrendingBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.NetworkStatusHelper
import com.memad.moviesmix.utils.Resource
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class TrendingFragment : Fragment(),
    DiscreteScrollView.ScrollStateChangeListener<TrendingAdapter.TrendingViewHolder>,
    TrendingAdapter.OnMoviesClickListener {

    @Volatile private var lastSize: Int = 0
    private val TAG: String = "TRENDS"
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
        setupDiscreteScrollView()
        setupObservables()
        binding.sparkButton.setOnClickListener {
            if (binding.sparkButton.isChecked) {
                binding.sparkButton.isChecked = false
            } else {
                binding.sparkButton.playAnimation()
                binding.sparkButton.isChecked = true
            }
        }
        return binding.root
    }

    private fun setupDiscreteScrollView() {
        trendingAdapter.trendingMovieClickListener = this
        binding.discreteScrollView.addScrollStateChangeListener(this)
        binding.discreteScrollView.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.25f)
                .setMinScale(0.8f)
                .build()
        )
        binding.discreteScrollView.setSlideOnFling(true)
        binding.discreteScrollView.adapter = trendingAdapter
        trendingAdapter.addLoadingItem(0)
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
            trendingViewModel.moviesResource.collect {
                Log.d(TAG, "setupObservables: $it")
                withContext(Dispatchers.Main) {
                    when (it) {
                        is Resource.Loading -> loading()
                        is Resource.Error -> error()
                        is Resource.Success -> success()
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            trendingViewModel.moviesList.collectLatest {
                lastSize = trendingAdapter.trendingMoviesList.size - 1
                success()
                trendingAdapter.trendingMoviesList = it
                handleDetailsUi(lastSize)
            }
        }
    }

    private fun error() {
        trendingAdapter.addErrorItem()
        binding.movieName.text = error
        binding.headerText.visibility = GONE
        binding.movieGenre.visibility = GONE
        binding.sparkButton.visibility = GONE
        binding.releaseYear.visibility = GONE
        binding.backdropImage.load(R.drawable.start_img_min_blur)
        binding.progressPercent.text = ""
        binding.progressBar.progress = 0
    }

    private fun success() {
        binding.headerText.text = getString(R.string.trending_header)
        binding.headerText.visibility = VISIBLE
        binding.movieGenre.visibility = VISIBLE
        binding.sparkButton.visibility = VISIBLE
        binding.releaseYear.visibility = VISIBLE
    }

    private fun loading() {
        binding.movieName.text = getString(R.string.loading)
        binding.movieGenre.visibility = GONE
        binding.sparkButton.visibility = GONE
        binding.releaseYear.visibility = GONE
        binding.progressPercent.text = ""
        createProgressAnimation(0.0)
        binding.backdropImage.load(R.drawable.start_img_min_blur)
    }

    private fun handleDetailsUi(
        position: Int
    ) {
        binding.movieName.text = trendingAdapter.trendingMoviesList[position].movie?.original_title
        binding.backdropImage.load(
            Constants.POSTER_BASE_URL +
                    trendingAdapter.trendingMoviesList[position].movie?.backdrop_path
        ) {
            transformations(BlurTransformation(requireContext(), 3f, 3f))
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_blur)
        }
        createProgressAnimation(trendingAdapter.trendingMoviesList[position].movie?.vote_average!!)
        binding.releaseYear.text = trendingAdapter.trendingMoviesList[position].movie?.release_date
    }

    private fun createProgressAnimation(voteAverage: Double) {
        binding.progressBar
        val animation: ObjectAnimator =
            ObjectAnimator.ofInt(
                binding.progressBar,
                "progress",
                binding.progressBar.progress,
                voteAverage.toInt() * 100
            )
        animation.duration = 1000
        animation.setAutoCancel(true)
        animation.interpolator = DecelerateInterpolator()
        animation.addUpdateListener {
            binding.progressPercent.text =
                (it.getAnimatedValue("progress") as Int).toFloat().div(100.0).toString()
        }
        animation.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onScroll(
        scrollPosition: Float,
        currentPosition: Int,
        newPosition: Int,
        currentHolder: TrendingAdapter.TrendingViewHolder?,
        newCurrent: TrendingAdapter.TrendingViewHolder?
    ) {
        loading()
    }

    override fun onScrollStart(
        currentItemHolder: TrendingAdapter.TrendingViewHolder,
        adapterPosition: Int
    ) {
        Log.i(TAG, "onScrollStart: ")
        if (adapterPosition == trendingAdapter.trendingMoviesList.size - 1) {
            loading()
        } else {
            handleDetailsUi(adapterPosition)
            success()
        }
    }

    override fun onScrollEnd(
        currentItemHolder: TrendingAdapter.TrendingViewHolder,
        adapterPosition: Int
    ) {
        Log.i(TAG, "onScrollEnd: ")
        if (adapterPosition == trendingAdapter.trendingMoviesList.size - 1) {
            loading()
            trendingViewModel.loadNextPage()
        } else {
            handleDetailsUi(adapterPosition)
            success()
        }
    }

    override fun onMovieClicked(position: Int, imageView: ImageView?) {
        Toast.makeText(context, "$position Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onMovieErrorStateClicked(position: Int) {
        trendingViewModel.reload()
    }
}