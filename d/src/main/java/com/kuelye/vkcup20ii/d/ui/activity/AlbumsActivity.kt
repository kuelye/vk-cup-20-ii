package com.kuelye.vkcup20ii.d.ui.activity

import android.os.Bundle
import android.util.Log
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.ui.activity.BaseActivity
import com.kuelye.vkcup20ii.d.R
import com.kuelye.vkcup20ii.d.ui.fragment.AlbumFragment
import com.kuelye.vkcup20ii.d.ui.fragment.AlbumsFragment
import com.vk.api.sdk.auth.VKScope

class AlbumsActivity : BaseActivity() {

    companion object {
        const val EDIT_MENU_ITEM_ID = 0
        const val ADD_MENU_ITEM_ID = 1
        const val BACK_MENU_ITEM_ID = 2
        private val TAG = AlbumsActivity::class.java.simpleName
    }

    init {
        Config.scopes = listOf(VKScope.PHOTOS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_albums)
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            show(AlbumsFragment())
        }
    }

}