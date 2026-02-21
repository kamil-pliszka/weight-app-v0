package com.pl.myweightapp.feature.addedit

sealed interface AddEditMeasureEvent {
    data object CloseDialog : AddEditMeasureEvent
}