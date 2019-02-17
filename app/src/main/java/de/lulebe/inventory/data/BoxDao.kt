package de.lulebe.inventory.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import java.util.*

@Dao
interface BoxDao {

    @Query("SELECT * FROM boxes")
    fun getAllBoxes() : List<Box>

    @Query("SELECT b.id, b.name, COUNT(i.id) as itemCount FROM boxes b LEFT JOIN items i ON i.boxId=b.id GROUP BY b.id")
    fun getAllBoxesWithContentLive() : LiveData<List<BoxWithContent>>

    @Query("SELECT b.id, b.name, COUNT(i.id) as itemCount FROM boxes b LEFT JOIN items i ON i.boxId=b.id WHERE b.id=:boxId GROUP BY b.id")
    fun getBoxWithContentLive(boxId: UUID) : LiveData<BoxWithContent>

    @Insert
    fun insertBox(box: Box)

    @Delete
    fun deleteBox(box: Box)

}