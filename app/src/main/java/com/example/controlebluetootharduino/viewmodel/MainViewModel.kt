package com.example.controlebluetootharduino.viewmodel

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlebluetootharduino.bloetooth.BluetoothController
import kotlinx.coroutines.launch

class MainViewModel(private val bluetoothController: BluetoothController) : ViewModel() {

    fun bluetoothIsConnected(): LiveData<Boolean> {
        val mutableLiveData = MutableLiveData<Boolean>(false)
        mutableLiveData.postValue(bluetoothController.isConnected())
        return mutableLiveData
    }
    fun isConnected() = bluetoothController.isConnected()

    fun bluetoothIsEnabled() = bluetoothController.isEnabled()

    fun getPairedDevices(): List<String>? {
        val pairedDevices = bluetoothController.getPairedDevices()
        pairedDevices?.forEach {
            val name = it.name
            val address = it.address
            Log.i(TAG, "name= $name, address= $address")
        }
        return pairedDevices?.map { it.name }
    }

    fun connectBluetoothDevice(bluetoothDeviceName: String) {
        viewModelScope.launch {
            val pairedDevices = bluetoothController.getPairedDevices()
            val bluetoothDevice: BluetoothDevice? =
                pairedDevices?.first { it.name == bluetoothDeviceName }
            bluetoothDevice?.let { bluetoothController.connectBluetoothDevice(it) }
        }

    }

    fun sendMessage(message: String) {
        if(bluetoothController.isConnected()){
            bluetoothController.sendMessage(message)
        }
    }

    fun disconnect() {
        bluetoothController.disconnectBluetoothDevice()
    }

    private val TAG = javaClass.name
}