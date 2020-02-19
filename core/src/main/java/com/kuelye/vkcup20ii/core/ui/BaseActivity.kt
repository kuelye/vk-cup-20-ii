package com.kuelye.vkcup20ii.core.ui

import androidx.appcompat.app.AppCompatActivity
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKScope

abstract class BaseActivity : AppCompatActivity() {

    abstract fun onLoggedIn()

    override fun onResume() {
        super.onResume()
        checkLogin()
    }

    private fun checkLogin() {
        if (VK.isLoggedIn()) {
            onLoggedIn()
        } else {
            VK.login(this, arrayListOf(VKScope.GROUPS))
        }
    }

}