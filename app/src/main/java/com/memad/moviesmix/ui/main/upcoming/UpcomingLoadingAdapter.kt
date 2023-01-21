package com.memad.moviesmix.ui.main.upcoming

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.UpcomingItemBinding
import com.memad.moviesmix.ui.main.trending.LoadingAdapter
import javax.inject.Inject

class UpcomingLoadingAdapter @Inject constructor() :
    RecyclerView.Adapter<UpcomingLoadingAdapter.LoadingViewHolder>() {
    lateinit var errorClickListener: LoadingAdapter.OnLoadingAdapterClickListener

    private var loadingStatus = true

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder {
        return LoadingViewHolder(
            UpcomingItemBinding.inflate(
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
            holder.itemBinding.upcomingLoadingVeilLayout.veil()
        } else {
            holder.itemBinding.upcomingLoadingVeilLayout.unVeil()
            holder.itemBinding.movieImage.load(R.drawable.ic_robot_broken) {
                crossfade(true)
                allowHardware(false)
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    /////////////////////////////////////////////////////////////////
/////////////////////////ViewHolder//////////////////////////////
/////////////////////////////////////////////////////////////////
    inner class LoadingViewHolder(val itemBinding: UpcomingItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.movieImage.setOnClickListener {
                if (!loadingStatus) {
                    errorClickListener.onMovieErrorStateClicked()
                }
            }
        }
    }
}