package com.example.controlebluetootharduino.bloetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.coroutineContext

class BluetoothControllerImp(private val bluetoothAdapter: BluetoothAdapter, val context: Context) :
    BluetoothController {
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var connectThread: ConnectThread? = null
    private var connectedThread: MyBluetoothService.ConnectedThread? = null

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

                Log.d(TAG, "socket connected!!!")
                while (true) {
                    try {
                        bluetoothIn?.let {
                            Log.d(TAG, "it.available(): " + it.available())
                            if (it.available() > 0) {
                                bluetoothIn.read(buffer, 0, it.available())
                                val result = String(buffer)
                                Log.d(TAG, "result: $result")

                                if(result.contains(":") and result.isNotEmpty()){
                                    Log.d(TAG, "result Completo: ${result.substringBefore(":")}")
                                    //val peso = result.substringBefore(":").toFloat()
                                    //Log.d(">>", peso.toString())
                                    //if(peso > 0){
                                    //callback.onComplete(result.substringBefore(":"))
                                    //}
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
        // connectThread = ConnectThread(bluetoothDevice)
        // connectThread?.start()
    }

    override fun disconnectBluetoothDevice() {
        mSocket?.close()
        Log.i(TAG, "Device disconnect")
    }

    override fun isConnected(): Boolean {
        return mSocket?.isConnected == true
    }

    override fun isEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    override fun disconnect() {
        connectThread?.cancel()
    }

    override fun sendMessage(mensagem: String) {
        if(isConnected())
            mSocket?.outputStream?.write(mensagem.toByteArray())

        // if (connectThread?.isBluetoothConnected() == true){
        //     connectedThread?.write(mensagem.toByteArray())
        // }
    }

    fun scanDevice() {
        val listDevice: ArrayList<BluetoothDevice> = arrayListOf()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        // Processo de discovery inicializado.
                        Log.i(TAG, "Processo de discovery inicializado.")
                        listDevice.clear()
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            ?.let {
                                if (!it.name.isNullOrBlank()) {
                                    Log.i(TAG, "Device encontrado: ${it.name}")
                                    listDevice.add(it)
                                }
                            }

                        // Encontrou um dispositivo
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.i(TAG, "Processo de discovery finalizado.")
                        // Processo de discovery finalizado.
                        //onScanDeviceCompleted.onScanDeviceComplited(listDevice)
                        context.unregisterReceiver(this)
                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filter)

        /*if (bluetoothAdapter.isDiscovering) {
            Log.i(TAG, "Scan already in progress")
            bluetoothAdapter.cancelDiscovery()
        }*/
        // iniciar a ação de encontrar dispositivos, isso pode demorar em torno de 12
        // segundos para finalizar.

        //resultLauncher.launch(Intent(BluetoothAdapter.))
        bluetoothAdapter.startDiscovery()
    }

    private inner class ConnectThread(val device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

        override fun run() {
            // Cancele a descoberta porque, de outra forma, torna a conexão mais lenta.
            bluetoothAdapter.cancelDiscovery()

            mmSocket.use { socket ->
                // Conecte-se ao dispositivo remoto através do soquete. Essa chamada é
                // bloqueada até que seja bem-sucedida ou lance uma exceção.
                try {
                    socket.connect()
                    manageMyConnectedSocket(socket)
                    Log.i(TAG,"socket.isConnected = ${socket.isConnected}")
                } catch (e:IOException) {
                    e.message?.let { Log.e("", it) }
                    try {
                        Log.e("","trying fallback...")

                        //socket =  device.get.getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                        socket.connect();

                        Log.e("","Connected");
                    }
                    catch (e2: Exception) {
                        Log.e("", "Couldn't establish Bluetooth connection!");
                    }
                }

                // A tentativa de conexão foi bem-sucedida. Execute o trabalho associado
                // à conexão em um thread separado.
                //manageMyConnectedSocket(socket)
            }
        }

        // Fecha o soquete do cliente e faz com que o encadeamento termine.
        fun cancel() {
            try {
                mmSocket.let {
                    it.close()
                    Log.i(TAG, "Device disconnect")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }

        fun isBluetoothConnected(): Boolean? = mmSocket?.isConnected

        private fun manageMyConnectedSocket(socket: BluetoothSocket) {

            val message = Message.obtain()
            message.what = MESSAGE_CONNECTED
            handler.sendMessage(message)

            connectedThread = MyBluetoothService(handler).ConnectedThread(socket)
            connectedThread?.start()
        }

        private val handler = object:  Handler(Looper.getMainLooper()) {

            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    //MESSAGE_CONNECTED -> myTextView?.text = "Connected"
                    //MESSAGE_CONNECTION_FAIL -> myTextView?.text = "Connection Failed"
                    MESSAGE_READ -> {
                        val readBuff = msg.obj as ByteArray
                        val tempMsg = String(readBuff, 0, msg.arg1)
                        Log.i(TAG, "mensagem recebida: $tempMsg")
                    }
                }
            }
        }
    }

    private val TAG: String = javaClass.name
}
