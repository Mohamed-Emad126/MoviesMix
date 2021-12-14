package com.memad.moviesmix.ui.main.popular

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.transition.MaterialFadeThrough
import com.liaoinstan.springview.widget.SpringView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.FragmentPopularBinding
import com.memad.moviesmix.databinding.PosterDialogBinding
import com.memad.moviesmix.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPopularBinding.inflate(inflater, container, false)
        networkStatusHelper.announceStatus()
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
            popularViewModel.isFirstLoading.collect {
                if(it){
                    firstLoadingHandle()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            popularViewModel.moviesResource.collect {
                when (it) {
                    is Resource.Loading -> {
                            loading()
                    }
                    is Resource.Error -> error(popularViewModel.moviesListLiveData.value)
                    is Resource.Success -> success()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            popularViewModel.moviesList.collectLatest {
                Log.i("TAG: pop fragB:", "${it.size} :-> ${popularAdapter.popularMoviesList.size}")
                popularAdapter.popularMoviesList = it
                Log.i("TAG: pop fragA:", "${it.size} :-> ${popularAdapter.popularMoviesList.size}")
            }
        }
    }


    private fun loading() {
        binding.springView.visibility = View.VISIBLE
        binding.errorLayout.errorLayout.visibility = View.GONE
        binding.loadingLayout.loadingLayout.visibility = View.GONE
    }

    private fun firstLoadingHandle(){
        binding.loadingLayout.loadingLayout.visibility = View.VISIBLE
        binding.springView.visibility = View.GONE
        binding.errorLayout.errorLayout.visibility = View.GONE
    }


    private fun success() {
        binding.springView.visibility = View.VISIBLE
        binding.loadingLayout.loadingLayout.visibility = View.GONE
        binding.springView.onFinishFreshAndLoad()
        binding.errorLayout.errorLayout.visibility = View.GONE
    }

    private fun error(data: List<MovieEntity>?) {
        networkStatusHelper.announceStatus()
        if (data.isNullOrEmpty()) {
            binding.springView.visibility = View.GONE
            binding.errorLayout.errorLayout.visibility = View.VISIBLE
            binding.errorLayout.errorText.text = this.error
        } else {
            binding.springView.visibility = View.VISIBLE
            binding.errorLayout.errorLayout.visibility = View.GONE
            Toast.makeText(context, this.error, Toast.LENGTH_SHORT).show()
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

    override fun onMovieClicked(position: Int, imageView: ImageView?) {
        Toast.makeText(context, "single", Toast.LENGTH_SHORT).show()
    }

    override fun onMovieDoubleClicked(position: Int, imageView: ImageView?) {
        Toast.makeText(context, "Double", Toast.LENGTH_SHORT).show()
    }

    override fun onMovieHoldDown(position: Int) {
        posterDialogBinding.posterDialogImage
            .load(Constants.POSTER_BASE_URL +
                    popularAdapter.popularMoviesList[position].movie.poster_path){
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
            }
        posterDialog.show()
    }
}
