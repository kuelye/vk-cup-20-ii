package com.kuelye.vkcup20ii.core.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.os.Bundle
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.ui.view.MenuView
import com.kuelye.vkcup20ii.core.utils.modifyAlpha
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_photo.*

class PhotoActivity : BaseActivity() {

    init {
        lightStatusBar = false
        statusBarColor = BLACK
    }

    companion object {
        private const val EXTRA_PHOTO = "PHOTO"
        private const val BACK_MENU_ITEM_ID = 0

        fun start(context: Context, photo: String) {
            val intent = Intent(context, PhotoActivity::class.java)
            intent.putExtra(EXTRA_PHOTO, photo)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        fixStatusBar()
        initializeLayout()
    }

    private fun initializeLayout() {
        initializeToolbar()
        val photo = intent.getStringExtra(EXTRA_PHOTO)
        Picasso.get().load(photo)
            .fit().centerInside()
            .into(imageView)
    }

    private fun initializeToolbar() {
        toolbar?.apply {
            setBackgroundColor(BLACK.modifyAlpha(.5f))
            setAlwaysCollapsed(true)
            title = getString(R.string.photo_title)
            titleTextColor = WHITE
            setMenuIconColor(WHITE)
            setMenu(MenuView.Item(R.drawable.ic_arrow_back_black_24dp, BACK_MENU_ITEM_ID, true))
            setOnMenuItemClickListener { id ->
                when(id) {
                    BACK_MENU_ITEM_ID -> finish()
                }
            }
        }
    }

}