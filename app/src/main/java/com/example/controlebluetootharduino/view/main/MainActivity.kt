package com.example.controlebluetootharduino.view.main

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.controlebluetootharduino.R
import com.example.controlebluetootharduino.databinding.ActivityMainBinding
import com.example.controlebluetootharduino.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()

    private lateinit var sensorManager: SensorManager
    private lateinit var mAcelerometro: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initObserver()

        configListDevices()
        checkBluetoothIsEnabled()

        configFab()

        configButton()
        configSeekBar()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //val sensor: Sensor = sensorManager.getDefaultSensor(Sensor.)

    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, mAcelerometro, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnect()
    }

    override fun onStart() {
        super.onStart()
        resultLauncher = activityResultLauncher()
    }

    private fun configListDevices() {
        binding.listDevice.apply {
            adapter = MainAdapter {
                viewModel.connectBluetoothDevice(it)
            }
        }
    }

    private fun configSeekBar() {
        binding.seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val aceleracao: Int = (p1*255) / 100
                Log.i(TAG, "onProgressChanged: $aceleracao")
                viewModel.sendAcceleration(aceleracao)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                //Log.i(TAG, "onStartTrackingTouch: true")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //Log.i(TAG, "onStopTrackingTouch: true")
            }
        })
    }

    private fun configButton() {
        binding.button.setOnClickListener {
            if (viewModel.isConnected()) {
                viewModel.sendMessage("123")
            }
        }
    }

    private fun configFab() {
        binding.fabBluetooth.setOnClickListener {
            if (!checkBluetoothIsEnabled()) {
                resultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    private fun initObserver() {
        viewModel.bluetoothIsConnected().observe(this) {
            if (it) {
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
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values?.get(0)
            val y = it.values?.get(1)
            val z = it.values?.get(2)

            binding.tvValorX.text = "X ${x.toString()}"
            binding.tvValorY.text = "Y ${y.toString()}"
            binding.tvValorZ.text = "Z ${z.toString()}"

        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }
}
