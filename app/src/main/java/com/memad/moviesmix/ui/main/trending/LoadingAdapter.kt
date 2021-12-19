package com.memad.moviesmix.ui.main.trending

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.MovieTrendItemLoadingBinding
import javax.inject.Inject

class LoadingAdapter @Inject constructor() :
    RecyclerView.Adapter<LoadingAdapter.LoadingViewHolder>() {
    lateinit var errorClickListener: OnLoadingAdapterClickListener

    private var loadingStatus = true

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder {
        return LoadingViewHolder(
            MovieTrendItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun error() {
        loadingStatus = false
        notifyItemChanged(0)
    }

    fun loading() {
        loadingStatus = true
        notifyItemChanged(0)
    }

    override fun onBindViewHolder(holder: LoadingViewHolder, position: Int) {
        if (loadingStatus) {
            holder.itemBinding.itemLoadingVeilLayout.veil()
        } else {
            holder.itemBinding.itemLoadingVeilLayout.unVeil()
            holder.itemBinding.posterImage.load(R.drawable.ic_robot_broken) {
                crossfade(true)
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    /////////////////////////////////////////////////////////////////
/////////////////////////ViewHolder//////////////////////////////
/////////////////////////////////////////////////////////////////
    inner class LoadingViewHolder(val itemBinding: MovieTrendItemLoadingBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.materialLoadingCardView.setOnClickListener {
                if (!loadingStatus) {
                    errorClickListener.onMovieErrorStateClicked()
                }
            }
        }
    }

    interface OnLoadingAdapterClickListener {
        fun onMovieErrorStateClicked()
    }
}