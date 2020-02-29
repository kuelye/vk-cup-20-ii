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
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.core.data.BaseRepository
import com.kuelye.vkcup20ii.core.data.PhotoRepository
import com.kuelye.vkcup20ii.core.model.photos.VKPhotoAlbum
import com.kuelye.vkcup20ii.core.ui.fragment.BaseRecyclerFragment
import com.kuelye.vkcup20ii.core.ui.misc.SpaceItemDecoration
import com.kuelye.vkcup20ii.core.ui.view.MenuView
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.d.R
import com.kuelye.vkcup20ii.d.ui.activity.AlbumsActivity.Companion.ADD_MENU_ITEM_ID
import com.kuelye.vkcup20ii.d.ui.activity.AlbumsActivity.Companion.EDIT_MENU_ITEM_ID
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VKApiCallback
import kotlinx.android.synthetic.main.fragment_albums.*

class AlbumsFragment : BaseRecyclerFragment<VKPhotoAlbum, AlbumsFragment.Adapter>() {

    companion object {
        private val TAG = AlbumsFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeLayout()
    }

    override fun onResume() {
        super.onResume()
        requestData(true)
        toolbar?.apply {
            title = getString(R.string.albums_title)
            alwaysCollapsed = true
            setMenu(MenuView.Item(R.drawable.ic_edit_outline_28, EDIT_MENU_ITEM_ID),
                MenuView.Item(R.drawable.ic_add_outline_28, ADD_MENU_ITEM_ID))
        }
    }

    override fun requestData(onlyCache: Boolean) {
        PhotoRepository.getPhotoAlbums(
            (pagesCount - 1) * countPerPage, countPerPage, onlyCache,
            object : VKApiCallback<BaseRepository.GetItemsResult<VKPhotoAlbum>> {
                override fun success(result: BaseRepository.GetItemsResult<VKPhotoAlbum>) {
                    showData(result.items, result.items?.size != result.totalCount)
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun fail(error: Exception) {
                    Log.e(TAG, "requestData>fail", error) // TODO
                }
            })
    }

    override fun initializeLayout() {
        val space = dimen(context!!, R.dimen.padding_standard_three_quarters)
        val (spanCount, itemWidth) = calculateLayout(context!!,
            padding = dimen(context!!, R.dimen.padding_standard),
            space = space,
            itemMinWidth = dimen(context!!, R.dimen.albums_item_min_width))

        layoutManager = GridLayoutManager(context!!, spanCount)
        adapter = Adapter(context!!, itemWidth)
        adapter.onItemClickListener = { album -> show(AlbumFragment.newInstance(album)) }

        super.initializeLayout()

        recyclerView.addItemDecoration(SpaceItemDecoration(space, space, spanCount))
    }

    class Adapter(
        context: Context,
        private val itemWidth: Int
    ) : BaseAdapter<VKPhotoAlbum>(context) {

        var onItemClickListener: ((VKPhotoAlbum) -> Unit)? = null
        var onItemLongClickListener: (() -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ITEM_VIEW_VALUE -> ItemViewHolder(
                    layoutInflater.inflate(R.layout.layout_album_item, parent, false))
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val album = items[position]
            if (album != null) updateItemLayout(holder as ItemViewHolder, album)
        }

        private fun updateItemLayout(holder: ItemViewHolder, photoAlbum: VKPhotoAlbum) {
            holder.photoImageView.setImageDrawable(ColorDrawable(Color.RED))
            holder.photoImageView.layoutParams.apply {
                width = itemWidth
                height = itemWidth
            }

            Picasso.get().load(photoAlbum.photo)
                .fit().centerCrop()
                .placeholder(ColorDrawable(Color.RED))
                .error(ColorDrawable(Color.RED))
                .into(holder.photoImageView)
            holder.titleTextView.text = photoAlbum.title
            holder.infoTextView.text = context.resources.getQuantityString(
                R.plurals.album_size_template, photoAlbum.size, photoAlbum.size)

            holder.itemView.setOnClickListener { onItemClickListener?.invoke(photoAlbum) }
            holder.itemView.setOnLongClickListener { onItemLongClickListener?.invoke(); true }
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
            val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            val infoTextView: TextView = itemView.findViewById(R.id.infoTextView)
        }

    }

}