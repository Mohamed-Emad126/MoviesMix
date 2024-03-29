package com.memad.moviesmix.ui.main.popular

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.transition.MaterialFadeThrough
import com.google.gson.Gson
import com.liaoinstan.springview.widget.SpringView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentPopularBinding
import com.memad.moviesmix.databinding.PosterDialogBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.createDialog
import com.varunest.sparkbutton.SparkButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PopularFragment : Fragment(), SpringView.OnFreshListener,
    PopularAdapter.OnMoviesClickListener {


    @Inject
    lateinit var popularAdapter: PopularAdapter

    private lateinit var posterDialog: Dialog

    private val popularViewModel by viewModels<PopularViewModel>()
    private var error: String = ""
    private var _binding: FragmentPopularBinding? = null
    private val binding get() = _binding!!
    private var _posterDialogBinding: PosterDialogBinding? = null
    private val posterDialogBinding get() = _posterDialogBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPopularBinding.inflate(inflater, container, false)
        setupObservables()
        initRecyclerView()
        _posterDialogBinding = PosterDialogBinding.inflate(inflater)
        posterDialog =
            requireActivity()
                .createDialog(posterDialogBinding.root, true)
        binding.springView.setListener(this)
        binding.errorLayout.refreshButton.setOnClickListener {
            popularViewModel.setIsFirstLoading(true)
            popularViewModel.refresh()
        }
        return binding.root
    }

    private fun initRecyclerView() {
        binding.moviesRecycler.isSaveEnabled = true
        binding.moviesRecycler.setHasFixedSize(true)
        popularAdapter.popularMovieClickListener = this
        binding.moviesRecycler.adapter = popularAdapter
    }

    private fun networkCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                popularViewModel.networkStatus.collectLatest {
                    when (it) {
                        is NetworkStatus.Disconnected -> {
                            error = getString(R.string.no_network)
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        }

                        is NetworkStatus.Unknown -> {
                            error = getString(R.string.no_network)
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupObservables() {
        viewLifecycleOwner.lifecycleScope.launch {
            popularViewModel.isFirstLoading.collectLatest {
                if (it) {
                    firstLoadingHandle()
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            popularViewModel.moviesResource.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        if (popularViewModel.isFirstLoading.value) {
                            firstLoadingHandle()
                        } else {
                            loading()
                        }
                    }

                    is Resource.Error -> {
                        error(popularViewModel.moviesListLiveData.value?.toMutableList())
                        if (it.data.isNullOrEmpty()) {
                            networkCheck()
                        }
                    }

                    is Resource.Success -> success()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                popularViewModel.moviesList.collectLatest {
                    //popularAdapter.popularMoviesList = it
                    popularAdapter.submitList(it.toMutableList())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                popularViewModel.checkFavouritesFlow.collectLatest {
                    popularAdapter.submitFavouritesList(it)
                }
            }
        }
    }


    private fun loading() {
        binding.springView.visibility = View.VISIBLE
        binding.errorLayout.errorLayout.visibility = View.GONE
        binding.loadingLayout.loadingLayout.visibility = View.GONE
    }

    private fun firstLoadingHandle() {
        binding.loadingLayout.loadingLayout.visibility = View.VISIBLE
        binding.springView.visibility = View.GONE
        binding.errorLayout.errorLayout.visibility = View.GONE
    }


    private fun success() {
        binding.springView.onFinishFreshAndLoad()
        if (binding.springView.visibility == View.GONE) {
            binding.springView.visibility = View.VISIBLE
        }
        binding.loadingLayout.loadingLayout.visibility = View.GONE
        binding.errorLayout.errorLayout.visibility = View.GONE
    }

    private fun error(data: List<MovieEntity>?) {
        if (data.isNullOrEmpty()) {
            binding.springView.visibility = View.GONE
            binding.errorLayout.errorLayout.visibility = View.VISIBLE
            binding.errorLayout.errorText.text = this.error
        } else {
            binding.springView.visibility = View.VISIBLE
            binding.errorLayout.errorLayout.visibility = View.GONE
        }
        binding.loadingLayout.loadingLayout.visibility = View.GONE
        binding.springView.onFinishFreshAndLoad()
    }


    override fun onRefresh() {
        popularViewModel.refresh()
    }

    override fun onLoadmore() {
        popularViewModel.loadNextPage()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _posterDialogBinding = null
    }

    override fun onMovieClicked(position: Int, imageView: ShapeableImageView) {
        val extras = FragmentNavigatorExtras(
            imageView to position.toString() + "poster"
        )
        findNavController().navigate(
            PopularFragmentDirections.actionPopularFragmentToMovieDescriptionFragment(
                Gson().toJson(popularAdapter.currentList[position]),
                position.toString()
            ),
            extras
        )
    }

    override fun onMovieDoubleClicked(position: Int, favouriteButton: SparkButton) {
        if (popularAdapter.favouritesList.isNotEmpty() && !popularAdapter.favouritesList[position]) {
            favouriteButton.visibility = View.VISIBLE
            favouriteButton.isChecked = true
            favouriteButton.isActivated = true
            popularViewModel.addToFavourites(popularAdapter.currentList[position]!!)
            favouriteButton.playAnimation()
        } else {
            favouriteButton.visibility = View.VISIBLE
            favouriteButton.isChecked = false
            favouriteButton.isActivated = false
            popularViewModel.removeFromFavourites(popularAdapter.currentList[position].movieId!!)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            favouriteButton.visibility = View.GONE
        }, 1000)
    }

    override fun onMovieHoldDown(position: Int) {
        posterDialogBinding.posterDialogImage
            .load(
                Constants.POSTER_BASE_URL +
                        popularAdapter.currentList[position].movie?.poster_path
            ) {
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
                allowHardware(false)
            }
        posterDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onResume() {
        super.onResume()
        popularViewModel.checkIsFavourites()
        success()
    }
}
