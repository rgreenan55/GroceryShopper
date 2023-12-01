package cs2063.groceryshopper.util

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.ListView
import android.widget.SimpleAdapter
import cs2063.groceryshopper.MainActivity
import cs2063.groceryshopper.R
import cs2063.groceryshopper.TripActivity
import cs2063.groceryshopper.model.Trip
import java.util.concurrent.Executors

class ListOfTripsGenerator {
    fun generateList(activity : MainActivity, db: DBHelper) {
        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())
                val trips = db.getAllTrips()

                mainHandler.post { updateDisplay(activity, trips) }
            }
    }

    private fun updateDisplay(activity:  MainActivity, trips: ArrayList<Trip>?){
        val listOfTrips = buildData(trips)
        val listView : ListView = activity.findViewById(R.id.tripList)
        val adapter = SimpleAdapter(
            activity,
            listOfTrips,
            R.layout.grocery_trip,
            arrayOf("date", "cost"),
            intArrayOf(R.id.tripDate, R.id.tripCost)
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, index, _ ->
            val tripActivityIntent = Intent(activity, TripActivity::class.java)
            val trip = trips!![index]
            tripActivityIntent.apply {
                putExtra("tripId", trip.id)
                putExtra("date", trip.date)
                putExtra("storeName", trip.storeName)
                putExtra("total", trip.total)
            }
            activity.startActivity(tripActivityIntent)
        }
    }

    private fun buildData(trips: ArrayList<Trip>?) : ArrayList<Map<String, String>> {
        val list = ArrayList<Map<String,String>>()
        if(trips == null) return list
        for (trip in trips) {
            list.add(putData(trip.date, trip.total.toString()))
        }
        return list
    }

    private fun putData(date : String, cost : String) : HashMap<String, String>{
        val item = HashMap<String, String>()
        item["date"] = date
        item["cost"] = "$cost$"
        return item
    }
}