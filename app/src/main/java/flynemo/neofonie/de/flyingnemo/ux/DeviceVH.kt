package flynemo.neofonie.de.flyingnemo.ux

import android.bluetooth.le.ScanResult
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

/**
 * Created by mdabrowski on 08/02/16.
 */

class DeviceVH(itemView: View, val deviceName: TextView, val deviceMac:TextView, val deviceRssi:TextView) : RecyclerView.ViewHolder(itemView) {
     var scanResult:ScanResult? = null
}