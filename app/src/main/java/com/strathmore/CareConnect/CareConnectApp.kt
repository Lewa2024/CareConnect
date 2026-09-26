package com.strathmore.CareConnect

import android.app.Application
import com.strathmore.CareConnect.data.local.AppDatabase

class CareConnectApp : Application() {
    // Lazily created, single shared Room instance for the whole app (offline-first).
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}