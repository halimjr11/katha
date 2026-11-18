<div align="center">

# 🎬 Katha

**Katalog film Android berbasis TMDB API v3**
Multi-Module Clean Architecture · MVVM · Unidirectional Data Flow

![Kotlin](https://img.shields.io/badge/Kotlin-2.4.21-7F52FF?logo=kotlin&logoColor=white)
![AGP](https://img.shields.io/badge/AGP-9.5.1-3DDC84?logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Koin](https://img.shields.io/badge/Koin-4.2.2-F58E20)
![MinSdk](https://img.shields.io/badge/minSdk-24-blue)

[Arsitektur](#-arsitektur) · [Modul](#-modul) · [Alur Data](#-alur-data) · [Mulai Cepat](#-mulai-cepat) · [Dokumentasi](#-dokumentasi) · [Status](#-status)

</div>

---

## ✨ Fitur

| | Fitur | Detail |
|---|---|---|
| 🏠 | **Beranda** | Layar pertama: semua film (endless scrolling) |
| 🔎 | **Cari Genre** | Ikon cari di beranda membuka daftar genre dengan kolom pencarian |
| 🎞️ | **Film per Genre** | Tap genre untuk melihat film genre itu.  Endless scrolling (Paging 3), retry saat gagal load halaman berikutnya |
| 📖 | **Detail Film** | Backdrop, poster, tagline, durasi, rating, genre, sinopsis |
| ▶️ | **Trailer** | Dibuka lewat YouTube (Trailer resmi → Trailer → Teaser) |
| 💬 | **Ulasan** | Detail menampilkan 3 ulasan sorotan; "See all reviews" membuka layar Reviews dengan endless scrolling |

## 🏗 Arsitektur

```mermaid
graph TD
    APP[":app"] --> F["feature:genres · movies · details"]
    APP --> DATA[":data"]
    F --> DOMAIN[":domain<br/>(logika murni)"]
    F --> UI[":core:ui"]
    F --> COMMON[":core:common<br/>(logika murni)"]
    DATA --> DOMAIN
    DATA --> NET[":core:network"]
    DATA --> COMMON
    DOMAIN --> COMMON
    NET -->|HTTPS + Bearer| TMDB[("TMDB API v3")]
```

> Fitur **tidak boleh** meng-import `:data` atau `:core:network`. Batas ini dijaga compiler lewat dependensi Gradle.

## 📦 Modul

| Modul | Tipe | Tanggung jawab |
|---|---|---|
| `:core:common` | Android Library (tanpa UI) | `NetworkResult`, `DispatcherProvider`, `KathaFormatters`; expose `coroutines-core` dan bundle Koin (`api`) |
| `:core:network` | Android Library | OkHttp, `TokenInterceptor`, Retrofit, `TmdbApiService`, DTO (semua field nullable), `networkModule` |
| `:domain` | Android Library (tanpa UI, logika murni) | Model non-null (masing-masing satu file, semua punya default), `MovieRepository`, 5 use case, `domainModule` |
| `:data` | Android Library | Mapper DTO → domain, `PagingSource`, `MovieRepositoryImpl`, `dataModule` |
| `:core:ui` | Android Compose | Tema & komponen bersama |
| `:feature:*` | Android Compose | Screen, ViewModel, `UiState`, modul Koin per fitur |

<details>
<summary><b>🔎 Detail use case (klik untuk buka)</b></summary>

| Use case | Mengembalikan |
|---|---|
| `GetGenresUseCase` | `NetworkResult<List<Genre>>` |
| `DiscoverMoviesUseCase` (genre opsional; tanpa genre = semua film) | `Flow<PagingData<Movie>>` |
| `GetMovieDetailUseCase` | `NetworkResult<MovieDetail>` |
| `GetMovieTrailerUseCase` | `NetworkResult<VideoTrailer?>` |
| `GetMovieReviewsUseCase` | `Flow<PagingData<Review>>` (layar Reviews, endless scrolling) |
| `GetMovieReviewHighlightsUseCase` | `NetworkResult<List<Review>>` (3 ulasan pertama untuk detail) |

</details>

## 🔄 Alur Data

```mermaid
sequenceDiagram
    participant UI as Compose Screen
    participant VM as ViewModel
    participant UC as UseCase (:domain)
    participant Repo as MovieRepositoryImpl (:data)
    participant API as TmdbApiService
    UI->>VM: event
    VM->>UC: invoke()
    UC->>Repo: repository call
    Repo->>API: GET (Dispatchers.IO)
    API-->>Repo: DTO (nullable)
    Repo-->>UC: NetworkResult<Domain non-null>
    UC-->>VM: result
    VM-->>UI: UiState
```

<details>
<summary><b>🧪 Aturan mapping penting</b></summary>

- **DTO nullable → domain non-null**: nilai kosong jadi `""` / `"N/A"`; UI cukup cek `isBlank()`.
- **Trailer**: hanya `site == "YouTube"`, urutan Trailer resmi → Trailer → Teaser; `null` bila tidak ada.
- **Paging**: halaman maksimal 500 (batas TMDB); `IOException`/`HttpException` → `LoadResult.Error`.
- **Avatar review**: `/https://...` (Gravatar) dibersihkan, path TMDB digabung ke `w185`.

</details>

## 🚀 Mulai Cepat

<details open>
<summary><b>1. Siapkan token TMDB</b></summary>

Ambil **Read Access Token** di [themoviedb.org/settings/api](https://www.themoviedb.org/settings/api), lalu tambahkan ke `local.properties` (file ini di-ignore git). **Nilai harus pakai tanda kutip** karena dipakai langsung sebagai `buildConfigField`:

```properties
TMDB_BEARER_TOKEN="eyJhbGciOi..."
```

</details>

<details open>
<summary><b>2. Build & test</b></summary>

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest :domain:test :core:common:test
```

</details>

**Prasyarat:** JDK 17, Android Studio terbaru (AGP 9.x), minSdk 24.

## 🧪 Testing

| Jenis | Status |
|---|---|
| Unit test (JUnit 5 Jupiter dengan `@ParameterizedTest`, MockK, Turbine, paging-testing): 142 test di semua modul | ✅ |
| Uji manual di emulator ([hasil QA](docs/qa_test_cases_and_summary.md)) | ✅ |
| Compose UI test dan instrumented test (`androidTest`) | 🚧 Segera ditambahkan |

## 🧰 Tech Stack

| Area | Pilihan |
|---|---|
| UI | Jetpack Compose, Material 3, Navigation 3 |
| DI | Koin 4.2.2 (Constructor DSL, tanpa KSP) |
| Network | Retrofit, OkHttp, kotlinx.serialization |
| Paging | Paging 3 |
| Gambar | Coil 3 |
| Test | JUnit 5 (Jupiter + params), MockK, Turbine, coroutines-test, paging-testing (Compose UI / instrumented test: segera, tetap JUnit 4) |

> ⚠️ **AGP 9.0+**: jangan apply `org.jetbrains.kotlin.android` di modul Android, Kotlin sudah built-in.
>
> ⚠️ `:core:common` dan `:domain` adalah Android library tanpa UI/Android API, jadi bundle Koin (`koin-android`) boleh di-expose lewat `api`. Jangan mengimpor class Android di `:domain`.

## 📚 Dokumentasi

- 📋 [Product Requirements (PRD)](docs/product_requirements_document_prd.md)
- 🏛 [Arsitektur & ADR](docs/architecture_engineering_decisions.md)
- ✅ [Test case QA & ringkasan](docs/qa_test_cases_and_summary.md)
- 🌐 [Spesifikasi API TMDB](docs/tmdb_api_specification.md)

## 🚧 Status

- [x] `:core:common`, `:core:network`, `:domain`, `:data`
- [x] `:core:ui` (tema & komponen)
- [x] `:feature:genres`, `:feature:movies`, `:feature:details`
- [x] Wiring Koin & Navigation 3 di `:app`
- [x] Unit test (142 test, termasuk `KoinModulesTest`)
- [x] Uji manual di emulator
- [ ] Compose UI test dan instrumented test (**segera ditambahkan**)

---

<div align="center">

Data film dari <a href="https://www.themoviedb.org">TMDB</a>. Aplikasi ini tidak didukung atau disertifikasi oleh TMDB.

</div>
