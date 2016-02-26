package flynemo.neofonie.de.flyingnemo.ux

import android.bluetooth.le.ScanResult
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import flynemo.neofonie.de.flyingnemo.R
import java.util.*
import java.util.concurrent.TimeUnit

class DeviceListAdapter : RecyclerView.Adapter<DeviceVH>() {
    val TIMEOUT = TimeUnit.SECONDS.toNanos(10)
    var itemSelectedListener: (scanResult: ScanResult) -> Unit = {}
    val mapList: MutableMap<String, ScanResult> = HashMap()
    val list: MutableList<ScanResult> = ArrayList()

    fun addScanResult(scanResult: ScanResult) {
        val oldVal = mapList.get(scanResult.device.address)
        mapList.put(scanResult.device.address, scanResult)
        val index = list.indexOfFirst { it == oldVal }
        if (index != -1) {
            list.set(index, scanResult)
            notifyItemChanged(index)
        } else {
            list.add(scanResult)
            notifyItemInserted(list.size - 1)
        }


    }

    fun removeScanResult(scanResult: ScanResult) {
        val scan = mapList.remove(scanResult.device.address)
        if ( scan != null) {
            val index = list.indexOfFirst { it.device.address == scanResult.device.address }
            if (index != -1) {
                list.removeAt(index)
                notifyItemRemoved(index)
            }
        }

    }


    fun clearList() {
        list.clear()
        mapList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): DeviceVH? {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.li_device_entry, viewGroup, false);
        val deviceName = v.findViewById(R.id.deviceName) as TextView
        val deviceMac = v.findViewById(R.id.deviceMac) as TextView
        val deviceRsii = v.findViewById(R.id.deviceRsii) as TextView
        val vh = DeviceVH(v, deviceName, deviceMac, deviceRsii)
        return vh
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(viewHolder: DeviceVH, position: Int) {
        var scanResult = list[position]
        viewHolder.deviceName.text = scanResult.device.name ?: "<Unknown>"
        viewHolder.deviceMac.text = scanResult.device.address
        viewHolder.deviceRssi.text = "Rssi:${scanResult.rssi}db"
        viewHolder.scanResult = scanResult
        viewHolder.itemView.setOnClickListener({ itemSelectedListener(scanResult) })
    }

}

