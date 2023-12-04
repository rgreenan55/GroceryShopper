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
import cs2063.groceryshopper.util.itemDetailsGlobal
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.Executors
import java.util.regex.Pattern

// ToDo: Clean this file up

class AddActivity : AppCompatActivity() {

    private lateinit var dbGlobal: DBHelper
    private lateinit var itemsListGlobal: ArrayList<ArrayList<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_view)

        // Setup Back Button
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val actionbarString = "Edit Recipt Details"
        this.supportActionBar?.title = actionbarString


        // Get Trip ID and Connect to DB
        val database = DBHelper(this)
        val extras: Bundle? = intent.extras
        val tripId : Int = extras?.getInt("tripId") ?: 0

        dbGlobal = database


        // ToDo: Grab the ORC text from that activity
        val rawText = extras!!.getString("OCR")!!

        itemsListGlobal = analizeOCRString(rawText)

        setUpList(database, itemsListGlobal)

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
            val itemsListView = this.findViewById<ListView>(R.id.editorsList)
            for (i in 0..<itemsListView!!.childCount){
                val item: RelativeLayout = itemsListView?.getChildAt(i) as RelativeLayout
                val itemName = item.findViewById<EditText>(R.id.name_edit_text).text.toString()
                val price = item.findViewById<EditText>(R.id.price_edit_text).text.toString().toDouble()
                itemDetailsGlobal?.get(i)?.set(0, itemName)
                itemDetailsGlobal?.get(i)?.set(1, price.toString())
            }
            itemsListGlobal.add(arrayListOf("", "0.0"))
            setUpList(database, itemsListGlobal)
        }
    }

    private fun analizeOCRString(rawText: String): ArrayList<ArrayList<String>>{
        val items = ArrayList<ArrayList<String>>()
        val lines = rawText.split('\n')
        val pat = Pattern.compile("(.*?)([0-9, ]*[., ]*[,.][., ]*[0-9][0-9])(.*)")
        for (line in lines){
            val matcher = pat.matcher(line)
            if(matcher.matches()){
                val name = matcher.group(1) + matcher.group(3)
                val price = cleanPrice(matcher.group(2))
                items.add(arrayListOf(name, price))
            }
        }
        return items
    }

    private fun cleanPrice(str: String): String{
        var output = str.replace("\\s+".toRegex(),"").replace("[,]".toRegex(),"").replace("[.]".toRegex(),"")
        return output.substring(0, output.length-2) + "." + output.substring(output.length-2)
    }

    private fun setUpList(db: DBHelper, itemDetails: ArrayList<ArrayList<String>>){
        // Generate List of Items from DB
        val listOfEditorsGenerator = ListOfEditorsGenerator()
        listOfEditorsGenerator.generateList(this, itemDetails)
    }

//    private fun updateList() {
//        setUpList(dbGlobal, tripIdGlobal)
//    }

    // Back Arrows sends to previous activity
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun addTrip(db: DBHelper){
        Executors.newSingleThreadExecutor()
            .execute {
                val mainHandler = Handler(Looper.getMainLooper())

                // Todo: Verify all the input fields are valid before adding anything to the DB

                val dateEditText = findViewById<EditText>(R.id.date)

                val datePattern = Pattern.compile("[1-2][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]")
                val dateMatcher = datePattern.matcher(dateEditText.text)

                if (!dateMatcher.matches()) {
                    mainHandler.post {
                        Toast.makeText(this, "Please Enter A Valid Date (yyyy-mm-dd)", Toast.LENGTH_LONG).show()
                    }
                }
                else{
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
    }
}
