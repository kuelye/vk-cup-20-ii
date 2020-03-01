package com.kuelye.vkcup20ii.core.ui.activity

import android.content.Intent
import android.graphics.Color.TRANSPARENT
import android.graphics.Color.WHITE
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
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

open class BaseActivity : AppCompatActivity() {

    companion object {
        private val TAG = BaseVKActivity::class.java.simpleName
    }

    val toolbar: Toolbar? by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val statusBarStubView: View? by lazy { findViewById<View>(R.id.statusBarStubView) }

    var lightStatusBar = true
    var statusBarColor = WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            clearFlags(FLAG_TRANSLUCENT_STATUS)
            addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            var systemUiFlags = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            if (lightStatusBar && SDK_INT >= 23) systemUiFlags = systemUiFlags or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            decorView.systemUiVisibility = systemUiFlags
            if (SDK_INT >= 23) statusBarColor = TRANSPARENT
        }
    }

    fun show(fragment: BaseFragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    protected fun fixStatusBar() {
        statusBarStubView?.apply {
            layoutParams = layoutParams.apply { height = getStatusBarHeight(this@BaseActivity) }
            setBackgroundColor(statusBarColor)
        }
    }

}