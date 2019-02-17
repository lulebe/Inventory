package de.lulebe.inventory.data

import android.arch.persistence.room.TypeConverter
import java.util.*

class TypeConverters {

    @TypeConverter
    fun UUIDtoString (uuid: UUID) = uuid.toString()
    @TypeConverter
    fun StringtoUUID (string: String) = UUID.fromString(string)

}