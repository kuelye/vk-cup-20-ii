package com.kuelye.vkcup20ii.b.ui.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MapFragment : SupportMapFragment(), OnMapReadyCallback {

    companion object {
        private val TAG = MapFragment::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 99
    }

    private var map: GoogleMap? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map
        checkPermission()
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

    private fun checkPermission() {
        if (context != null
            && checkSelfPermission(context!!, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        } else {
            enableLocation()
        }
    }

    private fun enableLocation() {
        map?.isMyLocationEnabled = true
    }

}