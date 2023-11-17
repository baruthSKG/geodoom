package com.example.ok2

sealed class Screen(val route: String){
    object MainScreen : Screen("main_screen")
    object CoordsScreen : Screen("coord_screen")
    object AboutScreen : Screen("about_screen")
    object LogScreen : Screen("log_screen")
    object AcceptScreen : Screen("accept_screen")
    object FinishScreen : Screen("finish_screen")
    object CheevoScreen : Screen("cheevo_screen")
    object SettingsScreen : Screen("settings_screen")
    object QuestionScreen : Screen("question_screen")
}
