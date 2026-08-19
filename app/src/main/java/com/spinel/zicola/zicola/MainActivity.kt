package com.spinel.zicola.zicola

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.isSystemInDarkTheme
import com.spinel.zicola.zicola.data.PreferencesManager
import com.spinel.zicola.zicola.ui.screens.SettingsScreen
import com.spinel.zicola.zicola.navigation.Route
import com.spinel.zicola.zicola.ui.screens.BookDetailsScreen
import com.spinel.zicola.zicola.ui.screens.HomeScreen
import com.spinel.zicola.zicola.ui.screens.ReaderScreen
import com.spinel.zicola.zicola.ui.theme.ZicolaTheme
import com.spinel.zicola.zicola.ui.viewmodel.HomeViewModel
import com.spinel.zicola.zicola.ui.viewmodel.ReaderViewModel

import com.spinel.zicola.zicola.ui.screens.ChaptersScreen

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted.
        } else {
            // Permission denied.
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        askNotificationPermission()
        enableEdgeToEdge()
        val prefs = PreferencesManager(this)
        setContent {
            val appTheme by prefs.appThemeFlow.collectAsState(initial = "SYSTEM")
            val darkTheme = when (appTheme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            ZicolaTheme(darkTheme = darkTheme) {
                ZicolaApp(prefs)
            }
        }
    }
}

@Composable
fun ZicolaApp(preferencesManager: PreferencesManager) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val readerViewModel: ReaderViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Route.Home.route
    ) {
composable(Route.Settings.route) {
            SettingsScreen(
                preferencesManager = preferencesManager,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Route.Home.route) {
            val books by homeViewModel.booksWithProgress.collectAsState()
            HomeScreen(
                booksWithProgress = books,
                onSettingsClick = { navController.navigate(Route.Settings.route) },
                onBookClick = { bookId ->
                    navController.navigate(Route.BookDetails.createRoute(bookId))
                }
            )
        }
        
        composable(
            route = Route.BookDetails.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            val books by homeViewModel.booksWithProgress.collectAsState()
            val bookWithProgress = books.find { it.book.id == bookId }
            
            if (bookWithProgress != null) {
                BookDetailsScreen(
                    bookWithProgress = bookWithProgress,
                    onBackClick = { navController.popBackStack() },
                    onChaptersClick = {
                        navController.navigate(Route.Chapters.createRoute(bookId))
                    },
                    onContinueClick = {
                        navController.navigate(Route.Reader.createRoute(bookId, -1))
                    }
                )
            }
        }
        
        composable(
            route = Route.Chapters.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            val books by homeViewModel.booksWithProgress.collectAsState()
            val bookWithProgress = books.find { it.book.id == bookId }
            
            if (bookWithProgress != null) {
                ChaptersScreen(
                    bookWithProgress = bookWithProgress,
                    onBackClick = { navController.popBackStack() },
                    onChapterClick = { chapterIndex ->
                        navController.navigate(Route.Reader.createRoute(bookId, chapterIndex))
                    }
                )
            }
        }
        
        composable(
            route = Route.Reader.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("chapterIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex") ?: -1
            
            // To pass title to reader without breaking encapsulation too much, 
            // we can look it up from the homeViewModel's list since it's already cached there.
            val books by homeViewModel.booksWithProgress.collectAsState()
            val book = books.find { it.book.id == bookId }?.book
            
            if (book != null) {
                ReaderScreen(
                    bookId = book.id,
                    bookTitle = book.title,
                    chapterIndex = chapterIndex,
                    viewModel = readerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onChaptersClick = {
                        navController.popBackStack(Route.Chapters.createRoute(book.id), false)
                    },
                    onNextBookClick = { nextBookId ->
                        navController.navigate(Route.BookDetails.createRoute(nextBookId)) {
                            popUpTo(Route.Home.route)
                        }
                    }
                )
            }
        }
    }
}
