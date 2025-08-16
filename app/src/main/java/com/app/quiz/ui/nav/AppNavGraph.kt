package com.app.quiz.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.app.quiz.ui.auth.LoginScreen
import com.app.quiz.ui.auth.SignupScreen
import com.app.quiz.ui.dashboard.DashboardScreen
import com.app.quiz.ui.history.HistoryScreen
import com.app.quiz.ui.home.HomeScreen
import com.app.quiz.ui.profile.ProfileScreen
import com.app.quiz.ui.quiz.QuizScreen
import com.app.quiz.ui.quiz.QuizVm
import com.app.quiz.ui.quiz.ResultVm
import com.app.quiz.ui.ranking.RankingScreen

private data class Tab(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun RootNavGraph(nav: NavHostController) {
    val tabs = listOf(
        Tab("dashboard", "Dashboard", Icons.Filled.Insights),
        Tab("home",      "Quizzes",   Icons.Filled.Home),
        Tab("history",   "Histórico", Icons.Filled.History),
        Tab("ranking",   "Ranking",   Icons.Filled.Star),
        Tab("profile",   "Perfil",    Icons.Filled.Person),
    )
    val routesWithBottomBar = tabs.map { it.route }.toSet()

    Scaffold(
        bottomBar = {
            val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute in routesWithBottomBar) {
                NavigationBar {
                    val backStack by nav.currentBackStackEntryAsState()
                    val selectedRoute = backStack?.destination?.route
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected   = selectedRoute == t.route,
                            onClick    = {
                                nav.navigate(t.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon       = { Icon(t.icon, contentDescription = t.title) },
                            label      = { Text(t.title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "login",
            modifier = Modifier.padding(padding)
        ) {
            // Auth
            composable("login") {
                LoginScreen(
                    onLogged = {
                        nav.navigate("home") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoToSignup = { nav.navigate("signup") }
                )
            }
            composable("signup") {
                SignupScreen(
                    onSigned = {
                        nav.navigate("home") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoToLogin = {
                        nav.navigate("login") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Tabs
            composable("dashboard") { DashboardScreen() }
            composable("home") {
                HomeScreen(
                    onOpenQuiz = { id, timeLimitSeconds ->
                        val t = timeLimitSeconds ?: -1
                        nav.navigate("quiz/$id?t=$t")
                    }
                )
            }
            composable("history") { HistoryScreen() }
            composable("ranking") { RankingScreen() }

            composable("profile") {
                ProfileScreen(
                    onLogout = {
                        nav.navigate("login") {
                            popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }


            composable(
                route = "quiz/{quizId}?t={t}",
                arguments = listOf(
                    navArgument("quizId") { type = NavType.StringType },
                    navArgument("t") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { entry ->
                val quizId = entry.arguments?.getString("quizId") ?: return@composable
                val quizVm: QuizVm = hiltViewModel()
                val resultVm: ResultVm = hiltViewModel()

                QuizScreen(
                    quizId = quizId,
                    vm = quizVm,
                    onFinish = { session ->
                        resultVm.submit(session)
                        nav.navigate("history") {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
