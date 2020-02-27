package com.kuelye.vkcup20ii.a.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.a.R
import com.kuelye.vkcup20ii.a.data.DocumentRepository
import com.kuelye.vkcup20ii.a.model.VKDocument
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.ui.activity.BaseActivity
import com.kuelye.vkcup20ii.core.utils.themeDimen
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.utils.VKUtils.dp
import kotlinx.android.synthetic.main.activity_documents.*

class DocumentsActivity : BaseActivity() {

    companion object {
        private val TAG = DocumentsActivity::class.java.simpleName
    }

    init {
        Config.scopes = listOf(VKScope.DOCS)
    }

    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)
        initializeLayout()
    }

    override fun onLogin() {
        requestDocuments()
    }

    private fun initializeLayout() {
        adapter = Adapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        swipeRefreshLayout.setProgressViewOffset(true, 0, dp(32))
        swipeRefreshLayout.setSlingshotDistance(dp(64))
        swipeRefreshLayout.setOnRefreshListener { requestDocuments() }
    }

    private fun requestDocuments() {
        DocumentRepository.getDocuments(
            object : VKApiCallback<List<VKDocument>> {
                override fun success(result: List<VKDocument>) {
                    adapter.documents = result
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun fail(error: Exception) {
                    Log.e(TAG, "requestGroups>fail", error) // TODO
                }
            })
    }

    private fun showDocumentMenu(v: View, document: VKDocument) {
        val menu = PopupMenu(this@DocumentsActivity, v)
        menu.inflate(R.menu.context_documents)
        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
//                        R.id.rename_action ->
                R.id.remove_action -> {
                    removeDocument(document)
                    true
                }
                else -> false
            }
        }
        menu.show()
    }

    private fun removeDocument(document: VKDocument) {
        DocumentRepository.removeDocument(document, object : VKApiCallback<Int> {
            override fun success(result: Int) {
                requestDocuments()
                Log.v(TAG, "success: $result")
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "fail", error)
            }
        })
    }

    private inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private val layoutInflater: LayoutInflater = LayoutInflater.from(this@DocumentsActivity)

        var documents: List<VKDocument>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int = documents?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(layoutInflater.inflate(R.layout.layout_document, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val document = documents!![position]
            updateLayout(holder, document)
        }

        private fun updateLayout(holder: ViewHolder, document: VKDocument) {
            holder.iconImageView.setImageResource(document.type.drawable)
            holder.titleTextView.text = document.title
            holder.infoTextView.text = document.getFormattedInfo(this@DocumentsActivity)
            if (document.tags == null) {
                holder.setTagsVisible(false)
                holder.titleTextView.maxLines = 2
            } else {
                holder.setTagsVisible(true)
                holder.titleTextView.maxLines = 1
                holder.tagsTextView.text = document.tags.joinToString(", ")
            }
            holder.moreImageView.setOnClickListener { v -> showDocumentMenu(v, document) }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
            val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            val infoTextView: TextView = itemView.findViewById(R.id.infoTextView)
            val tagsImageView: ImageView = itemView.findViewById(R.id.tagsImageView)
            val tagsTextView: TextView = itemView.findViewById(R.id.tagsTextView)
            val moreImageView: ImageView = itemView.findViewById(R.id.moreImageView)

            fun setTagsVisible(visible: Boolean) {
                tagsImageView.visibility = if (visible) VISIBLE else GONE
                tagsTextView.visibility = if (visible) VISIBLE else GONE
            }

        }

    }

}
