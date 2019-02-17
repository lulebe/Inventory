package de.lulebe.inventory.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(childColumns = ["boxId"], parentColumns = ["id"], entity = Box::class, onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(childColumns = ["imageId"], parentColumns = ["id"], entity = Image::class, onUpdate = ForeignKey.NO_ACTION, onDelete = ForeignKey.NO_ACTION)
    ],
    indices = [Index("boxId"), Index("commonId"), Index("name")]
)
data class Item(
    @PrimaryKey val id: UUID,
    val commonId: UUID,
    var boxId: UUID,
    var name: String,
    val imageId: UUID,
    var unit: String,
    var amount: Int,
    var hasImage: Boolean
)