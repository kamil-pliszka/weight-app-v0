package com.pl.myweightapp.data.csv

import android.util.Log
import com.pl.myweightapp.core.util.toLocalDate
import com.pl.myweightapp.domain.WeightMeasure
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.domain.csv.CsvParseException
import com.pl.myweightapp.domain.csv.CsvService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject


class CsvServiceImpl @Inject constructor(
    private val repo: WeightMeasureRepository,
    ) : CsvService {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    //EXPORT

    override suspend fun exportWeightCsv(
        history: List<WeightMeasure>?,
        output: OutputStream,
        onProgressChange: (Float) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val historyToExport = history ?: repo.findWeightMeasureHistory()
        Log.d(TAG,"history entities : ${historyToExport.size}")
        output.use { output ->
            val writer = output.bufferedWriter()
            writer.write("#Weight Date,Weight Measurement,Weight Unit\n")
            historyToExport.forEachIndexed { idx, measure ->
                val line =
                    "${measure.date},${measure.weight.toPlainString()},${measure.unit.name.lowercase()}"
                writer.write(line)
                writer.write("\n")
                onProgressChange((idx + 1).toFloat() / historyToExport.size)
            }
            writer.flush()
        }
        historyToExport.size //return
    }

    //PARSER
    fun parseWeightCsv( input: InputStream ): List<WeightEntryCsv> {
        return parseWeightCsv(
            readTextFileLines(input)
        )
    }

    fun readTextFileLines(input: InputStream): List<String> {
        val result = mutableListOf<String>()
        input.use { stream ->
            BufferedReader(InputStreamReader(stream)).forEachLine { line ->
                result.add(line)
            }
        }
        return result
    }

    private fun parseWeightCsv(lines: List<String>): List<WeightEntryCsv> {
        if (lines.isEmpty()) return emptyList()
        val (dataLines, startIndex) =
            if (looksLikeHeader(lines.first())) {
                lines.drop(1) to 2
            } else {
                lines to 1
            }
        return dataLines.mapIndexed { index, line ->
            parseLine(line, index + startIndex)
        }
    }

    private fun looksLikeHeader(line: String): Boolean {
        return line.startsWith('#') || (line.contains(
            "weight",
            ignoreCase = true
        ) && line.contains("date", ignoreCase = true))
    }

    private fun parseLine(
        line: String,
        lineNumber: Int
    ): WeightEntryCsv {
        val parts = if (line.contains(';'))
            line.split(';')
        else
            line.split(",")

        if (parts.size !in 2..3) {
            throw CsvParseException("Invalid column count at line $lineNumber")
        }

        val timestamp = parseTimestamp(parts[0], lineNumber)
        val weight = parseBigDecimal(parts[1], lineNumber)
        val unit = parseUnit(parts.getOrNull(2), lineNumber)

        if (weight <= BigDecimal.ZERO) {
            throw CsvParseException("Weight must be positive at line $lineNumber")
        }

        return WeightEntryCsv(
            timestamp = timestamp,
            value = weight,
            unit = unit,
        )
    }

    private fun parseTimestamp(
        raw: String,
        line: Int
    ): Instant {
        return try {
            Instant.parse(raw.trim())
        } catch (_: Exception) {
            try {
                LocalDate.parse(raw.trim())
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
            } catch (_: Exception) {
                throw CsvParseException("Invalid date/time at line $line: $raw")
            }
        }
    }

    private fun parseBigDecimal(
        raw: String,
        line: Int
    ): BigDecimal {
        return try {
            BigDecimal(raw.trim().replace(",", "."))
        } catch (_: Exception) {
            throw CsvParseException("Invalid weight at line $line: $raw")
        }
    }

    private fun parseUnit(
        raw: String?,
        line: Int
    ): WeightUnitCsv {
        if (raw.isNullOrEmpty()) {
            return WeightUnitCsv.KG
        } else return try {
            WeightUnitCsv.valueOf(raw.trim().uppercase())
        } catch (_: Exception) {
            throw CsvParseException("Invalid unit at line $line: $raw")
        }
    }


    //IMPORT
    override suspend fun importWeightCsv(
        input: InputStream,
        onProgressChange: (Float) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val entriesCsv = parseWeightCsv(input)
        Log.d(TAG,"entriesCsv : ${entriesCsv.size}")
        val historyEntities = repo.findWeightMeasureHistory()
        Log.d(TAG,"history entities : ${historyEntities.size}")
        val existingEntitiesByDate = historyEntities
            .groupBy { it.date.toLocalDate() }
            .mapValues { (_, list) ->
                list.sortedBy { it.id } // rosnąco po id
            }
        Log.d(TAG,"groupped by date size : ${existingEntitiesByDate.size}")
        val toInsert = mutableListOf<WeightMeasure>()
        val toUpdate = mutableListOf<WeightMeasure>()

        entriesCsv.forEachIndexed { idx, csvEntry ->
            val existingOnDate = existingEntitiesByDate[csvEntry.timestamp.toLocalDate()]
            if (existingOnDate == null) {
                Log.d(TAG,"Insert measure on date: ${csvEntry.timestamp}, weight: ${csvEntry.value}, idx = $idx")
                toInsert.add(
                    WeightMeasure(
                        date = csvEntry.timestamp,
                        weight = csvEntry.value,
                        unit = csvEntry.unit.toDomainWeightUnit()
                    )
                )
            } else {
                Log.d(TAG,"Update measure on date: ${csvEntry.timestamp.toLocalDate()}, weight: ${csvEntry.value}, idx = $idx")
                if (existingOnDate.size > 1) Log.d(TAG,"Matching entities: ${existingOnDate.size} !!!")
                val measureOnDate = findMeasureOnDate(csvEntry, existingOnDate)
                val updatedWeightMeasure = measureOnDate.copy(
                    weight = csvEntry.value,
                    unit = csvEntry.unit.toDomainWeightUnit()
                )
                toUpdate.add(updatedWeightMeasure)
            }
            onProgressChange((idx + 1).toFloat() / entriesCsv.size)
            //_state.update { it.copy(csvProgress = (idx + 1).toFloat() / entriesCsv.size) }
        }
        repo.import(toInsert, toUpdate)
        Log.d(TAG,"Imported")
        entriesCsv.size //return
    }

    private fun findMeasureOnDate(
        csvEntry: WeightEntryCsv,
        existingOnDate: List<WeightMeasure>
    ): WeightMeasure {
        assert(existingOnDate.isNotEmpty())
        if (existingOnDate.size == 1) {
            return existingOnDate.first()
        } else {
            val unitCsv = csvEntry.unit.toDomainWeightUnit()
            return existingOnDate.lastOrNull { it.date == csvEntry.timestamp }
                ?: existingOnDate.lastOrNull { it.weight == csvEntry.value && it.unit == unitCsv }
                ?: existingOnDate.lastOrNull { it.weight == csvEntry.value }
                ?: existingOnDate.last()
        }
    }
}