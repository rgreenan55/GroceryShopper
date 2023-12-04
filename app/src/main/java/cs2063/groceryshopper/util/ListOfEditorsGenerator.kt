package cs2063.groceryshopper.util

import android.app.Activity
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


class ListOfEditorsGenerator {
    fun generateList(activity : Activity, db: DBHelper, rawText: String) {

        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())
                val listOfItems = buildData(rawText)
                mainHandler.post { updateDisplay(activity, listOfItems) }
            }
    }

    private fun updateDisplay(activity: Activity, listOfItems: ArrayList<Map<String, String>>){
        val listView : ListView = activity.findViewById(R.id.editorsList)

        val adapter = SimpleAdapter(
            activity,
            listOfItems,
            R.layout.item_add,
            arrayOf("name", "cost"),
            intArrayOf(R.id.name_edit_text, R.id.price_edit_text)
        )
        listView.adapter = adapter
    }

    private fun buildData(rawText: String) : ArrayList<Map<String, String>> {
        val list = ArrayList<Map<String,String>>()
        // ToDo: Read the items from the OCR Text
        //      My suggestion here is to look at each line and any line with a price it
        //      in we assume is an item and we take the item and all the text from that
        //      line and put it in the editor area. Then the user can do the final clean up
        list.add(putData("Apple", 5.99))
        list.add(putData("Banana", 3.99))
        list.add(putData("Pear", 2.49))
        return list
    }

    private fun putData(name : String, cost : Double) : HashMap<String, String>{
        val item = HashMap<String, String>()
        item["name"] = name
        item["cost"] = "%,.2f".format(Locale.ENGLISH, cost)
        return item
    }
}

