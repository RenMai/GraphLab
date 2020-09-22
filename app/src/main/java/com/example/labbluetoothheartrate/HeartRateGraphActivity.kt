package com.example.labbluetoothheartrate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_heart_rate_graph.*

class HeartRateGraphActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_graph)

        val data = intent.getIntArrayExtra("heartRateList")
        val dataPoints = Array(data!!.size) { i -> DataPoint(i.toDouble(), data[i].toDouble()) }
        graph.addSeries(LineGraphSeries(dataPoints))
        graph.gridLabelRenderer.horizontalAxisTitle = "Time"
        graph.gridLabelRenderer.verticalAxisTitle = "BPM"
        graph.viewport.setMinY(30.0)
        graph.viewport.setMaxY(150.0)
    }
}