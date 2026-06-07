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

    object EnvironmentCheck : Screen("environment_check")

    object RehabSession : Screen("rehab_session")

    object PostSession : Screen("post_session")

    object Map : Screen("map")

    object ClinicDetail : Screen("clinic_detail/{clinicId}") {
        fun createRoute(clinicId: String) = "clinic_detail/$clinicId"
    }

    object RoutineList : Screen("routine_list")

    object Progress : Screen("progress")

    object Form : Screen("form")

    object User : Screen("user/{id}") {
        fun createRoute(id: String) = "user/$id"
    }

    object Dev3PlayGround : Screen("Dev3PlayGround")
}
