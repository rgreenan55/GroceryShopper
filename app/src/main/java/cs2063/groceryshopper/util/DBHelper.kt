package cs2063.groceryshopper.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cs2063.groceryshopper.model.Trip
import android.util.Log
import cs2063.groceryshopper.model.Item

class DBHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TRIP_TABLE_NAME (" +
                    "$TRIP_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "$TRIP_COLUMN_TOTAL REAL," +
                    "$TRIP_COLUMN_DATE TEXT," +
                    "$TRIP_COLUMN_STORE_NAME TEXT);"
        )
        db.execSQL(
            "CREATE TABLE $ITEM_TABLE_NAME (" +
                    "$ITEM_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "$ITEM_COLUMN_TRIP_ID INTEGER," +
                    "$ITEM_COLUMN_PRICE REAL," +
                    "$ITEM_COLUMN_ITEM_NAME TEXT);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TRIP_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $ITEM_TABLE_NAME")
        onCreate(db)
    }

    fun remakeDBs(){
        onUpgrade(this.writableDatabase, 0,1)
    }

    fun insertTrip(total: Double, date: String, storeName: String ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TRIP_COLUMN_TOTAL, total)
        contentValues.put(TRIP_COLUMN_DATE, date)
        contentValues.put(TRIP_COLUMN_STORE_NAME, storeName)
        db.insert(TRIP_TABLE_NAME, null, contentValues)
        return true
    }

    fun updateTrip(id: Int, total: Double, date: String, storeName: String ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TRIP_COLUMN_ID, id)
        contentValues.put(TRIP_COLUMN_TOTAL, total)
        contentValues.put(TRIP_COLUMN_DATE, date)
        contentValues.put(TRIP_COLUMN_STORE_NAME, storeName)
        db.update(TRIP_TABLE_NAME, contentValues, "id = ? ", arrayOf((id).toString()))
        return true
    }

    fun deleteTrip(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TRIP_TABLE_NAME, "$TRIP_COLUMN_ID = ? ", arrayOf((id).toString()))
    }

    fun getAllTrips(): ArrayList<Trip>?{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from $TRIP_TABLE_NAME", null)
        return formatTripsList(res)
    }

    private fun formatTripsList(res: Cursor): ArrayList<Trip>?{
        val output = ArrayList<Trip>()
        res.moveToFirst()
        val idCol = res.getColumnIndex(TRIP_COLUMN_ID)
        val totalCol = res.getColumnIndex(TRIP_COLUMN_TOTAL)
        val dateCol = res.getColumnIndex(TRIP_COLUMN_DATE)
        val storeNameCol = res.getColumnIndex(TRIP_COLUMN_STORE_NAME)
        //Log.i("Trips", "idCol: $idCol, totalCol: $totalCol, dateCol: $dateCol, storeNameCol: $storeNameCol")
        if (idCol == -1 || totalCol == -1 || dateCol == -1 || storeNameCol == -1) return null
        while (!res.isAfterLast) {
            output.add(Trip(res.getInt(idCol), res.getDouble(totalCol), res.getString(dateCol), res.getString(storeNameCol)))
            res.moveToNext()
        }
        //Log.i("Trips", "There are " + output.size + " trips")
        res.close()
        return output
    }

    fun insertItem(tripId: Int, price: Double, itemName: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ITEM_COLUMN_TRIP_ID, tripId)
        contentValues.put(ITEM_COLUMN_PRICE, price)
        contentValues.put(ITEM_COLUMN_ITEM_NAME, itemName)
        db.insert(ITEM_TABLE_NAME, null, contentValues)
        return true
    }

    fun updateItem(id: Int, tripId: Int, price: Double, itemName: String ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ITEM_COLUMN_ID, id)
        contentValues.put(ITEM_COLUMN_TRIP_ID, tripId)
        contentValues.put(ITEM_COLUMN_PRICE, price)
        contentValues.put(ITEM_COLUMN_ITEM_NAME, itemName)
        db.update(ITEM_TABLE_NAME, contentValues, "id = ? ", arrayOf((id).toString()))
        return true
    }

    fun deleteItem(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(ITEM_TABLE_NAME, "id = ? ", arrayOf((id).toString()))
    }

    fun getAllItems(): ArrayList<Item>?{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from $ITEM_TABLE_NAME", null)
        return formatItemList(res)
    }

    fun getAllItemsForTrip(tripId: Int): ArrayList<Item>?{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from $ITEM_TABLE_NAME where $ITEM_COLUMN_TRIP_ID = $tripId", null)
        return formatItemList(res)
    }

    private fun formatItemList(res: Cursor): ArrayList<Item>?{
        val output = ArrayList<Item>()
        res.moveToFirst()
        val idCol = res.getColumnIndex(ITEM_COLUMN_ID)
        val tripIdCol = res.getColumnIndex(ITEM_COLUMN_TRIP_ID)
        val priceCol = res.getColumnIndex(ITEM_COLUMN_PRICE)
        val itemNameCol = res.getColumnIndex(ITEM_COLUMN_ITEM_NAME)
        //Log.i("Items", "idCol: $idCol, tripIdCol: $tripIdCol, priceCol: $priceCol, itemNameCol: $itemNameCol")
        if (idCol == -1 || tripIdCol == -1 || priceCol == -1 || itemNameCol == -1) return null
        while (!res.isAfterLast) {
            output.add(Item(res.getInt(idCol), res.getInt(tripIdCol), res.getDouble(priceCol), res.getString(itemNameCol)))
            res.moveToNext()
        }
        res.close()
        //Log.i("Items", "There are " + output.size + " items")
        return output
    }

    fun testDBs(){
        remakeDBs()
        insertTrip(420.0, "Oct 3, 2032", "High Times")
        insertTrip(0.69, "Oct 5, 2032", "Dolls R Us")
        val tripsList = getAllTrips()
        Log.i("Trips", "Getting All Trips:")
        if(tripsList == null){
            Log.i("Trips", "Trips list not created")
        }
        else if(tripsList.size == 0){
            Log.i("Trips", "Trips List empty")
        }
        else{
            for(trip in tripsList) {
                Log.i("Trips", trip.toString())
            }
        }
        insertItem(1, 400.0, "High End Pipe")
        insertItem(1, 20.0, "Filters")
        insertItem(2, 0.69, "Lube Sample")
        val itemsList = getAllItemsForTrip(1)
        Log.i("Items", "Getting items for TripId 1")
        if(itemsList == null){
            Log.i("Items", "Trips list not created")
        }
        else if(itemsList.size == 0){
            Log.i("Items", "Trips List empty")
        }
        else{
            for(item in itemsList) {
                Log.i("Items", item.toString())
            }
        }
        val itemsList2 = getAllItems()
        Log.i("Items", "Getting items for All Trips")
        if(itemsList2 == null){
            Log.i("Items", "Trips list not created")
        }
        else if(itemsList2.size == 0){
            Log.i("Items", "Trips List empty")
        }
        else{
            for(item in itemsList2) {
                Log.i("Items", item.toString())
            }
        }
    }

    companion object {
        const val DATABASE_NAME = "GroceryShopper.db"

        const val TRIP_TABLE_NAME = "Trips"
        const val TRIP_COLUMN_ID = "tripID"                  // INT
        const val TRIP_COLUMN_TOTAL = "tripTotal"            // NUMBER (Double)
        const val TRIP_COLUMN_DATE = "tripDate"              // STRING (idk how to have it work if this is a DATE)
        const val TRIP_COLUMN_STORE_NAME = "storeName"       // STRING

        const val ITEM_TABLE_NAME = "Items"
        const val ITEM_COLUMN_ID = "itemID"                  // INT
        const val ITEM_COLUMN_TRIP_ID = "tripID"             // INT
        const val ITEM_COLUMN_PRICE = "price"                // NUMBER (Double)
        const val ITEM_COLUMN_ITEM_NAME = "itemName"         // STRING
    }

}