package com.kuelye.vkcup20ii.b.ui.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.b.ui.misc.MarkerHolder
import com.kuelye.vkcup20ii.core.data.GroupRepository
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.kuelye.vkcup20ii.core.model.VKGroup.Field.ADDRESSES
import com.kuelye.vkcup20ii.core.model.VKGroup.Field.DESCRIPTION
import com.kuelye.vkcup20ii.core.ui.fragment.BaseFragment
import com.kuelye.vkcup20ii.core.ui.view.BottomSheetLayout
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException
import kotlinx.android.synthetic.main.fragment_map.*
import java.lang.Exception

class MapFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private val TAG = MapFragment::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 99
        private val GROUP_EXTENDED_FIELDS = arrayOf(DESCRIPTION, ADDRESSES)
    }

    private var map: GoogleMap? = null
    private val markers: SparseArray<MarkerHolder> by lazy { SparseArray<MarkerHolder>() }
    private var selectedMarker: MarkerHolder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        initializeLayout()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        requestData()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map
        initializeMap()
        requestData()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    enableLocation()
                }
            }
        }
    }

    override fun onLogin() {
        super.onLogin()
        requestData()
    }

    private fun initializeLayout() {
        mapView.getMapAsync(this)
        (view as BottomSheetLayout).outsideScrollEnabled = true
    }

    private fun initializeMap() {
        checkLocationPermission()
        map!!.setOnMarkerClickListener { marker ->
            select(marker.tag as MarkerHolder)
            true
        }
    }

    private fun checkLocationPermission() {
        if (context != null
                && checkSelfPermission(context!!, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        } else {
            enableLocation()
        }
    }

    private fun enableLocation() {
        map?.isMyLocationEnabled = true
    }

    private fun requestData() {
        GroupRepository.getGroups(GROUP_EXTENDED_FIELDS, "groups",
                object : VKApiCallback<List<VKGroup>> {
            override fun success(result: List<VKGroup>) {
                updateMarkers(result)
            }

            override fun fail(e: Exception) {
                Log.e(TAG, "requestData>fail", e) // TODO
            }
        })
    }

    private fun updateMarkers(groups: List<VKGroup>) {
        if (map == null) return
        for (group in groups) {
            if (group.addresses.isNullOrEmpty()) continue
            for (address in group.addresses!!) {
                var markerHolder = markers.get(address.id)
                if (markerHolder == null) {
                    val marker = map!!.addMarker(MarkerOptions()
                        .position(address.position).anchor(0.5f, 0.5f))
                    markerHolder = MarkerHolder(marker, group, address)
                    marker.tag = markerHolder
                    markers.put(address.id, markerHolder)
                } else {
                    markerHolder.marker.position = address.position
                    markerHolder.setGroupAddress(group, address)
                }
            }
        }
    }

    private fun select(marker: MarkerHolder? = null) {
        selectedMarker?.selected = false
        selectedMarker = marker
        selectedMarker?.apply {
            selected = true
            (view as BottomSheetLayout).apply {
                animateExpanded(false) {
                    infoView.setGroupAddress(selectedMarker!!.group, selectedMarker!!.address)
                    animateExpanded(true)
                }
            }
        }
    }

}