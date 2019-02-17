package de.lulebe.inventory.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "images")
data class Image (
    @PrimaryKey val id: UUID,
    val file: String
)