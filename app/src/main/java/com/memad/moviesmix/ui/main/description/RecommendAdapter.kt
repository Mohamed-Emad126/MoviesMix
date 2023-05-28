package com.memad.moviesmix.ui.main.description

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.memad.moviesmix.databinding.ItemSimilarMovieBinding
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.utils.Constants
import javax.inject.Inject

class RecommendAdapter @Inject constructor() :
    ListAdapter<MoviesResponse.Result, RecommendAdapter.RecommendViewHolder>(
        MoviesSimilarDiffCallBack
    ) {
    var similarMovieClickListener: OnMovieClickListener? = null


    override fun submitList(list: MutableList<MoviesResponse.Result>?) {
        val oldSize = list?.size ?: 0
        super.submitList(list)
        notifyItemRangeInserted(oldSize, 20)
    }


    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        return RecommendViewHolder(
            ItemSimilarMovieBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        val movie = getItem(position)
        ViewCompat.setTransitionName(holder.itemBinding.posterDialogImage, position.toString())
        holder.itemBinding.posterDialogImage.load(
            Constants.POSTER_BASE_URL +
                    movie.poster_path
        ) {
            crossfade(true)
            placeholder(com.memad.moviesmix.R.drawable.start_img_min_blur)
            error(com.memad.moviesmix.R.drawable.start_img_min_broken)
            allowHardware(false)
        }
        holder.itemBinding.similarName.text = movie.title


    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////ViewHolder//////////////////////////////
    /////////////////////////////////////////////////////////////////
    inner class RecommendViewHolder(val itemBinding: ItemSimilarMovieBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.similarMovieItem.setOnClickListener {
                similarMovieClickListener?.onSimilarMovieClicked(
                    bindingAdapterPosition,
                    itemBinding.posterDialogImage
                )
            }
        }

    }

    /////////////////////////////////////////////////////////////////
    ///////////////////ClickListenerInterface////////////////////////
    /////////////////////////////////////////////////////////////////
    interface OnMovieClickListener {
        /**
         * Handle when the whole layout of the item clicked
         *
         * @param position he position of the item
         */
        fun onSimilarMovieClicked(position: Int, posterImage: ShapeableImageView)
    }
}

object MoviesSimilarDiffCallBack : DiffUtil.ItemCallback<MoviesResponse.Result>() {
    override fun areItemsTheSame(
        oldItem: MoviesResponse.Result,
        newItem: MoviesResponse.Result
    ): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(
        oldItem: MoviesResponse.Result,
        newItem: MoviesResponse.Result
    ): Boolean {
        return newItem.id == oldItem.id
    }
}