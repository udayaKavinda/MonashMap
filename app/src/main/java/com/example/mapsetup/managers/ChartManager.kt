package com.example.mapsetup.managers

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet

class ChartManager(
    private val lineChart: LineChart,
    private val scatterChart: ScatterChart,
    private val context: Context
) {

    private val realValuesMatrix = mutableListOf<List<Int>>()

    // Function to update the Line Chart with new data
    fun updateLineChart(int16Values: List<Pair<Int, Int>>) {
        val entries = ArrayList<Entry>()
        for (i in int16Values.indices) {
            val realValue = int16Values[i].first
            val imagValue = int16Values[i].second
            val RMS =
                Math.sqrt(Math.pow(realValue.toDouble(), 2.0) + Math.pow(imagValue.toDouble(), 2.0))

            // Add RMS values as data entries for the line chart
            entries.add(Entry(i.toFloat(), RMS.toFloat()))  // Plot real part
        }

        val dataSet = LineDataSet(entries, "Magnitude")  // Creating dataset for real part
        val lineData = LineData(dataSet)

        // Customize Y-axis
        val leftAxis = lineChart.axisLeft
        leftAxis.setLabelCount(5, true)  // Set the number of Y-axis labels
        lineChart.axisLeft.isEnabled = false  // Disable left axis labels if not needed

        lineChart.data = lineData
        lineChart.invalidate()  // Refresh chart
    }

    // Function to update the Heatmap (Scatter Chart) with new data
    fun updateHeatmap(newList: List<Pair<Int, Int>>) {
        val realValues = newList.map {
            Math.sqrt(Math.pow(it.first.toDouble(), 2.0) + Math.pow(it.second.toDouble(), 2.0))
                .toInt()
        }
        realValuesMatrix.add(realValues)

        if (realValuesMatrix.size > 60) {
            realValuesMatrix.removeAt(0) // Limit X-axis to 60 columns
        }

        val scatterData = generateScatterData(realValuesMatrix)
        scatterChart.data = scatterData
        scatterChart.notifyDataSetChanged() // Notify chart of data change
        scatterChart.invalidate() // Redraw chart
    }

    // Function to generate scatter data for the heatmap
    private fun generateScatterData(matrix: List<List<Int>>): ScatterData {
        val entries = ArrayList<Entry>()
        val colors = ArrayList<Int>()

        for (x in matrix.indices) {
            for (y in matrix[x].indices) {
                val realValue = matrix[x][y]
                entries.add(Entry(x.toFloat(), y.toFloat()))
                entries.add(Entry(x.toFloat(), y.toFloat())) // Add entry for x, y positions
                colors.add(generateColorForValue(realValue)) // Set color based on intensity
            }
        }

        val dataSet = ScatterDataSet(entries, "Heatmap")
        dataSet.setScatterShape(ScatterChart.ScatterShape.SQUARE) // Use square shape
        dataSet.colors = colors

        return ScatterData(dataSet)
    }

    // Function to generate grayscale color based on value
    private fun generateColorForValue(value: Int): Int {
        // Adjust the value to a range from 0 to 255, where 0 is black and 255 is white
        val adjustedValue = (255 * Math.log(value.toDouble()) / Math.log(2500.0)).toInt()

        // Clamp the value between 0 and 255
        val grayscale = if (adjustedValue > 255) 255 else if (adjustedValue < 0) 0 else adjustedValue

        // Return a grayscale color (R = G = B = grayscale)
        return Color.rgb(grayscale, grayscale, grayscale)
    }
}
