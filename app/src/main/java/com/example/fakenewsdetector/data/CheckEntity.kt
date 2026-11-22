package com.example.fakenewsdetector.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One saved check, shown on the History screen. */
@Entity(tableName = "checks")
data class CheckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val verdict: String,      // Verdict.name
    val confidence: Float,
    val reason: String,
    val language: String,     // Language.name
    val createdAt: Long = System.currentTimeMillis()
)
