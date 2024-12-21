package com.samyak2403.iptvmine.provider

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samyak2403.iptvmine.model.Channel
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class ChannelsProvider : ViewModel() {

    private val _channels = MutableLiveData<List<Channel>>()
    val channels: LiveData<List<Channel>> get() = _channels

    private val _filteredChannels = MutableLiveData<List<Channel>>()
    val filteredChannels: LiveData<List<Channel>> get() = _filteredChannels

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val sourceUrl = "https://raw.githubusercontent.com/FunctionError/PiratesTv/main/combined_playlist.m3u"

    private var fetchJob: Job? = null

    /**
     * Fetch the M3U file from the provided URL asynchronously.
     */
    fun fetchM3UFile() {
        fetchJob?.cancel() // Cancel any ongoing fetch job.

        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val urlConnection = URL(sourceUrl).openConnection() as HttpURLConnection
                urlConnection.apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val fileText = urlConnection.inputStream.bufferedReader().use(BufferedReader::readText)
                val tempChannels = parseM3UFile(fileText)

                withContext(Dispatchers.Main) {
                    _channels.value = tempChannels
                    _error.value = null
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to fetch channels: ${e.localizedMessage}"
                }
            }
        }
    }

    /**
     * Parse M3U file content and return a list of [Channel] objects.
     */
    private fun parseM3UFile(fileText: String): List<Channel> {
        val lines = fileText.lines()
        val channelsList = mutableListOf<Channel>()

        var name: String? = null
        var logoUrl: String = getDefaultLogoUrl()
        var streamUrl: String? = null

        for (line in lines) {
            when {
                line.startsWith("#EXTINF:") -> {
                    name = extractChannelName(line)
                    logoUrl = extractLogoUrl(line) ?: getDefaultLogoUrl()
                }
                line.isNotBlank() -> {
                    streamUrl = line
                    if (!name.isNullOrEmpty() && !streamUrl.isNullOrEmpty()) {
                        channelsList.add(Channel(name, logoUrl, streamUrl))
                    }
                    name = null
                    logoUrl = getDefaultLogoUrl()
                }
            }
        }
        return channelsList
    }

    /**
     * Provide a default logo URL if one is not specified in the M3U file.
     */
    private fun getDefaultLogoUrl() = "assets/images/ic_tv.png"

    /**
     * Extract the channel name from the EXTINF line.
     */
    private fun extractChannelName(line: String): String? {
        return line.substringAfterLast(",", "").trim()
    }

    /**
     * Extract the logo URL from the EXTINF line.
     */
    private fun extractLogoUrl(line: String): String? {
        val parts = line.split("\"")
        return parts.firstOrNull { isValidUrl(it) }
    }

    /**
     * Validate whether a string is a valid URL.
     */
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    /**
     * Filter channels based on the user's query and update [_filteredChannels].
     */
    fun filterChannels(query: String) {
        val filtered = _channels.value?.filter { it.name.contains(query, ignoreCase = true) } ?: emptyList()
        _filteredChannels.value = filtered
    }

    /**
     * Cancel any ongoing fetch when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()
    }
}



//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.samyak2403.iptvmine.model.Channel
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.net.HttpURLConnection
//import java.net.URL
//
//
//
//class ChannelsProvider : ViewModel() {
//
//    private val _channels = MutableLiveData<List<Channel>>()
//    val channels: LiveData<List<Channel>> = _channels
//
//    private val _filteredChannels = MutableLiveData<List<Channel>>()
//    val filteredChannels: LiveData<List<Channel>> = _filteredChannels
//
////    private val sourceUrl = "https://raw.githubusercontent.com/aniketda/iptv2050/main/iptv"
//    private val sourceUrl = "https://raw.githubusercontent.com/FunctionError/PiratesTv/main/combined_playlist.m3u'"
//
//    // Fetch the M3U file from the provided URL
//    fun fetchM3UFile() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val url = URL(sourceUrl)
//            val urlConnection = url.openConnection() as HttpURLConnection
//            try {
//                val fileText = urlConnection.inputStream.bufferedReader().readText()
//                val lines = fileText.split("\n")
//
//                val tempChannels = mutableListOf<Channel>()
//
//                var name: String? = null
//                var logoUrl: String? = null
//                var streamUrl: String? = null
//
//                for (line in lines) {
//                    when {
//                        line.startsWith("#EXTINF:") -> {
//                            val parts = line.split(",")
//                            name = parts.getOrNull(1)
//                            val logoParts = parts[0].split("\"")
//                            logoUrl = if (logoParts.size > 3) {
//                                logoParts[3]
//                            } else {
//                                "https://fastly.picsum.photos/id/125/536/354.jpg?hmac=EYT3s6VXrAoggrr4fXsOIIcQ3Grc13fCmXkqcE2FusY"
//                            }
//                        }
//                        line.isNotEmpty() -> {
//                            streamUrl = line
//                            if (!name.isNullOrEmpty()) {
//                                tempChannels.add(
//                                    Channel(
//                                        name = name,
//                                        logoUrl = logoUrl ?: "https://fastly.picsum.photos/id/928/200/200.jpg?hmac=5MQxbf-ANcu87ZaOn5sOEObpZ9PpJfrOImdC7yOkBlg",
//                                        streamUrl = streamUrl
//                                    )
//                                )
//                            }
//                            // Reset variables for the next channel
//                            name = null
//                            logoUrl = null
//                            streamUrl = null
//                        }
//                    }
//                }
//
//                // Update LiveData on the main thread
//                withContext(Dispatchers.Main) {
//                    _channels.value = tempChannels
//                }
//            } finally {
//                urlConnection.disconnect()
//            }
//        }
//    }
//
//    // Filter channels based on the search query
//    fun filterChannels(query: String) {
//        val filtered = _channels.value?.filter {
//            it.name.contains(query, ignoreCase = true)
//        } ?: emptyList()
//        _filteredChannels.value = filtered
//    }
//}
