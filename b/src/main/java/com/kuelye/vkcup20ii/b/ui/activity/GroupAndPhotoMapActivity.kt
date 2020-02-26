package com.kuelye.vkcup20ii.b.ui.activity

import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.b.ui.fragment.GroupMapFragment
import com.kuelye.vkcup20ii.b.ui.fragment.PhotoMapFragment
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.kuelye.vkcup20ii.core.ui.activity.BaseActivity
import com.vk.api.sdk.auth.VKScope.GROUPS
import com.vk.api.sdk.auth.VKScope.PHOTOS
import kotlinx.android.synthetic.main.activity_map.*
import java.lang.IllegalArgumentException

class GroupAndPhotoMapActivity : BaseActivity() {

    companion object {
        private val TAG = GroupAndPhotoMapActivity::class.java.simpleName
    }

    init {
        Config.scopes = listOf(GROUPS, PHOTOS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        initializeLayout()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem--
        }
    }

    private fun initializeLayout() {
        viewPager.adapter = Adapter(this, supportFragmentManager)
        viewPager.offscreenPageLimit = 2
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.onTabSelectedListener = {
            viewPager.currentItem = it
            Log.v(TAG, "initializeLayout: $it")
        }
    }

    private class Adapter(
        val context: Context,
        fm: FragmentManager
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 3

        override fun getItem(position: Int): Fragment =
            when (position) {
                0 -> GroupMapFragment.newInstance(VKGroup.Type.EVENT)
                1 -> PhotoMapFragment()
                2 -> GroupMapFragment.newInstance(VKGroup.Type.GROUP)
                else -> throw IllegalArgumentException()
            }

        override fun getPageTitle(position: Int): CharSequence? =
            context.getString(when (position) {
                0 -> R.string.map_events
                1 -> R.string.map_photos
                2 -> R.string.map_groups
                else -> throw IllegalArgumentException()
            })

    }

}
