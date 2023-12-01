package cs2063.groceryshopper.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cs2063.groceryshopper.model.Trip
import android.util.Log
import cs2063.groceryshopper.model.Item
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class DBHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TRIP_TABLE_NAME (" +
                    "$TRIP_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "$TRIP_COLUMN_TOTAL REAL," +
                    "$TRIP_COLUMN_DATE LONG," +
                    "$TRIP_COLUMN_STORE_NAME TEXT);"
        )
        db.execSQL(
            "CREATE TABLE $ITEM_TABLE_NAME (" +
                    "$ITEM_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "$ITEM_COLUMN_TRIP_ID INTEGER," +
                    "$ITEM_COLUMN_PRICE REAL," +
                    "$ITEM_COLUMN_ITEM_NAME TEXT," +
                    "$ITEM_COLUMN_ARCHIVED BOOLEAN DEFAULT 0);"
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
        val l = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val unix = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
        Log.i("Insert Trip", unix.toString())
        contentValues.put(TRIP_COLUMN_DATE, unix)
        contentValues.put(TRIP_COLUMN_STORE_NAME, storeName)
        db.insert(TRIP_TABLE_NAME, null, contentValues)
        return true
    }

    fun updateTrip(id: Int, total: Double, date: String, storeName: String ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TRIP_COLUMN_ID, id)
        contentValues.put(TRIP_COLUMN_TOTAL, total)
        val l = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val unix = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
        contentValues.put(TRIP_COLUMN_DATE, unix)
        contentValues.put(TRIP_COLUMN_STORE_NAME, storeName)
        db.update(TRIP_TABLE_NAME, contentValues, "$TRIP_COLUMN_ID = ? ", arrayOf((id).toString()))
        return true
    }

    fun deleteTrip(id: Int): Int {
        val db = this.writableDatabase
        db.delete(ITEM_TABLE_NAME, "$ITEM_COLUMN_TRIP_ID = ? ", arrayOf((id).toString()))
        return db.delete(TRIP_TABLE_NAME, "$TRIP_COLUMN_ID = ? ", arrayOf((id).toString()))
    }

    fun getAllTrips(): ArrayList<Trip>?{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from $TRIP_TABLE_NAME", null)
        return formatTripsList(res)
    }

    fun getAllTripsSortByDate(descending: Boolean): ArrayList<Trip>?{
        val db = this.readableDatabase
        val dir = if (descending) "DESC" else "ASC"
        val res = db.rawQuery("select * from $TRIP_TABLE_NAME ORDER BY $TRIP_COLUMN_DATE $dir", null)
        return formatTripsList(res)
    }

    fun getPastMonthsTrips(minDate: Long, maxDate: Long): ArrayList<Trip>?{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from $TRIP_TABLE_NAME WHERE $TRIP_COLUMN_DATE > $minDate AND $TRIP_COLUMN_DATE < $maxDate ORDER BY $TRIP_COLUMN_DATE ASC", null)
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
            Log.i("Read Trip", res.getLong(dateCol).toString())
            val date = SimpleDateFormat("yyyy-MM-dd").format(Date(res.getLong(dateCol)*1000))
            output.add(Trip(res.getInt(idCol), res.getDouble(totalCol), date, res.getString(storeNameCol)))
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
        db.execSQL("UPDATE $TRIP_TABLE_NAME SET $TRIP_COLUMN_TOTAL = $TRIP_COLUMN_TOTAL + $price WHERE $TRIP_COLUMN_ID = $tripId")
        return true
    }

    fun updateItem(id: Int, tripId: Int, price: Double, itemName: String ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ITEM_COLUMN_ID, id)
        contentValues.put(ITEM_COLUMN_TRIP_ID, tripId)
        contentValues.put(ITEM_COLUMN_PRICE, price)
        contentValues.put(ITEM_COLUMN_ITEM_NAME, itemName)
        db.update(ITEM_TABLE_NAME, contentValues, "$ITEM_COLUMN_ID = ? ", arrayOf((id).toString()))
        return true
    }

    fun deleteItem(id: Int): Int {
        val db = this.writableDatabase
        val item = getItem(id)!!
        val price = item.price
        val tripId = item.tripId
        if (!item.archived) {
            db.execSQL("UPDATE $TRIP_TABLE_NAME SET $TRIP_COLUMN_TOTAL = $TRIP_COLUMN_TOTAL - $price WHERE $TRIP_COLUMN_ID = $tripId")
        }
        return db.delete(ITEM_TABLE_NAME, "$ITEM_COLUMN_ID = ? ", arrayOf((id).toString()))
    }

    fun deleteItem(id: Int, tripId: Int, price: Double, archived: Boolean): Int {
        val db = this.writableDatabase
        Log.i("Delete", archived.toString())
        if (!archived) {
            db.execSQL("UPDATE $TRIP_TABLE_NAME SET $TRIP_COLUMN_TOTAL = $TRIP_COLUMN_TOTAL - $price WHERE $TRIP_COLUMN_ID = $tripId")
        }
        return db.delete(ITEM_TABLE_NAME, "$ITEM_COLUMN_ID = ? ", arrayOf((id).toString()))
    }

    fun archiveItem(id: Int){
        val db = this.writableDatabase
        val item = getItem(id)!!
        val tripId = item.tripId
        val archive = item.archived
        val price = if (archive) item.price else -item.price
        db.execSQL("UPDATE $TRIP_TABLE_NAME SET $TRIP_COLUMN_TOTAL = $TRIP_COLUMN_TOTAL + $price WHERE $TRIP_COLUMN_ID = $tripId")
        db.execSQL("UPDATE $ITEM_TABLE_NAME SET $ITEM_COLUMN_ARCHIVED = NOT $ITEM_COLUMN_ARCHIVED WHERE $ITEM_COLUMN_ID = $id")
    }

    fun archiveItem(id : Int, tripId: Int, price: Double, archived: Boolean){
        val db = this.writableDatabase
        val addSub = if (archived) '+' else '-'
        val archive = if (archived) 0 else 1
        Log.i("Archive", price.toString())
        Log.i("Archive", "UPDATE $TRIP_TABLE_NAME SET $TRIP_COLUMN_TOTAL = $TRIP_COLUMN_TOTAL $addSub $price WHERE $TRIP_COLUMN_ID = $tripId")
        db.execSQL("UPDATE $TRIP_TABLE_NAME SET $TRIP_COLUMN_TOTAL = $TRIP_COLUMN_TOTAL $addSub $price WHERE $TRIP_COLUMN_ID = $tripId")
        db.execSQL("UPDATE $ITEM_TABLE_NAME SET $ITEM_COLUMN_ARCHIVED = $archive WHERE $ITEM_COLUMN_ID = $id")
    }

    fun getItem(id: Int): Item{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from $ITEM_TABLE_NAME WHERE $ITEM_COLUMN_ID = $id", null)
        return formatItemList(res)!![0]
    }
    fun getAllItems(): ArrayList<Item>?{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from $ITEM_TABLE_NAME", null)
        return formatItemList(res)
    }

    fun getAllItemsForTrip(tripId: Int): ArrayList<Item>?{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from $ITEM_TABLE_NAME where $ITEM_COLUMN_TRIP_ID = $tripId ORDER BY $ITEM_COLUMN_ARCHIVED", null)
        return formatItemList(res)
    }

    fun getAllItemsForTripSortByPrice(tripId: Int, descending: Boolean): ArrayList<Item>?{
        val db = this.readableDatabase
        val dir = if (descending) "DESC" else "ASC"
        val res = db.rawQuery("select * from $ITEM_TABLE_NAME where $ITEM_COLUMN_TRIP_ID = $tripId ORDER BY $ITEM_COLUMN_ARCHIVED, $ITEM_COLUMN_PRICE $dir", null)
        return formatItemList(res)
    }
    fun getAllItemsForTripSortByName(tripId: Int, aToZ: Boolean): ArrayList<Item>?{
        val db = this.readableDatabase
        val dir = if (aToZ) "ASC" else "DESC"
        val res = db.rawQuery("select * from $ITEM_TABLE_NAME where $ITEM_COLUMN_TRIP_ID = $tripId ORDER BY $ITEM_COLUMN_ARCHIVED, $ITEM_COLUMN_ITEM_NAME $dir", null)
        return formatItemList(res)
    }

    private fun formatItemList(res: Cursor): ArrayList<Item>?{
        val output = ArrayList<Item>()
        res.moveToFirst()
        val idCol = res.getColumnIndex(ITEM_COLUMN_ID)
        val tripIdCol = res.getColumnIndex(ITEM_COLUMN_TRIP_ID)
        val priceCol = res.getColumnIndex(ITEM_COLUMN_PRICE)
        val itemNameCol = res.getColumnIndex(ITEM_COLUMN_ITEM_NAME)
        val archivedCol = res.getColumnIndex(ITEM_COLUMN_ARCHIVED)
        //Log.i("Items", "idCol: $idCol, tripIdCol: $tripIdCol, priceCol: $priceCol, itemNameCol: $itemNameCol")
        if (idCol == -1 || tripIdCol == -1 || priceCol == -1 || itemNameCol == -1) return null
        while (!res.isAfterLast) {
            output.add(Item(res.getInt(idCol), res.getInt(tripIdCol), res.getDouble(priceCol), res.getString(itemNameCol), res.getInt(archivedCol) == 1))
            res.moveToNext()
        }
        res.close()
        //Log.i("Items", "There are " + output.size + " items")
        return output
    }

    private fun createTestData(){
        insertTrip(0.0, "2023-10-27", "Sobey's")
        insertTrip(0.0, "2023-11-01", "Walmart")
        insertTrip(0.0, "2023-11-03", "Anti-Cancer Store")

        insertItem(1, 8.23, "Hot Dogs")
        insertItem(1, 5.14, "Buns")

        insertItem(2, 8.76, "Half Dozen Eggs")
        insertItem(2, 5.39, "Chocolate")
        insertItem(2, 2.35, "Apples")
        insertItem(2, 4.67, "Chips")
        insertItem(2, 14.27, "Ground Beef")
        insertItem(2, 20.25, "Chicken")
        insertItem(2, 12.32, "Ice Cream")
        insertItem(2, 7.92, "Milk")
        insertItem(2, 5.52, "Bread")
        insertItem(2, 8.95, "Peanut Butter")
        insertItem(2, 18.23, "Paper Towel")
        insertItem(2, 2.37, "Gum")

        insertItem(3, 1.1, "Anti-Cancer")

    }

    private fun logDBs(){
        Log.i("Trips", "Getting All Trips:")
        logTripQuery(getAllTrips())
        Log.i("Items", "Getting Items For TripId 1:")
        logItemQuery(getAllItemsForTrip(1))
        Log.i("Items", "Getting All Items:")
        logItemQuery(getAllItems())
    }

    fun logTripQuery(list: ArrayList<Trip>?){
        if(list == null){
            Log.i("Trips", "Trips list not created")
        }
        else if(list.size == 0){
            Log.i("Trips", "Trips List empty")
        }
        else{
            for(data in list) {
                Log.i("Trips", data.toString())
            }
        }
    }

    fun logItemQuery(list: ArrayList<Item>?){
        if(list == null){
            Log.i("Items", "Item list not created")
        }
        else if(list.size == 0){
            Log.i("Items", "Item List empty")
        }
        else{
            for(data in list) {
                Log.i("Items", data.toString())
            }
        }
    }

    fun testDBs(){
        remakeDBs()
        createTestData()
        logDBs()
    }

    companion object {
        const val DATABASE_NAME = "GroceryShopper.db"

        const val TRIP_TABLE_NAME = "Trips"
        const val TRIP_COLUMN_ID = "tripID"                  // INT
        const val TRIP_COLUMN_TOTAL = "tripTotal"            // NUMBER (Double)
        const val TRIP_COLUMN_DATE = "tripDate"              // LONG
        const val TRIP_COLUMN_STORE_NAME = "storeName"       // STRING

        const val ITEM_TABLE_NAME = "Items"
        const val ITEM_COLUMN_ID = "itemID"                  // INT
        const val ITEM_COLUMN_TRIP_ID = "tripID"             // INT
        const val ITEM_COLUMN_PRICE = "price"                // NUMBER (Double)
        const val ITEM_COLUMN_ITEM_NAME = "itemName"         // STRING
        const val ITEM_COLUMN_ARCHIVED = "archived"          // BOOLEAN
    }

}