package com.example.myculina.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myculina.data.local.FavoriteEntity
import com.example.myculina.data.local.UserRecipeEntity
import com.example.myculina.data.local.toMealDto
import com.example.myculina.data.repository.RecipeRepository
import com.example.myculina.model.CategoryDto
import com.example.myculina.model.MealDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipesViewModel(private val repo: RecipeRepository) : ViewModel() {

    private val _meals = MutableStateFlow<List<MealDto>>(emptyList())
    val meals = _meals.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _selectedMeal = MutableStateFlow<MealDto?>(null)
    val selectedMeal = _selectedMeal.asStateFlow()

    // -----------------------------------------
    // INIT
    // -----------------------------------------
    init {
        Log.d("RecipesViewModel", "Initializing...")
        loadDefaultMeals()
        loadCategories()
    }

    // -----------------------------------------
    // FAVORITES from DB
    // -----------------------------------------
    val favorites = repo.getFavorites()

    fun addFavoriteFromMeal(meal: MealDto) {
        viewModelScope.launch {
            try {
                val fav = FavoriteEntity(
                    id = meal.idMeal,
                    title = meal.strMeal ?: "No Title",
                    thumbnail = meal.strMealThumb,
                    category = meal.strCategory,
                    area = meal.strArea,
                    instructions = meal.strInstructions
                )
                repo.addFavorite(fav)
                Log.d("RecipesViewModel", "Added to favorites: ${meal.strMeal}")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Error adding favorite: ${e.message}", e)
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = repo.getCategories()
            } catch (e: Exception) {
                _categories.value = emptyList()
            }
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            try {
                repo.removeFavorite(id)
                Log.d("RecipesViewModel", "Removed from favorites: $id")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Error removing favorite: ${e.message}", e)
            }
        }
    }

    suspend fun checkIfFavorite(id: String): Boolean {
        return try {
            repo.isFavorite(id)
        } catch (e: Exception) {
            Log.e("RecipesViewModel", "Error checking favorite: ${e.message}", e)
            false
        }
    }

    // -----------------------------------------
    // USER RECIPES
    // -----------------------------------------
    val userRecipes = repo.getUserRecipes()

    fun addUserRecipe(
        title: String,
        category: String?,
        area: String?,
        instructions: String?,
        thumbnail: String?
    ) {
        viewModelScope.launch {
            try {
                val entity = UserRecipeEntity(
                    title = title,
                    category = category,
                    area = area,
                    instructions = instructions,
                    thumbnail = thumbnail
                )
                repo.addUserRecipe(entity)
                Log.d("RecipesViewModel", "Added user recipe: $title")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Error adding user recipe: ${e.message}", e)
            }
        }
    }

    fun updateUserRecipe(recipe: UserRecipeEntity) {
        viewModelScope.launch {
            try {
                repo.updateUserRecipe(recipe)
                Log.d("RecipesViewModel", "Updated user recipe: ${recipe.title}")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Error updating user recipe: ${e.message}", e)
            }
        }
    }

    fun deleteUserRecipe(recipe: UserRecipeEntity) {
        viewModelScope.launch {
            try {
                repo.deleteUserRecipe(recipe)
                Log.d("RecipesViewModel", "Deleted user recipe: ${recipe.title}")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Error deleting user recipe: ${e.message}", e)
            }
        }
    }

    // -----------------------------------------
    // SEARCH FUNCTIONS
    // -----------------------------------------

    fun search(query: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d("RecipesViewModel", "Searching for: $query")
                val results = repo.searchMeals(query)
                _meals.value = results
                Log.d("RecipesViewModel", "Search results: ${results.size} meals")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Search error: ${e.message}", e)
                _meals.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchByIngredient(ingredient: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d("RecipesViewModel", "Searching by ingredient: $ingredient")
                val results = repo.searchByIngredient(ingredient)
                _meals.value = results
                Log.d("RecipesViewModel", "Ingredient results: ${results.size} meals")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Ingredient search error: ${e.message}", e)
                _meals.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchByCategory(category: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d("RecipesViewModel", "Searching by category: $category")
                val results = repo.searchByCategory(category)
                _meals.value = results
                Log.d("RecipesViewModel", "Category results: ${results.size} meals")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Category search error: ${e.message}", e)
                _meals.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    // -----------------------------------------
    // DEFAULT MEALS - Using category instead of random
    // -----------------------------------------
    fun loadDefaultMeals() {
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d("RecipesViewModel", "Loading default meals...")
                val results = repo.getDefaultMeals()
                _meals.value = results
                Log.d("RecipesViewModel", "✅ Loaded ${results.size} default meals")

                if (results.isEmpty()) {
                    Log.w("RecipesViewModel", "⚠️ No meals loaded - check network/API")
                }
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "❌ Error loading default meals: ${e.message}", e)
                _meals.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    // -----------------------------------------
    // RECIPE DETAILS - FIXED
    // -----------------------------------------

    // ✅ Main function to load any recipe (API or user recipe)
    fun loadMealDetails(mealId: String) {
        viewModelScope.launch {
            try {
                Log.d("RecipesViewModel", "=== Loading meal details: $mealId ===")

                if (mealId.startsWith("user_")) {
                    // Load user recipe
                    Log.d("RecipesViewModel", "Detected user recipe ID")
                    val idString = mealId.removePrefix("user_")
                    Log.d("RecipesViewModel", "Extracted ID string: $idString")

                    val id = idString.toLongOrNull()
                    Log.d("RecipesViewModel", "Parsed ID: $id")

                    if (id != null) {
                        loadUserRecipeDetails(id)
                    } else {
                        Log.e("RecipesViewModel", "Invalid user recipe ID: $mealId")
                        _selectedMeal.value = null
                    }
                } else {
                    // Load API recipe
                    Log.d("RecipesViewModel", "Loading API recipe")
                    _selectedMeal.value = repo.getMealById(mealId)
                }
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Error loading meal details: ${e.message}", e)
                _selectedMeal.value = null
            }
        }
    }

    // ✅ Load user recipe by ID (called internally by loadMealDetails)
    private suspend fun loadUserRecipeDetails(id: Long) {
        try {
            Log.d("RecipesViewModel", "Loading user recipe with ID: $id")
            val userRecipe = repo.getUserRecipeById(id)

            if (userRecipe != null) {
                // Convert UserRecipeEntity to MealDto
                val mealDto = MealDto(
                    idMeal = "user_${userRecipe.uid}",
                    strMeal = userRecipe.title,
                    strMealThumb = userRecipe.thumbnail,
                    strCategory = userRecipe.category,
                    strArea = userRecipe.area,
                    strInstructions = userRecipe.instructions
                )
                _selectedMeal.value = mealDto
                Log.d("RecipesViewModel", "✅ Loaded user recipe: ${userRecipe.title}")
            } else {
                Log.e("RecipesViewModel", "❌ User recipe not found with ID: $id")
                _selectedMeal.value = null
            }
        } catch (e: Exception) {
            Log.e("RecipesViewModel", "Error loading user recipe: ${e.message}", e)
            _selectedMeal.value = null
        }
    }
}