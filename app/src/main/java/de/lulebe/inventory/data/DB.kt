package de.lulebe.inventory.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

@Database(
    version = 1,
    entities = [Box::class, Item::class, Image::class]
)
@TypeConverters(de.lulebe.inventory.data.TypeConverters::class)
abstract class DB : RoomDatabase() {
    companion object {
        private var instance: DB? = null
        fun getInstance (ctx: Context) : DB {
            if (instance == null)
                instance = Room.databaseBuilder(ctx.applicationContext, DB::class.java, "appdb").build()
            return instance!!
        }
    }
    abstract fun boxDao() : BoxDao
    abstract fun itemDao() : ItemDao
}