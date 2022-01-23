package com.example.controlebluetootharduino.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.controlebluetootharduino.R

class ConnectBluetoothDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity?.layoutInflater?.inflate(R.layout.scan_bluetooth,null)



        return super.onCreateDialog(savedInstanceState)
    }
}