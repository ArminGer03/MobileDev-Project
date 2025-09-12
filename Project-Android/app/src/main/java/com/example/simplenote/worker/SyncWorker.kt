package com.example.simplenote.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.simplenote.database.NotesDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.simplenote.notes.NotesRepository

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val accessToken = inputData.getString(KEY_ACCESS_TOKEN)
            if (accessToken.isNullOrBlank()) return@withContext Result.failure()

            val repo = NotesRepository(NotesDatabase.getInstance(applicationContext).noteDao())
            repo.syncNotesWithServer(accessToken)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry() // retry later if failed
        }
    }

    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
    }
}
