package com.pl.myweightapp.xxx

import android.content.Context
import android.widget.Toast
import com.pl.myweightapp.core.domain.util.NetworkError
import com.pl.myweightapp.core.presentation.util.toString

sealed interface ModelEvent {
    data class NetError(val error: NetworkError) : ModelEvent
    data class IOError(val error: String) : ModelEvent
    data class Succes(val msg: String) : ModelEvent
    data class Info(val msg: String) : ModelEvent
}

fun displayEvent(event: ModelEvent, context: Context) {
    when(event) {
        is ModelEvent.IOError -> showErrorToast(context, event.error)
        is ModelEvent.NetError -> showErrorToast(context, event.error.toString(context))
        is ModelEvent.Succes -> showToast(context, event.msg)
        is ModelEvent.Info -> showToast(context, event.msg)
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_LONG
    ).show()
}

