# Orbit TV — working notes for Claude

Android TV app for browsing and watching NASA videos with a D-pad remote. Portfolio project for a
senior Android engineer: code quality, architecture and polish matter more than speed.

The full, verbatim product spec is in [docs/SPEC.md](docs/SPEC.md). It is the source of truth; this
file is the condensed operating manual. Re-read the relevant SPEC section before starting a phase.

## Working agreement

- Work strictly phase by phase (see Phase plan). **Do only the phase the user asked for, then stop.**
  Do not start the next phase until the user says so.
- One git commit per phase with a clear message.
- After each phase: run the build, lint, detekt, ktlint and unit tests, and fix every failure before
  reporting. Then tell the user briefly what was done and what to check on the emulator.
- Ask the user only if something is truly blocking; otherwise pick the sensible default and mention it.
- Use the latest **stable** library versions (no alpha/beta/RC), all declared in
  `gradle/libs.versions.toml`. Never hardcode a version in a module build file.

## Verification commands

```bash
./gradlew assembleDebug                  # build
./gradlew test                           # all unit tests (Android + JVM modules)
./gradlew :app:lintDebug                 # Android lint for app + every module it depends on
./gradlew detekt ktlintCheck             # static analysis + formatting
./gradlew ktlintFormat                   # auto-fix formatting
./gradlew connectedDebugAndroidTest      # Compose UI tests (needs an emulator)
```

CI (`.github/workflows/ci.yml`) runs build, lint, detekt, ktlint and unit tests on every push.
Lint runs with `warningsAsErrors = true`, and detekt and ktlint fail on any finding: keep all of them at zero.

## Tech stack

- Kotlin, Jetpack Compose for TV (`androidx.tv:tv-material`) for **all** UI. No Leanback fragments,
  no `androidx.compose.material3` (phone Material) components.
- Media3 ExoPlayer with a custom Compose TV player UI (no `PlayerView` default controls).
- Hilt (KSP) for DI. Retrofit + kotlinx.serialization + OkHttp for networking. Coil 3 for images.
- Room for favorites and playback progress. DataStore (Preferences) for settings.
- Coroutines + Flow. Paging 3 for search results.
- `androidx.tvprovider` for the Watch Next channel.
- Tests: JUnit 4, Turbine, kotlinx-coroutines-test, Truth, hand-written fakes (no mocking libraries).
- AGP 9 with **built-in Kotlin** (do not apply `org.jetbrains.kotlin.android`).

## Data source

NASA Image and Video Library API, no key.
- Search: `https://images-api.nasa.gov/search?q={query}&media_type=video&page={page}`
- Assets: `https://images-api.nasa.gov/asset/{nasa_id}`
- Pick the best playable mp4: prefer `~orig` or `~large`, fall back to `~medium`. Pick the best thumbnail.
- Every DTO field is nullable/defaulted. Items with no playable file are filtered out or surfaced as
  an error, never a crash. Long descriptions are trimmed/clamped in the UI.
- What Phase 2 found in the real API (handled in `core:network`, covered by fixture tests):
  asset URLs are `http://` with raw spaces → `normalizeNasaUrl` upgrades to https and encodes;
  search items are decoded one by one so a malformed item is skipped, not the page; `keywords` is
  sometimes one comma-separated string; descriptions can contain HTML and run to ~5k chars; the
  API returns HTTP 400 past its deep-pagination cap (treated as end of results); `page_size` works.
