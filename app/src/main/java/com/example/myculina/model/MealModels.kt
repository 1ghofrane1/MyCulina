// model/MealModels.kt
package com.example.myculina.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealsResponse(
    @SerialName("meals") val meals: List<MealDto>? = null
)

@Serializable
data class MealDto(
    @SerialName("idMeal") val idMeal: String,
    @SerialName("strMeal") val strMeal: String? = null,
    @SerialName("strCategory") val strCategory: String? = null,
    @SerialName("strArea") val strArea: String? = null,
    @SerialName("strInstructions") val strInstructions: String? = null,
    @SerialName("strMealThumb") val strMealThumb: String? = null,
    // ... optionally map strIngredient1..20 if needed
)

@Serializable
data class CategoriesResponse(
    val categories: List<CategoryDto>
)

@Serializable
data class CategoryDto(
    val idCategory: String,
    val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
)

