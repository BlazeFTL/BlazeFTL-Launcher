package com.example.model

data class NowPlayingTrack(
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val packageName: String = ""
)
