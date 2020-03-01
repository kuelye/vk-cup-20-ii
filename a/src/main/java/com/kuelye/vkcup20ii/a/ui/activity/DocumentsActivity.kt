package com.kuelye.vkcup20ii.a.ui.activity

import android.content.Context
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_NULL
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.a.R
import com.kuelye.vkcup20ii.a.model.Type
import com.kuelye.vkcup20ii.a.model.Type.GIF
import com.kuelye.vkcup20ii.a.model.Type.IMAGE
import com.kuelye.vkcup20ii.a.model.getFormattedInfo
import com.kuelye.vkcup20ii.core.data.DocumentRepository
import com.kuelye.vkcup20ii.core.model.docs.VKDocument
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.data.BaseRepository.ItemsResult
import com.kuelye.vkcup20ii.core.ui.activity.BaseRecyclerActivity
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.activity_documents.*

class DocumentsActivity : BaseRecyclerActivity<VKDocument, DocumentsActivity.Adapter>() {

    companion object {
        private val TAG = DocumentsActivity::class.java.simpleName
    }

    init {
        Config.scopes = listOf(VKScope.DOCS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)
        initializeLayout()
    }

    override fun initializeLayout() {
        adapter = Adapter(this)
        adapter.onMenuClickListener = { v, document -> showDocumentMenu(v, document) }
        adapter.onRenameClickListener = { document -> renameDocument(document)}
        layoutManager = LinearLayoutManager(this)
        super.initializeLayout()
    }

    override fun requestData(onlyCache: Boolean) {
        DocumentRepository.getDocuments(
            (pagesCount - 1) * countPerPage, countPerPage, onlyCache,
            object : VKApiCallback<ItemsResult<VKDocument>> {
                override fun success(result: ItemsResult<VKDocument>) {
                    showData(result.items, result.items.size != result.totalCount)
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun fail(error: Exception) {
                    Log.e(TAG, "requestData>fail", error) // TODO
                }
            })
    }

    private fun showDocumentMenu(v: View, document: VKDocument) {
        val menu = PopupMenu(this@DocumentsActivity, v)
        menu.inflate(R.menu.context_documents)
        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.rename_action -> {
                    adapter.renameDocumentId = document.id
                    true
                }
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
                requestData(true)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "removeDocument>fail", error)
            }
        })
    }

    private fun renameDocument(document: VKDocument) {
        DocumentRepository.renameDocument(document, document.title, object : VKApiCallback<Int> {
            override fun success(result: Int) {
                requestData(true)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "renameDocument>fail", error)
            }
        })
    }

    class Adapter(
        context: Context
    ) : BaseAdapter<VKDocument>(context) {

        var onMenuClickListener: ((View, VKDocument) -> Unit)? = null
        var onRenameClickListener: ((VKDocument) -> Unit)? = null
        var renameDocumentId: Int? = null
            set(value) {
                if (field != value) {
                    field = value
                    notifyDataSetChanged()
                }
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ITEM_VIEW_VALUE -> ItemViewHolder(
                    layoutInflater.inflate(
                        R.layout.layout_document, parent, false
                    )
                )
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val document = items[position]
            if (document != null) updateItemLayout(holder as ItemViewHolder, document)
        }

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            if (holder is ItemViewHolder) Picasso.get().cancelRequest(holder.iconImageView)
        }

        private fun updateItemLayout(holder: ItemViewHolder, document: VKDocument) {
            val renameEnabled = document.id == renameDocumentId
            holder.titleTextView.isEnabled = renameEnabled
            holder.titleTextView.isSingleLine = renameEnabled
            holder.titleTextView.inputType = if (renameEnabled) TYPE_CLASS_TEXT else TYPE_NULL
            if (!renameEnabled) holder.titleTextView.keyListener = null
            holder.titleTextView.setText(document.title)

            holder.infoTextView.text = document.getFormattedInfo(context)
            if (document.tags == null) {
                holder.setTagsVisible(false)
                holder.titleTextView.maxLines = 2
            } else {
                holder.setTagsVisible(true)
                holder.titleTextView.maxLines = 1
                holder.tagsTextView.text = document.tags!!.joinToString(", ")
            }

            if (renameEnabled) {
                holder.moreImageView.setImageResource(R.drawable.ic_check_black_16)
                holder.moreImageView.setOnClickListener {
                    renameDocumentId = null
                    document.title = holder.titleTextView.text.toString()
                    onRenameClickListener?.invoke(document)
                }
            } else {
                holder.moreImageView.setImageResource(R.drawable.ic_more_vertical_16)
                holder.moreImageView.setOnClickListener { v ->
                    onMenuClickListener?.invoke(v, document)
                }
            }

            val type = Type.forValue(document.type)
            if (type == IMAGE || type == GIF && document.iconUrl != null) {
                Picasso.get().load(document.iconUrl)
                    .fit().centerCrop()
                    .placeholder(R.drawable.ic_placeholder_document_image_72)
                    .error(R.drawable.ic_placeholder_document_image_72)
                    .into(holder.iconImageView)
            } else {
                holder.iconImageView.setImageResource(type.drawable)
            }
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
            val titleTextView: EditText = itemView.findViewById(R.id.titleTextView)
            val infoTextView: TextView = itemView.findViewById(R.id.infoTextView)
            val tagsTextView: TextView = itemView.findViewById(R.id.tagsTextView)
            val moreImageView: ImageView = itemView.findViewById(R.id.moreImageView)
            private val tagsImageView: ImageView = itemView.findViewById(R.id.tagsImageView)

            fun setTagsVisible(visible: Boolean) {
                tagsImageView.visibility = if (visible) VISIBLE else GONE
                tagsTextView.visibility = if (visible) VISIBLE else GONE
            }
        }

    }

}
