package com.memad.moviesmix.ui.main.description

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
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
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.gson.Gson
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentMovieDescriptionBinding
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
class MovieDescriptionFragment : Fragment(), RecommendAdapter.OnMovieClickListener {
    private var _binding: FragmentMovieDescriptionBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<MovieDescriptionFragmentArgs>()
    private lateinit var movieEntity: MovieEntity

    @Inject
    lateinit var recommendAdapter: RecommendAdapter

    @Inject
    lateinit var castsAdapter: CastsAdapter

    private val movieDescriptionViewModel by viewModels<MovieDescriptionViewModel>()

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
        ViewCompat.setTransitionName(binding.posterImage, args.movieId)
        initViews()
        init()
        initRecommendations()
        initCasts()
        setupObservers()
        return binding.root
    }

    private fun initCasts() {
        binding.recyclerViewCast.adapter = castsAdapter
        binding.recyclerViewCast.setHasFixedSize(true)
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
            toViewer(movieEntity.movie?.poster_path!!)
        }
        binding.detailsBannerImage.setOnClickListener {
            toViewer(movieEntity.movie?.backdrop_path!!)
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
                movieDescriptionViewModel.removeFromFavourites(args.movieId.toInt())
            }
        }

        movieEntity.movie?.let { movie ->
            GenresUtils.getGenres(movie.genre_ids).forEach {
                binding.textGenres.addChip(requireContext(), it)
            }
        }
    }

    private fun toViewer(url: String) {
        val extras = FragmentNavigatorExtras(
            binding.posterImage to args.movieId
        )
        val action =
            MovieDescriptionFragmentDirections.actionMovieDescriptionFragmentToViewerFragment(
                Constants.POSTER_BASE_URL + url, args.movieId
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
            extras
        )
    }
}