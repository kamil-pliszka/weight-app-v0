package com.pl.myweightapp.domain

import java.io.InputStream

interface StorageSupport {
    fun logStorage()
    fun cleanupTemporary()
    fun moveTmpToFinal(fromPath: String, toFilename: String): String
    fun saveProfileImage(input: InputStream): String
    fun exists(path: String): Boolean
}