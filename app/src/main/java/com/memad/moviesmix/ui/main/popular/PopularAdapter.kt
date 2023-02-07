package com.memad.moviesmix.ui.main.popular

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.MoviePopularItemBinding
import com.memad.moviesmix.models.Movie
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.DoubleClickListener
import javax.inject.Inject

class PopularAdapter @Inject constructor() :
    ListAdapter<MovieEntity, PopularAdapter.PopularViewHolder>(MoviesDiffCallBack) {

    lateinit var popularMovieClickListener: OnMoviesClickListener

    override fun submitList(list: MutableList<MovieEntity>?) {
        val oldSize = list?.size ?: 0
        super.submitList(list)
        notifyItemRangeInserted(oldSize, 20)
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        return PopularViewHolder(
            MoviePopularItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.itemBinding.posterImage.transitionName = getItem(position).movieId.toString()
        holder.itemBinding.posterImage
            .load(
                Constants.POSTER_BASE_URL +
                        getItem(position).movie?.poster_path
            ) {
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
                allowHardware(false)
            }
        holder.itemBinding.movieRate.text =
            getItem(position).movie?.vote_average.toString()
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).movieId?.toLong()!!
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////ViewHolder//////////////////////////////
    /////////////////////////////////////////////////////////////////
    inner class PopularViewHolder(val itemBinding: MoviePopularItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.movieCard.setOnClickListener(object : DoubleClickListener() {
                override fun onDoubleClick() {
                    popularMovieClickListener.onMovieDoubleClicked(
                        bindingAdapterPosition,
                        itemBinding.posterImage
                    )
                }

                override fun onSingleClick() {
                    popularMovieClickListener.onMovieClicked(
                        bindingAdapterPosition,
                        itemBinding.posterImage
                    )
                }
            })
            itemBinding.movieCard.setOnLongClickListener {
                popularMovieClickListener.onMovieHoldDown(bindingAdapterPosition)
                true
            }
        }

    }

    /////////////////////////////////////////////////////////////////
    ///////////////////ClickListenerInterface////////////////////////
    /////////////////////////////////////////////////////////////////
    interface OnMoviesClickListener {
        fun onMovieClicked(position: Int, imageView: ShapeableImageView)
        fun onMovieDoubleClicked(position: Int, imageView: ImageView?)
        fun onMovieHoldDown(position: Int)
    }
}

object MoviesDiffCallBack : DiffUtil.ItemCallback<MovieEntity>() {
    override fun areItemsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return newItem.movie == oldItem.movie
    }

    override fun areContentsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return newItem.movie?.id == oldItem.movie?.id
    }
}