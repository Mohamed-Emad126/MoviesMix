package com.memad.moviesmix.ui.main.search

import android.annotation.SuppressLint
import android.graphics.Color
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
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.gson.Gson
import com.liaoinstan.springview.widget.SpringView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentSearchBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(),
    SearchAdapter.OnMoviesClickListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel by viewModels<SearchViewModel>()
    private var query: String = ""

    @Inject
    lateinit var searchAdapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
            duration = resources.getInteger(R.integer.duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply {
            duration = resources.getInteger(R.integer.duration_large).toLong()
        }
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        initViews()
        initVisibility()
        initLoadMore()
        initObservers()
        return binding.root
    }

    private fun initVisibility() {
        binding.springView.visibility = View.GONE
    }

    private fun networkCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.networkStatus.collectLatest {
                    when (it) {
                        is NetworkStatus.Connected -> {
                        }

                        else -> {
                            binding.emptyLayout.emptyText.text =
                                getString(R.string.no_network)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchStatus.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            hideKeyboard(binding.searchEdit)
                            binding.emptyLayout.emptyLayout.visibility = View.GONE
                        }

                        is Resource.Success -> {
                            binding.springView.visibility = View.VISIBLE
                            binding.loadingLayout.loadingLayout.visibility = View.GONE
                            if (it.data?.results?.isEmpty() == true) {
                                binding.emptyLayout.emptyLayout.visibility = View.VISIBLE
                                binding.emptyLayout.emptyText.text =
                                    String.format(getString(R.string.no_movies_found), query)
                                binding.springView.visibility = View.GONE
                            } else {
                                binding.emptyLayout.emptyLayout.visibility = View.GONE
                                binding.springView.visibility = View.VISIBLE
                            }
                        }

                        is Resource.Error -> {
                            binding.springView.visibility = View.GONE
                            binding.emptyLayout.emptyLayout.visibility = View.VISIBLE
                            binding.loadingLayout.loadingLayout.visibility = View.GONE
                            networkCheck()
                        }
                    }
                    binding.springView.onFinishFreshAndLoad()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchResultFlow.collectLatest {
                    searchAdapter.submitList(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.checkFavouritesFlow.collectLatest {
                    searchAdapter.submitFavouritesList(it)
                }
            }
        }
    }

    private fun initLoadMore() {
        binding.springView.setListener(object : SpringView.OnFreshListener {
            override fun onRefresh() {
            }

            override fun onLoadmore() {
                searchViewModel.loadMore(query)
            }
        })
    }

    private fun initViews() {
        binding.searchToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.searchRecycler.adapter = searchAdapter
        binding.searchRecycler.isSaveEnabled = true
        searchAdapter.movieClickListener = this

        binding.searchEdit.setOnEditorActionListener { _, _, _ ->
            searchViewModel.searchMovies(binding.searchEdit.text.toString())
            binding.loadingLayout.loadingLayout.visibility = View.VISIBLE
            query = binding.searchEdit.text.toString()
            binding.searchEdit.clearFocus()
            true
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMovieClicked(position: Int, imageView: ShapeableImageView) {
        val extras = FragmentNavigatorExtras(
            imageView to position.toString()
        )

        val movieResult = searchAdapter.currentList[position]
        findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToMovieDescriptionFragment(
                Gson().toJson(MovieEntity(movieResult.id, Constants.SEARCH, 1, movieResult)),
                position.toString()
            ),
            extras
        )
    }

    override fun onMovieFavouriteClicked(position: Int, isActivated: Boolean) {
        if (isActivated) {
            searchViewModel.addToFavourites(
                MovieEntity(
                    movieId = null,
                    Constants.FAVOURITE,
                    1,
                    searchAdapter.currentList[position]
                )
            )
        } else {
            searchViewModel.removeFromFavourites(searchAdapter.currentList[position].id)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }
}