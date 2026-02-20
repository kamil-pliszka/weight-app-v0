package com.pl.myweightapp.core.presentation
/*
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.core.util.exceptionToString
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseViewModel : ViewModel() {
    companion object {
        private const val TAG = "BaseVM"
    }

    //output events
    //private val _events = Channel<UiEvent>(Channel.BUFFERED)
    //val events = _events.receiveAsFlow()
    //podobno te rozwiazanie jest lepsze
    private val _events = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    suspend fun sendEvent(event: UiEvent) {
        //viewModelScope.launch {
            _events.emit(event)//send(event)
        //}
    }

    suspend fun errorHandler(e: Throwable) {
        //Log.e(TAG, e.message, e)
        //sendEvent(UiEvent.Error((getApplication() as Context).getString(R.string.error_msg_prefix) + e.message))
        errorHandler(e, R.string.error_msg_prefix, exceptionToString(e))
    }

    suspend fun errorHandler(e: Throwable, @StringRes resId: Int, vararg args: Any) {
        Log.e(TAG, e.message, e)
        sendEvent(UiEvent.Error(resId, args.toList()))
    }

    suspend fun sendInfo(@StringRes resId: Int, vararg args: Any) {
        sendEvent(UiEvent.Info(resId, args.toList()))
    }


    inline fun launchSafely(
        crossinline block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorHandler(e)
            }
        }
    }

}
*/