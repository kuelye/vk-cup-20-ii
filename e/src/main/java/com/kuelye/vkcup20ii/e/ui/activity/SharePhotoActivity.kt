package com.kuelye.vkcup20ii.e.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kuelye.vkcup20ii.core.ui.BaseActivity
import com.kuelye.vkcup20ii.e.R

class SharePhotoActivity : BaseActivity() {

    override fun onLoggedIn() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_photo)
    }
}
