package cs2063.groceryshopper.util

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import cs2063.groceryshopper.R
import cs2063.groceryshopper.TripActivity
import cs2063.groceryshopper.model.Item
import java.util.Locale
import java.util.concurrent.Executors


class ListOfItemsGenerator {
    fun generateList(activity : TripActivity, db: DBHelper, tripId: Int) {

        // TODO: BEN -> Might not need to be here?
        val spinner : Spinner = activity.findViewById<Spinner>(R.id.sorter)
        val sortOption : String = spinner.selectedItem.toString()

        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())

                val items = when (sortOption) {
                    "Default" -> { db.getAllItemsForTrip(tripId) }
                    "Name" -> { db.getAllItemsForTripSortByName(tripId, aToZ = true) }
                    "Price" -> { db.getAllItemsForTripSortByPrice(tripId, descending = true) }
                    else -> { ArrayList<Item>() }
                }

                val listOfItems = buildData(items)
                mainHandler.post { updateDisplay(activity, listOfItems) }
            }
    }

    private fun updateDisplay(activity: TripActivity, listOfItems: ArrayList<Map<String, String>>){
        val listView : ListView = activity.findViewById(R.id.tripList)

        val adapter = MySimpleAdapter(
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
            list.add(putData(item.itemName, item.price, item.id, item.archived))
        }
        return list
    }

    private fun putData(name : String, cost : Double, id: Int, archived: Boolean) : HashMap<String, String>{
        val item = HashMap<String, String>()
        item["name"] = name
        item["cost"] = "%,.2f".format(Locale.ENGLISH, cost) + "$"
        item["id"] = id.toString()
        item["archived"] = archived.toString()
        return item
    }
}

class MySimpleAdapter(
    context: Context,
    data: ArrayList<Map<String, String>>,
    @LayoutRes
    res: Int,
    from: Array<String>,
    @IdRes
    to: IntArray
): SimpleAdapter(context, data, res, from, to) {
    override fun getView(position: Int, viewIn: View?, parent: ViewGroup) : View {
        val view = super.getView(position, viewIn, parent)

        val nameTV: TextView = view.findViewById<TextView>(R.id.item)
        val costTV: TextView = view.findViewById<TextView>(R.id.itemCost)
        val archivedTV: TextView = view.findViewById<TextView>(R.id.itemArchived)

        if ((archivedTV.text as String).toBoolean()) {
            nameTV.setTextColor(Color.LTGRAY)
            costTV.setTextColor(Color.LTGRAY)
        } else {
            nameTV.setTextColor(Color.BLACK)
            costTV.setTextColor(Color.BLACK)
        }

        return view
    }
}

