package com.kuelye.vkcup20ii.core.ui.activity

import android.content.Intent
import android.graphics.Color.TRANSPARENT
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.appcompat.app.AppCompatActivity
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.ui.fragment.BaseFragment
import com.kuelye.vkcup20ii.core.ui.view.Toolbar
import com.kuelye.vkcup20ii.core.utils.getStatusBarHeight
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback

open class BaseActivity : AppCompatActivity(), OnLoginListener {

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
    }

    val onLoginListeners: MutableList<OnLoginListener> by lazy { mutableListOf<OnLoginListener>() }
    val toolbar: Toolbar? by lazy { findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            clearFlags(FLAG_TRANSLUCENT_STATUS)
            addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            var systemUiFlags = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            if (SDK_INT >= 23) systemUiFlags = systemUiFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            decorView.systemUiVisibility = systemUiFlags
            statusBarColor = TRANSPARENT
            decorView.setPadding(decorView.paddingLeft, getStatusBarHeight(context),
                decorView.paddingRight, decorView.paddingBottom)
        }
    }

    override fun onResume() {
        super.onResume()
        checkLogin()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                onLogin()
            }

            override fun onLoginFailed(errorCode: Int) {
                Log.w(TAG, "onLoginFailed: errorCode=$errorCode")
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onLogin() {
        onLoginListeners.map { it.onLogin() }
    }

    fun show(fragment: BaseFragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkLogin() {
        if (VK.isLoggedIn()) {
            onLogin()
        } else {
            VK.login(this, Config.scopes)
        }
    }

}

interface OnLoginListener {
    fun onLogin()
}