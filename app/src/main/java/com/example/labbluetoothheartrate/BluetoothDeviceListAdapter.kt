package com.example.labbluetoothheartrate

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.handler.BleWrapper
import kotlinx.android.synthetic.main.activity_main.*

class BluetoothDeviceListAdapter(
    private val activity: MainActivity,
    private val scanDevices: MutableList<ScanResult>
) : BaseAdapter(), BleWrapper.BleCallback {
    private val inflater: LayoutInflater =
        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var mBleWrapper: BleWrapper

    override fun getCount(): Int {
        return scanDevices.size
    }

    override fun getItem(position: Int): Any {
        return scanDevices[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.device_list_item, parent, false)
        val thisDevice = scanDevices[position]
        var textView: TextView = rowView.findViewById(R.id.deviceName)

        textView.text = thisDevice.device.name
        textView = rowView.findViewById(R.id.deviceAddress)
        textView.text = thisDevice.device.address
        textView = rowView.findViewById(R.id.deviceSignal)
        textView.text = thisDevice.rssi.toString()

        rowView.isEnabled = thisDevice.isConnectable
        rowView.setOnClickListener {
            mBleWrapper = BleWrapper(activity, scanDevices[position].device.address)
            mBleWrapper.addListener(this)
            mBleWrapper.connect(false)
        }
        return rowView
    }

    override fun onDeviceReady(gatt: BluetoothGatt) {
        mBleWrapper.getNotifications(
            gatt,
            mBleWrapper.HEART_RATE_SERVICE_UUID,
            mBleWrapper.HEART_RATE_MEASUREMENT_CHAR_UUID
        )
    }

    override fun onDeviceDisconnected() {
        Log.d("onDeviceDisconnected", "Disconnected")
    }

    override fun onNotify(characteristic: BluetoothGattCharacteristic) {
        Log.d("onNotify", "${characteristic.value[1]}")
        activity.heartRateButton.text = characteristic.value[1].toString()
        activity.heartRateList.add(characteristic.value[1].toInt())
    }

    override fun isEnabled(position: Int): Boolean {
        return scanDevices[position].isConnectable
    }
}