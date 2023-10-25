package cs2063.groceryshopper

import android.content.Intent
import android.widget.ListView
import android.widget.SimpleAdapter

class ListOfTripsGenerator {
    fun generateList(activity : MainActivity) {
        val listView : ListView = activity.findViewById(R.id.tripList)
        val listOfTrips = buildData()
        val adapter = SimpleAdapter(
            activity,
            listOfTrips,
            R.layout.grocery_trip,
            arrayOf<String>("date", "cost"),
            intArrayOf(R.id.tripDate, R.id.tripCost)
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, _, _ ->
            val tripActivityIntent = Intent(activity, TripActivity::class.java)
            activity.startActivity(tripActivityIntent)
        }
    }

    // TODO: Get actual data
    private fun buildData() : ArrayList<Map<String, String>> {
        val list = ArrayList<Map<String,String>>()
        for (i in 1..20) {
            list.add(putData("October $i, 2023", "$${(100..250).random()}.${(10..99).random()}"))
        }
        return list
    }

    private fun putData(date : String, cost : String) : HashMap<String, String>{
        val item = HashMap<String, String>()
        item["date"] = date
        item["cost"] = cost
        return item
    }
}