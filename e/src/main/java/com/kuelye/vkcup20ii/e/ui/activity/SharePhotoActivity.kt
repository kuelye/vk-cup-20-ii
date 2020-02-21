package com.kuelye.vkcup20ii.e.ui.activity

import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.kuelye.vkcup20ii.core.ui.BaseActivity
import com.kuelye.vkcup20ii.core.utils.hideKeyboard
import com.kuelye.vkcup20ii.e.R
import kotlinx.android.synthetic.main.activity_share_photo.*


class SharePhotoActivity : BaseActivity() {

    companion object {
        private const val PICK_PHOTO_REQUEST_CODE = 1
    }

    override fun onLoggedIn() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_photo)
        initializeLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == PICK_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK && intent != null && intent.data != null) {
                showShareSheet(intent.data!!)
            } else {
                // TODO
            }
        } else super.onActivityResult(requestCode, resultCode, intent)
    }

    private fun initializeLayout() {
        chooseButton.setOnClickListener { pickPhoto() }
        shareSheetToolbar.title = getString(R.string.share_sheet_title)
        shareSheetToolbar.dismissImageView.setOnClickListener { bottomSheetLayout.dismiss() }
        bottomSheetLayout.onCollapsedListener = { hideKeyboard(this, commentEditText) }
    }

    private fun pickPhoto() {
        val pickPhoto = Intent(ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, PICK_PHOTO_REQUEST_CODE)
    }

    private fun showShareSheet(photoUri: Uri) {
        photoImageView.setImageURI(photoUri)
        bottomSheetLayout.switch()
    }

}
