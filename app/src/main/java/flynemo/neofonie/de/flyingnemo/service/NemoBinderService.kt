package flynemo.neofonie.de.flyingnemo.service

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.ParcelUuid
import flynemo.neofonie.de.flyingnemo.ux.DeviceListAdapter


/**
 * Created by mdabrowski on 26/02/16.
 */

class NemoService() : Service() {

    val NEMO_SERVICE_UUID = ParcelUuid.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    val CHARACTERISTIC_RX_UUUID = ParcelUuid.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    val CHARACTERISTIC_TX_UUUID = ParcelUuid.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");

    lateinit var bleManager: BluetoothManager
    lateinit var bleAdapter: BluetoothAdapter
    var scanCallback: ScanCallback? = null
    lateinit var bleScanner: BluetoothLeScanner
    var gatt: BluetoothGatt? = null


    var adapter: DeviceListAdapter = DeviceListAdapter()

    override fun onCreate() {
        super.onCreate()
        bleManager = getSystemService(BLUETOOTH_SERVICE)!! as BluetoothManager
        bleAdapter = bleManager.adapter!!
        bleScanner = bleAdapter.bluetoothLeScanner

    }

    override fun onBind(p0: Intent?): IBinder? {
        stopScanning() // kick everybody else
        return LocalBinder();
    }


    inner class LocalBinder() : Binder() {
        fun getNemoService(): NemoService {
            return this@NemoService;
        }
    }


    fun startScanning(callback: (ScanResult?) -> Unit) {

        bleScanner = bleAdapter.bluetoothLeScanner
        bleScanner.stopScan(scanCallback)

        val bleFilterBuilder = ScanFilter.Builder().setServiceUuid(NEMO_SERVICE_UUID)
        val filters = arrayListOf(bleFilterBuilder.build())
        val scanSettingsBuilder = ScanSettings.Builder()
        val scanSettings = scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                callback(result)
            }
        }

        bleScanner.startScan(filters, scanSettings, scanCallback)
    }

    fun stopScanning() {
        bleScanner.stopScan(scanCallback)
    }


    fun connect(device: BluetoothDevice, onConnected: () -> Unit) {
        val bleCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    this@NemoService.gatt = gatt!!
                    gatt.discoverServices()
                } else {
                    this@NemoService.gatt = null
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                onConnected()
            }
        }
        device.connectGatt(this, true, bleCallback)
    }


    fun sendCommand(command: Char) {
        if (gatt != null) {
            val service: BluetoothGattService = gatt!!.getService(NEMO_SERVICE_UUID.uuid)!!
            val characteristic: BluetoothGattCharacteristic = service.getCharacteristic(CHARACTERISTIC_TX_UUUID.uuid)!!
            characteristic.setValue(byteArrayOf(command.toByte()))
            gatt!!.writeCharacteristic(characteristic)
        }

    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
    }


}
