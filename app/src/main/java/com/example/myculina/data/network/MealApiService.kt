package com.example.myculina.data.network

import android.util.Log
import com.example.myculina.model.CategoriesResponse
import com.example.myculina.model.CategoryDto
import com.example.myculina.model.MealDto
import com.example.myculina.model.MealsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class MealApiService(private val client: HttpClient = KtorClient.client) {

    private val base = "https://www.themealdb.com/api/json/v1/1"

    // 🔍 Search by name (search.php?s=)
    suspend fun searchMealsByName(query: String): List<MealDto> = try {
        Log.d("MealAPI", "Searching for: $query")
        val response = client.get("$base/search.php") {
            parameter("s", query)
        }.body<MealsResponse>()
        Log.d("MealAPI", "Search found ${response.meals?.size ?: 0} meals")
        response.meals ?: emptyList()
    } catch (e: Exception) {
        Log.e("MealAPI", "Search error: ${e.message}", e)
        emptyList()
    }

    // 🔍 Filter by ingredient (filter.php?i=)
    suspend fun filterByIngredient(ingredient: String): List<MealDto> = try {
        Log.d("MealAPI", "Filtering by ingredient: $ingredient")
        val response = client.get("$base/filter.php") {
            parameter("i", ingredient)
        }.body<MealsResponse>()
        Log.d("MealAPI", "Filter found ${response.meals?.size ?: 0} meals")
        response.meals ?: emptyList()
    } catch (e: Exception) {
        Log.e("MealAPI", "Filter error: ${e.message}", e)
        emptyList()
    }

    // 📁 Filter by category (filter.php?c=) - BETTER for default display
    suspend fun filterByCategory(category: String): List<MealDto> = try {
        Log.d("MealAPI", "Filtering by category: $category")
        val response = client.get("$base/filter.php") {
            parameter("c", category)
        }.body<MealsResponse>()
        Log.d("MealAPI", "Category found ${response.meals?.size ?: 0} meals")
        response.meals ?: emptyList()
    } catch (e: Exception) {
        Log.e("MealAPI", "Category filter error: ${e.message}", e)
        emptyList()
    }

    // 🔍 Get full recipe by ID (lookup.php?i=)
    suspend fun getMealById(id: String): MealDto? = try {
        Log.d("MealAPI", "Getting meal by ID: $id")
        val response = client.get("$base/lookup.php") {
            parameter("i", id)
        }.body<MealsResponse>()
        response.meals?.firstOrNull()
    } catch (e: Exception) {
        Log.e("MealAPI", "Get meal error: ${e.message}", e)
        null
    }

    // 🎲 Get random meal - keep for single random only
    suspend fun getRandomMeal(): MealDto? = try {
        Log.d("MealAPI", "Getting random meal")
        client.get("$base/random.php")
            .body<MealsResponse>()
            .meals?.firstOrNull()
    } catch (e: Exception) {
        Log.e("MealAPI", "Random meal error: ${e.message}", e)
        null
    }

    suspend fun getCategories(): List<CategoryDto> {
        return client.get {
            url("https://www.themealdb.com/api/json/v1/1/categories.php")
        }.body<CategoriesResponse>().categories
    }

}