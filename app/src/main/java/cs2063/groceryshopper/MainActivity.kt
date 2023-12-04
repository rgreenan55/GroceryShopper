package cs2063.groceryshopper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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

        // TODO: Remove This ? Or keep for initial data
//        db.testDBs()

        // ListView Creator
        val listOfTripsGenerator = ListOfTripsGenerator()
        listOfTripsGenerator.generateList(this, db)

        // Button for Adding New Receipt
        val newTripButton = findViewById<FloatingActionButton>(R.id.newTrip)
        newTripButton.setOnClickListener {
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