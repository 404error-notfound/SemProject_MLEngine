package com.example.fakenewsdetector.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One saved check, shown on the History screen. Lists are stored as delimited
 * text so no Room TypeConverters or JSON parser are needed.
 */
@Entity(tableName = "checks")
data class CheckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val verdict: String,      // Verdict.name
    val confidence: Float,
    /** Kept from version 1 for migration compatibility. */
    val reason: String,
    val summary: String,
    val reasons: String,      // encoded list
    val sources: String,      // encoded FactCheckSource list
    val language: String,     // Language.name
    val sourceApp: String?,
    val createdAt: Long
)
