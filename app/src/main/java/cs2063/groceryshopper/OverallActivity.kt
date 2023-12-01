package cs2063.groceryshopper

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import cs2063.groceryshopper.util.DBHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class OverallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.overall_view)

        val actionbarString = "Overall View"
        this.supportActionBar?.title = actionbarString
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val db = DBHelper(this)
        val lineChart = setupChart()
        lineChart.data = getData(db)
        setupXAxis(lineChart)
        setupYAxis(lineChart)
        lineChart.invalidate()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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

    private fun getData(db: DBHelper) : LineData {

        val months = 6
        val now = LocalDateTime.now()
        val firstDayOfMonth = now
            .with(TemporalAdjusters.firstDayOfMonth())
            .with(LocalTime.MIN)

        val epochTime = firstDayOfMonth.atZone(ZoneId.systemDefault()).toEpochSecond()
        val maxEpoch = epochTime+2592000
        val minEpoch = epochTime-(months-2)*2592000

        val trips = db.getPastMonthsTrips(minEpoch, maxEpoch)

        val x = ArrayList<Entry>()
//        x.add(Entry(0f, 50f))
//        x.add(Entry(1.5f, 93.1235f))
//        x.add(Entry(2f, 111f))
//        x.add(Entry(3f, 112.2f))
//        x.add(Entry(4f, 113.4f))
//        x.add(Entry(5f, 120f))
        if (trips != null) {
            for(trip in trips){
                val l = LocalDate.parse(trip.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val unix = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
                val location = (unix-minEpoch).toFloat()/2592000.0f
                val total = trip.total.toFloat()
                Log.i("Graph", "Location: $location, total: $total")
                x.add(Entry(location, total))
            }
        }
        val data = LineDataSet(x, "Trip History")
        data.valueFormatter = LabelFormatter()
        data.lineWidth = 4f
        data.circleRadius = 8f
        data.valueTextSize = 12f

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

        xAxis.setLabelCount(6, true)
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

        override fun getFormattedValue(value : Float): String {
            val currentMonth = YearMonth.now().monthValue

            fun calcMonth(month : Int) : Int {
                return (month + (5-currentMonth)) % 12
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

    class LabelFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "$" + "%,.2f".format(Locale.ENGLISH, value)
        }
    }
}