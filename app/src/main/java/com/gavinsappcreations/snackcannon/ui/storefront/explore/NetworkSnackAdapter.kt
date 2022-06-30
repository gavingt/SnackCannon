package com.gavinsappcreations.snackcannon.ui.storefront.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gavinsappcreations.snackcannon.R
import com.gavinsappcreations.snackcannon.data.network.NetworkSnack
import com.gavinsappcreations.snackcannon.databinding.SnackSearchListItemBinding


/**
 * Adapter for the list of repositories.
 */
class NetworkSnackAdapter : PagingDataAdapter<NetworkSnack, NetworkSnackViewHolder>(NetworkSnackViewHolder.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkSnackViewHolder {
        return NetworkSnackViewHolder(
            SnackSearchListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: NetworkSnackViewHolder, position: Int) {
        val networkSnack = getItem(position)
        networkSnack?.let {
            holder.bind(networkSnack)
        }
    }
}


class NetworkSnackViewHolder(
    private var binding: SnackSearchListItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    init {
/*        view.setOnClickListener {
            repo?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }*/
    }

    fun bind(networkSnack: NetworkSnack?) {
        networkSnack?.let {
            binding.snack = networkSnack
            binding.productImageView.bindImage(it.image_small_url)
            binding.executePendingBindings()
        }
    }


    private fun ImageView.bindImage(imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(context)
                .load(if (imgUrl.isNullOrEmpty()) R.drawable.broken_image else imgUri)
                .fitCenter()
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.broken_image)
                )
                .into(this)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NetworkSnack>() {
        override fun areItemsTheSame(oldItem: NetworkSnack, newItem: NetworkSnack): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: NetworkSnack, newItem: NetworkSnack): Boolean {
            // TODO: I may need to add more conditions here, especially if we add properties to our object like "isInCart".
            return oldItem.code == newItem.code
                    && oldItem.product_name == newItem.product_name

        }
    }

}
