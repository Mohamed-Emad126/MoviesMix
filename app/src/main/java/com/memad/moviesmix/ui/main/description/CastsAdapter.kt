package com.memad.moviesmix.ui.main.description

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.ItemCastBinding
import com.memad.moviesmix.models.Cast
import com.memad.moviesmix.utils.Constants
import javax.inject.Inject

class CastsAdapter @Inject constructor() :
    ListAdapter<Cast, CastsAdapter.CastViewHolder>(MoviesCastDiffCallBack) {

    override fun submitList(list: MutableList<Cast>?) {
        val oldSize = list?.size ?: 0
        super.submitList(list)
        notifyItemRangeInserted(oldSize, 20)
    }


    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        return CastViewHolder(
            ItemCastBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val cast = getItem(position)
        holder.itemBinding.posterDialogImage.load(
            Constants.POSTER_BASE_URL +
                    cast.profile_path
        ) {
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_broken)
            allowHardware(false)
        }
        holder.itemBinding.castName.text = cast.name


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
    inner class CastViewHolder(val itemBinding: ItemCastBinding) :
        RecyclerView.ViewHolder(itemBinding.root)
}

object MoviesCastDiffCallBack : DiffUtil.ItemCallback<Cast>() {
    override fun areItemsTheSame(oldItem: Cast, newItem: Cast): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(oldItem: Cast, newItem: Cast): Boolean {
        return newItem.id == oldItem.id
    }
}