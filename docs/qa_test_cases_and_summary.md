# QA Test Cases & Summary — Katha

**Build:** debug APK, `com.halimjr11.katha`
**Device:** Android emulator `Pixel_10a` (`emulator-5554`, 1080x2424, Android arm64)
**Method:** manual run on the emulator, driven with `adb` (`input tap/swipe`, `screencap`). The ARTEMIS agent could not run because it has no LLM API key configured, so no ARTEMIS task was executed.
**Data:** live TMDB API v3. Offline cases used `svc wifi/data disable`.
**Evidence:** screenshots in the session scratchpad `/private/tmp/claude-501/qa/` (not part of the repo).

> **Flow change:** after this report the app became *movies-first*: Home (all movies) → search icon → Genres with a search field → genre movies → detail. The manual cases below were run on the earlier genres-first flow and have not been re-run in full. The new path (home, genre search "dra" → Drama, genre movies, back keeps the query) was checked once on the emulator.

Legend: ✅ Pass · ⚠️ Pass with note · ⏭ Not executed on device (reason given)

---

## 1. Test Cases

### FR-01 Genres screen

| ID | AC | Steps | Expected | Result |
|---|---|---|---|---|
| TC-01 | 1.1 | Launch app online | Title "Katha — Movie Genres"; 2-column grid of genre cards (Action … Western, 19 genres) | ✅ |
| TC-02 | 1.1 | Scroll genre grid to the bottom | All genres reachable, last item Western | ✅ |
| TC-03 | 1.2 | Launch app (cold start) | Centered progress indicator before data appears | ⏭ Load is too fast to capture reliably; covered by `GenreListViewModelTest` initial state (`isLoading = true`) |
| TC-04 | 1.3 | Disable network, cold-start app | Friendly error text + **Retry** button | ✅ "Couldn't load genres. Check your connection and try again." |
| TC-05 | 1.3 | From TC-04, enable network, tap Retry | Genres load and grid shows | ✅ |
| TC-06 | 1.3 | API returns empty genre list | Error message + Retry | ⏭ Needs mocked response; covered by `GenreListViewModelTest.emptyListIsTreatedAsError` |

### FR-02 Movies by genre

| ID | AC | Steps | Expected | Result |
|---|---|---|---|---|
| TC-07 | 2.1 | Tap **Action** | Title "Action Movies", back arrow, 2-column poster grid | ✅ |
| TC-08 | 2.1 | Inspect a card | Poster, title (max 2 lines, ellipsis), year, `★ rating` | ✅ e.g. "The Odyssey / 2026 ★ 8.0"; long title wraps to 2 lines |
| TC-09 | 2.2 | Scroll past the first page | Next page appends without losing scroll position | ✅ Items from later pages (1999, 2018, 2017 releases) appended |
| TC-10 | 2.3 | Disable network, tap a genre | Full-screen error + Retry | ✅ "Couldn't load movies. Check your connection and try again." |
| TC-11 | 2.3 | From TC-10, enable network, tap Retry | Movie grid loads | ✅ |
| TC-12 | 2.4 | Scroll to end of loaded pages with network off | Loaded items stay; footer "Failed to load more — Retry" | ✅ |
| TC-13 | 2.4 | From TC-12, enable network, tap footer Retry | Next page appends | ✅ |
| TC-14 | 2.5 | Movie with `poster_path = null` | Placeholder icon, no crash | ⏭ No such movie met on device; covered by `RemoteImage` blank-URL branch (no unit test) |
| TC-15 | — | Tap a movie card | Opens detail screen for that movie | ✅ |
| TC-16 | — | Tap toolbar back arrow | Returns to previous screen | ✅ |

### FR-03 Movie detail

