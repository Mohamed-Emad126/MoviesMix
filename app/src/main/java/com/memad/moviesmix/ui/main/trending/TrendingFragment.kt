package com.memad.moviesmix.ui.main.trending

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
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
import coil.load
import com.commit451.coiltransformations.BlurTransformation
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.gson.Gson
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentTrendingBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.GenresUtils
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.NetworkStatusHelper
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.getSnapPosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class TrendingFragment : Fragment(), TrendingAdapter.OnMoviesClickListener {

    private lateinit var snapHelper: LinearSnapHelper

    @Volatile
    private var lastSize: Int = 0

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
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.main_nav_host_fragment
            scrimColor = Color.TRANSPARENT
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.main_nav_host_fragment
            scrimColor = Color.TRANSPARENT
        }
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
                trendingViewModel.removeFromFavourites(
                    trendingAdapter.currentList[snapHelper.getSnapPosition(
                        binding.recyclerTrending
                    )].movieId!!
                )
            } else {
                binding.sparkButton.playAnimation()
                binding.sparkButton.isChecked = true
                trendingViewModel.addToFavourites(
                    trendingAdapter.currentList[snapHelper.getSnapPosition(
                        binding.recyclerTrending
                    )]
                )
            }
        }
        return binding.root
    }

    private fun setupDiscreteScrollView() {
        binding.recyclerTrending.adapter = trendingAdapter
        binding.recyclerTrending.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        trendingAdapter.trendingMovieClickListener = this
        snapHelper = object : LinearSnapHelper() {
            override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
                val firstVisiblePosition =
                    (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                val firstItem = 0
                val lastItem = layoutManager.itemCount - 1
                return when {
                    firstItem == firstVisiblePosition -> layoutManager.findViewByPosition(
                        firstVisiblePosition
                    )

                    lastItem == lastVisiblePosition -> layoutManager.findViewByPosition(
                        lastVisiblePosition
                    )

                    else -> super.findSnapView(layoutManager)
                }
            }
        }
        snapHelper.attachToRecyclerView(binding.recyclerTrending)
        binding.recyclerTrending
            .addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val viewHolder: RecyclerView.ViewHolder? =
                        binding.recyclerTrending.findViewHolderForAdapterPosition(
                            snapHelper.getSnapPosition(recyclerView)
                        )
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && viewHolder is TrendingAdapter.TrendingViewHolder) {
                        binding.recyclerTrending.layoutManager?.let {
                            snapPositionChange(
                                it.getPosition(
                                    snapHelper.findSnapView(
                                        binding.recyclerTrending.layoutManager
                                    )!!
                                )
                            )
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollHorizontally(1)) {
                        trendingViewModel.loadNextPage()
                    }
                }
            })
    }


    private fun setupObservables() {
        networkStatusHelper.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkStatus.Unavailable -> {
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }

                else -> {}
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                trendingViewModel.moviesResource.collect {
                    when (it) {
                        is Resource.Loading -> loading()
                        is Resource.Error -> error(list = it.data!!)
                        is Resource.Success -> success()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                trendingViewModel.moviesList.collectLatest {
                    lastSize = trendingAdapter.currentList.size
                    trendingAdapter.submitList(it)
                    success()
                    if (trendingAdapter.currentList.size > 20) {
                        handleDetailsUi(trendingAdapter.currentList.size - 21)
                    } else {
                        handleDetailsUi(0)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                trendingViewModel.checkFavouritesFlow.collect { booleans ->
                    trendingAdapter.submitFavouritesList(booleans)
                }
            }
        }
    }

    private fun error(list: List<MovieEntity>) {
        if (list.isEmpty()) {
            binding.movieName.text = error
            binding.headerText.visibility = GONE
            binding.movieGenre.visibility = GONE
            binding.sparkButton.visibility = GONE
            binding.releaseYear.visibility = GONE
            binding.backdropImage.load(R.drawable.start_img_min_blur) { allowHardware(false) }
            binding.progressPercent.text = ""
            binding.progressBar.progress = 0
        }
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
        binding.backdropImage.load(R.drawable.start_img_min_blur) {
            allowHardware(false)
        }
    }

    private fun handleDetailsUi(
        position: Int
    ) {
        if (trendingAdapter.currentList[position].moviePage == -122)
            return
        binding.movieName.text = trendingAdapter.currentList[position].movie?.original_title
        binding.backdropImage.load(
            Constants.POSTER_BASE_URL + trendingAdapter.currentList[position].movie?.backdrop_path
        ) {
            transformations(BlurTransformation(requireContext(), 3f, 3f))
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_blur)
            allowHardware(false)
        }
        createProgressAnimation(trendingAdapter.currentList[position].movie?.vote_average!!)
        binding.releaseYear.text = trendingAdapter.currentList[position].movie?.release_date
        binding.movieGenre.text = trendingAdapter.currentList[position].movie?.genre_ids.let {
            GenresUtils.getGenres(
                it!!
            ).joinToString(separator = ", ")
        }.toString()
        if (trendingAdapter.favouriteList.isNotEmpty()) {
            binding.sparkButton.isChecked = trendingAdapter.favouriteList[position]
        }
    }

    private fun createProgressAnimation(voteAverage: Double) {
        binding.progressBar
        val animation: ObjectAnimator = ObjectAnimator.ofInt(
            binding.progressBar, "progress", binding.progressBar.progress, voteAverage.toInt() * 100
        )
        animation.duration = 300
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

    override fun onMovieClicked(position: Int, imageView: ShapeableImageView) {
        val extras = FragmentNavigatorExtras(
            imageView to position.toString()
        )

        val movieResult = trendingAdapter.currentList[position]
        findNavController().navigate(
            TrendingFragmentDirections.actionTrendingFragmentToMovieDescriptionFragment(
                Gson().toJson(movieResult),
                position.toString()
            ),
            extras
        )
    }

    fun snapPositionChange(position: Int) {
        handleDetailsUi(position)
        success()
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerTrending.layoutManager?.let {
            handleDetailsUi(
                it.getPosition(
                    snapHelper.findSnapView(
                        binding.recyclerTrending.layoutManager
                    )!!
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }
}
