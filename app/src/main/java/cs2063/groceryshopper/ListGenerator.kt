package cs2063.groceryshopper

import android.widget.ListView
import android.widget.SimpleAdapter

class ListGenerator {
    fun generateList(activity : MainActivity) {
        val listView : ListView = activity.findViewById(R.id.tripList)
        val tripList = buildData()
        val adapter = SimpleAdapter(
            activity,
            tripList,
            R.layout.grocery_trip_item,
            arrayOf<String>("date", "cost"),
            intArrayOf(R.id.tripDate, R.id.tripCost)
        )
        listView.adapter = adapter
    }

    private fun buildData() : ArrayList<Map<String, String>> {
        val list = ArrayList<Map<String,String>>()
        for (i in 1..20) {
            list.add(putData("Hello $i", "$200.00"))
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