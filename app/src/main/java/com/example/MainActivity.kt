package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AuthViewModel
import com.example.ui.NotesViewModel
import com.example.ui.screens.AddEditNoteScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val notesViewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentUser = try {
                        FirebaseAuth.getInstance().currentUser
                    } catch (e: IllegalStateException) {
                        null
                    }
                    val startDestination = if (currentUser != null) "dashboard" else "auth"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("auth") {
                            AuthScreen(
                                authViewModel = authViewModel,
                                onAuthSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                notesViewModel = notesViewModel,
                                authViewModel = authViewModel,
                                onAddNoteClick = {
                                    navController.navigate("add_edit_note")
                                },
                                onEditNoteClick = { noteId ->
                                    navController.navigate("add_edit_note?noteId=$noteId")
                                },
                                onLoggedOut = {
                                    navController.navigate("auth") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(
                            route = "add_edit_note?noteId={noteId}",
                            arguments = listOf(navArgument("noteId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")
                            AddEditNoteScreen(
                                notesViewModel = notesViewModel,
                                noteId = noteId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
