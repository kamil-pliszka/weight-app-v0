package com.pl.myweightapp.feature.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

inline fun <T> T.launchWithErrorHandling(
    showErrorMessage: Boolean = true,
    crossinline onError: (Throwable) -> Unit = {},
    crossinline block: suspend CoroutineScope.() -> Unit
) : Job where T : ViewModel, T : UiEventOwner =
    viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val tag = this@launchWithErrorHandling::class.java.simpleName
            Log.e(tag, e.message, e)
            onError(e)
            if (showErrorMessage) {
                sendException(e)
            }
        }
    }
