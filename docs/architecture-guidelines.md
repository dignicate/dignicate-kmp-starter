# Architecture Guidelines

## 1. Module Structure

This project uses a strict multi-module clean architecture. Each module compiles to a standalone KMP framework.

```
composeApp        # App entry point (Compose Multiplatform)
providers         # Koin DI wiring — imports all other modules
viewmodel         # ViewModels — observes Domain Use Cases
domain            # Business logic, models, repository interfaces, use cases
data              # Repository implementations, API clients, DTOs
core              # Shared platform utilities (expect/actual)
```

**Dependency direction** (one-way only):

```
composeApp → providers → viewmodel → domain ← data
                                        ↑
                                       core
```

## 2. Layer Responsibilities

### Domain Layer (`:domain`)
- Data models (e.g., `TimeInfo`)
- Repository interfaces (e.g., `TimeRepository`)
- Use Case implementations — **must follow the Mandatory CQS pattern** (see §3)

### Data Layer (`:data`)
- `@Serializable` DTOs matching API responses (e.g., `TimeDto`)
- API client interfaces and Ktor implementations
- Repository implementations using `callbackFlow`
- DTO-to-domain mapping as `private` extension functions inside the `RepositoryImpl` class

### ViewModel Layer (`:viewmodel`)
- Subscribes to Use Case `StateFlow` for state
- Calls Use Case `suspend` trigger functions to initiate actions
- Never accesses repositories or API clients directly
- Exposes UI state as an inner `data class UiState` and a `StateFlow<UiState>` named `uiState` (following Google's official UI layer naming convention)

### Providers Layer (`:providers`)
- Single source of truth for all Koin module definitions
- Provides `HttpClient` (with `ContentNegotiation` + JSON), API clients, repositories, use cases, and view models

## 3. Use Case Pattern: Mandatory CQS

Every Use Case **must** follow a Reactive CQS (Command Query Separation) pattern.

### Structure

- **Trigger (Command)**: A `suspend` function (e.g., `fetch()`, `execute()`, `save()`) that initiates an action. It must never return the result — it only drives the internal flow.
- **Stream (Query)**: A `StateFlow` (or `Flow`) exposed publicly as the sole channel through which consumers receive data, status, or errors.

### Example

```kotlin
class GetCurrentTimeUseCase(private val repository: TimeRepository) {

    private val _state = MutableStateFlow<Result<TimeInfo>?>(null)
    val state: StateFlow<Result<TimeInfo>?> = _state.asStateFlow()

    suspend fun fetch() {
        repository.getCurrentTime()
            .collect { _state.emit(it) }
    }
}
```

### Consuming from ViewModel

```kotlin
class TimeViewModel(private val useCase: GetCurrentTimeUseCase) {

    val timeState: StateFlow<Result<TimeInfo>?> = useCase.state

    fun onFetchRequested() {
        viewModelScope.launch { useCase.fetch() }
    }
}
```

### Rules

1. **Never return data from a trigger function.** Consumers must subscribe to the exposed stream.
2. **All business-logic operators** (`debounce`, `flatMapLatest`, `distinctUntilChanged`, etc.) belong inside the Use Case, not the ViewModel.
3. **Use `StateFlow` for state** (last-value semantics, always has a current value) and `Flow` when a stream-only model is more appropriate.
4. **One Use Case per action.** Do not combine unrelated triggers into a single class.

### Rationale

- Eliminates "suspend vs stream?" decision fatigue across the team.
- Accommodates future complexity (retries, progress, debounce) without breaking the public interface.
- Centralizes reactive operators in the Domain layer where business rules live.

## 4. Repository Pattern

Repositories in the Data layer bridge `suspend` network calls into Kotlin Flows using `flow`. Wrap results in `kotlin.Result`.

```kotlin
override fun getCurrentTime(): Flow<Result<TimeInfo>> = flow {
    val result = try {
        Result.success(apiClient.getTime().toDomainObject())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    emit(result)
}
```

`HttpClient` instances are owned by DI and must not be closed from repository flows.

DTO-to-domain mapping is a `private` extension function inside the `RepositoryImpl` — not a separate mapper file — unless multiple consumers need it.

## 5. Dependency Injection (Koin)

All bindings are declared in `providers/src/commonMain/.../KoinModule.kt`. New components must be registered there.

- `HttpClient` — `single`, configured with `ContentNegotiation` + `kotlinx.serialization` JSON
- API clients — `single<Interface> { Impl(get()) }`
- Repositories — `single<Interface> { Impl(get()) }`
- Use Cases — `factory { UseCase(get()) }`
- ViewModels — `factory { ViewModel(get()) }`

## 6. Technology Stack

| Concern | Library |
|---|---|
| HTTP client | Ktor (`ktor-client-core`, engine: `okhttp` / `darwin`) |
| JSON serialization | `kotlinx.serialization` + `ktor-serialization-kotlinx-json` |
| Async / flows | `kotlinx.coroutines` |
| Dependency injection | Koin 4 |
| UI | Compose Multiplatform |

## 7. Package Structure (Feature-Oriented)

Within the `:ui` and `:viewmodel` modules, screen-level code is organized by **feature**, not by layer. Cross-cutting concerns live in dedicated sibling packages.

### `:ui` module

```
com.dignicate.kmpstarter.ui
├── feature/
│   ├── home/
│   │   ├── HomeTabScreen.kt
│   │   └── components/       # Feature-private composables (only if needed)
│   ├── catalog/
│   │   └── CatalogTabScreen.kt
│   ├── saved/
│   │   └── SavedTabScreen.kt
│   ├── menu/
│   │   └── MenuTabScreen.kt
│   ├── settings/
│   │   └── SettingsScreen.kt
│   └── launch/
│       └── LaunchScreen.kt
├── components/               # Reusable composables shared across features
│   ├── CustomAppBar.kt
│   └── AppDrawer.kt
└── navigation/               # Tab enums, navigation containers, route definitions
    ├── MainTab.kt
    └── MainNavigationContainer.kt
```

### `:viewmodel` module

```
com.dignicate.kmpstarter.viewmodel
└── feature/
    ├── home/
    │   └── HomeViewModel.kt
    └── greeting/
        └── GreetingViewModel.kt
```

### Rules

1. **One feature per package.** A new screen goes into `ui/feature/<name>/`. Its ViewModel goes into `viewmodel/feature/<name>/`. Use the same `<name>` on both sides.
2. **Feature packages are independent.** A feature must not import from another feature's package. Cross-feature reuse goes through `ui/components/` (UI) or the `:domain` layer (logic).
3. **`ui/components/` is for reusable UI only.** If a composable is used by exactly one feature, place it under `ui/feature/<name>/components/` instead — keep the global namespace clean.
4. **`ui/navigation/` owns navigation surface.** Tab enums, route keys, and top-level navigation containers (e.g., `MainNavigationContainer`) live here. Individual screens never reference each other directly; they go through navigation.
5. **No top-level files in `ui/` or `viewmodel/`.** Every file belongs to a sub-package.

### Rationale

- A feature can be deleted by removing one folder. Discoverability scales with feature count, not file count.
- Pre-stages a future migration to per-feature Gradle modules (`:feature-home`, `:feature-catalog`, …) without forcing the build cost up front.
- Mirrors `:ui` and `:viewmodel` package shapes, so jumping from a screen to its ViewModel is a predictable path.
