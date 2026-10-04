package com.dieletech.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dieletech.mobile.data.api.SessionManager
import com.dieletech.mobile.ui.screens.*
import com.dieletech.mobile.ui.theme.DielTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DielTechTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DielTechApp()
                }
            }
        }
    }
}

@Composable
fun DielTechApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Rehidrata el JWT en memoria para el interceptor de red.
    androidx.compose.runtime.remember { SessionManager.restore(context); true }

    // Determine start destination based on login state and role
    val start = if (SessionManager.isLoggedIn(context)) {
        when (SessionManager.role(context)) {
            "INSTRUCTOR" -> "instructor_main"
            "ADMIN" -> "admin_main"
            else -> "main"
        }
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = start) {
        // ── Autenticación (HU-01, HU-03) ──
        composable("login") { LoginScreen(navController = navController) }
        composable("register") { RegisterScreen(navController = navController) }
        composable("forgot_password") { ForgotPasswordScreen(navController = navController) }
        composable("reset_password/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(email = email, navController = navController)
        }
        composable("verify_code/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyCodeScreen(email = email, navController = navController)
        }

        // ── Top-level por rol ──
        composable("main") { MainScreen(navController = navController) }
        composable("instructor_main") { InstructorMainScreen(navController = navController) }
        composable("admin_main") { AdminMainScreen(navController = navController) }

        // ── Flujo del estudiante (HU-05, HU-06, HU-07) ──
        composable("course/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toLongOrNull() ?: 0L
            CourseDetailScreen(courseId = courseId, navController = navController)
        }
        composable("checkout/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toLongOrNull() ?: 0L
            CheckoutScreen(courseId = courseId, navController = navController)
        }
        composable("success/{courseName}") { backStackEntry ->
            val courseName = backStackEntry.arguments?.getString("courseName") ?: "Curso"
            PurchaseSuccessScreen(courseName = courseName, navController = navController)
        }

        // ── Tareas (HU-37, HU-38) ──
        composable("assignments/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toLongOrNull() ?: 0L
            AssignmentListScreen(courseId = courseId, navController = navController)
        }
        composable("assignment/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            AssignmentDetailScreen(assignmentId = id, navController = navController)
        }

        // ── Evaluación (HU-10) ──
        composable("quiz/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toLongOrNull() ?: 0L
            QuizScreen(courseId = courseId, navController = navController)
        }

        // ── Certificados (HU-12) ──
        composable("certificates") { CertificatesScreen(navController = navController) }
        composable("cert/verify") { VerifyCertificateScreen(navController = navController) }

        // ── Perfil (HU-14) ──
        composable("profile/edit") { EditProfileScreen(navController = navController) }

        // ── Admin / Instructor (HU-09) ──
        composable("admin/course/{courseId}/assignments") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toLongOrNull() ?: 0L
            AdminAssignmentManagerScreen(courseId = courseId, navController = navController)
        }

        // ── Mapa de navegación (HU-44, laboratorio) ──
        composable("nav_map") { NavigationMapScreen(navController = navController) }
    }
}
