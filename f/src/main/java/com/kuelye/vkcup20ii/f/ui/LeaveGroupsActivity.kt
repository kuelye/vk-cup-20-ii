package com.kuelye.vkcup20ii.f.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.f.R
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope.GROUPS
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.requests.VKRequest
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_leave_group.*
import kotlinx.android.synthetic.main.layout_group_item.*
import org.json.JSONObject

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

        adapter = Adapter(this)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
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

    class VKGroupsRequest : VKRequest<List<VKGroup>> {
        constructor() : super("groups.get") {
            addParam("extended", 1)
        }

        override fun parse(r: JSONObject): List<VKGroup> {
            val groups = r.getJSONObject("response").getJSONArray("items")
            Log.v(TAG, groups.toString())
            val result = ArrayList<VKGroup>()
            for (i in 0 until groups.length()) {
                result.add(VKGroup.parse(groups.getJSONObject(i)))
            }
            return result
        }
    }

    class VKGroup(
        val name: String,
        val photo200: String
    ) {
        companion object {
            fun parse(r: JSONObject): VKGroup {
                return VKGroup(r.getString("name"), r.getString("photo_200"))
            }
        }

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
                Picasso.get().load(group.photo200).into(photoImageView) // TODO .error(R.drawable.user_placeholder)
                nameTextView.text = group.name
            }

        }

    }

}
