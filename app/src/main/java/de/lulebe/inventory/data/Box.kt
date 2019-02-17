package de.lulebe.inventory.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "boxes"
)
data class Box(
    @PrimaryKey val id: UUID,
    var name: String
)