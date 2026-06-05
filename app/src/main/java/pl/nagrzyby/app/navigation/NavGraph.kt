package pl.nagrzyby.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.nagrzyby.app.ui.ForestViewModel
import pl.nagrzyby.app.ui.detail.DetailScreen
import pl.nagrzyby.app.ui.detail.DetailViewModelFactory
import pl.nagrzyby.app.ui.help.HelpScreen
import pl.nagrzyby.app.ui.history.HistoryScreen
import pl.nagrzyby.app.ui.main.MainScreen
import pl.nagrzyby.app.ui.map.MapScreen
import pl.nagrzyby.app.ui.map.MapViewModelFactory

object Routes {
    const val MAIN = "main"
    const val DETAIL = "detail/{districtId}"
    const val MAP = "map/{districtId}"
    const val MAP_SEARCH = "map_search"
    const val HISTORY = "history"
    const val HELP = "help"

    fun detail(districtId: String) = "detail/$districtId"
    fun map(districtId: String) = "map/$districtId"
}

@Composable
fun NaGrzybyNavGraph(
    startDistrictId: String? = null,
    viewModel: ForestViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as android.app.Application

    LaunchedEffect(startDistrictId) {
        if (!startDistrictId.isNullOrBlank()) {
            navController.navigate(Routes.detail(startDistrictId)) {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToDistrict = { districtId ->
                    navController.navigate(Routes.detail(districtId))
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                },
                onNavigateToHelp = {
                    navController.navigate(Routes.HELP)
                },
                onNavigateToMapSearch = {
                    navController.navigate(Routes.MAP_SEARCH)
                },
                viewModel = viewModel,
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("districtId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val districtId = backStackEntry.arguments?.getString("districtId") ?: return@composable
            val detailViewModel: pl.nagrzyby.app.ui.detail.DetailViewModel = viewModel(
                factory = DetailViewModelFactory(
                    application = application,
                    districtId = districtId,
                ),
            )
            DetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onShowMap = { navController.navigate(Routes.map(districtId)) },
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = Routes.MAP,
            arguments = listOf(navArgument("districtId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val districtId = backStackEntry.arguments?.getString("districtId") ?: return@composable
            val mapViewModel: pl.nagrzyby.app.ui.map.MapViewModel = viewModel(
                factory = MapViewModelFactory(
                    application = application,
                    districtId = districtId,
                ),
            )
            MapScreen(
                viewModel = mapViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToDistrict = { districtId ->
                    navController.navigate(Routes.detail(districtId))
                },
            )
        }

        composable(Routes.MAP_SEARCH) {
            val mapViewModel: pl.nagrzyby.app.ui.map.MapViewModel = viewModel(
                factory = MapViewModelFactory(
                    application = application,
                    districtId = null,
                ),
            )
            MapScreen(
                viewModel = mapViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToDistrict = { districtId ->
                    navController.navigate(Routes.detail(districtId))
                },
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                recentVerdicts = viewModel.uiState.value.allHistory,
                isLoading = viewModel.uiState.value.isLoadingHistory,
                onNavigateToDistrict = { districtId ->
                    navController.navigate(Routes.detail(districtId))
                },
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.HELP) {
            HelpScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
