package com.memad.moviesmix.ui.main.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.transition.MaterialFadeThrough
import com.google.gson.Gson
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentFavouritesBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.setSwipeToDelete
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FavouritesFragment : Fragment(), FavouritesAdapter.OnFavouriteEntityClickListener {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var favouritesAdapter: FavouritesAdapter

    private var lastDeleted = -1

    private val favouritesViewModel by viewModels<FavouritesViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        binding.emptyLayout.emptyText.text = getString(R.string.no_favourites_yet)
        favouritesViewModel.getFavoriteMovies()
        initAdapter()
        initObservers()
        return binding.root
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                favouritesViewModel.favoriteMoviesFlow.collectLatest {
                    favouritesAdapter.submitList(it)
                    if (it.isEmpty()) {
                        binding.emptyLayout.emptyLayout.visibility = View.VISIBLE
                        binding.favouritesRecycler.visibility = View.GONE
                    } else {
                        binding.emptyLayout.emptyLayout.visibility = View.GONE
                        binding.favouritesRecycler.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            favouritesViewModel.deleteStatusFlow.collectLatest {
                if (it && lastDeleted != -1) {
                    favouritesAdapter.notifyItemRemoved(lastDeleted)
                }
            }
        }
    }

    private fun initAdapter() {
        binding.favouritesRecycler.adapter = favouritesAdapter
        binding.favouritesRecycler.setHasFixedSize(true)
        favouritesAdapter.favouriteClickListener = this
        binding.favouritesRecycler.setSwipeToDelete {
            lastDeleted = it
            favouritesViewModel.removeFromFavourites(favouritesAdapter.currentList[it].movieId!!)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFavouriteEntityClicked(position: Int, posterImageView: ShapeableImageView) {
        val extras = FragmentNavigatorExtras(
            posterImageView to position.toString() + "poster"
        )

        val movieResult = favouritesAdapter.currentList[position]
        findNavController().navigate(
            FavouritesFragmentDirections.actionFavouritesFragmentToMovieDescriptionFragment(
                Gson().toJson(
                    MovieEntity(
                        movieResult.movieId,
                        Constants.FAVOURITE,
                        1,
                        movieResult.movie
                    )
                ),
                position.toString()
            ),
            extras
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }
}