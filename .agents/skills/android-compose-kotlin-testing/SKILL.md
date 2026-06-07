---
name: android-compose-kotlin-testing
description: Use this skill when writing, debugging, or reviewing tests for Android applications using Jetpack Compose and Kotlin. It covers Compose UI tests, Coroutines/Flows testing, ViewModel testing, and integration with the TDD cycle. Trigger when the user mentions Compose tests, Kotlin unit tests, testing ViewModels, mock/fake repository testing, or runTest.
---

# Android Compose & Kotlin Testing

This skill provides comprehensive guidelines, rules, and best practices for writing high-quality tests in modern Android projects using Jetpack Compose and Kotlin (Coroutines, Flows, ViewModels, etc.), following Test-Driven Development (TDD) principles.

---

## The Core Principles

1. **Test Behavior, Not Implementation:** Focus on what the component *does*, not how it is constructed internally. Avoid mocking internals; mock only boundaries (network, databases, system APIs).
2. **First Fail (TDD):** In line with the TDD Iron Law: *No production code without a failing test first*. Ensure you run and watch the test fail before implementing or fixing.
3. **Deterministic & Isolated:** Tests must run reliably, in any order, without dependencies on external networks, systems, or mutable shared state.

---

## 1. Kotlin Unit Testing (Coroutines & Flows)

Testing asynchronous Kotlin code requires managing time and dispatchers. Always use the `kotlinx-coroutines-test` library.

### MainDispatcherRule
ViewModels and other components using `Dispatchers.Main` require overriding the main dispatcher in unit tests.

```kotlin
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### runTest & Dispatchers
Use `runTest` to run suspend functions. It executes test code in a single-threaded environment with virtual time.
- Use **`UnconfinedTestDispatcher`** for simple tests where coroutines start and execute immediately.
- Use **`StandardTestDispatcher`** if you need precise control over coroutine execution ordering or timing (e.g., using `advanceUntilIdle()` or `advanceTimeBy()`).

### Testing StateFlow & SharedFlow
Flows are hot streams that never complete on their own. You must collect them in a coroutine that gets cancelled, or use the **Turbine** library.

<Good>
**Using Turbine (Recommended):**
```kotlin
@Test
fun whenActionHappens_stateIsUpdated() = runTest {
    val viewModel = MyViewModel(repository)
    
    viewModel.uiState.test {
        // Initial state
        expect RosaState(isLoading = false)
        
        viewModel.onRefresh()
        
        // Assert subsequent states
        expectMostRecentItem().apply {
            assert(isLoading)
        }
        expectMostRecentItem().apply {
            assert(!isLoading)
            assertEquals("Data loaded", data)
        }
    }
}
```
</Good>

<Good>
**Without Turbine (using backgroundScope):**
```kotlin
@Test
fun whenActionHappens_stateIsUpdated_manualCollect() = runTest {
    val viewModel = MyViewModel(repository)
    val results = mutableListOf<MyUiState>()
    
    // Collect in a background coroutine that runTest automatically cancels
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.uiState.collect { results.add(it) }
    }
    
    viewModel.onRefresh()
    
    assertEquals(MyUiState(isLoading = false), results[0])
    assertEquals(MyUiState(isLoading = true), results[1])
}
```
</Good>

<Bad>
**Blocking the test thread with runBlocking:**
```kotlin
@Test
fun badTest() = runBlocking {
    val viewModel = MyViewModel(repository)
    // This will block or run infinitely if the flow doesn't complete
    viewModel.uiState.collect { ... }
}
```
</Bad>

---

## 2. Jetpack Compose UI Testing

Compose UI tests verify user interactions and UI state transitions using standard Semantic APIs.

### Test Rules
- Use **`createComposeRule()`** if testing a standalone Compose component.
- Use **`createAndroidComposeRule<MyActivity>()`** if you need access to Android activity resources, string resources, or the system context.

### Finding Nodes (Semantics)
Never find components by internal implementation details. Use Semantics:
- Match by text: `onNodeWithText("Submit")`
- Match by content description: `onNodeWithContentDescription("Close")`
- Match by tag (only when text or description is not unique or applicable): `onNodeWithTag("submit_button")`
- Hierarchical matching: `onNodeWithTag("list").onChildren().onLast()`

### Actions & Assertions
Perform actions and verify states using the fluent Compose Testing API:
- `performClick()`, `performTextInput("value")`, `performScrollTo()`
- `assertIsDisplayed()`, `assertIsEnabled()`, `assertTextEquals("expected")`, `assertDoesNotExist()`

<Good>
```kotlin
@Test
fun whenSubmitClicked_showsSuccessMessage() {
    composeTestRule.setContent {
        MyScreen(onSubmit = {})
    }

    composeTestRule.onNodeWithText("Name").performTextInput("Alice")
    composeTestRule.onNodeWithText("Submit").performClick()

    composeTestRule.onNodeWithText("Success!").assertIsDisplayed()
}
```
</Good>

### Testing Configuration Overrides
To test layouts under different conditions, use `DeviceConfigurationOverride`:

```kotlin
@Test
fun screen_rendersCorrectly_onLargeFont() {
    composeTestRule.setContent {
        DeviceConfigurationOverride(
            DeviceConfigurationOverride.FontScale(1.5f)
        ) {
            MyScreen()
        }
    }
    composeTestRule.onNodeWithText("Welcome").assertIsDisplayed()
}
```

### Controlling the MainClock (Animations)
If components rely on animations, control time manually using the test rule's clock:

```kotlin
@Test
fun animation_progressesState() {
    composeTestRule.mainClock.autoAdvance = false
    
    composeTestRule.setContent {
        AnimatedComponent()
    }
    
    // Advance time manually to test intermediate animation states
    composeTestRule.mainClock.advanceTimeBy(100)
    // Assert animation progress
}
```

---

## 3. Data & Integration Testing

### Fakes vs. Mocks
- **Use Fakes for domain-specific interfaces** (e.g., `FakeUserRepository` instead of mocking `UserRepository`). Fakes are more predictable, maintain state across calls, and reduce brittle test configurations.
- **Use Mocks (with MockK) only for external boundaries** or when creating a full fake is disproportionately complex.

<Good>
**Using a Fake Repository:**
```kotlin
class FakeUserRepository : UserRepository {
    private val users = mutableMapOf<String, User>()
    
