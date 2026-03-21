# Repository Guidelines

## Project Structure & Module Organization
This repository is an Android multi-module project organized by app layer and feature:
- `app/`: application entry point, navigation host, and top-level UI wiring.
- `core:data/`: Room database, entities, DAOs, converters, and DI modules.
- `core:domain/`: domain use cases and business logic helpers.
- `core:ui/`: shared UI components/resources reused across features.
- `feature:tasks/` and `feature:trips/`: feature-specific Fragments, ViewModels, adapters, and resources.
- `library:contacts-manager/`: reusable contacts selection library module.

Tests live next to their modules under `src/test/` (unit) and `src/androidTest/` (instrumented).

## Build, Test, and Development Commands
Use the Gradle wrapper from the repo root:
- `./gradlew assembleDebug`: builds debug artifacts for all modules.
- `./gradlew :app:installDebug`: installs the app on a connected device/emulator.
- `./gradlew test`: runs all JVM unit tests.
- `./gradlew connectedAndroidTest`: runs instrumented tests on a device/emulator.
- `./gradlew lint`: runs Android lint checks.

For focused work, run module-scoped tasks, for example:
- `./gradlew :feature:tasks:test`
- `./gradlew :core:data:connectedDebugAndroidTest`

## Coding Style & Naming Conventions
Code is Kotlin-first with AndroidX, Hilt, Navigation, and Room.
- Use 4-space indentation; avoid tabs.
- Class names: `PascalCase` (`TasksViewModel`), functions/vars: `camelCase`, constants: `UPPER_SNAKE_CASE`.
- Name fragments/adapters/viewmodels by role (`TaskManagerListFragment`, `TripRecyclerViewAdapter`).
- Keep package names lowercase and feature-scoped (`com.planner.feature.tasks...`).

## Testing Guidelines
- Unit tests use JUnit4 and coroutines test utilities; Mockito is used in feature modules.
- Instrumented tests use AndroidX test runner and Espresso.
- Test files should end with `Test` and mirror production package structure.
- Add/adjust tests with behavior changes, especially around DAOs, converters, and ViewModel logic.

## Commit & Pull Request Guidelines
Recent history mixes styles, but preferred commits are short, imperative, and scoped (example: `Fix trip edit string formatting (#167)`).
- Keep one logical change per commit.
- Reference issue/PR IDs when applicable (`#123`).
- PRs should include: summary, affected modules, test evidence (`./gradlew test` output), and screenshots/video for UI changes.
