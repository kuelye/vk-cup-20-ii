package com.kuelye.vkcup20ii.d

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.PluralFormat
import android.icu.text.PluralRules
import android.icu.util.ULocale
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.data.BaseRepository.GetItemsResult
import com.kuelye.vkcup20ii.core.data.PhotoRepository
import com.kuelye.vkcup20ii.core.model.photos.VKPhotoAlbum
import com.kuelye.vkcup20ii.core.ui.activity.BaseRecyclerActivity
import com.kuelye.vkcup20ii.core.ui.misc.SpaceItemDecoration
import com.kuelye.vkcup20ii.core.ui.view.MenuView
import com.kuelye.vkcup20ii.core.utils.dimen
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.utils.VKUtils
import kotlinx.android.synthetic.main.activity_albums.*
import java.text.MessageFormat
import java.util.*
import kotlin.math.floor

class AlbumsActivity : BaseRecyclerActivity<VKPhotoAlbum, AlbumsActivity.Adapter>() {

    companion object {
        private val TAG = AlbumsActivity::class.java.simpleName
        private const val EDIT_MENU_ITEM_ID = 0
        private const val ADD_MENU_ITEM_ID = 1
    }

    init {
        Config.scopes = listOf(VKScope.PHOTOS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_albums)
        initializeLayout()
    }

    override fun onResume() {
        super.onResume()
        toolbar.setMenu(listOf(MenuView.Item(R.drawable.ic_edit_outline_28, EDIT_MENU_ITEM_ID),
            MenuView.Item(R.drawable.ic_add_outline_28, ADD_MENU_ITEM_ID)))
    }

    override fun requestData(onlyCache: Boolean) {
        PhotoRepository.getPhotoAlbums(
            (pagesCount - 1) * countPerPage, pagesCount * countPerPage, onlyCache,
            object : VKApiCallback<GetItemsResult<VKPhotoAlbum>> {
                override fun success(result: GetItemsResult<VKPhotoAlbum>) {
                    showData(result.items, result.items.size != result.totalCount)
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun fail(error: Exception) {
                    Log.e(TAG, "requestData>fail", error) // TODO
                }
            })
    }

    override fun initializeLayout() {
        val paddingStandard = dimen(this, R.dimen.padding_standard)
        val space = dimen(this, R.dimen.padding_standard_three_quarters)
        val itemMinWidth = dimen(this, R.dimen.albums_item_min_width)
        val totalWidth = (VKUtils.width(this) - paddingStandard * 2).toFloat()
        val spanCount = floor((totalWidth - space) / (itemMinWidth + space)).toInt()
        val itemWidth = ((totalWidth - (spanCount - 1) * space) / spanCount).toInt()
        adapter = Adapter(this, itemWidth)
        layoutManager = GridLayoutManager(this, spanCount)
        super.initializeLayout()
        recyclerView.addItemDecoration(SpaceItemDecoration(space, space, spanCount))
    }

    class Adapter(
        context: Context,
        private val itemWidth: Int
    ) : BaseRecyclerActivity.BaseAdapter<VKPhotoAlbum>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ITEM_VIEW_VALUE -> ItemViewHolder(layoutInflater.inflate(
                    R.layout.layout_album_item, parent, false))
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
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
            val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            val infoTextView: TextView = itemView.findViewById(R.id.infoTextView)
        }

    }

}