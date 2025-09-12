package com.example.simplenote.worker

import android.content.Context
import androidx.work.*

import java.util.concurrent.TimeUnit

object SyncScheduler {
    fun schedulePeriodicSync(context: Context, accessToken: String) {
        val workManager = WorkManager.getInstance(context)

        val inputData = workDataOf(SyncWorker.KEY_ACCESS_TOKEN to accessToken)

        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES // minimum allowed interval
        )
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "note_sync_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
