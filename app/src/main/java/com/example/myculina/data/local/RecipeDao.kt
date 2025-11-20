package com.example.myculina.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    // Favorites
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    // User recipes
    @Query("SELECT * FROM user_recipes")
    fun getUserRecipes(): Flow<List<UserRecipeEntity>>

    @Insert
    suspend fun insertUserRecipe(recipe: UserRecipeEntity)

    @Delete
    suspend fun deleteUserRecipe(recipe: UserRecipeEntity)

    @Update
    suspend fun updateUserRecipe(recipe: UserRecipeEntity)

    @Query("SELECT * FROM user_recipes WHERE uid = :id")
    suspend fun getUserRecipeById(id: Long): UserRecipeEntity?

}
