package com.kuelye.vkcup20ii.f

import android.app.Application
import android.content.Context
import com.vk.api.sdk.VK

class LeaveGroupApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        VK.initialize(this)
    }

}