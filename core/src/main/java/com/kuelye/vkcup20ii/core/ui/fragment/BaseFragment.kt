package com.kuelye.vkcup20ii.core.ui.fragment

import android.content.Context
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.ANY
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.CACHE
import com.kuelye.vkcup20ii.core.ui.activity.BaseVKActivity
import com.kuelye.vkcup20ii.core.ui.activity.OnLoginListener
import com.kuelye.vkcup20ii.core.ui.view.Toolbar

open class BaseFragment : Fragment(), OnLoginListener {

    companion object {
        const val COUNT_PER_PAGE_DEFAULT = 10
    }

    protected var pagesCount = 1
    protected var countPerPage: Int = COUNT_PER_PAGE_DEFAULT

    val toolbar: Toolbar?
        get() = if (activity is BaseVKActivity) (activity as BaseVKActivity).toolbar else null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is BaseVKActivity) (activity as BaseVKActivity).onLoginListeners.add(this)
    }

    override fun onResume() {
        super.onResume()
        requestData(CACHE)
    }

    override fun onDetach() {
        if (activity is BaseVKActivity) (activity as BaseVKActivity).onLoginListeners.remove(this)
        super.onDetach()
    }

    @CallSuper
    override fun onLogin() {
        pagesCount = 1
    }

    fun show(fragment: BaseFragment) {
        if (activity is BaseVKActivity) (activity as BaseVKActivity).show(fragment)
    }

    protected open fun requestData(source: Source = ANY) {}

}