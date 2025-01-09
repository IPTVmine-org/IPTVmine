///*
// * Created by Samyak kamble on 8/14/24, 11:33 AM
// *  Copyright (c) 2024 . All rights reserved.
// *  Last modified 8/14/24, 11:33 AM
// */

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

        adapter = ChannelsAdapter(emptyList()) { channel: Channel ->
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