| ID | AC | Steps | Expected | Result |
|---|---|---|---|---|
| TC-17 | 3.1 | Open "The Odyssey" | Backdrop with gradient, poster overlay, title, tagline | ✅ |
| TC-18 | 3.1 | Check metadata row | `dd MMM yyyy` date, runtime `2h 53m`, `★ 8.0 / 10`, status | ✅ "15 Jul 2026 • 2h 53m • ★ 8.0 / 10 • Released" |
| TC-19 | 3.1 | Check genre chips and overview | Chips (Adventure, Action, Fantasy) and full synopsis | ✅ |
| TC-20 | 3.2 | Movie with empty overview / null runtime | "No synopsis available." / runtime fallback | ⏭ Needs specific data. Overview fallback is in `MovieMapper`; runtime shows `N/A` (PRD aligned, see Issue 3) |
| TC-21 | — | Detail load fails (offline) | Error text + Retry | ✅ "Failed to load movie. Check your connection and try again." |
| TC-22 | — | From TC-21, enable network, tap Retry | Detail loads | ✅ Detail loaded; reviews needed a second retry in the first build (Issue 1, fixed) |

### FR-04 Trailer

| ID | AC | Steps | Expected | Result |
|---|---|---|---|---|
| TC-23 | 5.1 | Tap **Watch Trailer** | Opens YouTube app on the movie's trailer | ✅ YouTube opened "The Odyssey – Countdown Trailer" |
| TC-24 | 5.1 | Press Back from YouTube | Returns to Katha | ✅ Needed 2–3 Back presses because YouTube kept a mini player |
| TC-25 | 5.2 | Movie without a YouTube video | "Trailer Unavailable", disabled | ⏭ No such movie met on device; trailer-null path covered by `VideoMapperTest.noYoutubeReturnsNull` and `MovieDetailViewModelTest.trailerFailureKeepsDetail` |
| TC-26 | 5.1 | Device without YouTube app | Falls back to browser URL | ⏭ Not testable (YouTube installed); code path `ActivityNotFoundException` → `youtubeUrl` |

### FR-05 Reviews

| ID | AC | Steps | Expected | Result |
|---|---|---|---|---|
| TC-27 | 4.1 | Scroll detail page | Reviews in the same scroll as the detail (no nested scroll crash) | ✅ |
| TC-28 | 4.1 | Inspect a review card | Author, avatar (or initial), rating, date, content | ✅ Avatar image, initial fallback ("M", "T", "S"), `★ 4.0`, `14 Jul 2026` |
| TC-29 | — | Long review | Truncated at 5 lines with "Read more"; tap expands, "Show less" collapses | ✅ |
| TC-30 | 4.1 | Scroll to end of reviews | Next page loads automatically | ✅ Dates progressed from Jul to Sep 2026, i.e. a later page appended |
| TC-31 | 4.2 | Movie with no reviews | "No user reviews yet." | ⏭ No such movie met on device; code path in `reviewItems` |
| TC-32 | 4.3 | Reviews fail to load (offline) | Inline "Failed to load reviews — Retry" | ✅ Seen after the detail Retry in the first build |

### Navigation and platform

| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-33 | Genres → Movies → Detail → Back → Back | Each Back returns one level | ✅ |
| TC-34 | App launch | No crash; Koin graph resolves | ✅ Also `KoinModulesTest` |

### Automated unit tests (142 tests, 0 failures)

Unit tests run on **JUnit 5 (Jupiter)**; scenarios are `@ParameterizedTest` rows (`@CsvSource`, `@MethodSource`, `@ValueSource`), and every row is reported as its own test. Instrumented tests (`androidTest`) will stay on JUnit 4.

