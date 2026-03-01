package com.pl.myweightapp.domain

import java.io.InputStream

interface StorageSupport {
    suspend fun logStorage()
    suspend fun cleanupTemporary()
    suspend fun copyTmpToFinal(fromPath: String, toFilename: String): String
    suspend fun saveProfileImage(input: InputStream): String
    suspend fun exists(path: String): Boolean
}