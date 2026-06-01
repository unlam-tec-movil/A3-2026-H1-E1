package ar.edu.unlam.mobile.scaffolding.ui.screens

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>

    data class Success<out T>(
        val data: T,
    ) : UiState<T>

    data class Error(
        val exception: Throwable,
    ) : UiState<Nothing>
}