    override suspend fun saveUser(user: User) {
        users[user.id] = user
    }
    
    override suspend fun getUser(id: String): User? = users[id]
}
```
</Good>

<Bad>
**Brittle Mocking with MockK:**
```kotlin
val mockRepo = mockk<UserRepository>()
coEvery { mockRepo.getUser("1") } returns User("1", "Alice")
coEvery { mockRepo.getUser("2") } returns User("2", "Bob")
// If the internal logic calls getUser differently, this mock breaks
```
</Bad>

### Testing Database Code (Room)
Always test DAO queries and migrations using an in-memory Room database on an emulator or Robolectric:

```kotlin
@Before
fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries() // Only allowed in tests
        .build()
    userDao = db.userDao()
}

@After
fun closeDb() {
    db.close()
}
```

---

## Verification Checklist

Before finalizing any Compose or Kotlin tests, verify the following:

- [ ] ViewModels have `MainDispatcherRule` applied to prevent Main Dispatcher errors.
- [ ] Suspend functions and flows are tested inside `runTest`, not `runBlocking`.
- [ ] Unfinished Flows (hot flows) are either tested with Turbine or collected inside `backgroundScope` to avoid hangs.
- [ ] Compose UI elements are queried primarily via text or content description; `testTag` is used only as a last resort.
- [ ] Tests verify behavior and UI state, not implementation details.
- [ ] Fakes are preferred over mocks for custom repository and data layer interfaces.
- [ ] Watch the tests fail (`RED`) first to verify they are catching actual issues, then watch them pass (`GREEN`).
