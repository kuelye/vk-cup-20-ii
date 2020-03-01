package com.kuelye.vkcup20ii.b.ui.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.util.forEach
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.CameraUpdateFactory.newLatLng
import com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.kuelye.vkcup20ii.b.ui.misc.BaseMarkerHolder
import com.kuelye.vkcup20ii.b.ui.misc.MarkerRenderer
import com.kuelye.vkcup20ii.core.ui.fragment.BaseFragment
import com.kuelye.vkcup20ii.core.ui.view.BottomSheetLayout
import com.kuelye.vkcup20ii.core.ui.view.TouchableView
import kotlinx.android.synthetic.main.fragment_group_map.*

abstract class BaseMapFragment<T : BaseMarkerHolder> : BaseFragment(), OnMapReadyCallback,
    ClusterManager.OnClusterItemClickListener<T> {

    companion object {
        private val TAG = BaseMapFragment::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 99
        private const val MARKERS_CAMERA_PADDING = 100
        private const val MARKERS_LOCATION_ZOOM = 12f

    }

    protected var map: GoogleMap? = null
    protected var clusterManager: ClusterManager<T>? = null
    protected var clusterRenderer: MarkerRenderer<T>? = null
    protected val markers: SparseArray<T> by lazy { SparseArray<T>() }

    private var userTouched: Boolean = false
    private var userScrolled: Boolean = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        (view as BottomSheetLayout).apply {
            onTouchListener = { userTouched = true }
            onScrollListener = { userScrolled = true }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        requestData()
        mapView.onResume()
        initializeCamera()
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
        initializeCamera()
        requestData()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
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
        if (!userScrolled) map?.animateCamera(newLatLng(marker.position))
        return false
    }

    protected fun initializeCamera() {
        //Log.v(TAG, "initializeCamera: userTouched=$userTouched")
        if (map == null || userTouched) return
        if (markers.size() != 0) {
            val boundsBuilder = LatLngBounds.builder()
            for (i in 0 until markers.size()) {
                boundsBuilder.include(markers.get(markers.keyAt(i)).position)
            }
            map!!.animateCamera(newLatLngBounds(boundsBuilder.build(), MARKERS_CAMERA_PADDING))
            return
        }

        if (lastLocation != null) {
            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(lastLocation!!.latitude, lastLocation!!.longitude), MARKERS_LOCATION_ZOOM))
        }
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
            cluster.items.forEach { marker -> boundsBuilder.include(marker.position) }
            map!!.animateCamera(newLatLngBounds(boundsBuilder.build(), MARKERS_CAMERA_PADDING))
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationClient.lastLocation.addOnSuccessListener {
            lastLocation = it
            initializeCamera()
        }
    }

}