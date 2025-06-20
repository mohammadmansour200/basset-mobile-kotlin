package com.basset.operations.domain.cut_operation

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow

interface MediaPlaybackManager {
    fun loadMedia(uri: Uri)

    fun releaseMedia()

    val events: MutableSharedFlow<Event>

    sealed interface Event {
        data class PositionChanged(val position: Long) : Event
    }
}