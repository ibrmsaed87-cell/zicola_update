package com.spinel.zicola.zicola

import android.app.Application
import com.spinel.zicola.zicola.ads.AppOpenAdManager

class ZicolaApplication : Application() {
    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()
        appOpenAdManager = AppOpenAdManager(this)
    }
}
