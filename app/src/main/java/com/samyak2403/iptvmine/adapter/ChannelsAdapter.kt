/*
 * Created by Samyak Kamble on 8/14/24, 12:18 PM
 *  Copyright (c) 2024. All rights reserved.
 *  Last modified 8/14/24, 12:18 PM
 */

package com.samyak2403.iptvmine.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.samyak2403.iptvmine.Ads.MyInterstitialAds
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.model.Channel


class ChannelsAdapter(
    private var channels: List<Channel>,
    private val interstitialAds: MyInterstitialAds,
    private val onChannelClicked: (com.samyak2403.iptvmine.model.Channel) -> Unit
) : RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(channels[position])
    }

    override fun getItemCount(): Int = channels.size

    fun updateChannels(newChannels: List<Channel>) {
        val diffResult = DiffUtil.calculateDiff(ChannelDiffCallback(channels, newChannels))
        channels = newChannels
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val logoImageView: ImageView = itemView.findViewById(R.id.logoImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bind(channel: Channel) {
            nameTextView.text = channel.name
            Glide.with(itemView.context)
                .load(channel.logoUrl)
                .placeholder(R.drawable.ic_tv)
                .into(logoImageView)

//            itemView.setOnClickListener {
//                onChannelClicked(channel)
//            }

            itemView.setOnClickListener {
                interstitialAds.showInterstitialAds(placementId = itemView.context.getString(R.string.interstitial_android)) {
                    onChannelClicked(channel)
                }
            }
        }
    }

    private class ChannelDiffCallback(
        private val oldList: List<Channel>,
        private val newList: List<Channel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Assuming the name is unique for each channel
            return oldList[oldItemPosition].name == newList[newItemPosition].name
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Check if contents are the same, including the logoUrl and streamUrl
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}


///*
// * Created by Samyak kamble on 8/14/24, 12:18 PM
// *  Copyright (c) 2024 . All rights reserved.
// *  Last modified 8/14/24, 12:18 PM
// */
//
//package com.samyak2403.iptv.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.samyak2403.iptv.R
//import com.samyak2403.iptv.provider.Channel
//
//class ChannelsAdapter(
//    private var channels: List<Channel>,
//    private val onChannelClicked: (Channel) -> Unit
//) : RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_channel, parent, false)
//        return ChannelViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
//        holder.bind(channels[position])
//    }
//
//    override fun getItemCount(): Int = channels.size
//
//    fun updateChannels(newChannels: List<Channel>) {
//        channels = newChannels
//        notifyDataSetChanged()
//    }
//
//    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val logoImageView: ImageView = itemView.findViewById(R.id.logoImageView)
//        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
//
//        fun bind(channel: Channel) {
//            nameTextView.text = channel.name
//            Glide.with(itemView.context)
//                .load(channel.logoUrl)
//                .placeholder(R.drawable.tv)
//                .into(logoImageView)
//
//            itemView.setOnClickListener {
//                onChannelClicked(channel)
//            }
//        }
//    }
//}
