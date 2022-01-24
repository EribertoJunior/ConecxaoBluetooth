package com.example.controlebluetootharduino.bloetooth

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData

interface BluetoothController {
    fun getPairedDevices(): Set<BluetoothDevice>?
    suspend fun connectBluetoothDevice(bluetoothDevice: BluetoothDevice)
    fun disconnectBluetoothDevice()
    fun isConnected(): Boolean
    fun isEnabled(): Boolean
    fun sendMessage(mensagem: String)
}