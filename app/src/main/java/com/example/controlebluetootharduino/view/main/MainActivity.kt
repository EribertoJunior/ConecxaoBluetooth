package com.example.controlebluetootharduino.view.main

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.controlebluetootharduino.R
import com.example.controlebluetootharduino.databinding.ActivityMainBinding
import com.example.controlebluetootharduino.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()

    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initObserver()

        binding.listDevice.apply {
            adapter = MainAdapter(){
                viewModel.connectBluetoothDevice(it)
            }
        }
        checkBluetoothIsEnabled()

        binding.fabBluetooth.setOnClickListener {
            if (!checkBluetoothIsEnabled()) {
                resultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }

        binding.button.setOnClickListener {
            if (isConnected) {
                viewModel.sendMessage("abc")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnect()
    }

    private fun initObserver() {
        viewModel.bluetoothIsConnected().observe(this) {
            isConnected = it
            if (isConnected) {
                binding.fabBluetooth.setImageResource(R.drawable.bluetooth_connect)
                Log.i(TAG, "Bluetooth connected")
            } else {
                binding.fabBluetooth.setImageResource(R.drawable.bluetooth)
                Log.i(TAG, "Bluetooth disconnected")
            }
        }
    }

    private fun checkBluetoothIsEnabled(): Boolean {
        val bluetoothIsEnabled = viewModel.bluetoothIsEnabled()
        if (bluetoothIsEnabled) {
            Log.i(TAG, "Bluetooth enabled")
            binding.fabBluetooth.setImageResource(R.drawable.bluetooth)
            setListPairedDevices()
        } else {
            Log.i(TAG, "Bluetooth disenabled")
            binding.fabBluetooth.setImageResource(R.drawable.bluetooth_off)
        }
        return bluetoothIsEnabled
    }

    override fun onStart() {
        super.onStart()
        resultLauncher = activityResultLauncher()
    }

    private fun activityResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            var msg = ""
            when (result.resultCode) {
                RESULT_OK -> {
                    msg = "Bluetooth Ativado"
                    Log.i(TAG, msg)
                    binding.fabBluetooth.setImageResource(R.drawable.bluetooth)
                    setListPairedDevices()
                }
                RESULT_CANCELED -> {
                    msg = "Falha ao ativar o Bluetooth"
                    Log.i(TAG, msg)
                    binding.fabBluetooth.setImageResource(R.drawable.bluetooth_off)
                }
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setListPairedDevices() {
        (binding.listDevice.adapter as MainAdapter).dataSet(viewModel.getPairedDevices())
    }

    private val TAG: String = "MainActivity"
}
