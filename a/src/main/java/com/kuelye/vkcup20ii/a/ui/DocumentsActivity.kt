package com.kuelye.vkcup20ii.a.ui

import android.content.Context
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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.a.R
import com.kuelye.vkcup20ii.a.data.DocumentRepository
import com.kuelye.vkcup20ii.a.data.DocumentRepository.COUNT_PER_PAGE
import com.kuelye.vkcup20ii.a.model.VKDocument
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.ui.activity.BaseActivity
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
    private var pagesCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)
        initializeLayout()
    }

    override fun onLogin() {
        requestDocuments()
    }

    private fun initializeLayout() {
        adapter = Adapter(this)
        adapter.onMenuClickListener = { v, document -> showDocumentMenu(v, document) }
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = object : DefaultItemAnimator() {
            override fun animateMove(
                holder: RecyclerView.ViewHolder?, fromX: Int, fromY: Int, toX: Int, toY: Int
            ): Boolean {
                return if (holder is Adapter.ProgressViewHolder) {
                    dispatchMoveFinished(holder)
                    false
                } else {
                    super.animateMove(holder, fromX, fromY, toX, toY)
                }
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (adapter.hasMore
                        && layoutManager.findLastVisibleItemPosition() == adapter.itemCount - 1
                        && adapter.itemCount >= pagesCount * COUNT_PER_PAGE) {
                    pagesCount++
                    requestDocuments()
                }
            }
        })

        swipeRefreshLayout.setProgressViewOffset(true, 0, dp(32))
        swipeRefreshLayout.setSlingshotDistance(dp(64))
        swipeRefreshLayout.setOnRefreshListener { requestDocuments() }
    }

    private fun requestDocuments() {
        DocumentRepository.getDocuments(
            pagesCount,
            object : VKApiCallback<DocumentRepository.GetDocumentsResult> {
                override fun success(result: DocumentRepository.GetDocumentsResult) {
                    adapter.swap(result.documents, result.documents.size != result.totalCount)
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

    private class Adapter(
        val context: Context
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val ITEM_VIEW_VALUE = 0
            private const val PROGRESS_VIEW_VALUE = 1
            private const val PROGRESS_VIEW_ID = 0L
        }

        var hasMore: Boolean = false
        var onMenuClickListener: ((View, VKDocument) -> Unit)? = null

        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        private var documents: List<VKDocument?>? = null

        init {
            setHasStableIds(true)
        }

        override fun getItemCount(): Int = documents?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ITEM_VIEW_VALUE -> ItemViewHolder(layoutInflater.inflate(R.layout.layout_document, parent, false))
                else -> ProgressViewHolder(layoutInflater.inflate(R.layout.layout_progress, parent, false))
            }
        }

        override fun onBindViewHolder(holder:  RecyclerView.ViewHolder, position: Int) {
            val document = documents!![position]
            if (document != null) updateItemLayout(holder as ItemViewHolder, document)
        }

        override fun getItemViewType(position: Int): Int {
            return if (isProgress(position)) PROGRESS_VIEW_VALUE else ITEM_VIEW_VALUE
        }

        override fun getItemId(position: Int): Long {
            return if (isProgress(position)) PROGRESS_VIEW_ID else documents!![position]!!.id.toLong()
        }

        fun swap(documents: List<VKDocument>?, hasMore: Boolean) {
            val oldDocuments = this.documents
            val newDocuments = if (hasMore) documents?.plus<VKDocument?>(null) else documents
            this.hasMore = hasMore
            this.documents = newDocuments
            DiffUtil.calculateDiff(DiffCallback(oldDocuments, newDocuments)).dispatchUpdatesTo(this)
        }

        private fun isProgress(position: Int) = hasMore && position == documents?.lastIndex

        private fun updateItemLayout(holder: ItemViewHolder, document: VKDocument) {
            holder.iconImageView.setImageResource(document.type.drawable)
            holder.titleTextView.text = document.title
            holder.infoTextView.text = document.getFormattedInfo(context)
            if (document.tags == null) {
                holder.setTagsVisible(false)
                holder.titleTextView.maxLines = 2
            } else {
                holder.setTagsVisible(true)
                holder.titleTextView.maxLines = 1
                holder.tagsTextView.text = document.tags.joinToString(", ")
            }
            holder.moreImageView.setOnClickListener { v -> onMenuClickListener?.invoke(v, document) }
        }

//        class Item(val document: VKDocument? = null) {
//            val id: Long = document?.id?.toLong() ?: 0L
//        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
            val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            val infoTextView: TextView = itemView.findViewById(R.id.infoTextView)
            val tagsTextView: TextView = itemView.findViewById(R.id.tagsTextView)
            val moreImageView: ImageView = itemView.findViewById(R.id.moreImageView)
            private val tagsImageView: ImageView = itemView.findViewById(R.id.tagsImageView)

            fun setTagsVisible(visible: Boolean) {
                tagsImageView.visibility = if (visible) VISIBLE else GONE
                tagsTextView.visibility = if (visible) VISIBLE else GONE
            }

        }

        class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        class DiffCallback(
            val oldDocuments: List<VKDocument?>?,
            val newDocuments: List<VKDocument?>?
        ) : DiffUtil.Callback() {

            override fun getOldListSize(): Int = oldDocuments?.size ?: 0

            override fun getNewListSize(): Int = newDocuments?.size ?: 0

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldDocuments?.get(oldItemPosition)?.id == newDocuments?.get(newItemPosition)?.id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldDocuments?.get(oldItemPosition) == newDocuments?.get(newItemPosition)

        }

    }

}
