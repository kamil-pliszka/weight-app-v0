package com.pl.myweightapp.xxx.csv

import android.content.Context
import android.net.Uri
import com.pl.myweightapp.AppModule
import com.pl.myweightapp.persistence.WeightMeasureEntity
import com.pl.myweightapp.persistence.WeightUnit
import com.pl.myweightapp.xxx.toLocalDate

private fun WeightUnitCsv.toEntityWeightUnit(): WeightUnit {
    return when (this) {
        WeightUnitCsv.KG -> WeightUnit.KG
        WeightUnitCsv.LB -> WeightUnit.LB
    }
}

suspend fun importWeightCsv(
    context: Context,
    uri: Uri,
    onProgressChange: (Float) -> Unit
): Int {
    val entriesCsv = parseWeightCsv(context, uri)
    println("entriesCsv : ${entriesCsv.size}")
    val repo = AppModule.provideWeightMeasureRepository()
    val historyEntities = repo.findWeightMeasureHistory()
    println("history entities : ${historyEntities.size}")
    val existingEntitiesByDate = historyEntities
        .groupBy { it.date.toLocalDate() }
        .mapValues { (_, list) ->
            list.sortedBy { it.id } // rosnąco po id
        }
    println("groupped by date size : ${existingEntitiesByDate.size}")

    entriesCsv.forEachIndexed { idx, csvEntry ->
        val existingOnDate = existingEntitiesByDate[csvEntry.timestamp.toLocalDate()]
        if (existingOnDate == null) {
            println("Inserting measure on date: ${csvEntry.timestamp}, weight: ${csvEntry.value}, idx = $idx")
            repo.insertMeasure(
                csvEntry.timestamp,
                csvEntry.value,
                csvEntry.unit.toEntityWeightUnit()
            )
        } else {
            println("Update measure on date: ${csvEntry.timestamp.toLocalDate()}, weight: ${csvEntry.value}, idx = $idx")
            if (existingOnDate.size > 1) println("Matching entities: ${existingOnDate.size} !!!")
            val entityOnDate = findEntityOnDate(csvEntry, existingOnDate)
            val updatedWeightEntity = entityOnDate.copy(
                weight = csvEntry.value,
                unit = csvEntry.unit.toEntityWeightUnit()
            )
            repo.update(updatedWeightEntity)
        }
        onProgressChange((idx + 1).toFloat() / entriesCsv.size)
        //_state.update { it.copy(csvProgress = (idx + 1).toFloat() / entriesCsv.size) }
    }
    return entriesCsv.size
}

private fun findEntityOnDate(
    csvEntry: WeightEntryCsv,
    existingOnDate: List<WeightMeasureEntity>
): WeightMeasureEntity {
    assert(existingOnDate.isNotEmpty())
    if (existingOnDate.size == 1) {
        return existingOnDate.first()
    } else {
        val unitCsv = csvEntry.unit.toEntityWeightUnit()
        return existingOnDate.lastOrNull { it.date == csvEntry.timestamp }
            ?: existingOnDate.lastOrNull { it.weight == csvEntry.value && it.unit == unitCsv }
            ?: existingOnDate.lastOrNull { it.weight == csvEntry.value }
            ?: existingOnDate.last()
    }
}