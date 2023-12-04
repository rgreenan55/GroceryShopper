package cs2063.groceryshopper

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout.LayoutParams
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cs2063.groceryshopper.util.DBHelper
import cs2063.groceryshopper.util.ListOfEditorsGenerator
import cs2063.groceryshopper.util.ListOfItemsGenerator
import java.util.Locale
import java.util.concurrent.Executors

// ToDo: Clean this file up

class AddActivity : AppCompatActivity() {

    private var tripPriceTotal: Double = 0.0
    private lateinit var dbGlobal: DBHelper
    private var tripIdGlobal: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_view)

        // Setup Back Button
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tripPriceTotal = intent.extras?.getDouble("total")!!

        // ToDo: Fix title bar (not sure what to put there)
        // Update Titles
//        val title = this.findViewById<TextView>(R.id.dateTitle)
//        val actionbarString = intent.extras?.getString("storeName") + "  |  " + intent.extras?.getString("date")
//        val titleString = "Total Spent: " + "%,.2f".format(Locale.ENGLISH, tripPriceTotal) + "$"
//        this.supportActionBar?.title = actionbarString
//        title.text = titleString

        // Setup Dropdown
//        setupSpinner()

        // Get Trip ID and Connect to DB
        val database = DBHelper(this)
        val extras: Bundle? = intent.extras
        val tripId : Int = extras?.getInt("tripId") ?: 0

        dbGlobal = database
        tripIdGlobal = tripId


        // ToDo: Grab the ORC text from that activity
        val rawText = "Grab this from the OCR activity through the extras"

        setUpList(database, rawText)

//         Setup Add Trip Button
        val addTripButton = this.findViewById<Button>(R.id.addTrip)
        addTripButton.setOnClickListener {
            // Delete Data Here
            val builder = AlertDialog.Builder(this@AddActivity)
            builder.setTitle("Add Trip")
            builder.setMessage("Are you sure you want to add this trip?")
            builder.setCancelable(false)
            builder.setPositiveButton("Yes") { _, _-> addTrip(database) }
            builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            val alert = builder.create()
            alert.show()
        }
        val addItemButton = this.findViewById<Button>(R.id.addItem)
        addItemButton.setOnClickListener {
            // ToDo: Have this button add a blank entry to the list

            // The below does not work because the "addView" does not work, we may have to recompute the whole list

//            val editors = this.findViewById<ListView>(R.id.editorsList)
//            editors.addView(this.findViewById(R.id.itemAddCard))
        }
    }
//
    private fun setUpList(db: DBHelper, rawText: String){
        // Generate List of Items from DB
        val listOfEditorsGenerator = ListOfEditorsGenerator()
        listOfEditorsGenerator.generateList(this, db, rawText)

//        // Setup Archive / Delete for Items
          // ToDo: Add the delete functionality to all the rows 
          // (idk if it would be here or in the list generator, 
          // i assume here as you would want to remove it from 
          // a list in this activity)
//        val listView : ListView = this.findViewById(R.id.tripList)
//        listView.onItemLongClickListener = OnItemLongClickListener { _, item: View, _: Int, _ ->
//            true
//        }
    }
//
//    private fun updateList() {
//        setUpList(dbGlobal, tripIdGlobal)
//    }
//
//    // Back Arrows sends to previous activity
//    override fun onSupportNavigateUp(): Boolean {
//        finish()
//        return true
//    }
//
//    private fun setupSpinner() {
//        val spinner : Spinner = this.findViewById<Spinner>(R.id.sorter)
//        spinner.onItemSelectedListener = Listener()
//
//        val sortOptions = ArrayList<String>()
//        sortOptions.add("Default")
//        sortOptions.add("Name")
//        sortOptions.add("Price")
//
//        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortOptions)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//        spinner.adapter = adapter
//    }
//
//    inner class Listener() : AdapterView.OnItemSelectedListener {
//        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//            updateList()
//        }
//        override fun onNothingSelected(arg0: AdapterView<*>?) {}
//    }
//
    private fun addTrip(db: DBHelper){
        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())

                // Todo: Verify all the input fields are valid before adding anything to the DB

                val storeName = this.findViewById<EditText>(R.id.storeName).text.toString()
                val date = this.findViewById<EditText>(R.id.date).text.toString()
                val newTripId = db.getTripFromRowId(db.insertTrip(0.0, date, storeName))

                val itemsListView = this.findViewById<ListView>(R.id.editorsList)
                for (i in 0..<itemsListView!!.childCount){
                    val item: RelativeLayout = itemsListView?.getChildAt(i) as RelativeLayout
                    val itemName = item.findViewById<EditText>(R.id.name_edit_text).text.toString()
                    val price = item.findViewById<EditText>(R.id.price_edit_text).text.toString().toDouble()
                    db.insertItem(newTripId, price, itemName)
                }

                mainHandler.post {
                    Toast.makeText(this, "Trip Added", Toast.LENGTH_SHORT).show()
                    // ToDo: Remove the OCR Activities from the back button stack or whatever its called
                    // finish()
                    val intent = Intent(this@AddActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
    }
//
//    private fun archiveItem(db: DBHelper, itemId : Int, tripId: Int, price: Double, archived: Boolean, itemView: View) {
//        Executors.newSingleThreadExecutor()
//            .execute {
//                val mainHandler = Handler(Looper.getMainLooper())
//                db.archiveItem(itemId, tripId, price, archived)
//                mainHandler.post {
//                    Toast.makeText(this, if (archived) "Item Unarchived" else "Item Archived", Toast.LENGTH_SHORT).show()
//                    tripPriceTotal += if (archived) price else -price
//                    this.findViewById<TextView>(R.id.dateTitle).text = "Total Spent: " + "%,.2f".format(Locale.ENGLISH, tripPriceTotal) + "$"
//                    itemView.findViewById<TextView>(R.id.itemArchived).text = (!archived).toString()
//                    setUpList(db, tripId)
//                }
//            }
//
//    }
//
//    private fun deleteItem(db: DBHelper, itemId : Int, tripId: Int, price: Double, archived: Boolean) {
//        Log.i("Delete", "DeleteItem")
//
//        Executors.newSingleThreadExecutor()
//            .execute {
//                val mainHandler = Handler(Looper.getMainLooper())
//                db.deleteItem(itemId, tripId, price, archived)
//                mainHandler.post {
//                    Toast.makeText(this, "Item Deleted", Toast.LENGTH_SHORT).show()
//                    setUpList(db, tripId)
//                    if (!archived) {
//                        tripPriceTotal -= price
//                        this.findViewById<TextView>(R.id.dateTitle).text = "Total Spent: $tripPriceTotal$"
//                    }
//                }
//            }
//    }
}
