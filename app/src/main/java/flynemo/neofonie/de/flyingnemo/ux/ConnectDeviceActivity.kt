package flynemo.neofonie.de.flyingnemo.ux

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import flynemo.neofonie.de.flyingnemo.R
import flynemo.neofonie.de.flyingnemo.service.NemoService

class ConnectDeviceActivity : AppCompatActivity() {
    companion object {
        val EXTRA_DEVICE = "EXTRA_DEVICE"
        val ALL_BUTTONS_ID = intArrayOf(R.id.left, R.id.right, R.id.up, R.id.down, R.id.idle)
        lateinit var ALL_BUTTONS: List<View>
        val TAG = ConnectDeviceActivity::class.java.simpleName
    }

    var device: BluetoothDevice? = null;
    lateinit var nemo: NemoService

    val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            nemo = (p1 as NemoService.LocalBinder).getNemoService()
            nemo.connect(device!!, { runOnUiThread { setButtonsEnabled(true) } })
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            nemo.connect(device!!, { runOnUiThread { setButtonsEnabled(false) } })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_device)
        ALL_BUTTONS = ALL_BUTTONS_ID.map { findViewById(it) }

        ALL_BUTTONS.forEach {
            it.setOnClickListener({ v: View ->
                sendCommand(v.id)
            })
        }
        device = intent!!.getParcelableExtra(EXTRA_DEVICE)!!

        bindNemo()

    }

    private fun bindNemo() {
        val i = Intent()
        i.setClass(this, NemoService::class.java)
        bindService(i, connection, Context.BIND_AUTO_CREATE)

    }

    private fun sendCommand(id: Int) {
        val cmd = when (id) {
            R.id.left -> 'l'
            R.id.right -> 'r'
            R.id.idle -> 'i'
            R.id.up -> 'u'
            R.id.down -> 'd'
            else -> 'i'
        }
        nemo.sendCommand(cmd)
        Log.i(TAG, "Command send " + cmd)
    }

    fun setButtonsEnabled(b: Boolean) {
        ALL_BUTTONS.forEach { it.isEnabled = b }
    }

    override fun onDestroy() {
        super.onDestroy()
        nemo?.disconnect()
        unbindService(connection)
    }
}
