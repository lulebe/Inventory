package de.lulebe.inventory.data

import java.util.*

data class BoxWithContent(
    val id: UUID,
    var name: String,
    var itemCount: Int
)