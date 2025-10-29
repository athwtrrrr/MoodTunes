package com.example.myspecial.moodtunes.data.api

import com.google.gson.annotations.SerializedName

data class DeezerSearchResponse(
    @SerializedName("data") val data: List<DeezerTrack>,
    @SerializedName("total") val total: Int
)

// New response types for chart endpoints
data class DeezerChartResponse(
    @SerializedName("tracks") val tracks: DeezerTracksData?,
    @SerializedName("albums") val albums: DeezerAlbumsData?,
    @SerializedName("artists") val artists: DeezerArtistsData?,
    @SerializedName("playlists") val playlists: DeezerPlaylistsData?
)

data class DeezerTracksData(
    @SerializedName("data") val data: List<DeezerTrack>,
    @SerializedName("total") val total: Int
)

data class DeezerAlbumsData(
    @SerializedName("data") val data: List<DeezerAlbum>,
    @SerializedName("total") val total: Int
)

data class DeezerArtistsData(
    @SerializedName("data") val data: List<DeezerArtist>,
    @SerializedName("total") val total: Int
)

data class DeezerPlaylistsData(
    @SerializedName("data") val data: List<DeezerPlaylist>,
    @SerializedName("total") val total: Int
)

data class DeezerPlaylist(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("tracklist") val tracklist: String
)

data class DeezerTrack(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("link") val link: String,
    @SerializedName("preview") val preview: String, // 30-second preview URL
    @SerializedName("artist") val artist: DeezerArtist,
    @SerializedName("album") val album: DeezerAlbum
)

data class DeezerArtist(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String
)

data class DeezerAlbum(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("cover_small") val cover_small: String,
    @SerializedName("cover_medium") val cover_medium: String,
    @SerializedName("cover_big") val cover_big: String,
    @SerializedName("cover_xl") val cover_xl: String
)