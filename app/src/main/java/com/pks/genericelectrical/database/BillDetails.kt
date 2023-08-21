package com.pks.genericelectrical.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "electricity_table")
data class BillDetails(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var customer_serial_number: String,
    var meter_reading: String
)