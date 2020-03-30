package com.example.testmap

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
                .snippet(
                    "Guess how long this could contain?\n" +
                            "Maybe just one row?"
                )
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        mMap.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this))

        locationInit()
        addLocationListener()
    }

    override fun onResume() {
        super.onResume()

        permissionCheck(cancel = {
            showPermissionInfoDialog()
        }, ok = {
            // good
        })
    }

    private fun addLocationListener(){
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: MyLocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private fun locationInit() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationCallback = MyLocationCallback()
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    }

    private fun permissionCheck(cancel: () -> Unit, ok: () -> Unit) {
        // check permission for location
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // has denied before
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                cancel()
                // request the permission
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1000
                )
            }
            // when granted
        } else {
            ok()
        }

    }

    private fun showPermissionInfoDialog() {
        alert("Need a permission for location", "reason") {
            yesButton {
                ActivityCompat.requestPermissions(
                    this@MapsActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1000
                )
            }
            noButton {
                System.exit(0)
            }
        }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if ((grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // granted
                    addLocationListener()
                } else {
                    toast("denied")
                }
            }
        }
    }

    inner class MyLocationCallback : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)

            var location = p0?.lastLocation

            location?.run {
                var currentLocation = LatLng(latitude, longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(currentLocation)
                        .title("My Current Location!")
                        .snippet(
                            "Latitude : $latitude\n" +
                                    "Longitude : $longitude"
                        )

                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f))
            }
        }
    }
}
