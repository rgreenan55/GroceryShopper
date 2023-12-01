package cs2063.groceryshopper.util

import android.os.Handler
import android.os.Looper
import android.widget.ListView
import android.widget.SimpleAdapter
import cs2063.groceryshopper.R
import cs2063.groceryshopper.TripActivity
import cs2063.groceryshopper.model.Item
import java.util.concurrent.Executors


class ListOfItemsGenerator {
    fun generateList(activity : TripActivity, db: DBHelper, tripId: Int) {
        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())
                val items = db.getAllItemsForTrip(tripId)
//                val items = db.getAllItemsForTripSortByPrice(tripId, descending = true)
//                val items = db.getAllItemsForTripSortByName(tripId, aToZ = true)
                val listOfItems = buildData(items)
                mainHandler.post { updateDisplay(activity, listOfItems) }
            }
    }

    private fun updateDisplay(activity: TripActivity, listOfItems: ArrayList<Map<String, String>>){
        val listView : ListView = activity.findViewById(R.id.tripList)

        val adapter = SimpleAdapter(
            activity,
            listOfItems,
            R.layout.grocery_trip_item,
            arrayOf("name", "cost", "id", "archived"),
            intArrayOf(R.id.item, R.id.itemCost, R.id.itemId, R.id.itemArchived)
        )
        listView.adapter = adapter
    }

    private fun buildData(items: ArrayList<Item>?) : ArrayList<Map<String, String>> {
        val list = ArrayList<Map<String,String>>()
        if(items == null) return list
        for (item in items) {
            list.add(putData(item.itemName, item.price.toString(), item.id, item.archived))
        }
        return list
    }

    private fun putData(name : String, cost : String, id: Int, archived: Boolean) : HashMap<String, String>{
        val item = HashMap<String, String>()
        item["name"] = name
        item["cost"] = "$cost$"
        item["id"] = id.toString()
        item["archived"] = archived.toString()
        return item
    }
}

