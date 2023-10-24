package cs2063.groceryshopper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Open Overall Activity
        val overallActivityIntent = Intent(this, OverallActivity::class.java)
        val overallButton = findViewById<Button>(R.id.viewOverall)
        overallButton.setOnClickListener {
            this.startActivity(overallActivityIntent)
        }

        // ListView Creator
        // https://www.vogella.com/tutorials/AndroidListView/article.html
        // TODO: Utilize ID maybe for onClick so that we can retrieve trip details
        val listGenerator = ListGenerator()
        listGenerator.generateList(this)

        // Button for Adding New Receipt
        val newTripButton = findViewById<FloatingActionButton>(R.id.newTrip)
        newTripButton.setOnClickListener {
            // TODO: Add Functionality for adding new Trip
            Toast.makeText(this, "New Trip Created", Toast.LENGTH_SHORT).show()
        }

        /*binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
    }

}