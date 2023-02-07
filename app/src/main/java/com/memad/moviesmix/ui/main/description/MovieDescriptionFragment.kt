package com.memad.moviesmix.ui.main.description

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.Gson
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentMovieDescriptionBinding
import com.memad.moviesmix.utils.Constants
import com.varunest.sparkbutton.SparkEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDescriptionFragment : Fragment() {
    private var _binding: FragmentMovieDescriptionBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<MovieDescriptionFragmentArgs>()
    private lateinit var movieEntity: MovieEntity

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
        postponeEnterTransition()
        binding.root.doOnPreDraw { startPostponedEnterTransition() }
        movieEntity = Gson().fromJson(args.movie, MovieEntity::class.java)

        binding.movieCover.transitionName = args.movieId
        init()
        setupObservers()
        initViews()

        return binding.root
    }

    private fun initViews() {
        binding.movieCover.load(
            Constants.POSTER_BASE_URL +
                    args.moviePoster
        ) {
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_broken)
            allowHardware(false)
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.favouriteButton.setOnClickListener {
            binding.favouriteButton.isActivated = !binding.favouriteButton.isActivated
            if (binding.favouriteButton.isActivated) {
                binding.favouriteButton.isChecked = true
                movieDescriptionViewModel.addToFavourites(movieEntity)
                binding.favouriteButton.playAnimation()
            } else {
                binding.favouriteButton.isChecked = false
                movieDescriptionViewModel.removeFromFavourites(args.movieId.toInt())
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            movieDescriptionViewModel.isFavourite.collectLatest{
                binding.favouriteButton.isActivated = it
                binding.favouriteButton.isChecked = it
            }
        }
    }

    private fun init() {

        movieDescriptionViewModel.getMovie(args.movieId.toInt())
        movieDescriptionViewModel.checkIsFavourites(args.movieId.toInt())
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
}