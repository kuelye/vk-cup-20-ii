package com.kuelye.vkcup20ii.d

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.model.VKAlbum
import com.kuelye.vkcup20ii.core.ui.activity.BaseRecyclerActivity
import com.kuelye.vkcup20ii.core.ui.misc.SpaceItemDecoration
import com.kuelye.vkcup20ii.core.ui.view.MenuView
import com.kuelye.vkcup20ii.core.utils.dimen
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.utils.VKUtils
import kotlinx.android.synthetic.main.activity_albums.*
import kotlin.math.floor
import kotlin.random.Random

class AlbumsActivity : BaseRecyclerActivity<VKAlbum, AlbumsActivity.Adapter>() {

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

    override fun requestData() {
        showData(List(20) { VKAlbum(Random.nextInt()) })
    }

    override fun initializeLayout() {
        val paddingStandard = dimen(this, R.dimen.padding_standard)
        val space = dimen(this, R.dimen.padding_standard_three_quarters)
        val itemMinWidth = dimen(this, R.dimen.albums_item_min_width)
        val totalWidth = (VKUtils.width(this) - paddingStandard * 2).toFloat()
        val spanCount = floor((totalWidth - space) / (itemMinWidth + space)).toInt()
        val itemWidth = ((totalWidth - (spanCount - 1) * space) / spanCount).toInt()
        Log.v(TAG, "GUB $paddingStandard $space $itemMinWidth $totalWidth $spanCount $itemWidth")
        adapter = Adapter(this, itemWidth)
        layoutManager = GridLayoutManager(this, spanCount)
        super.initializeLayout()
        recyclerView.addItemDecoration(SpaceItemDecoration(space, space, spanCount))
    }

    class Adapter(
        context: Context,
        private val itemWidth: Int
    ) : BaseRecyclerActivity.BaseAdapter<VKAlbum>(context) {

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

        private fun updateItemLayout(holder: ItemViewHolder, document: VKAlbum) {
            holder.photoImageView.setImageDrawable(ColorDrawable(Color.RED))
            holder.photoImageView.layoutParams.apply {
                width = itemWidth
                height = itemWidth
            }
            holder.nameTextView.text = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
            holder.infoTextView.text = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            val infoTextView: TextView = itemView.findViewById(R.id.infoTextView)
        }

    }

}