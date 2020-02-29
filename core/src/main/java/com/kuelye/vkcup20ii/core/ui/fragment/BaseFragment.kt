package com.kuelye.vkcup20ii.core.ui.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import com.kuelye.vkcup20ii.core.ui.activity.BaseActivity
import com.kuelye.vkcup20ii.core.ui.activity.OnLoginListener
import com.kuelye.vkcup20ii.core.ui.view.Toolbar

open class BaseFragment : Fragment(), OnLoginListener {

    val toolbar: Toolbar?
        get() = if (activity is BaseActivity) (activity as BaseActivity).toolbar else null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is BaseActivity) (activity as BaseActivity).onLoginListeners.add(this)
    }

    override fun onDetach() {
        if (activity is BaseActivity) (activity as BaseActivity).onLoginListeners.remove(this)
        super.onDetach()
    }

    override fun onLogin() {
        // stub
    }

    fun show(fragment: BaseFragment) {
        if (activity is BaseActivity) (activity as BaseActivity).show(fragment)
    }

}