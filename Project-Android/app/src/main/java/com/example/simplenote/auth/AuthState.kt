package com.example.simplenote.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AuthState {
    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut

    fun triggerLogout() {
        _loggedOut.value = true
    }

    fun reset() {
        _loggedOut.value = false
    }
}
