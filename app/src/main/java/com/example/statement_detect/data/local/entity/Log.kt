package com.example.statement_detect.data.local.entity

import android.health.connect.datatypes.units.Percentage
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class Log(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "records")
    val records: List<Record>,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "percent")
    val percentage: Float
)

