package com.example.simplenote

import android.app.Application
import com.example.simplenote.auth.TokenStore

class SimpleNoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
