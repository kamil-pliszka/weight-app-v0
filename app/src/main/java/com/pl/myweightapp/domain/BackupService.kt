package com.pl.myweightapp.domain

interface BackupService {
    suspend fun tryToRestoreBackup(): String
    suspend fun isAvailableRestore(): Boolean
}