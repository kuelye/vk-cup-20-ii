package com.kuelye.vkcup20ii.e.ui.activity

import android.os.Bundle
import com.kuelye.vkcup20ii.core.ui.BaseActivity
import com.kuelye.vkcup20ii.e.R
import kotlinx.android.synthetic.main.activity_share_photo.*


class SharePhotoActivity : BaseActivity() {

    override fun onLoggedIn() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_photo)
        initializeLayout()
    }

    private fun initializeLayout() {
        chooseButton.setOnClickListener { bottomSheetLayout.switch() }
        dismissImageView.setOnClickListener { bottomSheetLayout.dismiss() }
    }

}
