Build a production quality Android TV app called Orbit TV for my portfolio. It lets users browse and watch NASA videos on a TV using a D-pad remote. I am a senior Android engineer, so code quality, architecture and polish matter more than speed.

DATA SOURCE
Use the NASA Image and Video Library API, which needs no API key.
Search: https://images-api.nasa.gov/search?q={query}&media_type=video&page={page}
Asset files for one item: https://images-api.nasa.gov/asset/{nasa_id}
From the asset response, pick the best playable mp4 (prefer the orig or large variant, fall back to medium) and the best thumbnail. Handle missing fields, items with no playable file, and very long descriptions safely.

TECH STACK
1. Kotlin, Jetpack Compose for TV (androidx.tv.material3) for all UI. No Leanback fragments.
2. Media3 ExoPlayer for playback with a custom Compose TV player UI.
3. Hilt for DI, Retrofit or Ktor with kotlinx.serialization, Coil for images.
4. Room for favorites and playback progress, DataStore for settings.
5. Coroutines and Flow, Paging 3 for search results.
6. androidx.tvprovider for the Watch Next channel on the Google TV home screen.
7. Check and use the latest stable versions of every library, managed with a Gradle version catalog.

ARCHITECTURE
Clean Architecture, multi module: app, core:designsystem, core:network, core:database, core:model, core:common, domain, feature:home, feature:details, feature:player, feature:search, feature:favorites, feature:settings. Use MVVM with unidirectional data flow: each screen has a ViewModel exposing one immutable UiState via StateFlow, plus one events channel. Use cases live in domain. Repositories map DTOs to domain models. Add convention plugins in build-logic so the module build files stay small.

SCREENS
1. Home: a full width hero carousel of featured videos at the top, then horizontal rows for Apollo, Mars, Hubble, International Space Station, Earth, plus Continue Watching and Favorites rows when they have content. The hero background crossfades to the focused card's thumbnail.
2. Details: large backdrop, title, date, center, description, keywords as chips, and Play, Resume and Favorite buttons. Below that, a row of related videos found by the first keyword.
3. Player: full screen ExoPlayer with custom controls that auto hide. Play and pause, seek with left and right on the D-pad in 10 second steps, a progress bar with elapsed and total time, and title overlay. Save progress every few seconds and on exit. Resume from saved position. Handle buffering, errors with retry, and audio focus.
4. Search: a TV friendly search field, debounced queries, paged results in a grid, plus empty, loading and error states.
5. Favorites: a grid of saved videos with an option to remove.
6. Settings: autoplay next video toggle, clear watch history, and an about section with the app version and NASA attribution.
Use a TV navigation drawer on the left to move between Home, Search, Favorites and Settings.

TV SPECIFIC REQUIREMENTS
1. Focus must be predictable everywhere. Restore focus to the last focused card when returning from Details. Remember the focused item per row. The first launch focuses the hero. No focus traps.
2. Focused cards scale up with a glow border and smooth animation. Every interactive element must show a clearly visible focus state from 3 meters away.
3. Respect TV overscan safe areas and use 10 foot UI typography.
4. Handle the Back key correctly on every screen and the media keys (play, pause, fast forward, rewind) in the player.
5. The manifest declares android.software.leanback as required false, touchscreen as not required, and a LEANBACK_LAUNCHER intent filter with a 320x180 banner.
6. Publish partially watched videos to the Watch Next channel and remove them when finished. Deep links from the home screen open straight into Details or the Player.

DESIGN
A dark space theme: deep navy and black backgrounds, one accent color, and subtle gradients behind the hero. Create a proper design system in core:designsystem with colors, typography, spacing and reusable components (VideoCard, HeroBanner, ContentRow, LoadingShimmer, ErrorState). Generate a simple vector app icon and TV banner with a planet and orbit motif.

QUALITY
1. Unit tests for use cases, repositories and ViewModels with Turbine and fakes.
2. Compose UI tests for focus behavior on Home and the Player controls.
3. Detekt and ktlint configured and passing.
4. A GitHub Actions workflow that builds, runs lint and runs the tests on every push.
5. Offline and error handling everywhere. No crashes on bad API data.
6. Baseline profile module for faster startup.

README
Write a strong README for recruiters: a one paragraph pitch, a screenshots section with placeholders, feature list, architecture diagram as Mermaid, module graph, tech stack, TV specific challenges solved (focus management, Watch Next, player controls), how to run on the Android TV emulator, and NASA content attribution.

HOW TO WORK
Work in phases and make one git commit per phase with a clear message.
Phase 1: project setup, build-logic, version catalog, modules, design system, CI.
Phase 2: network, database, domain, repositories, with tests.
Phase 3: Home and Details with focus handling.
Phase 4: Player with progress saving and media keys.
Phase 5: Search, Favorites, Settings, navigation drawer.
Phase 6: Watch Next integration and deep links.
Phase 7: baseline profile, polish, README.
After each phase, run the build and tests and fix every failure before moving on. Tell me briefly what was done and what to check on the emulator. Ask me only if something is truly blocking.
