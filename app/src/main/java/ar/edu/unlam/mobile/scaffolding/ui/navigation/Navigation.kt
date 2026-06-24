package ar.edu.unlam.mobile.scaffolding.ui.navigation

sealed class Screen(
    val route: String,
) {
    object Splash : Screen("splash")

    object Onboarding : Screen("onboarding")

    object Login : Screen("login")

    object Register : Screen("register")

    object Dashboard : Screen("dashboard")

    object Profile : Screen("profile")

    object EnvironmentCheck : Screen("environment_check/{exerciseId}") {
        fun createRoute(exerciseId: String) = "environment_check/$exerciseId"
    }

    object RehabSession : Screen("rehab_session/{exerciseId}") {
        fun createRoute(exerciseId: String) = "rehab_session/$exerciseId"
    }

    object PostSession : Screen("post_session")

    object ClinicDetail : Screen("clinic_detail/{clinicId}") {
        fun createRoute(clinicId: String) = "clinic_detail/$clinicId"
    }

    object RoutineList : Screen("routine_list")

    object Progress : Screen("progress")

    object User : Screen("user/{id}") {
        fun createRoute(id: String) = "user/$id"
    }

    object MapScreen : Screen("MapScreen")

    object Achievements : Screen("achievements")
}
