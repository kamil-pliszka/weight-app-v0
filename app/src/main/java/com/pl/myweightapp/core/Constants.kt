package com.pl.myweightapp.core

import java.util.Locale

object Constants {
    /**
     * Określa czy ma używać wbudowanej kontrolki do prezentowania wykresów - true
     * czy też generować wykresy do obrazka i prezentować jako image - true
     */
    const val USE_EMBEDDED_CHART: Boolean = true

    //const val BASE_URL = "https://api.coinpaprika.com/"
    const val PROFILE_PHOTO_FILENAME = "profile_photo.jpg"
    const val WEIGHT_CHART_FILENAME = "weight_chart.png"
    const val DEFAULT_DISPLAY_PERIOD = "P3M"
}