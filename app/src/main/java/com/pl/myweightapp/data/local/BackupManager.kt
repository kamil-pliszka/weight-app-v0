package com.pl.myweightapp.data.local

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.pl.myweightapp.R
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.data.csv.exportWeightCsv
import com.pl.myweightapp.data.csv.importWeightCsv
import com.pl.myweightapp.domain.BackupService
import com.pl.myweightapp.domain.UserProfile
import com.pl.myweightapp.domain.UserProfileRepository
import com.pl.myweightapp.domain.WeightMeasure
import com.pl.myweightapp.domain.WeightMeasureRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val weightRepository: WeightMeasureRepository,
    private val userProfileRepository: UserProfileRepository,
    scope: CoroutineScope,
) : BackupService {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

//    init {
//        Log.d(TAG, "start")
//        repository.observeWeightMeasureHistory()
//            .debounce(1000)
//            .distinctUntilChanged()
//            .onEach { list ->
//                backup(list)
//            }
//            .launchIn(scope)
//    }

    private var backupJob: Job? = null

    init {
        Log.d(TAG, "init")
        weightRepository.observeWeightMeasureHistory()
            .distinctUntilChanged()
            .onEach { list ->
                backupJob?.cancel()
                backupJob = scope.launch {
                    delay(1000)
                    backupWeightHistory(list)
                }
            }
            .launchIn(scope)
    }

    private fun getWeightBackupFile(): File {
        return File(context.filesDir, Constants.WEIGHT_BACKUP_CSV)
    }

    private fun getProfilePhotoFile(): File {
        return File(context.filesDir, Constants.PROFILE_PHOTO_FILENAME)
    }

    private suspend fun backupWeightHistory(
        history: List<WeightMeasure>
    ) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Start backup, items: ${history.size}")
        if (history.size <= 1) return@withContext


        val backupFile = getWeightBackupFile()
        val tempFile = File(context.filesDir, "weight_backup_tmp.csv")
        exportWeightCsv(history, context, tempFile.toUri()) {}
        // 🔐 atomic write
        //tempFile.writeText(serialized)
        tempFile.renameTo(backupFile)
        Log.d(TAG, "backup sucessfull")
    }

    /**
     * Próba odzyskania backupu
     * @return - komunikat o wyniku operacji
     */
    override suspend fun tryToRestoreBackup(): String = withContext(Dispatchers.IO) {
        //Constants.PROFILE_PHOTO_FILENAME
        if (weightRepository.hasAny()) {
            return@withContext context.getString(R.string.backup_measurement_records_already_exist_in_db)
        }
        if (userProfileRepository.hasAny()) {
            return@withContext context.getString(R.string.backup_profile_already_exist_in_db)
        }
        val weightBackupFile = getWeightBackupFile()
        val profilePhotoFile = getProfilePhotoFile()
        if (!weightBackupFile.exists() && !profilePhotoFile.exists()) {
            return@withContext context.getString(R.string.backup_no_backup_files)
        }
        val msgs = mutableListOf<String>()
        if (weightBackupFile.exists()) {
            val cnt = restoreWeightBackupFile(weightBackupFile)
            msgs += context.getString(R.string.backup_measuremets_restored, cnt)
        }
        if (profilePhotoFile.exists()) {
            restoreProfilePhotoFile(profilePhotoFile)
            msgs += context.getString(R.string.backup_profile_photo_restored)
        }
        msgs.joinToString()
    }

    private suspend fun restoreWeightBackupFile(file: File): Int {
        return importWeightCsv(weightRepository, context, file.toUri()) {}
    }

    private suspend fun restoreProfilePhotoFile(file: File) {
        userProfileRepository.save(
            UserProfile(
                photoPath = file.absolutePath,
            )
        )
    }

    override suspend fun isAvailableRestore(): Boolean = withContext(Dispatchers.IO) {
        (!userProfileRepository.hasAny()
                && !weightRepository.hasAny())
                && (getWeightBackupFile().exists() || getProfilePhotoFile().exists())
    }
}