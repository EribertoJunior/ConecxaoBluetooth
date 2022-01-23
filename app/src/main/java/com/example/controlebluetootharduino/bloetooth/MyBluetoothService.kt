package com.example.controlebluetootharduino.bloetooth

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val TAG = "MY_APP_DEBUG_TAG"

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
const val MESSAGE_CONNECTED: Int = 3
// ... (Add other message types here as needed.)

class MyBluetoothService(
    // manipulador que obtém informações do serviço Bluetooth
    private val handler: Handler
) {

    inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes retornados de read()

            // Continue ouvindo o InputStream até que ocorra uma exceção.
            while (true) {
                if (mmInStream.available() > 0) {
                    // Leia do InputStream.
                    numBytes = try {
                        mmInStream.read(mmBuffer)
                        Log.d(TAG, "Lendo")
                    } catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }

                    // Envie os bytes obtidos para a atividade da interface do usuário.
                    val readMsg = handler.obtainMessage(
                        MESSAGE_READ, numBytes, -1,
                        mmBuffer)
                    readMsg.sendToTarget()
                }
                else SystemClock.sleep(100);
            }
        }

        // Chame isso da atividade principal para enviar dados para o dispositivo remoto.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Envie uma mensagem de falha de volta para a activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Compartilhe a mensagem enviada com a UI activity.
            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}