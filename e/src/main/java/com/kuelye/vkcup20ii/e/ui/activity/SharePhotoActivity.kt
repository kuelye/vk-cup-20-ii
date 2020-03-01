package com.kuelye.vkcup20ii.e.ui.activity

import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.util.Log
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.api.wall.VKWallPostCommand
import com.kuelye.vkcup20ii.core.ui.activity.BaseVKActivity
import com.kuelye.vkcup20ii.core.utils.hideKeyboard
import com.kuelye.vkcup20ii.e.R
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKScope.PHOTOS
import com.vk.api.sdk.auth.VKScope.WALL
import kotlinx.android.synthetic.main.activity_share_photo.*

class SharePhotoActivity : BaseVKActivity() {

    init {
        Config.scopes = listOf(WALL, PHOTOS)
    }

    companion object {
        private val TAG = SharePhotoActivity::class.java.simpleName
        private const val EXTRA_PHOTO_URI = "PHOTO_URI"
        private const val PICK_PHOTO_REQUEST_CODE = 99
    }

    private var photoUri: Uri? = null
    private var yetCollapsed: Boolean = false

    override fun onBackPressed() {
        if (bottomSheetLayout.expanded) {
            bottomSheetLayout.animateExpanded(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoUri = savedInstanceState?.getParcelable(EXTRA_PHOTO_URI)
        setContentView(R.layout.activity_share_photo)
        initializeLayout()
        if (photoUri != null) showShareSheet(photoUri!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_PHOTO_URI, photoUri)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK && intent != null && intent.data != null) {
                photoUri = intent.data!!
                showShareSheet(photoUri!!)
            } else {
                // TODO
            }
        } else super.onActivityResult(requestCode, resultCode, intent)
    }

    private fun initializeLayout() {
        chooseButton.setOnClickListener { pickPhoto() }
        shareSheetToolbar.title = getString(R.string.share_sheet_title)
        shareSheetToolbar.dismissImageView.setOnClickListener { bottomSheetLayout.dismiss() }
        bottomSheetLayout.onCollapsedListener = {
            hideKeyboard(this, commentEditText)
            setShareEnabled(true)
            commentEditText.text = null
            bottomSheetLayout.scrollTo(0, 0)
            yetCollapsed = true
            photoUri = null
        }
    }

    private fun pickPhoto() {
        val intent = Intent(ACTION_PICK, EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_PHOTO_REQUEST_CODE)
    }

    private fun showShareSheet(photoUri: Uri) {
        photoImageView.setImageURI(photoUri)
        bottomSheetLayout.animateExpanded(true)
        sendButton.setOnClickListener { wallPost(photoUri) }
    }

    private fun wallPost(photoUri: Uri) {
        val comment = commentEditText.text.toString()
        setShareEnabled(false)
        yetCollapsed = false
        VK.execute(VKWallPostCommand(this, comment, photoUri), object : VKApiCallback<Int> {
            override fun success(result: Int) {
                if (!yetCollapsed) bottomSheetLayout.animateExpanded(false)
            }

            override fun fail(error: Exception) {
                Log.v(TAG, "wallPost>fail", error) // TODO
            }
        })
    }

    private fun setShareEnabled(enabled: Boolean) {
        commentEditText.isEnabled = enabled
        sendButton.isEnabled = enabled
    }

}