| Module | Class | Tests | Covers |
|---|---|---|---|
| :core:common | `KathaFormattersTest` | 42 | Parameterized rows: runtime, dates incl. leap year, vote average, poster/backdrop/avatar URLs, Gravatar edge case |
| :core:common | `NetworkResultTest` | 2 | `map` on Success and Error |
| :core:network | `TokenInterceptorTest` | 1 | Bearer (from BuildConfig) and Accept headers |
| :core:network | `DtoParsingTest` | 4 | Gson: nullable DTOs, snake_case, unknown keys, null rating |
| :domain | `GetGenresUseCaseTest` | 5 | Rows: genres, empty list, offline, 401, 500 |
| :domain | `DiscoverMoviesUseCaseTest` | 7 | Turbine rows: two emissions, empty flow, upstream error; genre id passed (3 genres); no genre = all movies |
| :domain | `GetMovieDetailUseCaseTest` | 4 | Rows: found, another movie, 404, offline |
| :domain | `GetMovieTrailerUseCaseTest` | 4 | Rows: trailer, null trailer, offline, 404 |
| :domain | `GetMovieReviewsUseCaseTest` | 6 | Turbine rows (3 upstream behaviours); movie id passed (3 movies) |
| :data | `MappersTest` | 15 | Genre/Movie/Detail/Review mapping, defaults, author fallback (4 rows), null ids dropped |
| :data | `VideoMapperTest` | 2 | Trailer priority, no-YouTube case |
| :data | `DiscoverMoviesPagingSourceTest` | 12 | Page-key rows (6), failures (3 rows), null body, cancellation, first page |
| :data | `MovieReviewsPagingSourceTest` | 10 | Page-key rows (5), failures (3 rows), cancellation, first page |
| :data | `MovieRepositoryImplTest` | 15 | Failure rows (6: IO, 401/404/429/500, malformed JSON), cancellation, success, empty, invalid detail, trailer, paged flows |
| :feature:genres | `GenreListViewModelTest` | 4 | Turbine rows: success/error/empty; retry sequence |
| :feature:movies | `MovieListViewModelTest` | 2 | Paged movies for genre, empty genre |
| :feature:details | `MovieDetailViewModelTest` | 7 | Turbine rows (4 scenarios); stays loading; retry; reviews flow |
| :app | `KoinModulesTest` | 1 | Cross-module DI graph (`verify()`) |

Placeholder `ExampleUnitTest` files were removed from all modules.

---

## 2. Issues Found

| # | Severity | Issue | Status |
|---|---|---|---|
| 1 | Low | After a detail load failure, tapping Retry reloaded the detail but left the reviews section on "Failed to load reviews". | **Fixed**: the detail Retry now also calls `reviews.refresh()`. Re-verified by build and unit tests; the offline flow was not re-run on device. |
| 2 | Low | Posters that failed to load while offline stay blank after the network returns until the card is recomposed or scrolled away. | **Open**: Coil does not retry on its own. Add an image retry or `ImageRequest` network policy if this matters. |
| 3 | Info | PRD AC 3.2 said runtime null/0 shows "-", but the API spec and `KathaFormatters.formatRuntime` return "N/A". | **Fixed**: PRD now says "N/A". |
| 4 | Info | "System UI isn't responding" dialog appeared once on cold boot, and the first emulator froze and was restarted. | Emulator performance, not an app defect. |
| 6 | Low | Movie cards in a row had different heights when one title wrapped to 2 lines. | **Fixed**: title reserves 2 lines (`minLines = 2`). Verified on emulator: both cards in a row now match. |
| 5 | Info | Template `ExampleUnitTest` in `:core:network` and `:core:ui` did not compile (no JUnit dependency). | **Fixed**: added `testImplementation(libs.junit)`. |

## 3. Not Covered

- TC-03, 06, 14, 20, 25, 26, 31 need specific data or mocked responses (loading spinner, empty genre list, null poster, empty overview, no trailer, no YouTube app, no reviews).
- Compose UI tests and instrumented tests (`androidTest`) are not written yet and **will be added soon**; the UI was verified manually instead. The `androidTest` template files were removed from the three feature modules.
- Not tested: rotation, dark mode, process death, small screens, tablets, accessibility (TalkBack).
- Release build (R8 minification is enabled for `release`) was not built or run; Retrofit/kotlinx.serialization keep rules have not been verified.

## 4. Summary

| | Count |
|---|---|
| Manual test cases | 34 |
| Executed on device | 27 (all passed) |
| Not executed | 7 |
| Unit tests | 142 run, 0 failed |
| Defects fixed | 4 (Issue 1, 3, 5 and 6) |
| Open items | 1 low (Issue 2) |

**Verdict:** all three screens work end to end against live TMDB on the emulator: genres, endless-scrolling movie list, detail with trailer hand-off to YouTube, and endless reviews. Every error path that was exercised (initial load, append load, detail, reviews) shows its message and recovers on Retry. The remaining risk is the untested data-dependent cases above and the unverified release build.
