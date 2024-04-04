package com.example.beaconble


import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.beaconble.BeaconScanPermissionsActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onResume(){
        super.onResume()

        if(!BeaconScanPermissionsActivity.allPermissionsGranted(this,true)){
            val intent = Intent (this, BeaconScanPermissionsActivity::class.java)
            intent.putExtra("backgroundAccessRequested", true)
            startActivity(intent)
        }

    }

}