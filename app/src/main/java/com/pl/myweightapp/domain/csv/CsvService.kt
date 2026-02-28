package com.pl.myweightapp.domain.csv

import com.pl.myweightapp.domain.WeightMeasure
import java.io.InputStream
import java.io.OutputStream

interface CsvService {
    suspend fun exportWeightCsv(
        history: List<WeightMeasure>?,
        output: OutputStream,
        onProgressChange: (Float) -> Unit
    ): Int

    /**
     * @throws CsvParseException
     */
    suspend fun importWeightCsv(
        input: InputStream,
        onProgressChange: (Float) -> Unit
    ): Int
}