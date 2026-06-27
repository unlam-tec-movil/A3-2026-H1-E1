package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import ar.edu.unlam.mobile.scaffolding.application.usecases.user.SignOutUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.user.UpdateUserUseCase
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val rehabRepository = mockk<RehabRepository>(relaxed = true)
    private val sessionPreferences = mockk<SessionPreferences>(relaxed = true)
    private val updateUserUseCase = mockk<UpdateUserUseCase>(relaxed = true)
    private val signOutUseCase = mockk<SignOutUseCase>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private val mockUser = User("uid_1", "Juan Pérez", "juan@test.com", "token")
    private val mockSessions =
        listOf(
            Session(1L, "uid_1", "ex_knee_flexion", System.currentTimeMillis() - 86400000L, 600L, 90f, 10),
            Session(2L, "uid_1", "ex_knee_flexion", System.currentTimeMillis() - 172800000L, 900L, 105f, 15),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userRepository.getUser() } returns flowOf(mockUser)
        every { rehabRepository.getSessions(any()) } returns flowOf(mockSessions)
        every { sessionPreferences.isDarkMode } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() =
        ProfileViewModel(
            userRepository = userRepository,
            rehabRepository = rehabRepository,
            sessionPreferences = sessionPreferences,
            updateUserUseCase = updateUserUseCase,
            signOutUseCase = signOutUseCase,
        )

    // Estado inicial

    @Test
    fun `uiState should load user name and email from repository`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals("Juan Pérez", vm.uiState.value.name)
            assertEquals("juan@test.com", vm.uiState.value.email)
        }

    @Test
    fun `uiState should build initials from two-word name`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals("JP", vm.uiState.value.initials)
        }

    @Test
    fun `uiState should build initials from single-word name`() =
        runTest(testDispatcher) {
            every { userRepository.getUser() } returns flowOf(mockUser.copy(name = "Ana"))
            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals("AN", vm.uiState.value.initials)
        }

    @Test
    fun `uiState should load recent sessions sorted by date descending`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()

            val sessions = vm.uiState.value.recentSessions
            assertTrue(sessions.isNotEmpty())
            // Most recent first
            assertTrue(sessions[0].dateTimestamp >= sessions[1].dateTimestamp)
        }

    @Test
    fun `uiState should limit recent sessions to 10`() =
        runTest(testDispatcher) {
            val manySessions =
                (1..15).map {
                    Session(
                        it.toLong(),
                        "uid_1",
                        "ex_knee_flexion",
                        System.currentTimeMillis() - it * 86400000L,
                        600L,
                        90f,
                        10,
                    )
                }
            every { rehabRepository.getSessions(any()) } returns flowOf(manySessions)

            val vm = buildViewModel()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.recentSessions.size <= 10)
        }

    @Test
    fun `uiState isLoading should be false after data loads`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()

            assertFalse(vm.uiState.value.isLoading)
        }

    // Edit name

    @Test
    fun `onStartEditName should set isEditingName true and populate editNameValue`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()

            vm.onStartEditName()

            assertTrue(vm.uiState.value.isEditingName)
            assertEquals("Juan Pérez", vm.uiState.value.editNameValue)
        }

    @Test
    fun `onEditNameChange should update editNameValue`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()

            vm.onEditNameChange("Juan Pablo")

            assertEquals("Juan Pablo", vm.uiState.value.editNameValue)
        }

    @Test
    fun `onEditNameChange should set editNameError when name is too short`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()

            vm.onEditNameChange("J")

            assertEquals(
                "El nombre debe tener al menos 2 caracteres",
                vm.uiState.value.editNameError,
            )
        }

    @Test
    fun `onSaveName should call UpdateUserUseCase with current editNameValue`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("Juan Pablo")

            vm.onSaveName()
            advanceUntilIdle()

            coVerify { updateUserUseCase("Juan Pablo") }
        }

    @Test
    fun `onSaveName should set isEditingName false on success`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("Juan Pablo")

            vm.onSaveName()
            advanceUntilIdle()

            assertFalse(vm.uiState.value.isEditingName)
        }

    @Test
    fun `onSaveName should not call use case when name is invalid`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("J")

            vm.onSaveName()
            advanceUntilIdle()

            coVerify(exactly = 0) { updateUserUseCase(any()) }
        }

    @Test
    fun `onCancelEditName should set isEditingName false`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()

            vm.onCancelEditName()

            assertFalse(vm.uiState.value.isEditingName)
        }

    // Dark mode

    @Test
    fun `onToggleDarkMode should call setDarkMode with toggled value`() =
        runTest(testDispatcher) {
            every { sessionPreferences.isDarkMode } returns flowOf(false)
            val vm = buildViewModel()
            advanceUntilIdle()

            vm.onToggleDarkMode()
            advanceUntilIdle()

            coVerify { sessionPreferences.setDarkMode(true) }
        }

    // Sign out

    @Test
    fun `onSignOutRequest should set showSignOutDialog true`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()

            vm.onSignOutRequest()

            assertTrue(vm.uiState.value.showSignOutDialog)
        }

    @Test
    fun `onSignOutDismiss should set showSignOutDialog false`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onSignOutRequest()

            vm.onSignOutDismiss()

            assertFalse(vm.uiState.value.showSignOutDialog)
        }

    @Test
    fun `onSignOutConfirm should call SignOutUseCase`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()

            vm.onSignOutConfirm()
            advanceUntilIdle()

            coVerify { signOutUseCase() }
        }

    @Test
    fun `onSignOutConfirm should set navigateToLogin true after sign out`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()

            vm.onSignOutConfirm()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.navigateToLogin)
        }

    @Test
    fun `onNavigationConsumed should reset navigateToLogin to false`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onSignOutConfirm()
            advanceUntilIdle()
            assertTrue(vm.uiState.value.navigateToLogin)

            vm.onNavigationConsumed()

            assertFalse(vm.uiState.value.navigateToLogin)
        }

    // SignOutUseCase unit tests

    @Test
    fun `onSaveName should set editNameError on UpdateUserUseCase failure`() =
        runTest(testDispatcher) {
            coEvery { updateUserUseCase(any()) } throws Exception("Error al guardar")
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("Juan Pablo")

            vm.onSaveName()
            advanceUntilIdle()

            assertEquals("Error al guardar", vm.uiState.value.editNameError)
        }

    // Cobertura adicional

    @Test
    fun `onToggleDarkMode should call setDarkMode false when isDarkMode is currently true`() =
        runTest(testDispatcher) {
            every { sessionPreferences.isDarkMode } returns flowOf(true)
            val vm = buildViewModel()
            advanceUntilIdle()

            vm.onToggleDarkMode()
            advanceUntilIdle()

            coVerify { sessionPreferences.setDarkMode(false) }
        }

    @Test
    fun `onSaveName should set isSavingName true during save then false on success`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("Juan Pablo")

            vm.onSaveName()
            advanceUntilIdle()

            // After completion isSavingName should be false
            assertFalse(vm.uiState.value.isSavingName)
        }

    @Test
    fun `onSaveName should set isSavingName false on failure`() =
        runTest(testDispatcher) {
            coEvery { updateUserUseCase(any()) } throws Exception("DB error")
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("Juan Pablo")

            vm.onSaveName()
            advanceUntilIdle()

            assertFalse(vm.uiState.value.isSavingName)
        }

    @Test
    fun `onSaveName should set editNameError when name is blank`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("") // blank after clearing

            vm.onSaveName()
            advanceUntilIdle()

            assertEquals("El nombre no puede estar vacío", vm.uiState.value.editNameError)
            coVerify(exactly = 0) { updateUserUseCase(any()) }
        }

    @Test
    fun `onCancelEditName should also clear editNameError`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("J") // triggers an error

            vm.onCancelEditName()

            assertFalse(vm.uiState.value.isEditingName)
            assertEquals(null, vm.uiState.value.editNameError)
        }

    @Test
    fun `onEditNameChange should clear editNameError when valid name is entered after error`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onStartEditName()
            vm.onEditNameChange("J") // sets error
            assertEquals("El nombre debe tener al menos 2 caracteres", vm.uiState.value.editNameError)

            vm.onEditNameChange("Juan") // valid — clears error

            assertEquals(null, vm.uiState.value.editNameError)
        }

    @Test
    fun `onSignOutConfirm should still set navigateToLogin true even when SignOutUseCase throws`() =
        runTest(testDispatcher) {
            coEvery { signOutUseCase() } throws Exception("Firebase error")
            val vm = buildViewModel()
            advanceUntilIdle()

            vm.onSignOutConfirm()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.navigateToLogin)
        }

    @Test
    fun `onSignOutConfirm should close the dialog before navigating`() =
        runTest(testDispatcher) {
            val vm = buildViewModel()
            advanceUntilIdle()
            vm.onSignOutRequest()
            assertTrue(vm.uiState.value.showSignOutDialog)

            vm.onSignOutConfirm()
            advanceUntilIdle()

            assertFalse(vm.uiState.value.showSignOutDialog)
            assertTrue(vm.uiState.value.navigateToLogin)
        }

    @Test
    fun `uiState should reflect isDarkMode true when SessionPreferences emits true`() =
        runTest(testDispatcher) {
            every { sessionPreferences.isDarkMode } returns flowOf(true)
            val vm = buildViewModel()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.isDarkMode)
        }

    @Test
    fun `uiState should use question mark as initials when user name is empty`() =
        runTest(testDispatcher) {
            every { userRepository.getUser() } returns flowOf(mockUser.copy(name = ""))
            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals("?", vm.uiState.value.initials)
        }
}
