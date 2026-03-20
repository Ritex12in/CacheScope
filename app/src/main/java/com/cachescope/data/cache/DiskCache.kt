package com.cachescope.data.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiskCache @Inject constructor(
    private val fileDir: File
) : CacheDataSource<String> {

    override suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        val file = File(fileDir, sanitize(key))
        if (file.exists()) file.readText() else null
    }

    override suspend fun put(key: String, value: String) = withContext(Dispatchers.IO) {
        fileDir.mkdirs()
        File(fileDir, sanitize(key)).writeText(value)
    }

    override suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        fileDir.listFiles()?.forEach { it.delete() }
    }

    val sizeBytes: Long get() = fileDir.walkTopDown().sumOf { it.length() }

    // Replace characters that are invalid in filenames
    private fun sanitize(key: String) = key.replace(Regex("[^a-zA-Z0-9._-]"), "_")
}
