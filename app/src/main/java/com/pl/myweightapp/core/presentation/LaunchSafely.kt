package com.pl.myweightapp.core.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.core.util.exceptionToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

inline fun <T> T.launchSafely(
    crossinline onError: (Exception) -> Unit = {},
    crossinline block: suspend CoroutineScope.() -> Unit
) where T : ViewModel, T : UiEventOwner {
    viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val tag = this@launchSafely::class.java.simpleName
            Log.e(tag, e.message, e)
            onError(e)
            sendError(
                R.string.error_msg_prefix,
                exceptionToString(e)
            )
        }
    }
}