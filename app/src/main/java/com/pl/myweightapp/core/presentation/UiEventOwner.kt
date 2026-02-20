package com.pl.myweightapp.core.presentation

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface UiEventOwner {
    val events: SharedFlow<UiEvent>
    suspend fun sendEvent(event: UiEvent)
}

class DefaultUiEventOwner : UiEventOwner {

    private val _events = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1
    )
    override val events = _events.asSharedFlow()

    override suspend fun sendEvent(event: UiEvent) {
        _events.emit(event)
    }
}


suspend fun UiEventOwner.sendError(
    @StringRes resId: Int,
    vararg args: Any
) {
    sendEvent(UiEvent.Error(resId, args.toList()))
}

suspend fun UiEventOwner.sendInfo(
    @StringRes resId: Int,
    vararg args: Any
) {
    sendEvent(UiEvent.Info(resId, args.toList()))
}

