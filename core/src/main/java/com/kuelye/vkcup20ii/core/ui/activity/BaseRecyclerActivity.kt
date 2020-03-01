package com.kuelye.vkcup20ii.core.ui.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.model.Identifiable
import com.vk.api.sdk.utils.VKUtils

open class BaseRecyclerActivity<I : Identifiable, A : BaseRecyclerActivity.BaseAdapter<I>> : BaseVKActivity() {

    companion object {
        private const val COUNT_PER_PAGE_DEFAULT = 10
    }

    protected lateinit var adapter: A
    protected lateinit var layoutManager: LinearLayoutManager
    protected var pagesCount = 1
    protected var countPerPage = COUNT_PER_PAGE_DEFAULT

    protected lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onLogin() {
        pagesCount = 1
        requestData()
    }

    @CallSuper
    protected open fun initializeLayout() {
        progressBar = findViewById(android.R.id.progress)
        recyclerView = findViewById(android.R.id.list)
        emptyTextView = findViewById(android.R.id.empty)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = object : DefaultItemAnimator() {
            override fun animateMove(
                holder: RecyclerView.ViewHolder?, fromX: Int, fromY: Int, toX: Int, toY: Int
            ): Boolean {
                return if (holder is BaseAdapter.ProgressViewHolder) {
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
                    && adapter.itemCount >= pagesCount * COUNT_PER_PAGE_DEFAULT
                ) {
                    pagesCount++
                    requestData()
                }
            }
        })

        swipeRefreshLayout.setProgressViewOffset(true, 0, VKUtils.dp(32))
        swipeRefreshLayout.setSlingshotDistance(VKUtils.dp(64))
        swipeRefreshLayout.setOnRefreshListener { pagesCount = 1; requestData() }

        showData(null)
    }

    protected open fun requestData(onlyCache: Boolean = false) {}

    protected fun showData(documents: List<I>?,  hasMore: Boolean = false) {
        progressBar.visibility = if (documents == null) VISIBLE else GONE
        recyclerView.visibility = if (documents.isNullOrEmpty()) GONE else VISIBLE
        emptyTextView.visibility = if (documents != null && documents.isEmpty()) VISIBLE else GONE
        adapter.swap(documents, hasMore)
    }

    abstract class BaseAdapter<I : Identifiable>(
        val context: Context
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val ITEM_VIEW_VALUE = 0
            const val PROGRESS_VIEW_VALUE = 1
            const val PROGRESS_VIEW_ID = 0L
        }

        var hasMore: Boolean = false
        protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        protected val items: MutableList<I?> by lazy { mutableListOf<I?>() }

        init {
            @Suppress("LeakingThis")
            setHasStableIds(true)
        }

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int): Int {
            return if (isProgress(position)) PROGRESS_VIEW_VALUE else ITEM_VIEW_VALUE
        }

        override fun getItemId(position: Int): Long {
            return if (isProgress(position)) PROGRESS_VIEW_ID else items[position]!!.id.toLong()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ProgressViewHolder(layoutInflater.inflate(R.layout.layout_progress, parent, false))
        }

        fun swap(documents: List<I>?, hasMore: Boolean) {
            val oldDocuments = this.items
            val newDocuments = if (hasMore) documents?.plus<I?>(null) else documents
            val diffResult = DiffUtil.calculateDiff(DiffCallback(oldDocuments, newDocuments))
            this.hasMore = hasMore
            this.items.clear()
            if (newDocuments != null) this.items.addAll(newDocuments)
            diffResult.dispatchUpdatesTo(this)
        }

        private fun isProgress(position: Int) = hasMore && position == items.lastIndex

        class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        class DiffCallback<I : Identifiable>(
            private val oldDocuments: List<I?>?,
            private val newDocuments: List<I?>?
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