- Stream preference is `~large` > `~orig` > `~medium` (large is the reliable web encode; orig can be
  multi-GB or use codecs a TV can't decode). `VideoAssets.streams` keeps all of them, best first,
  so the player can fall back.

## Modules

| Module | Type | Responsibility | May depend on |
|---|---|---|---|
| `app` | Android app | Activity, Application, Hilt root, navigation host, drawer, manifest, Watch Next wiring | everything |
| `core:model` | Kotlin JVM | Pure domain models (`Video`, `PlaybackProgress`, …) | — |
| `core:common` | Kotlin JVM | `@Dispatcher`/`@ApplicationScope` qualifiers, `Outcome`/`DataError` | — |
| `domain` | Kotlin JVM | Repository interfaces + use cases; fakes in `testFixtures` | `core:model`, `core:common` (api) |
| `core:network` | Android lib | Retrofit API, DTOs, DTO→model mappers, remote repository impls, paging sources | `domain`, `core:model`, `core:common` |
| `core:database` | Android lib | Room DB/DAOs/entities, DataStore settings, local repository impls | `domain`, `core:model`, `core:common` |
| `core:designsystem` | Android lib (Compose) | Theme, components, focus/scroll helpers, `DataError` messages | `core:common` |
| `feature:home` | Android feature | Home screen | `domain`, `core:model`, `core:designsystem`, `core:common` |
| `feature:details` | Android feature | Details screen | same as above |
| `feature:player` | Android feature | Player screen | same as above (+ Media3) |
| `feature:search` | Android feature | Search screen | same as above (+ Paging) |
| `feature:favorites` | Android feature | Favorites screen | same as above |
| `feature:settings` | Android feature | Settings screen | same as above |

Rules:
- Feature modules never depend on each other, on `core:network` or on `core:database`. They talk to
  data only through `domain` use cases. Navigation between features is wired in `app`.
- Repository **interfaces** live in `domain`; **implementations** live in `core:network` /
  `core:database` (dependency inversion), bound with Hilt `@Binds`. Implementations map DTOs/entities
  to `core:model` types; DTOs and entities never leave their module (`internal`).
- `core:model`, `core:common` and `domain` are pure Kotlin: no Android imports.
- `core:designsystem` knows nothing about domain models; components take primitives (title, image
  URL, progress fraction, callbacks).

## Build logic

Convention plugins in `build-logic/convention` keep module build files tiny:

| Plugin id | Use for |
|---|---|
| `orbit.android.application` | `app` (SDK levels, Java/Kotlin toolchain, lint, quality) |
| `orbit.android.application.compose` | Compose for `app` |
| `orbit.android.library` | any Android library |
| `orbit.android.library.compose` | Compose for an Android library |
| `orbit.android.feature` | feature modules (library + compose + hilt + standard feature deps) |
| `orbit.android.hilt` | Hilt + KSP |
| `orbit.android.room` | Room + KSP + schema export |
| `orbit.jvm.library` | pure Kotlin modules |

Every base plugin also applies detekt and ktlint (ktlint includes the Compose rules); JVM modules
also get `com.android.lint`. Shared config: `config/detekt/detekt.yml`, `.editorconfig` (2-space
indent, 120 cols), `config/compose/stability.conf`. SDK levels and the JVM target live in
`OrbitConfig.kt` in build-logic.

Tooling notes:
- Hilt runs with `dagger.fullBindingGraphValidation=ERROR`, so a missing binding fails the build.
- `domain` runs the Dagger KSP processor itself, so each use case's `_Factory` is generated once.
  Without it, every feature injecting the same use case generates its own copy and dexing fails
  with duplicate classes.
- Opt in to experimental APIs locally with `@OptIn`; there are no global opt-ins.
- `app` has Robolectric UI tests too (the drawer); they use a plain `Application` via `@Config`, so
  no Hilt graph is needed. App build metadata reaches features as `AppInfo` (`core:common`).
- Library modules run host (unit) tests for debug only. Gradle 9's `failOnNoDiscoveredTests` is
  relaxed only for feature modules that have no `src/test` yet.
- detekt is 1.23.x (2.0 is still alpha). It runs with `detekt.android.disabled=true` and a pinned
  Kotlin version on its own classpath (see `Quality.kt`), because it predates AGP 9 and Kotlin 2.4.
- Catalog aliases for convention plugins use camelCase for nested ids
  (`orbit-android-libraryCompose`), so they don't clash with `orbit-android-library` in the
  accessor tree.

## Architecture & coding conventions

- **MVVM + UDF.** Each screen: one `@HiltViewModel` exposing exactly one immutable
  `StateFlow<XxxUiState>` and one `Flow<XxxEvent>` for one-off events (backed by
  `Channel(Channel.BUFFERED)` + `receiveAsFlow()`). The UI sends intents through plain ViewModel
  functions (`onPlayClick()`, …) or a single `onAction(XxxAction)`.
- UiState is an `@Immutable` data class or sealed interface; collections are `ImmutableList`
  (kotlinx.collections.immutable) or otherwise stable. Use `stateIn(viewModelScope,
  SharingStarted.WhileSubscribed(5_000), initial)`.
- Screens split into a stateful `XxxRoute(viewModel = hiltViewModel())` and a stateless
  `XxxScreen(uiState, onAction)` that is previewable and UI-testable.
- Use cases: one public `operator fun invoke`, named `VerbNounUseCase`, constructor-injected.
- Errors: repositories return `Outcome<T>` (`Success`/`Failure(DataError)`) and never throw;
  `CancellationException` is always rethrown. Paging errors arrive as `DataErrorException`; map with
  `Throwable.toDataError()`. Room/DataStore reads fall back to empty values on storage errors.
- Local data: favorites and progress reference a shared `videos` snapshot table (FK + cascade) so
  lists render offline; unreferenced snapshots are pruned in the same transaction. Timestamps come
  from an injected `java.time.Clock`.
- Inject `CoroutineDispatcher`s via qualifiers from `core:common`; never hardcode `Dispatchers.IO`
  in classes that need tests.
- Visibility: default to `internal` in library modules; only expose the module's API.
- Naming: package root `com.klt.orbit`; module packages follow the module path
  (`com.klt.orbit.core.designsystem`, `com.klt.orbit.feature.home`, …).
- No `!!`, no `GlobalScope`, no `runBlocking` in production code. No hardcoded user-facing
  strings in Kotlin: use string resources.
- File names match their single top-level declaration (`OrbitSpacing.kt`); `XxxDefaults` objects
  go *below* the composable they belong to.
- Previews are `private`, annotated `@PreviewTv` (from `core:designsystem`), and wrapped in the theme.
- Compose UI tests run under Robolectric in `src/test` (so CI runs them): `@Config(sdk = [36],
  qualifiers = "w960dp-h540dp-land-xhdpi")`, `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule`,
  D-pad via `performKeyInput { pressKey(...) }`, navigation-return via `StateRestorationTester`.
  `boundsInRoot` is clipped to the visible area; assert on-screen placement with `positionInRoot`
  + `size`. Verify a regression test fails without its fix.
- Tests: fakes over mocks, Turbine for flows, `runTest`. Shared fakes (`FakeVideoRepository`, …),
  `TestData` builders and `MainDispatcherRule` live in `domain/src/testFixtures`; use them with
  `testImplementation(testFixtures(projects.domain))`. Room and repository tests run on Robolectric
  (SDK 36, JVM args set in build-logic); network tests use MockWebServer + JSON fixtures in
  `core/network/src/test/resources`. Test names in backticks describing behavior.

## TV rules (apply to every screen)

- Focus is predictable: first launch focuses the hero; returning from Details restores the last
  focused card; each row remembers its focused item (`focusRestorer`, saved list state); no focus
  traps; drawer reachable with D-pad left.
- Focused cards scale up with a glow border and smooth animation. Every interactive element has a
  focus state visible from 3 m (use the design-system components / `OrbitFocus` values).
- Focus restoration pattern (Home, Details): a small `@Stable` focus holder (`HomeFocus`,
  `DetailsFocus`) keeps the last focused key in `rememberSaveable` plus the `FocusRequester`s.
  Restore with `FocusRequester.tryRequestFocus()` (never throws), then `LazyListState.revealItem()`
  — requesting focus alone does not reliably scroll a restored item into view. Re-run the reveal
  when rows change: after navigating back, the first frame can still show the previous rows.
- Hero: `HeroCarousel` (design system) pages with Left/Right but lets Left on the first slide and
  Right on the last pass through (no trap), auto-advances only while focused, and keeps the same
  focused button across slides. Back on Home moves focus to the hero before exiting.
- Scrolling: Compose's default TV bring-into-view pins every focused item at ~30% of the viewport,
  which pushes the hero off screen and parks cards outside the safe area. Vertical page lists must
  provide `LocalBringIntoViewSpec provides OrbitScrolling.Page`; `ContentRow` already uses
  `OrbitScrolling.Row`. `HeroBanner` reveals itself fully when focused and has a stable min height.
- Overscan safe area: content padding from `OrbitSpacing.safeArea*` (48dp horizontal, 27dp vertical).
- 10-foot typography from `OrbitTheme.typography`; never set raw `sp` values in features.
- Back key handled correctly everywhere; media keys (play, pause, play/pause, FF, rewind) in player.
- Player: ExoPlayer sits behind the `VideoPlayer` interface (`feature:player/engine`) so the
  ViewModel is tested with `FakeVideoPlayer`. D-pad Left/Right always seek ±10 s (the transport row
  has a single focusable, play/pause); with controls hidden the screen root holds focus. Panels
  (error, finished, up next) own focus and disable seek keys. Back hides controls before leaving.
  ViewModel tests clear the ViewModel at the end (`playerTest { }`): the position-polling loop would
  otherwise keep `runTest` advancing virtual time forever.
- Drawer (`app/navigation/OrbitNavigationDrawer`): tv-material `ModalNavigationDrawer`, a collapsed
  icon rail that expands over the content while focused, shown only on top-level screens
  (`TopLevelDestination`). Top-level screens are inset by `OrbitNavigationDrawerDefaults.ContentInset`.
  Entering lands on the current screen's item (`focusRestorer`); leaving (Right, Back, re-selecting
  the current screen) uses an explicit `FocusRequester` on the content group, because the open drawer
  covers the screen's left edge and a D-pad move finds nothing to its right. Switching screens uses
  `saveState`/`restoreState`, so each keeps its query, scroll and focus.
