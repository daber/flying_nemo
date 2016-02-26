package flynemo.neofonie.de.flyingnemo.ux

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.WindowManager
import android.widget.Toast
import flynemo.neofonie.de.flyingnemo.R
import flynemo.neofonie.de.flyingnemo.service.NemoService

class SelectDeviceActivity() : AppCompatActivity() {

    private val REQUEST_CODE = 1000

    lateinit var mRecycler: RecyclerView
    lateinit var nemo: NemoService
    lateinit var powerService: PowerManager

    var adapter: DeviceListAdapter = DeviceListAdapter();
    val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            nemo = (p1 as NemoService .LocalBinder).getNemoService()
            nemo.startScanning { adapter.addScanResult(it!!) }
        }
        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        checkPermissionsRequestIfNeeded()
        mRecycler = findViewById(R.id.recyclerView) as RecyclerView

        mRecycler.layoutManager = LinearLayoutManager(this)
        mRecycler.adapter = adapter

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }


    override fun onResume() {
        super.onResume()
        adapter.clearList()
        adapter.itemSelectedListener = { scanResult: ScanResult ->
            Toast.makeText(this, "Connecting to device " + scanResult.device, Toast.LENGTH_SHORT).show()
            val i = Intent().setClass(this, ConnectDeviceActivity::class.java)
            i.putExtra(ConnectDeviceActivity.EXTRA_DEVICE, scanResult.device)
            startActivity(i)
        }


    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        nemo.stopScanning()
        unbindService(connection)
    }




    private fun checkPermissionsRequestIfNeeded() {
        val hasBluetooth = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        val hasBluetoothAdmin = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if ( booleanArrayOf(hasBluetooth, hasBluetoothAdmin, hasCoarseLocation).any { !it }) {
            val permissions = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION)
            requestPermissions(permissions, REQUEST_CODE)
        } else {
            bindNemoService()
        }
    }

    private fun bindNemoService() {
        val i = Intent()
        i.setClass(this, NemoService::class.java)
        bindService(i,connection, Context.BIND_AUTO_CREATE)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            bindNemoService()
        } else {
            Toast.makeText(this, "Insufficient permissions", Toast.LENGTH_LONG).show()
            finish();
        }
    }
}
