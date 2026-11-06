package com.tecsup.suarez.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.suarez.ui.screens.CoursesScreen
import com.tecsup.suarez.ui.screens.LoginScreen
import com.tecsup.suarez.ui.screens.RegisterScreen
import com.tecsup.suarez.viewmodel.AuthViewModel
import com.tecsup.suarez.viewmodel.CourseViewModel

/**
 * Navegación de la app:
 *  login <-> register   (sin sesión)
 *  courses              (con sesión; se llega automáticamente al autenticarse)
 */
@Composable
fun AuthApp(authVm: AuthViewModel = viewModel()) {
    val nav = rememberNavController()
    val auth by authVm.state.collectAsState()
    val user = auth.user

    // Reacciona a login / logout cambiando de grafo
    LaunchedEffect(user?.uid) {
        if (user != null) {
            nav.navigate("courses") { popUpTo(0) { inclusive = true } }
        } else {
            nav.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(navController = nav, startDestination = if (user != null) "courses" else "login") {
        composable("login") {
            LoginScreen(
                state = auth,
                onLogin = authVm::login,
                onGoRegister = { authVm.clearError(); nav.navigate("register") },
                onErrorShown = authVm::clearError
            )
        }
        composable("register") {
            RegisterScreen(
                state = auth,
                onRegister = authVm::register,
                onBack = { authVm.clearError(); nav.popBackStack() },
                onErrorShown = authVm::clearError
            )
        }
        composable("courses") {
            // Sin usuario no se dibuja nada (evita crear el VM con uid nulo)
            val uid = user?.uid ?: return@composable
            val courseVm: CourseViewModel = viewModel(
                key = uid, // un ViewModel distinto por cada usuario
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        CourseViewModel(uid) as T
                }
            )
            val state by courseVm.state.collectAsState()
            CoursesScreen(
                email = user.email.orEmpty(),
                state = state,
                onSave = courseVm::save,
                onDelete = courseVm::delete,
                onLogout = authVm::logout,
                onMessageShown = courseVm::messageShown
            )
        }
    }
}
