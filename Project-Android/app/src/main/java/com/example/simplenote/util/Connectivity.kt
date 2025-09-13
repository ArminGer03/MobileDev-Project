package com.example.simplenote.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface ConnectivityObserver {
    enum class Status { Available, Losing, Lost, Unavailable }
    fun observe(): Flow<Status>
}

class AndroidNetworkMonitor(private val context: Context) : ConnectivityObserver {
    override fun observe(): Flow<ConnectivityObserver.Status> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network)      { trySend(ConnectivityObserver.Status.Available) }
            override fun onLosing(network: Network, maxMsToLive: Int) { trySend(ConnectivityObserver.Status.Losing) }
            override fun onLost(network: Network)           { trySend(ConnectivityObserver.Status.Lost) }
            override fun onUnavailable()                    { trySend(ConnectivityObserver.Status.Unavailable) }
        }
        cm.registerDefaultNetworkCallback(cb)

        // Emit initial
        val active = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(active)
        val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        trySend(if (isOnline) ConnectivityObserver.Status.Available else ConnectivityObserver.Status.Unavailable)

        awaitClose { cm.unregisterNetworkCallback(cb) }
    }
}
