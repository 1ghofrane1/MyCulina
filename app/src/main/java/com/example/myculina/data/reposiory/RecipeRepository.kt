package com.example.myculina.data.repository

import com.example.myculina.data.local.FavoriteEntity
import com.example.myculina.data.local.RecipeDao
import com.example.myculina.data.local.UserRecipeEntity
import com.example.myculina.data.network.MealApiService
import com.example.myculina.model.CategoryDto
import com.example.myculina.model.MealDto
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val api: MealApiService,
    private val dao: RecipeDao
) {

    // Network - Search
    suspend fun searchMeals(query: String): List<MealDto> =
        api.searchMealsByName(query)

    suspend fun searchByIngredient(ingredient: String): List<MealDto> =
        api.filterByIngredient(ingredient)

    suspend fun searchByCategory(category: String): List<MealDto> =
        api.filterByCategory(category)

    suspend fun getMealById(id: String): MealDto? =
        api.getMealById(id)

    suspend fun getRandomMeal(): MealDto? =
        api.getRandomMeal()

    suspend fun getCategories(): List<CategoryDto> =
        api.getCategories()





    // ✅ Get default meals - use popular category instead of random
    suspend fun getDefaultMeals(): List<MealDto> {
        // Try multiple popular categories to ensure we get results
        val categories = listOf("Seafood", "Chicken", "Beef", "Dessert", "Vegetarian")

        for (category in categories) {
            val meals = api.filterByCategory(category)
            if (meals.isNotEmpty()) {
                return meals.take(10) // Return first 10 meals
            }
        }

        // Fallback: return empty if all fail
        return emptyList()
    }

    // Local DB - Favorites
    fun getFavorites(): Flow<List<FavoriteEntity>> = dao.getAllFavorites()
    suspend fun addFavorite(f: FavoriteEntity) = dao.insertFavorite(f)
    suspend fun removeFavorite(id: String) = dao.deleteFavoriteById(id)
    suspend fun isFavorite(id: String) = dao.isFavorite(id)

    // Local DB - User recipes
    suspend fun getUserRecipeById(id: Long): UserRecipeEntity? =
        dao.getUserRecipeById(id)

    fun getUserRecipes(): Flow<List<UserRecipeEntity>> = dao.getUserRecipes()
    suspend fun addUserRecipe(r: UserRecipeEntity) = dao.insertUserRecipe(r)
    suspend fun updateUserRecipe(r: UserRecipeEntity) = dao.updateUserRecipe(r)
    suspend fun deleteUserRecipe(r: UserRecipeEntity) = dao.deleteUserRecipe(r)
}