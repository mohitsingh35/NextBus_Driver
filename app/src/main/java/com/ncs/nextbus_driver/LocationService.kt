package com.ncs.nextbus_driver

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class LocationService: Service() {
    private lateinit var viewModel: ViewModel
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private lateinit var databaseReference:DatabaseReference
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        databaseReference=Firebase.database.reference.child("data")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val busData = intent?.getSerializableExtra("busDetails") as? BusData
        val driverData = intent?.getSerializableExtra("driverDetails") as? DriverDataResponse

        when(intent?.action) {
            ACTION_START -> start(busData!!,driverData!!)
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(busData: BusData,driverData: DriverDataResponse)  {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(5000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val updatedNotification = notification.setContentText(
                    "Location: ($lat, $long)"
                )
                notificationManager.notify(1, updatedNotification.build())
                val locationData = RealtimeDB.locationData(
                    accuracy = location.accuracy.toDouble(),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    speed = location.speed.toDouble(),
                    time = location.time,
                    busNum = busData.item?.busNum!!,
                    busID =  busData.key!!,
                    fueltype = busData.item?.fueltype!!,
                    start=busData.item?.start!!,
                    destination=busData.item?.destination!!,
                    driverName=driverData.item?.name!!,
                    driverphNum=driverData.item?.phNum!!,
                    driverprofilepic= driverData.item?.profilepic?.get(0)!!,
                    driverId=driverData.item?.userId!!,
                    busName = busData.item.name!!
                )

                val locationKey = busData.key
                val busLocationReference = databaseReference.child("bus_location")
                busLocationReference.child(locationKey!!).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            busLocationReference.child(locationKey!!).setValue(locationData)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        } else {
                            busLocationReference.child(locationKey).setValue(locationData)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
