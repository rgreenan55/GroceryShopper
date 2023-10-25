package cs2063.groceryshopper.model

data class Trip(val id: Int, val total: Double, val date: String, val storeName: String){
    override fun toString(): String {
        return "Trip: {id: $id, total: $total, date: $date, storeName: $storeName}"
    }
}
