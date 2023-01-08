package com.memad.moviesmix.ui.main.trending

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.transition.MaterialFadeThrough
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentTrendingBinding
import com.memad.moviesmix.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class TrendingFragment : Fragment(), TrendingAdapter.OnMoviesClickListener,
    LoadingAdapter.OnLoadingAdapterClickListener {

    private lateinit var snapHelper: LinearSnapHelper
    private lateinit var concatAdapter: ConcatAdapter

    @Volatile
    private var lastSize: Int = 0
    private val TAG: String = "TRENDS"
    private var error: String = ""

    @Inject
    lateinit var networkStatusHelper: NetworkStatusHelper

    @Inject
    lateinit var trendingAdapter: TrendingAdapter

    @Inject
    lateinit var loadingAdapter: LoadingAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val trendingViewModel by viewModels<TrendingViewModel>()
    private var _binding: FragmentTrendingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.discreteScrollView.layoutManager = layoutManager
        concatAdapter = ConcatAdapter(trendingAdapter, loadingAdapter)
        trendingAdapter.trendingMovieClickListener = this
        loadingAdapter.errorClickListener = this
        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.discreteScrollView)
        binding.discreteScrollView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val viewHolder: RecyclerView.ViewHolder? =
                    binding.discreteScrollView.findViewHolderForAdapterPosition(
                        snapHelper.getSnapPosition(recyclerView)
                    )
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    (viewHolder as TrendingAdapter.TrendingViewHolder).itemBinding.materialCardView.apply {
                            animate().setDuration(300).scaleX(1F).scaleY(1F)
                                .setInterpolator(AccelerateInterpolator()).start()
                        }
                    snapPositionChange(snapHelper.getSnapPosition(recyclerView))
                } else {
                    (viewHolder as TrendingAdapter.TrendingViewHolder).itemBinding.materialCardView.apply {
                            animate().setDuration(300).scaleX(.75F).scaleY(.75F)
                                .setInterpolator(AccelerateInterpolator()).start()
                        }
                }
            }
        })
        binding.discreteScrollView.adapter = concatAdapter
    }


    private fun setupObservables() {
        networkStatusHelper.observe(viewLifecycleOwner) {
            this.error = when (it) {
                is NetworkStatus.Available -> requireContext().resources.getString(
                    R.string.something_went_wrong
                )
                is NetworkStatus.Unavailable -> requireContext().resources.getString(
                    R.string.no_network
                )
            }
        }
        lifecycleScope.launchWhenStarted {
            trendingViewModel.moviesResource.collect {
                Log.d(TAG, "setupObservables: $it")
                withContext(Dispatchers.Main) {
                    when (it) {
                        is Resource.Loading -> loading()
                        is Resource.Error -> error(list = it.data!!)
                        is Resource.Success -> success()
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            trendingViewModel.moviesList.collectLatest {
                lastSize = trendingAdapter.trendingMoviesList.size
                success()
                Log.i(TAG, "setupObservables: ${it.size}")
                trendingAdapter.trendingMoviesList = it
                if (lastSize == 0) {
                    handleDetailsUi(lastSize)
                }
            }
        }
    }

    private fun error(list: List<MovieEntity>) {
        if (list.isEmpty()) {
            loadingAdapter.error()
            binding.movieName.text = error
            binding.headerText.visibility = GONE
            binding.movieGenre.visibility = GONE
            binding.sparkButton.visibility = GONE
            binding.releaseYear.visibility = GONE
            binding.backdropImage.load(R.drawable.start_img_min_blur)
            binding.progressPercent.text = ""
            binding.progressBar.progress = 0
        }
    }

    private fun success() {
        loadingAdapter.loading()
        binding.headerText.text = getString(R.string.trending_header)
        binding.headerText.visibility = VISIBLE
        binding.movieGenre.visibility = VISIBLE
        binding.sparkButton.visibility = VISIBLE
        binding.releaseYear.visibility = VISIBLE
    }

    private fun loading() {
        loadingAdapter.loading()
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
            Constants.POSTER_BASE_URL + trendingAdapter.trendingMoviesList[position].movie?.backdrop_path
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
        val animation: ObjectAnimator = ObjectAnimator.ofInt(
            binding.progressBar, "progress", binding.progressBar.progress, voteAverage.toInt() * 100
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

    override fun onMovieClicked(position: Int, imageView: ImageView?) {
        Toast.makeText(context, "$position Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onMovieErrorStateClicked() {
        trendingViewModel.reload()
    }

    fun snapPositionChange(position: Int) {
        Log.i(TAG, "onScrollEnd: ")
        if (position == trendingAdapter.trendingMoviesList.size - 1) {
            trendingViewModel.loadNextPage()
        } else {
            handleDetailsUi(position)
            success()
        }
    }

}