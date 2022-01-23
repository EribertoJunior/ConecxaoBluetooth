import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.appcompat.app.AppCompatActivity
import com.example.controlebluetootharduino.bloetooth.BluetoothController
import com.example.controlebluetootharduino.bloetooth.BluetoothControllerImp
import com.example.controlebluetootharduino.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var appModule = module {

    single<BluetoothAdapter> {
        val bluetoothManager =
            androidApplication().getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    factory<BluetoothController> { BluetoothControllerImp(get(), androidApplication()) }

    viewModel { MainViewModel(get()) }
}