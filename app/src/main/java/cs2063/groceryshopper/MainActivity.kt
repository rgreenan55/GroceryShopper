package cs2063.groceryshopper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cs2063.groceryshopper.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Open Overall Activity
        val overallButton = findViewById<Button>(R.id.viewOverall)
        overallButton.setOnClickListener {
            val overallActivityIntent = Intent(this, OverallActivity::class.java)
            this.startActivity(overallActivityIntent)
        }

        val db = DBHelper(this)


        // ListView Creator
        // https://www.vogella.com/tutorials/AndroidListView/article.html
        // TODO: Utilize ID maybe for onClick so that we can retrieve trip details
        val listOfTripsGenerator = ListOfTripsGenerator()
        listOfTripsGenerator.generateList(this, db)

        // Button for Adding New Receipt
        val newTripButton = findViewById<FloatingActionButton>(R.id.newTrip)
        newTripButton.setOnClickListener {
            // TODO: Add Functionality for adding new Trip
//            Toast.makeText(this, "New Trip Created", Toast.LENGTH_SHORT).show()
//            db.testDBs()
//            listOfTripsGenerator.generateList(this, db)
            val intent = Intent(this@MainActivity, OCRActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val listOfTripsGenerator = ListOfTripsGenerator()
        val db = DBHelper(this)
        listOfTripsGenerator.generateList(this, db)
    }

}