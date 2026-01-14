data class Track(
    val id: Long,
    val title: String,
    val artist: String?,
    val duration: Long,
    val uri: String,
    val albumId: Long?,
    val albumArtUri: String?
)