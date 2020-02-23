package com.kuelye.vkcup20ii.b.ui.activity

import android.os.Bundle
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.ui.activity.BaseActivity
import com.vk.api.sdk.auth.VKScope.GROUPS
import com.vk.api.sdk.auth.VKScope.PHOTOS

class MapActivity : BaseActivity() {

    init {
        Config.scopes = listOf(GROUPS, PHOTOS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
    }

}
