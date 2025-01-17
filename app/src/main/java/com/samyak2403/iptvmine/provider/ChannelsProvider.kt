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
                val url = URL(sourceUrl)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                }

                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val fileText = urlConnection.inputStream.bufferedReader().use(BufferedReader::readText)
                    val tempChannels = parseM3UFile(fileText)

                    withContext(Dispatchers.Main) {
                        _channels.value = tempChannels
                        _error.value = null
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _error.value = "Error: HTTP ${urlConnection.responseCode}"
                    }
                }
                urlConnection.disconnect()
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
                line.isNotBlank() && isValidStreamUrl(line) -> {
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
     * Validate whether a string is a valid stream URL.
     * Specifically supports M3U8 links and other common video stream formats.
     */
    private fun isValidStreamUrl(url: String): Boolean {
        return isValidUrl(url) && (url.endsWith(".m3u8") || url.endsWith(".mp4") || url.endsWith(".avi") || url.endsWith(".mkv"))
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
