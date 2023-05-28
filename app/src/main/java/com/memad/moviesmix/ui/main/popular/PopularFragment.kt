package com.memad.moviesmix.ui.main.popular

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.gson.Gson
import com.liaoinstan.springview.widget.SpringView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentPopularBinding
import com.memad.moviesmix.databinding.PosterDialogBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.NetworkStatusHelper
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.createDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PopularFragment : Fragment(), SpringView.OnFreshListener,
    PopularAdapter.OnMoviesClickListener {

    @Inject
    lateinit var networkStatusHelper: NetworkStatusHelper

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
        //exitTransition = MaterialElevationScale(false)
        sharedElementReturnTransition =  MaterialContainerTransform().apply {
            drawingViewId = R.id.main_nav_host_fragment
            scrimColor = Color.TRANSPARENT
        }
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

                    is Resource.Error -> error(popularViewModel.moviesListLiveData.value?.toMutableList())
                    is Resource.Success -> success()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            popularViewModel.moviesList.collectLatest {
                Log.i(
                    "TAG: popp:",
                    "${it.size} :-> ${popularAdapter.currentList.size}"
                )
                //popularAdapter.popularMoviesList = it
                popularAdapter.submitList(it.toMutableList())
                Log.i(
                    "TAG: popp:",
                    "${it.size} :-> ${popularAdapter.currentList.size}"
                )
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
        binding.springView.visibility = View.VISIBLE
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
            //Toast.makeText(context, this.error, Toast.LENGTH_SHORT).show()
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
            imageView to position.toString()
        )
        findNavController().navigate(
            PopularFragmentDirections.actionPopularFragmentToMovieDescriptionFragment(
                Gson().toJson(popularAdapter.currentList[position]),
                position.toString()
            ),
            extras
        )
    }

    override fun onMovieDoubleClicked(position: Int, imageView: ImageView?) {
        Toast.makeText(context, "Double", Toast.LENGTH_SHORT).show()
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
}
