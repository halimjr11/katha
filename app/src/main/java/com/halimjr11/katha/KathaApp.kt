package com.halimjr11.katha

import android.app.Application
import com.halimjr11.katha.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KathaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KathaApp)
            modules(appModules)
        }
    }
}
