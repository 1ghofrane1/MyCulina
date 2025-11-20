// data/local/Entities.kt
package com.example.myculina.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnail: String?,
    val category: String?,
    val area: String?,
    val instructions: String?
)

@Entity(tableName = "user_recipes")
data class UserRecipeEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val title: String,
    val category: String?,
    val area: String?,
    val instructions: String?,
    val thumbnail: String? // maybe allow local URI later
)

