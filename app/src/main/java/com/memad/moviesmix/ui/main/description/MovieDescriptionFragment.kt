package com.memad.moviesmix.ui.main.description

import android.graphics.Color
import android.os.Bundle
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
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.Gson
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentMovieDescriptionBinding
import com.memad.moviesmix.models.Cast
import com.memad.moviesmix.models.VideosResponse
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.Constants.RECOMMENDED
import com.memad.moviesmix.utils.GenresUtils
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.addChip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MovieDescriptionFragment : Fragment(), RecommendAdapter.OnMovieClickListener,
    CastsAdapter.OnCastClickListener {
    private lateinit var videosResponse: List<VideosResponse.Result>
    private var _binding: FragmentMovieDescriptionBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<MovieDescriptionFragmentArgs>()
    private lateinit var movieEntity: MovieEntity

    @Inject
    lateinit var recommendAdapter: RecommendAdapter

    @Inject
    lateinit var castsAdapter: CastsAdapter

    private val movieDescriptionViewModel by viewModels<DescriptionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.main_nav_host_fragment
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMovieDescriptionBinding.inflate(inflater, container, false)
        movieEntity = Gson().fromJson(args.movie, MovieEntity::class.java)
        binding.posterImage.transitionName = args.movieId + "poster"
        init()
        initViews()
        initRecommendations()
        initCasts()
        setupObservers()
        return binding.root
    }

    private fun initCasts() {
        binding.recyclerViewCast.adapter = castsAdapter
        binding.recyclerViewCast.setHasFixedSize(true)
        castsAdapter.castClickListener = this
    }

    private fun initRecommendations() {
        binding.recyclerViewRelated.adapter = recommendAdapter
        binding.recyclerViewRelated.setHasFixedSize(true)
        recommendAdapter.similarMovieClickListener = this

    }

    private fun initViews() {
        binding.posterImage.load(
            Constants.POSTER_BASE_URL +
                    (movieEntity.movie?.poster_path)
        ) {
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_broken)
            allowHardware(false)
        }.job.invokeOnCompletion {
            binding.posterImage.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
        binding.textMovieTitle.text = movieEntity.movie?.title
        binding.textDescription.text = movieEntity.movie?.overview
        binding.textReleaseDate.text = movieEntity.movie?.release_date
        binding.textRating.text = movieEntity.movie?.vote_average.toString()
        binding.detailsBannerImage.load(
            Constants.POSTER_BASE_URL +
                    movieEntity.movie?.backdrop_path
        ) {
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_broken)
            allowHardware(false)
        }

        binding.posterImage.setOnClickListener {
            toViewer(movieEntity.movie?.poster_path!!, false)
        }
        binding.detailsBannerImage.transitionName = args.movieId
        binding.detailsBannerImage.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                binding.detailsBannerImage to args.movieId
            )
            val action =
                MovieDescriptionFragmentDirections.actionMovieDescriptionFragmentToViewerFragment(
                    movieEntity.movie?.backdrop_path!!, args.movieId, false
                )
            findNavController().navigate(action, extras)
        }

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonFavorite.setOnClickListener {
            binding.buttonFavorite.isActivated = !binding.buttonFavorite.isActivated
            if (binding.buttonFavorite.isActivated) {
                binding.buttonFavorite.isChecked = true
                movieDescriptionViewModel.addToFavourites(movieEntity)
                binding.buttonFavorite.playAnimation()
            } else {
                binding.buttonFavorite.isChecked = false
                movieEntity.movieId?.let { it1 -> movieDescriptionViewModel.removeFromFavourites(it1) }
            }
        }

        movieEntity.movie?.let { movie ->
            GenresUtils.getGenres(movie.genre_ids).forEach {
                binding.textGenres.addChip(requireContext(), it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                movieDescriptionViewModel.videos.collectLatest {
                    when (it) {
                        is Resource.Success -> {
                            binding.fabPlayButton.visibility = View.VISIBLE
                            videosResponse = it.data?.results!!
                        }

                        is Resource.Error -> {
                            binding.fabPlayButton.visibility = View.GONE
                        }

                        is Resource.Loading -> {
                            binding.fabPlayButton.visibility = View.GONE
                        }
                    }
                }
            }
        }
        binding.fabPlayButton.setOnClickListener {
            var allUrls = videosResponse
            allUrls = allUrls.filter {
                it.site == "YouTube" && it.type == "Trailer" && it.official
            }
            if (movieEntity.movie?.video == true || allUrls.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.video_not_available),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                toViewer(allUrls.joinToString(separator = "**") { it.key }, true)
            }
        }
    }

    private fun toViewer(url: String, isVideo: Boolean) {
        val extras = FragmentNavigatorExtras(
            binding.posterImage to args.movieId
        )
        val action =
            MovieDescriptionFragmentDirections.actionMovieDescriptionFragmentToViewerFragment(
                url, args.movieId, isVideo
            )
        findNavController().navigate(action, extras)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            movieDescriptionViewModel.isFavourite.collectLatest {
                binding.buttonFavorite.isActivated = it
                binding.buttonFavorite.isChecked = it
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                movieDescriptionViewModel.similarMovies.collectLatest {
                    if (it is Resource.Success) {
                        binding.headingRelated.visibility = View.VISIBLE
                        binding.recyclerViewRelated.visibility = View.VISIBLE
                        recommendAdapter.submitList(it.data?.results?.toMutableList())
                    } else {
                        binding.headingRelated.visibility = View.GONE
                        binding.recyclerViewRelated.visibility = View.GONE
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                movieDescriptionViewModel.cast.collectLatest {
                    if (it is Resource.Success) {
                        binding.headingCast.visibility = View.VISIBLE
                        binding.recyclerViewCast.visibility = View.VISIBLE
                        castsAdapter.submitList(it.data?.cast?.toMutableList())
                    } else {
                        binding.headingCast.visibility = View.GONE
                        binding.recyclerViewCast.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun init() {
        movieEntity.movie?.let {
            movieDescriptionViewModel.getMovie(it.id)
            movieDescriptionViewModel.checkIsFavourites(it.id)
            movieDescriptionViewModel.getSimilarMovies(it.id.toString())
            movieDescriptionViewModel.getCastOfMovie(it.id.toString())
            movieDescriptionViewModel.getVideo(it.id.toString())
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onSimilarMovieClicked(position: Int, posterImage: ShapeableImageView) {
        val extras = FragmentNavigatorExtras(
            posterImage to position.toString()
        )
        val movieResult = recommendAdapter.currentList[position]
        findNavController().navigate(
            MovieDescriptionFragmentDirections.actionMovieDescriptionFragmentSelf(
                Gson().toJson(MovieEntity(movieResult.id, RECOMMENDED, 1, movieResult)),
                position.toString()
            ),
            navigatorExtras = extras
        )
    }

    override fun onCastClick(position: Int, cast: Cast, imageView: ShapeableImageView) {
        val extras = FragmentNavigatorExtras(
            imageView to position.toString()
        )
        findNavController().navigate(
            MovieDescriptionFragmentDirections.actionMovieDescriptionFragmentToViewerFragment(
                cast.profile_path, position.toString(), false
            ),
            extras
        )
    }
}