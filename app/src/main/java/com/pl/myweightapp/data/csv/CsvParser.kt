package com.pl.myweightapp.data.csv

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CsvParseException(message: String) : Exception(message)

fun parseWeightCsv(
    context: Context,
    uri: Uri,
): List<WeightEntryCsv> {
    return parseWeightCsv(
        readTextFileLines(context, uri)
    )
}

fun readTextFileLines(context: Context, uri: Uri): List<String> {
    val result = mutableListOf<String>()
    context.contentResolver.openInputStream(uri)?.use { stream ->
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


