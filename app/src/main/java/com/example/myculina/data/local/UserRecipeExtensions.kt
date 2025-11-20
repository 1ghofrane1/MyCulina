package com.example.myculina.data.local

import com.example.myculina.model.MealDto

fun UserRecipeEntity.toMealDto(): MealDto {
    return MealDto(
        idMeal = "user_${this.uid}",
        strMeal = this.title,
        strMealThumb = this.thumbnail,
        strCategory = this.category,
        strArea = this.area,
        strInstructions = this.instructions
    )
}
