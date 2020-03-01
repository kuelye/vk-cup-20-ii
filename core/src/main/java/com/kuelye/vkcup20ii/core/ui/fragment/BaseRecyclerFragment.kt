package com.kuelye.vkcup20ii.core.ui.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
import com.kuelye.vkcup20ii.core.data.BaseRepository
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.FRESH
import com.kuelye.vkcup20ii.core.model.Identifiable
import com.kuelye.vkcup20ii.core.ui.fragment.BaseRecyclerFragment.BaseAdapter
import com.vk.api.sdk.utils.VKUtils
import kotlin.math.floor

open class BaseRecyclerFragment<I : Identifiable, A : BaseAdapter<I>> : BaseFragment() {

    companion object {
        fun calculateLayout(
            context: Context,
            padding: Int,
            space: Int,
            itemMinWidth: Int
        ): Pair<Int, Int> {
            val totalWidth = (VKUtils.width(context) - padding * 2).toFloat()
            val spanCount = floor((totalWidth - space) / (itemMinWidth + space)).toInt()
            val itemWidth = ((totalWidth - (spanCount - 1) * space) / spanCount).toInt()
            return Pair(spanCount, itemWidth)
        }
    }

    protected lateinit var adapter: A
    protected lateinit var layoutManager: LinearLayoutManager

    protected lateinit var recyclerView: RecyclerView
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView

    override fun onLogin() {
        super.onLogin()
        requestData()
    }

    @CallSuper
    protected open fun initializeLayout() {
        progressBar = view!!.findViewById(android.R.id.progress)
        recyclerView = view!!.findViewById(android.R.id.list)
        emptyTextView = view!!.findViewById(android.R.id.empty)
        swipeRefreshLayout = view!!.findViewById(R.id.swipeRefreshLayout)

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
                checkMore()
            }
        })

        swipeRefreshLayout.setProgressViewOffset(true, 0, VKUtils.dp(32))
        swipeRefreshLayout.setSlingshotDistance(VKUtils.dp(64))
        swipeRefreshLayout.setOnRefreshListener { pagesCount = 1; requestData(FRESH) }

        showData(null)
    }

    protected fun showData(documents: List<I>?,  hasMore: Boolean = false) {
        progressBar.visibility = if (documents == null) View.VISIBLE else View.GONE
        recyclerView.visibility = if (documents.isNullOrEmpty()) View.GONE else View.VISIBLE
        emptyTextView.visibility = if (documents != null && documents.isEmpty()) View.VISIBLE else View.GONE
        adapter.swap(documents, hasMore)
        checkMore()
    }

    protected fun checkMore() {
        if (adapter.hasMore
            && layoutManager.findLastVisibleItemPosition() == adapter.itemCount - 1
            && adapter.itemCount >= pagesCount * countPerPage
        ) {
            pagesCount++
            requestData()
        }
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