package com.kuelye.vkcup20ii.b.ui.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.kuelye.vkcup20ii.b.ui.misc.MarkerRenderer
import com.kuelye.vkcup20ii.b.ui.misc.BaseMarkerHolder
import com.kuelye.vkcup20ii.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_group_map.*

abstract class BaseMapFragment<T : BaseMarkerHolder> : BaseFragment(), OnMapReadyCallback,
    ClusterManager.OnClusterItemClickListener<T> {

    companion object {
        private val TAG = BaseMapFragment::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 99
    }

    protected var map: GoogleMap? = null
    protected var clusterManager: ClusterManager<T>? = null
    protected var clusterRenderer: MarkerRenderer<T>? = null
    protected val markers: SparseArray<T> by lazy { SparseArray<T>() }

    abstract fun requestData()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
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
        mapView?.onDestroy()
        super.onDestroy()
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

    override fun onClusterItemClick(marker: T): Boolean {
        return false
    }

    private fun initializeMap() {
        checkLocationPermission()

        clusterManager = ClusterManager(context, map!!)
        map!!.setOnCameraIdleListener(clusterManager)
        map!!.setOnMarkerClickListener(clusterManager)

        clusterRenderer = MarkerRenderer(context!!, map!!, clusterManager!!)
        clusterManager!!.renderer = clusterRenderer
        clusterManager!!.setOnClusterClickListener { cluster ->
            val boundsBuilder = LatLngBounds.builder()
            for (marker in cluster.items) {
                boundsBuilder.include(marker.position)
            }
            map!!.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
            true
        }
        clusterManager!!.setOnClusterItemClickListener(this)

        requestData()
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

}