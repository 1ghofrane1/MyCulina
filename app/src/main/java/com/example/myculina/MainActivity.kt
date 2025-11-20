package com.example.myculina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myculina.data.auth.AuthRepository
import com.example.myculina.data.local.DatabaseModule
import com.example.myculina.data.network.MealApiService
import com.example.myculina.data.repository.RecipeRepository
import com.example.myculina.ui.screens.*
import com.example.myculina.ui.viewmodel.AuthViewModel
import com.example.myculina.ui.viewmodel.RecipesViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize repositories
        val db = DatabaseModule.provideDatabase(applicationContext)
        val api = MealApiService()
        val recipeRepo = RecipeRepository(api, db.recipeDao())
        val authRepo = AuthRepository(applicationContext)

        setContent {
            val navController = rememberNavController()

            // ✅ ViewModels
            val recipesVM: RecipesViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { RecipesViewModel(recipeRepo) }
                }
            )

            val authVM: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { AuthViewModel(authRepo) }
                }
            )

            // Check if user is authenticated
            val startDestination = if (authVM.isUserSignedIn()) "main" else "login"

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                // ✅ Login screen
                composable("login") {
                    LoginScreen(
                        authViewModel = authVM,
                        onLoginSuccess = {
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                // ✅ Main screen with tabs (Discover + My Recipes)
                composable("main") {
                    TabbedMainScreen(
                        recipesViewModel = recipesVM,
                        navController = navController,
                        onOpenFavorites = { navController.navigate("favorites") },
                        // ✅ FIX: This receives the FULL meal ID (either "52772" or "user_1")
                        onRecipeClick = { mealId ->
                            navController.navigate("detail/$mealId")
                        },
                        onAddRecipe = { navController.navigate("add") },
                        onEditRecipe = { recipeId: Long ->
                            navController.navigate("edit/$recipeId")
                        },
                        onSignOut = {
                            authVM.signOut()
                            navController.navigate("login") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    )
                }

                // Recipe detail screen with mealId parameter
                composable(
                    route = "detail/{mealId}",
                    arguments = listOf(navArgument("mealId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val mealId = backStackEntry.arguments?.getString("mealId") ?: ""
                    RecipeDetailScreen(
                        mealId = mealId,
                        recipesViewModel = recipesVM,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Favorites screen
                composable("favorites") {
                    FavoritesScreen(
                        recipesViewModel = recipesVM,
                        onBack = { navController.popBackStack() },
                        onRecipeClick = { mealId ->
                            navController.navigate("detail/$mealId")
                        }
                    )
                }

                // Add recipe screen
                composable("add") {
                    AddRecipeScreen(
                        recipesViewModel = recipesVM,
                        onDone = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Edit recipe screen
                composable(
                    route = "edit/{recipeId}",
                    arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
                    EditRecipeScreen(
                        recipeId = recipeId,
                        recipesViewModel = recipesVM,
                        onDone = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}