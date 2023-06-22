package com.example.aadityaassignment_infraveo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.aadityaassignment_infraveo.databinding.ActivityHomeBinding
import com.example.zocnutassignment1.ItemViewClick
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity(), ItemViewClick {

    var captureCount=0
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var latitudeLabel: String? = null
    private var longitudeLabel: String? = null
    private var latitudeText: TextView? = null
    private var longitudeText: TextView? = null

    var handler: Handler = Handler()
    var runnable: Runnable? = null
//    var delay = 900000
    var delay = 60000

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latitudeLabel = resources.getString(R.string.latitudeBabel)
        longitudeLabel = resources.getString(R.string.longitudeBabel)
        latitudeText = findViewById<View>(R.id.latitudeText) as TextView
        longitudeText = findViewById<View>(R.id.longitudeText) as TextView
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.tvFrequency.text=(delay/60000).toString()

//        currentTV = findViewById(R.id.idTVCurrent)




        setDateTime()
        ConnectivityCheck()
        BatteryChargingCheck()
        BatteryPercentageCheck()
        GetLocation()

        binding.btnManualRefresh.setOnClickListener {
            setDateTime()
            ConnectivityCheck()
            BatteryChargingCheck()
            BatteryPercentageCheck()
            GetLocation()

        }




    }

    private fun setDateTime() {
        val sdf = SimpleDateFormat("''dd-MM-yyyy ''HH:mm:ss")

        val currentDateAndTime = sdf.format(Date())

        binding.tvDateTime.text = currentDateAndTime
    }

    override fun onResume() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())

            setDateTime()
            ConnectivityCheck()
            BatteryChargingCheck()
            BatteryPercentageCheck()
            GetLocation()

            captureCount=captureCount+1
            binding.tvCaptureCount.text=captureCount.toString()

            Toast.makeText(this@HomeActivity, "Refreshed in every ${(delay/60000).toString()} minutes", Toast.LENGTH_SHORT).show()
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }

    private fun GetLocation() {


    }


    public override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions()
            }
        }
        else {
            getLastLocation()
        }
    }
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient?.lastLocation!!.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                latitudeText!!.text = ""+(lastLocation)!!.latitude
                longitudeText!!.text = ""+(lastLocation)!!.longitude
            }
            else {
                Log.w(TAG, "getLastLocation:exception", task.exception)
                showMessage("No location detected. Make sure location is enabled on the device.")
            }
        }
    }
    private fun showMessage(string: String) {
        val container = findViewById<View>(R.id.linearLayout)
        if (container != null) {
            Toast.makeText(this@HomeActivity, string, Toast.LENGTH_LONG).show()
        }
    }
    private fun showSnackbar(
        mainTextStringId: String, actionStringId: String,
        listener: View.OnClickListener
    ) {
        Toast.makeText(this@HomeActivity, mainTextStringId, Toast.LENGTH_LONG).show()
    }
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }
    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@HomeActivity,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar("Location permission is needed for core functionality", "Okay",
                View.OnClickListener {
                    startLocationPermissionRequest()
                })
        }
        else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLastLocation()
                }
                else -> {
                    showSnackbar("Permission was denied", "Settings",
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                Build.DISPLAY, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }







    private fun BatteryChargingCheck() {

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }

        // isCharging if true indicates charging is ongoing and vice-versa
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        // Display whatever the state in the form of a Toast
        if(isCharging) {
            binding.tvBatteryCharging.text="ON"
        } else {
            binding.tvBatteryCharging.text="OFF"
        }
    }

    private fun BatteryPercentageCheck() {
        val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
        // Get the battery percentage and store it in a INT variable
        val batLevel:Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        // Display the variable using a Toast
        binding.tvBatteryCharge.text="$batLevel%".toString()
//        Toast.makeText(applicationContext,"Battery is $batLevel%",Toast.LENGTH_LONG).show()
    }


    private fun ConnectivityCheck() {
        if (checkForInternet(this)) {
            binding.tvConnectivity.text="ON"
//                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()

        } else {
            binding.tvConnectivity.text="OFF"
//                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkForInternet(context: Context): Boolean {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }


    override fun viewItems(name: String, type: String, price: Int) {

    }
}
