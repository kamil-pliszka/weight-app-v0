package com.pl.myweightapp.core.presentation

import android.content.Context
import com.pl.myweightapp.core.domain.util.NetworkError

sealed interface UiEvent {
    data class NetError(val error: NetworkError) : UiEvent

    //data class IOError(val error: String) : UiEvent
    data class Error(
        val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiEvent

    data class Message(
        val message: String
    ) : UiEvent

    data class Info(
        val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiEvent
}

fun UiEvent.Error.asString(context: Context): String {
    return context.getString(
        resId,
        *args.toTypedArray()
    )
}

fun UiEvent.Info.asString(context: Context): String {
    return context.getString(
        resId,
        *args.toTypedArray()
    )
}