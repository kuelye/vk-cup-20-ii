package com.kuelye.vkcup20ii.f.ui

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.core.utils.px
import com.kuelye.vkcup20ii.f.R
import com.kuelye.vkcup20ii.f.api.VKGroupsRequest
import com.kuelye.vkcup20ii.f.model.VKGroup
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope.GROUPS
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.utils.VKUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_leave_group.*
import kotlinx.android.synthetic.main.layout_group_item.*
import kotlin.math.floor

class LeaveGroupsActivity : AppCompatActivity() {

    companion object {
        private val TAG = LeaveGroupsActivity::class.java.simpleName
    }

    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_group)

        toolbar.title = getString(R.string.leave_title)
        toolbar.subtitle = getString(R.string.leave_subtitle)

        val paddingStandard = dimen(this, R.dimen.padding_standard)
        val totalWidth = VKUtils.width(this) - paddingStandard * 2
        val itemWidth = dimen(this, R.dimen.group_item_width)
        val spanCount = floor((totalWidth + paddingStandard * .5) / (itemWidth + paddingStandard * .5)).toInt()
        val space = (totalWidth - spanCount * itemWidth) / (spanCount - 1)

        adapter = Adapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        recyclerView.addItemDecoration(HorizontalSpaceItemDecoration(space, spanCount))
    }

    override fun onResume() {
        super.onResume()
        checkLogin()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                checkGroups()
            }

            override fun onLoginFailed(errorCode: Int) {
                Log.w(TAG, "onLoginFailed: errorCode=$errorCode")
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkLogin() {
        if (VK.isLoggedIn()) {
            checkGroups()
        } else {
            VK.login(this, arrayListOf(GROUPS))
        }
    }

    private fun checkGroups() {
        Log.w(TAG, "checkGroups")
        VK.execute(VKGroupsRequest(), object : VKApiCallback<List<VKGroup>> {
            override fun success(result: List<VKGroup>) {
                adapter.groups = result
            }

            override fun fail(error: VKApiExecutionException) {
                Log.w(TAG, "fail: error=$error")
            }
        })
    }

    class Adapter(context: Context) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        var groups: List<VKGroup>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int = groups?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(layoutInflater.inflate(R.layout.layout_group_item, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.fill(groups!![position])
        }

        class ViewHolder(
            override val containerView: View
        ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

            fun fill(group: VKGroup) {
                Picasso.get().load(group.photo200)
                    .into(photoImageView) // TODO .error(R.drawable.user_placeholder)
                nameTextView.text = group.name
            }

        }

    }

    class HorizontalSpaceItemDecoration(
        private val space: Int,
        private val spanCount: Int
    ) : RecyclerView.ItemDecoration() {

        private val spacePerSpan = space / spanCount

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val column = parent.getChildLayoutPosition(view) % spanCount
            outRect.left = column * spacePerSpan
            outRect.right = space - (column + 1) * spacePerSpan
        }

    }

}
