package com.example.labbluetoothheartrate

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.handler.BleWrapper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var isScanning = false
    private val mScanCallback = BtleScanCallback()
    val heartRateList: MutableList<Int> = mutableListOf()

    companion object {
        const val SCAN_PERIOD: Long = 3000
        private var mScanResults: MutableList<ScanResult> = mutableListOf()
        private lateinit var listAdapter: BluetoothDeviceListAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!hasPermissions()) return

        listAdapter = BluetoothDeviceListAdapter(this, mScanResults)
        deviceListView.adapter = listAdapter
        scanButton.setOnClickListener {
            if (isScanning) {
                stopScan()
                return@setOnClickListener
            }
            startScan()
        }

        heartRateButton.setOnClickListener {
            if (heartRateList.size == 0) return@setOnClickListener

            val intent = Intent(this, HeartRateGraphActivity::class.java).apply {
                putExtra("heartRateList", heartRateList.toIntArray())
            }
            startActivity(intent)
        }
    }

    private fun hasPermissions(): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.d("noPermissions", "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("noFineLocation", "No fine location access")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
            return true
        }
        return true
    }

    private fun stopScan() {
        isScanning = false
        scanButton.text = getString(R.string.start_scan_button)
        bluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
    }

    private fun startScan() {
        scanButton.text = getString(R.string.stop_scan_button)
        Log.d("startScan", "Scan start")

        mScanResults.clear()
        val mBluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
        val filter: List<ScanFilter>? = null
        val mHandler = Handler()
        mHandler.postDelayed({ stopScan() }, SCAN_PERIOD)
        isScanning = true
        mBluetoothLeScanner!!.startScan(filter, settings, mScanCallback)
    }

    private class BtleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d("onScanFailed", "BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceAddress = device.address
            mScanResults.add(result)
            listAdapter.notifyDataSetChanged()
            Log.d("addScanResult", "Device: ${device.name} with address $deviceAddress (${result.isConnectable})")
        }
    }
}