- Grids (Search, Favorites): `GridCells.FixedSize(VideoCardDefaults.Width)` with the safe area as
  *content* padding so focused cards are not clipped; remember the focused card with `FocusMemory`.
  Favorites' edit mode moves focus to the card that takes a removed card's place.
- Search: the query is a `TextFieldState` held by the ViewModel (no async StateFlow round trip for
  text). `OrbitSearchField` opens the keyboard only on OK, and Up/Down always leave the field.
- Manifest: `android.software.leanback` required=false, touchscreen required=false,
  `LEANBACK_LAUNCHER` intent filter, 320x180 banner.

## Design system (`core:designsystem`)

Dark space theme: deep navy and black backgrounds, a single accent color (cyan), subtle gradients
behind the hero. Exposes `OrbitTheme`, `OrbitColors`, `OrbitTypography`, `OrbitSpacing`,
`OrbitFocus`, `OrbitShape`, `OrbitGradients`, `OrbitIcons` (hand-built ImageVectors, no
material-icons-extended) and components `VideoCard`, `HeroBanner`/`HeroBackdrop`, `ContentRow`,
`LoadingShimmer`/`LoadingShimmerGrid`, `ErrorState`/`DataErrorState`, `EmptyState`, `OrbitButton`,
`OrbitIconButton`, `HeroCarousel`, `KeywordChip`, `PlaybackProgressBar`, `BufferingIndicator`,
`OrbitSearchField`, `OrbitListItem`/`OrbitSwitchListItem`, `OrbitConfirmDialog`,
plus `OrbitScrolling`, `tryRequestFocus`/`revealItem`, `FocusMemory`, `ObserveAsEvents`, `videoOverline`. Add new reusable UI here rather than in features.

## Phase plan

1. Project setup, build-logic, version catalog, modules, design system, CI.
2. Network, database, domain, repositories, with tests.
3. Home and Details with focus handling.
4. Player with progress saving and media keys.
5. Search, Favorites, Settings, navigation drawer.
6. Watch Next integration and deep links.
7. Baseline profile, polish, README.

Status: track completed phases here.
- [x] Phase 1
- [x] Phase 2
- [x] Phase 3
- [x] Phase 4
- [x] Phase 5
- [ ] Phase 6
- [ ] Phase 7
