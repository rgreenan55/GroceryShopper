package cs2063.groceryshopper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout.LayoutParams
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cs2063.groceryshopper.util.DBHelper
import cs2063.groceryshopper.util.ListOfItemsGenerator
import java.util.Locale
import java.util.concurrent.Executors


class TripActivity : AppCompatActivity() {

    private var tripPriceTotal: Double = 0.0
    private lateinit var dbGlobal: DBHelper
    private var tripIdGlobal: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trip_view)

        // Setup Back Button
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tripPriceTotal = intent.extras?.getDouble("total")!!

        // Update Titles
        val title = this.findViewById<TextView>(R.id.dateTitle)
        val actionbarString = intent.extras?.getString("storeName") + "  |  " + intent.extras?.getString("date")
        val titleString = "Total Spent: " + "%,.2f".format(Locale.ENGLISH, tripPriceTotal) + "$"
        this.supportActionBar?.title = actionbarString
        title.text = titleString

        // Setup Dropdown
        setupSpinner()

        // Get Trip ID and Connect to DB
        val database = DBHelper(this)
        val extras: Bundle? = intent.extras
        val tripId : Int = extras?.getInt("tripId") ?: 0

        dbGlobal = database
        tripIdGlobal = tripId

        setUpList(database, tripId)

        // Setup Delete Trip Button
        val deleteButton = this.findViewById<Button>(R.id.deleteTrip)
        deleteButton.setOnClickListener {
            // Delete Data Here
            val builder = AlertDialog.Builder(this@TripActivity)
            builder.setTitle("Delete Trip")
            builder.setMessage("Are you sure you want to delete this trip?")
            builder.setCancelable(false)
            builder.setPositiveButton("Yes") { _, _-> deleteTrip(database, tripId) }
            builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun setUpList(db: DBHelper, tripId: Int){
        // Generate List of Items from DB
        val listOfItemsGenerator = ListOfItemsGenerator()
        listOfItemsGenerator.generateList(this, db, tripId)

        // Setup Archive / Delete for Items
        val listView : ListView = this.findViewById(R.id.tripList)
        listView.onItemLongClickListener = OnItemLongClickListener { _, item: View, _: Int, _ ->

            // Get Item Id for Database
            val itemIdTV : TextView? = item.findViewById<TextView>(R.id.itemId)
            val itemTV : TextView? = item.findViewById<TextView>(R.id.item)
            val itemPriceTV : TextView? = item.findViewById<TextView>(R.id.itemCost)
            val itemArchivedTV : TextView? = item.findViewById<TextView>(R.id.itemArchived)

            val itemName = itemTV!!.text
            val itemId : Int = (itemIdTV!!.text as String).toInt()
            val itemPrice : Double = (itemPriceTV!!.text as String).dropLast(1).toDouble()
            val itemArchived : Boolean = (itemArchivedTV!!.text as String).toBoolean()

            // Build Dialog
            val builder = AlertDialog.Builder(this@TripActivity)
            builder.setTitle("Item Options | $itemName")
            builder.setMessage("What would you like to do?")
            builder.setCancelable(false)
            builder.setNeutralButton(if (itemArchived) "Unarchive" else "Archive") { dialog, _ ->
                archiveItem(db, itemId, tripId, itemPrice, itemArchived, item)
                dialog.dismiss()
            }
            builder.setNegativeButton("Delete") { dialog, _ ->
                deleteItem(db, itemId, tripId, itemPrice, itemArchived)
                dialog.dismiss()
            }
            builder.setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            val alert = builder.create()
            alert.show()

            // Set up Button Formatting
            val btnArchive : Button = alert.getButton(AlertDialog.BUTTON_NEUTRAL)
            val btnDelete : Button = alert.getButton(AlertDialog.BUTTON_NEGATIVE)
            val btnCancel : Button = alert.getButton(AlertDialog.BUTTON_POSITIVE)

            val archiveLayout : LayoutParams = btnArchive.layoutParams as LayoutParams
            val deleteLayout : LayoutParams = btnDelete.layoutParams as LayoutParams
            val cancelLayout : LayoutParams = btnCancel.layoutParams as LayoutParams

            archiveLayout.weight = 10f
            deleteLayout.weight = 10f
            cancelLayout.weight = 10f

            btnArchive.layoutParams = archiveLayout
            btnDelete.layoutParams = deleteLayout
            btnCancel.layoutParams = cancelLayout
            // Done Formatting

            true
        }
    }

    // TODO: BEN -> Figure out how to recall Database
    private fun updateList() {
        setUpList(dbGlobal, tripIdGlobal)
    }

    // Back Arrows sends to previous activity
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupSpinner() {
        val spinner : Spinner = this.findViewById<Spinner>(R.id.sorter)
        spinner.onItemSelectedListener = Listener()

        val sortOptions = ArrayList<String>()
        sortOptions.add("Default")
        sortOptions.add("Name")
        sortOptions.add("Price")

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
    }

    inner class Listener() : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            // TODO: BEN -> Figure out how to recall Database
            updateList()
        }
        override fun onNothingSelected(arg0: AdapterView<*>?) {}
    }

    private fun deleteTrip(db: DBHelper, tripId: Int){
        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())
                db.deleteTrip(tripId)
                mainHandler.post {
                    Toast.makeText(this, "Trip Deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    private fun archiveItem(db: DBHelper, itemId : Int, tripId: Int, price: Double, archived: Boolean, itemView: View) {
        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())
                db.archiveItem(itemId, tripId, price, archived)
                mainHandler.post {
                    Toast.makeText(this, if (archived) "Item Unarchived" else "Item Archived", Toast.LENGTH_SHORT).show()
                    tripPriceTotal += if (archived) price else -price
                    this.findViewById<TextView>(R.id.dateTitle).text = "Total Spent: " + "%,.2f".format(Locale.ENGLISH, tripPriceTotal) + "$"
                    itemView.findViewById<TextView>(R.id.itemArchived).text = (!archived).toString()
                    setUpList(db, tripId)
                }
            }

    }

    private fun deleteItem(db: DBHelper, itemId : Int, tripId: Int, price: Double, archived: Boolean) {
        Log.i("Delete", "DeleteItem")

        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())
                db.deleteItem(itemId, tripId, price, archived)
                mainHandler.post {
                    Toast.makeText(this, "Item Deleted", Toast.LENGTH_SHORT).show()
                    setUpList(db, tripId)
                    if (!archived) {
                        tripPriceTotal -= price
                        this.findViewById<TextView>(R.id.dateTitle).text = "Total Spent: $tripPriceTotal$"
                    }
                }
            }
    }
}