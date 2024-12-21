package com.samyak2403.iptvmine.screens

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.samyak2403.iptvmine.Ads.MyInterstitialAds
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.adapter.ChannelsAdapter
import com.samyak2403.iptvmine.model.Channel
import com.samyak2403.iptvmine.provider.ChannelsProvider

class HomeFragment : Fragment() {

    private lateinit var channelsProvider: ChannelsProvider
    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChannelsAdapter

    private var debounceHandler: Handler? = null
    private var isSearchVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        channelsProvider = ViewModelProvider(this).get(ChannelsProvider::class.java)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchIcon = view.findViewById(R.id.search_icon)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Use requireActivity() to get the Activity context
        val interstitialAds = MyInterstitialAds(requireActivity())

        adapter = ChannelsAdapter(emptyList(), interstitialAds) { channel: Channel ->
            PlayerActivity.start(requireContext(), channel)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupObservers()
        fetchData()

        // Set click listener to toggle the search bar visibility
        searchIcon.setOnClickListener {
            toggleSearchBar()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                debounceHandler?.removeCallbacksAndMessages(null)
                debounceHandler = Handler(Looper.getMainLooper())
                debounceHandler?.postDelayed({
                    filterChannels(s.toString())
                }, 500)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun setupObservers() {
        channelsProvider.channels.observe(viewLifecycleOwner, Observer { data ->
            adapter.updateChannels(data)
        })

        channelsProvider.filteredChannels.observe(viewLifecycleOwner, Observer { data ->
            adapter.updateChannels(data)
        })
    }

    private fun fetchData() {
        progressBar.visibility = View.VISIBLE
        channelsProvider.fetchM3UFile()
        progressBar.visibility = View.GONE
    }

    private fun filterChannels(query: String) {
        channelsProvider.filterChannels(query)
    }

    private fun toggleSearchBar() {
        if (isSearchVisible) {
            searchEditText.visibility = View.GONE
            isSearchVisible = false
        } else {
            searchEditText.visibility = View.VISIBLE
            isSearchVisible = true
        }
    }
}




///*
// * Created by Samyak kamble on 8/14/24, 11:33 AM
// *  Copyright (c) 2024 . All rights reserved.
// *  Last modified 8/14/24, 11:33 AM
// */
//
//package com.samyak2403.iptv.screens
//
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.samyak2403.iptv.R
//import com.samyak2403.iptv.model.Channel
//import com.samyak2403.iptv.provider.ChannelsProvider
//
//import kotlin.concurrent.timer
//
//class HomeFragment : Fragment() {
//
//    private lateinit var channelsProvider: ChannelsProvider
//    private lateinit var searchEditText: EditText
//    private lateinit var progressBar: ProgressBar
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: ChannelsAdapter
//
//    private var channels: List<Channel> = emptyList()
//    private var filteredChannels: List<Channel> = emptyList()
//    private var debounceHandler: Handler? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_home, container, false)
//
//        channelsProvider = ViewModelProvider(this).get(ChannelsProvider::class.java)
//        searchEditText = view.findViewById(R.id.searchEditText)
//        progressBar = view.findViewById(R.id.progressBar)
//        recyclerView = view.findViewById(R.id.recyclerView)
//
//        adapter = ChannelsAdapter(filteredChannels) { channel ->
//            PlayerActivity.start(requireContext(), channel)
//        }
//
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.adapter = adapter
//
//        fetchData()
//
//        searchEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                debounceHandler?.removeCallbacksAndMessages(null)
//                debounceHandler = Handler(Looper.getMainLooper())
//                debounceHandler?.postDelayed({
//                    filterChannels(s.toString())
//                }, 500)
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//        return view
//    }
//
//    private fun fetchData() {
//        progressBar.visibility = View.VISIBLE
//        channelsProvider.fetchM3UFile().observe(viewLifecycleOwner) { data ->
//            channels = data
//            filteredChannels = data
//            adapter.updateChannels(filteredChannels)
//            progressBar.visibility = View.GONE
//        }
//    }
//
//    private fun filterChannels(query: String) {
//        filteredChannels = channelsProvider.filterChannels(query)
//        adapter.updateChannels(filteredChannels)
//    }
//}
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
//            // Load image using Glide or similar library
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
