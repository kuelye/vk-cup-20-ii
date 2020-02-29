package com.kuelye.vkcup20ii.d.ui.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.core.data.BaseRepository.GetItemsResult
import com.kuelye.vkcup20ii.core.data.PhotoRepository
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.ui.fragment.BaseRecyclerFragment
import com.kuelye.vkcup20ii.core.ui.misc.SpaceItemDecoration
import com.kuelye.vkcup20ii.core.ui.view.MenuView
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.d.R
import com.kuelye.vkcup20ii.d.ui.activity.AlbumsActivity.Companion.ADD_MENU_ITEM_ID
import com.kuelye.vkcup20ii.d.ui.fragment.AlbumFragment.Adapter
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VKApiCallback

class AlbumFragment : BaseRecyclerFragment<VKPhoto, Adapter>() {

    companion object {
        private val TAG = AlbumFragment::class.java.simpleName
        private const val EXTRA_ALBUM_ID = "ALBUM_ID"

        fun newInstance(albumId: Int): AlbumFragment {
            val fragment = AlbumFragment()
            fragment.arguments = Bundle().apply { putInt(EXTRA_ALBUM_ID, albumId) }
            return fragment
        }
    }

    private val groupId: Int?
        get() = arguments?.getInt(EXTRA_ALBUM_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeLayout()
    }

    override fun onResume() {
        super.onResume()
        requestData(true)
        toolbar?.apply {
            title = null
            setMenu(MenuView.Item(R.drawable.ic_add_outline_28, ADD_MENU_ITEM_ID))
        }
    }

    override fun requestData(onlyCache: Boolean) {
        PhotoRepository.getPhotos(groupId,
            (pagesCount - 1) * countPerPage, countPerPage, onlyCache,
            object : VKApiCallback<GetItemsResult<VKPhoto>> {
                override fun success(result: GetItemsResult<VKPhoto>) {
                    Log.v(TAG, "GUB success: items.size=${result.items?.size} totalCount=${result.totalCount}")
                    showData(result.items, result.items?.size != result.totalCount)
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun fail(error: Exception) {
                    Log.e(TAG, "requestData>fail", error) // TODO
                }
            })
    }

    override fun initializeLayout() {
        val space = dimen(context!!, R.dimen.padding_standard_eight)
        val (spanCount, itemWidth) = calculateLayout(context!!,
            padding = 0,
            space = space,
            itemMinWidth = dimen(context!!, R.dimen.albums_item_min_width))

        layoutManager = GridLayoutManager(context!!, spanCount)
        adapter = Adapter(context!!, itemWidth)
        adapter.onItemClickListener = {
            // TODO open photo
        }

        super.initializeLayout()

        recyclerView.addItemDecoration(SpaceItemDecoration(space, space, spanCount))
    }

    class Adapter(
        context: Context,
        private val itemWidth: Int
    ) : BaseAdapter<VKPhoto>(context) {

        var onItemClickListener: ((VKPhoto) -> Unit)? = null
        var onItemLongClickListener: (() -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ITEM_VIEW_VALUE -> ItemViewHolder(
                    layoutInflater.inflate(R.layout.layout_photo_item, parent, false))
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val photo = items[position]
            if (photo != null) updateItemLayout(holder as ItemViewHolder, photo)
        }

        private fun updateItemLayout(holder: ItemViewHolder, photo: VKPhoto) {
            holder.photoImageView.setImageDrawable(ColorDrawable(Color.RED))
            holder.photoImageView.layoutParams.apply {
                width = itemWidth
                height = itemWidth
            }

            Picasso.get().load(photo.photo)
                .fit().centerCrop()
                .placeholder(ColorDrawable(Color.RED))
                .error(ColorDrawable(Color.RED))
                .into(holder.photoImageView)

            holder.itemView.setOnClickListener { onItemClickListener?.invoke(photo) }
            holder.itemView.setOnLongClickListener { onItemLongClickListener?.invoke(); true }
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photoImageView: ImageView = itemView as ImageView
        }

    }

}