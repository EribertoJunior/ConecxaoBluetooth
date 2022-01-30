@file:Suppress("BlockingMethodInNonBlockingContext")

package com.example.controlebluetootharduino.bloetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class BluetoothControllerImp(private val bluetoothAdapter: BluetoothAdapter, private val context: Context) :
    BluetoothController {
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mSocket: BluetoothSocket? = null

    override fun getPairedDevices(): Set<BluetoothDevice>? {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

        bluetoothAdapter.cancelDiscovery()
        return pairedDevices
    }

    override suspend fun connectBluetoothDevice(bluetoothDevice: BluetoothDevice) {
        withContext(Dispatchers.IO){
            try {
                mSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid)
                mSocket?.connect()

                val bluetoothIn = mSocket?.inputStream
                val buffer = ByteArray(256)

                Log.d(tag, "socket connected!!!")
                while (true) {
                    try {
                        bluetoothIn?.let {
                            Log.d(tag, "it.available(): " + it.available())
                            if (it.available() > 0) {
                                bluetoothIn.read(buffer, 0, it.available())
                                val result = String(buffer)
                                Log.d(tag, "result: $result")

                                if(result.contains(":") and result.isNotEmpty()){
                                    Log.d(tag, "result Completo: ${result.substringBefore(":")}")
                                }
                            }
                        }
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun disconnectBluetoothDevice() {
        mSocket?.close()
        Log.i(tag, "Device disconnect")
    }

    override fun isConnected(): Boolean {
        return mSocket?.isConnected == true
    }

    override fun isEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    override fun sendMessage(mensagem: String) {
        if(isConnected())
            mSocket?.outputStream?.write(mensagem.toByteArray())
    }

    override fun sendAcceleration(acceleration: Int) {
        if(isConnected())
            mSocket?.outputStream?.write(acceleration)
    }

    fun scanDevice() {
        val listDevice: ArrayList<BluetoothDevice> = arrayListOf()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        // Processo de discovery inicializado.
                        Log.i(tag, "Processo de discovery inicializado.")
                        listDevice.clear()
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            ?.let {
                                if (!it.name.isNullOrBlank()) {
                                    Log.i(tag, "Device encontrado: ${it.name}")
                                    listDevice.add(it)
                                }
                            }

                        // Encontrou um dispositivo
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.i(tag, "Processo de discovery finalizado.")
                        // Processo de discovery finalizado.
                        //onScanDeviceCompleted.onScanDeviceComplited(listDevice)
                        context.unregisterReceiver(this)
                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(receiver, filter)

        bluetoothAdapter.startDiscovery()
    }

    private val tag: String = javaClass.name
}
