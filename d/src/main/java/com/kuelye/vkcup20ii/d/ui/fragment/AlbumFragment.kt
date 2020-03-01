package com.kuelye.vkcup20ii.d.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.core.data.BaseRepository
import com.kuelye.vkcup20ii.core.data.BaseRepository.ItemsResult
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.CACHE
import com.kuelye.vkcup20ii.core.data.PhotoRepository
import com.kuelye.vkcup20ii.core.data.PhotoRepository.RequestPhotosArguments
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.model.photos.VKPhotoAlbum
import com.kuelye.vkcup20ii.core.ui.activity.PhotoActivity
import com.kuelye.vkcup20ii.core.ui.fragment.BaseRecyclerFragment
import com.kuelye.vkcup20ii.core.ui.misc.SpaceItemDecoration
import com.kuelye.vkcup20ii.core.ui.view.MenuView
import com.kuelye.vkcup20ii.core.utils.color
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.d.R
import com.kuelye.vkcup20ii.d.ui.activity.AlbumsActivity.Companion.ADD_MENU_ITEM_ID
import com.kuelye.vkcup20ii.d.ui.activity.AlbumsActivity.Companion.BACK_MENU_ITEM_ID
import com.kuelye.vkcup20ii.d.ui.fragment.AlbumFragment.Adapter
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VKApiCallback

class AlbumFragment : BaseRecyclerFragment<VKPhoto, Adapter>() {

    companion object {
        private val TAG = AlbumFragment::class.java.simpleName
        private const val EXTRA_ALBUM_ID = "ALBUM_ID"
        private const val EXTRA_ALBUM_TITLE = "ALBUM_TITLE"
        private const val EXTRA_ALBUM_SYSTEM = "ALBUM_SYSTEM"
        private const val PICK_PHOTO_REQUEST_CODE = 99

        fun newInstance(album: VKPhotoAlbum): AlbumFragment {
            val fragment = AlbumFragment()
            fragment.arguments = Bundle().apply {
                putInt(EXTRA_ALBUM_ID, album.id)
                putString(EXTRA_ALBUM_TITLE, album.title)
                putBoolean(EXTRA_ALBUM_SYSTEM, album.isSystem)
            }
            return fragment
        }
    }

    init {
        countPerPage = 50
    }

    private val albumId: Int?
        get() = arguments?.getInt(EXTRA_ALBUM_ID)

    private val albumTitle: String?
        get() = arguments?.getString(EXTRA_ALBUM_TITLE)

    private val isSystem: Boolean?
        get() = arguments?.getBoolean(EXTRA_ALBUM_SYSTEM)

    private var photosListener: BaseRepository.Listener<VKPhoto>? = null

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
        subscribePhotos()
        super.onResume()
        updateToolbar()
    }

    override fun onPause() {
        super.onPause()
        unsubscribePhotos()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == PICK_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK && intent != null && intent.data != null) {
                savePhoto(intent.data!!)
            } else {
                // TODO
            }
        } else super.onActivityResult(requestCode, resultCode, intent)
    }

    override fun requestData(source: Source) {
        PhotoRepository.requestPhotos(
            RequestPhotosArguments((pagesCount - 1) * countPerPage, countPerPage, albumId),
            source)
    }

    private fun subscribePhotos() {
        if (photosListener == null) {
            photosListener = object : BaseRepository.Listener<VKPhoto> {
                override fun onNextItems(result: ItemsResult<VKPhoto>) {
                    //Log.v(TAG, "subscribePhotos>success: result=$result")
                    showData(result.items, result.items?.size != result.totalCount)
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun onFail(error: java.lang.Exception) {
                    Log.e(TAG, "subscribeGroups>fail", error) // TODO
                }

                override fun getFilter(): Int? = albumId
            }
        }
        PhotoRepository.photoCache.listeners.add(photosListener!!)
    }

    private fun unsubscribePhotos() {
        if (photosListener != null) {
            PhotoRepository.photoCache.listeners.remove(photosListener!!)
            photosListener = null
        }
    }

    private fun updateToolbar() {
        toolbar?.apply {
            title = albumTitle
            setAlwaysCollapsed(alwaysCollapsed = false, animate = true)
            if (isSystem == true) {
                setMenu(MenuView.Item(R.drawable.ic_back_outline_28, BACK_MENU_ITEM_ID, true))
            } else {
                setMenu(MenuView.Item(R.drawable.ic_back_outline_28, BACK_MENU_ITEM_ID, true),
                    MenuView.Item(R.drawable.ic_add_outline_28, ADD_MENU_ITEM_ID))
            }
            setOnMenuItemClickListener { id ->
                when (id) {
                    BACK_MENU_ITEM_ID -> fragmentManager?.popBackStack()
                    ADD_MENU_ITEM_ID -> pickPhoto()
                }
            }
        }
    }

    private fun pickPhoto() {
        val intent = Intent(ACTION_PICK, EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_PHOTO_REQUEST_CODE)
    }

    private fun savePhoto(photo: Uri) {
        PhotoRepository.savePhoto(context!!, photo, albumId!!, object : VKApiCallback<VKPhoto> {
            override fun success(result: VKPhoto) {
                requestData(CACHE)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "savePhoto>fail", error) // TODO
            }
        })
    }

    override fun initializeLayout() {
        val space = dimen(context!!, R.dimen.padding_standard_eight)
        val (spanCount, itemWidth) = calculateLayout(
            context!!,
            padding = 0,
            space = space,
            itemMinWidth = dimen(context!!, R.dimen.albums_item_min_width)
        )

        layoutManager = GridLayoutManager(context!!, spanCount)
        adapter = Adapter(context!!, itemWidth)
        adapter.onItemClickListener = { photo ->
            PhotoActivity.start(context!!, photo.photo)
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
                    layoutInflater.inflate(R.layout.layout_photo_item, parent, false)
                )
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val photo = items[position]
            if (photo != null) updateItemLayout(holder as ItemViewHolder, photo)
        }

        private fun updateItemLayout(holder: ItemViewHolder, photo: VKPhoto) {
            holder.photoImageView.layoutParams.apply {
                width = itemWidth
                height = itemWidth
            }

            Picasso.get().load(photo.photo)
                .fit().centerCrop()
                .placeholder(ColorDrawable(color(context, R.color.placeholder_color)))
                .error(ColorDrawable(color(context, R.color.placeholder_color)))
                .into(holder.photoImageView)

            holder.itemView.setOnClickListener { onItemClickListener?.invoke(photo) }
            holder.itemView.setOnLongClickListener { onItemLongClickListener?.invoke(); true }
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photoImageView: ImageView = itemView as ImageView
        }

    }

}