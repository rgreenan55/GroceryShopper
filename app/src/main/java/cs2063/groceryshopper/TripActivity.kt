package cs2063.groceryshopper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.LinearLayout.LayoutParams
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cs2063.groceryshopper.util.DBHelper
import cs2063.groceryshopper.util.ListOfItemsGenerator
import java.util.concurrent.Executors


class TripActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trip_view)

        // Setup Back Button
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Update Titles
        val title = this.findViewById<TextView>(R.id.dateTitle)
        val actionbarString = intent.extras?.getString("storeName") + "  |  " + intent.extras?.getString("date")
        val titleString = "Total Spent: " + intent.extras?.getDouble("total") + "$"
        this.supportActionBar?.title = actionbarString
        title.text = titleString

        // Get Trip ID and Connect to DB
        val db = DBHelper(this)
        val extras: Bundle? = intent.extras
        val tripId : Int = extras?.getInt("tripId") ?: 0

        // Generate List of Items from DB
        val listOfItemsGenerator = ListOfItemsGenerator()
        listOfItemsGenerator.generateList(this, db, tripId)


        // Setup Archive / Delete for Items
        val listView : ListView = this.findViewById(R.id.tripList)
        listView.onItemLongClickListener = OnItemLongClickListener { _, _, pos: Int, _ ->

            // Get Item Id for Database
            val item : View = listView.getChildAt(pos)
            val itemIdTV : TextView? = item.findViewById<TextView>(R.id.itemId)
            val itemTV : TextView? = item.findViewById<TextView>(R.id.item)

            val itemName = itemTV!!.text
            val itemId : Int = (itemIdTV!!.text as String).toInt()

            // Build Dialog
            val builder = AlertDialog.Builder(this@TripActivity)
            builder.setTitle("Item Options | $itemName")
            builder.setMessage("What would you like to do?")
            builder.setCancelable(false)
            builder.setNeutralButton("Archive") { dialog, _ ->
                archiveItem(itemId)
                dialog.dismiss()
            }
            builder.setNegativeButton("Delete") { dialog, _ ->
                deleteItem(itemId)
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

        // Setup Delete Trip Button
        val deleteButton = this.findViewById<Button>(R.id.deleteTrip)
        deleteButton.setOnClickListener {
            // Delete Data Here
            val builder = AlertDialog.Builder(this@TripActivity)
            builder.setTitle("Delete Trip")
            builder.setMessage("Are you sure you want to delete this trip?")
            builder.setCancelable(false)
            builder.setPositiveButton("Yes") { _, _-> deleteTrip(db, tripId) }
            builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            val alert = builder.create()
            alert.show()
        }
    }

    // Back Arrows sends to previous activity
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
    }

    private fun deleteTrip(db: DBHelper, tripId: Int){
        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())
                db.deleteTrip(tripId)
                mainHandler.post {
                    Toast.makeText(this, "Trip deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    private fun archiveItem(itemId : Int) {
        Log.i("Archive", "ArchiveItem")

        // TODO: Mark Item as Archived
    }

    private fun deleteItem(itemId : Int) {
        Log.i("Delete", "DeleteItem")

        // TODO: Delete Item
    }
}