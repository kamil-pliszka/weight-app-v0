package com.pl.myweightapp.core.presentation

import com.pl.myweightapp.core.domain.util.NetworkError

sealed interface UiEvent {
    data class NetError(val error: NetworkError) : UiEvent
    data class IOError(val error: String) : UiEvent
    data class Succes(val msg: String) : UiEvent
    data class Info(val msg: String) : UiEvent
}