package cs2063.groceryshopper

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TripActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trip_view)

        // TODO: Retrieve date from data
        val title = this.findViewById<TextView>(R.id.dateTitle)
        title.text = "Date Here"

        // TODO: Retrieve items from data
        val listView : ListView = this.findViewById(R.id.tripList)
        val listOfItems = getItems()
        val adapter = SimpleAdapter(
            this,
            listOfItems,
            R.layout.grocery_trip_item,
            arrayOf<String>("name", "cost"),
            intArrayOf(R.id.item, R.id.tripCost)
        )
        listView.adapter = adapter

        // TODO: Delete trip from data
        val deleteButton = this.findViewById<Button>(R.id.deleteTrip)
        deleteButton.setOnClickListener {
            // Delete Data Here
            Toast.makeText(this, "Trip deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getItems() : ArrayList<Map<String,String>>{
        val list = ArrayList<Map<String,String>>()
        for (i in 1..10) {
            list.add(putData("Item $i, 2023", "$${(10..20).random()}.${(10..99).random()}"))
        }
        return list
    }

    private fun putData(date : String, cost : String) : HashMap<String, String>{
        val item = HashMap<String, String>()
        item["name"] = date
        item["cost"] = cost
        return item
    }

}