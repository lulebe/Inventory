package de.lulebe.inventory.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import java.util.*

@Dao
interface ItemDao {

    @Query("SELECT COUNT(*) FROM items")
    fun getTotalItemCount() : Int

    @Query("SELECT COUNT(*) FROM items WHERE boxId=:boxId")
    fun getBoxItemCountLive(boxId: UUID) : LiveData<Int>

    @Query("SELECT * FROM items WHERE boxId=:boxId")
    fun getItemsInBox(boxId: UUID) : List<Item>

    @Query("SELECT * FROM items WHERE boxId=:boxId")
    fun getItemsInBoxLive(boxId: UUID) : LiveData<List<Item>>

    @Insert
    fun insertItem(item: Item)

    @Delete
    fun deleteItem(item: Item)

}