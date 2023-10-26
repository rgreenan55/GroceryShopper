package cs2063.groceryshopper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cs2063.groceryshopper.util.DBHelper
import cs2063.groceryshopper.util.ListOfItemsGenerator
import java.util.concurrent.Executors

class TripActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trip_view)

        // TODO: Retrieve date from data
        val title = this.findViewById<TextView>(R.id.dateTitle)
        val titleString = intent.extras?.getString("storeName") + " - " + intent.extras?.getString("date") + " - " + intent.extras?.getDouble("total") + "$"
        title.text = titleString

        val db = DBHelper(this)

        val extras: Bundle? = intent.extras
        val tripId : Int = extras?.getInt("tripId") ?: 0

        val listOfItemsGenerator = ListOfItemsGenerator()
        listOfItemsGenerator.generateList(this, db, tripId)

        // TODO: Delete trip from data
        val deleteButton = this.findViewById<Button>(R.id.deleteTrip)
        deleteButton.setOnClickListener {
            // Delete Data Here
            deleteTrip(db, tripId)
        }
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

}