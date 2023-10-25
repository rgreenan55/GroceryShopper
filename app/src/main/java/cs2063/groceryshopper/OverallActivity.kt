package cs2063.groceryshopper

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.time.YearMonth

class OverallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.overall_view)

        val lineChart = setupChart()
        lineChart.data = getData()
        setupXAxis(lineChart)
        setupYAxis(lineChart)
        lineChart.invalidate()
    }

    private fun setupChart() : LineChart {
        val lineChart = findViewById<LineChart>(R.id.lineChart)
        lineChart.extraBottomOffset = 50f
        lineChart.description = null
        lineChart.setTouchEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.setScaleEnabled(false)
        lineChart.isDragEnabled = false
        lineChart.legend.isEnabled = false

        return lineChart
    }

    private fun getData() : LineData {
        // TODO: Get actual data
        val x = ArrayList<Entry>()
        x.add(Entry(0f, 50f))
        x.add(Entry(1f, 93f))
        x.add(Entry(2f, 111f))
        x.add(Entry(3f, 112.2f))
        x.add(Entry(4f, 113.4f))
        x.add(Entry(5f, 120f))
        x.add(Entry(6f, 84.2f))
        x.add(Entry(7f, 10.3f))
        x.add(Entry(8f, 87.2f))
        x.add(Entry(9f, 83.24f))
        x.add(Entry(10f, 92.26f))
        x.add(Entry(11f, 110.4f))
        x.add(Entry(12f, 190f))

        // TODO: Remove Rounding https://stackoverflow.com/questions/51267312/chart-values-is-rounded-when-present-on-mpandroidchart
        val data = LineDataSet(x, "Trip History")
        data.lineWidth = 2f
        data.circleRadius = 4f

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(data)

        // Setup Line
        val line = LineData(dataSets)
        line.setValueTextColor(Color.BLACK)

        return line
    }

    private fun setupXAxis(lineChart : LineChart) {
        val xAxis = lineChart.xAxis

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.BLACK
        xAxis.isEnabled = true

        xAxis.setLabelCount(13, true)
        xAxis.setDrawGridLines(true)
        xAxis.valueFormatter = MonthFormatter()
    }

    private fun setupYAxis(lineChart : LineChart) {
        val yAxis = lineChart.axisLeft
        yAxis.textColor = Color.BLACK

        yAxis.axisMinimum = 0f
        yAxis.granularity = 10f
        yAxis.setDrawGridLines(false)

        val yAxisR = lineChart.axisRight
        yAxisR.isEnabled = false
    }

    class MonthFormatter : IndexAxisValueFormatter() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun getFormattedValue(value : Float): String {
            val currentMonth = YearMonth.now().monthValue

            fun calcMonth(month : Int) : Int {
                Log.i("Me", "$currentMonth")
                return (month + (12-currentMonth)) % 12
            }

            return when (value.toInt()) {
                calcMonth(0) -> "Jan"
                calcMonth(1) -> "Feb"
                calcMonth(2) -> "Mar"
                calcMonth(3) -> "Apr"
                calcMonth(4) -> "May"
                calcMonth(5) -> "Jun"
                calcMonth(6) -> "Jul"
                calcMonth(7) -> "Aug"
                calcMonth(8) -> "Sep"
                calcMonth(9) -> "Oct"
                calcMonth(10) -> "Nov"
                calcMonth(11) -> "Dec"
                else -> ""
            }
        }
    }
}