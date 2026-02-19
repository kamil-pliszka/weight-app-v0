package com.pl.myweightapp.data.csv

import android.content.Context
import android.net.Uri
import android.util.Log
import com.pl.myweightapp.app.di.AppModule
import com.pl.myweightapp.core.domain.WeightUnit
import com.pl.myweightapp.core.util.toLocalDate
import com.pl.myweightapp.data.local.WeightMeasureEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun WeightUnitCsv.toEntityWeightUnit(): WeightUnit {
    return when (this) {
        WeightUnitCsv.KG -> WeightUnit.KG
        WeightUnitCsv.LB -> WeightUnit.LB
    }
}

private const val TAG = "CsvImport"
suspend fun importWeightCsv(
    context: Context,
    uri: Uri,
    onProgressChange: (Float) -> Unit
): Int = withContext(Dispatchers.IO) {
    val entriesCsv = parseWeightCsv(context, uri)
    Log.d(TAG,"entriesCsv : ${entriesCsv.size}")
    val repo = AppModule.provideWeightMeasureRepository()
    val historyEntities = repo.findWeightMeasureHistory()
    Log.d(TAG,"history entities : ${historyEntities.size}")
    val existingEntitiesByDate = historyEntities
        .groupBy { it.date.toLocalDate() }
        .mapValues { (_, list) ->
            list.sortedBy { it.id } // rosnąco po id
        }
    Log.d(TAG,"groupped by date size : ${existingEntitiesByDate.size}")
    val toInsert = mutableListOf<WeightMeasureEntity>()
    val toUpdate = mutableListOf<WeightMeasureEntity>()

    entriesCsv.forEachIndexed { idx, csvEntry ->
        val existingOnDate = existingEntitiesByDate[csvEntry.timestamp.toLocalDate()]
        if (existingOnDate == null) {
            Log.d(TAG,"Insert measure on date: ${csvEntry.timestamp}, weight: ${csvEntry.value}, idx = $idx")
            toInsert.add(
                WeightMeasureEntity(
                    date = csvEntry.timestamp,
                    weight = csvEntry.value,
                    unit = csvEntry.unit.toEntityWeightUnit()
                )
            )
        } else {
            Log.d(TAG,"Update measure on date: ${csvEntry.timestamp.toLocalDate()}, weight: ${csvEntry.value}, idx = $idx")
            if (existingOnDate.size > 1) Log.d(TAG,"Matching entities: ${existingOnDate.size} !!!")
            val entityOnDate = findEntityOnDate(csvEntry, existingOnDate)
            val updatedWeightEntity = entityOnDate.copy(
                weight = csvEntry.value,
                unit = csvEntry.unit.toEntityWeightUnit()
            )
            toUpdate.add(updatedWeightEntity)
        }
        onProgressChange((idx + 1).toFloat() / entriesCsv.size)
        //_state.update { it.copy(csvProgress = (idx + 1).toFloat() / entriesCsv.size) }
    }
    repo.import(AppModule.provideMyDatabase(), toInsert, toUpdate)
    Log.d(TAG,"Imported")
    entriesCsv.size //return
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