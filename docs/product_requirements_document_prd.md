# Product Requirements Document (PRD) — Katha

## 1. Document Control & Overview
* **Product Name:** Katha *(Sanskrit: "kisah" atau "narasi" — merepresentasikan esensi bahwa di balik setiap karya sinema terdapat cerita yang bermakna untuk dijelajahi)*
* **Package Name:** `com.halimjr11.katha`
* **Tech Stack:** Kotlin, Jetpack Compose (Material 3), MVVM, Clean Architecture, Coroutines & Flow, Koin 4.2.2, Navigation 3, Paging 3, Retrofit, Coil 3.
* **Target Platform:** Android (Min SDK 24 / Android 7.0, Target SDK 37).
* **Objective:** Menyediakan aplikasi eksplorasi film berbasis TMDB API dengan arsitektur *scalable*, performa UI responsif, penanganan pagination *endless scrolling*, serta manajemen *error/edge-case* yang tangguh.

---

## 2. User Flow & Navigation Map
```text
[MoviesHomeScreen] (Start Destination: all movies, endless grid)
       │
       ├── Tap Movie Card (movieId) ───────────────┐
       │                                           │
       ├── Tap Search icon                         │
       ▼                                           │
[GenreListScreen] (Search field filters genres)    │
       │                                           │
       ├── Tap Genre Card (genreId, genreName)     │
       ▼                                           │
[GenreMoviesScreen] (Endless grid, one genre)      │
       │                                           │
       ├── Tap Movie Card (movieId)                │
       ▼                                           ▼
[MovieDetailScreen] (Primary Info + Trailer CTA + Endless Reviews)
       │
       └── Tap "Watch Trailer" ──► External YouTube App / Custom Tab Intent
```

---

## 3. Functional Requirements & Acceptance Criteria (AC)

### FR-01: Official Movie Genres Screen (AC 1)
* **Deskripsi:** Halaman utama menampilkan daftar genre resmi film dari TMDB.
* **UI/UX Behavior:**
  * Menampilkan `TopAppBar` berjudul **"Katha — Movie Genres"** (atau **"Movie Genres"**).
  * Daftar genre ditampilkan dalam `LazyVerticalGrid` (2 kolom) berupa Material 3 `ElevatedCard`.
  * Setiap kartu menampilkan nama genre (contoh: *Action, Adventure, Animation*).
* **Acceptance Criteria:**
  * **AC 1.1 (Positive):** Saat data berhasil dimuat, seluruh daftar genre tampil dan dapat diklik menuju daftar film sesuai `genreId`.
  * **AC 1.2 (Loading):** Menampilkan centered `CircularProgressIndicator` saat *initial fetch*.
  * **AC 1.3 (Negative - Error/Empty):** Jika gagal memuat (offline/timeout/401) atau list kosong, tampilkan pesan error ramah beserta tombol **"Retry"**.

### FR-02: Discover Movies by Genre Screen (AC 2 & AC 6)
* **Deskripsi:** Menampilkan daftar film berdasarkan genre yang dipilih dengan fitur *endless scrolling*.
* **UI/UX Behavior:**
  * `TopAppBar` menampilkan nama genre yang dipilih (misal: *"Action Movies"*) dan tombol navigasi kembali (*Back*).
  * Menampilkan film dalam `LazyVerticalGrid` (2 kolom).
  * Setiap kartu film memuat: **Poster** (`w500`), **Judul Film** (maks. 2 baris, *ellipsis*), **Tahun Rilis**, dan **Rating** (format `★ 7.8`).
  * **Endless Scrolling:** Secara otomatis memuat halaman berikutnya (`page + 1`) saat pengguna *scroll* mendekati akhir daftar.
* **Acceptance Criteria:**
  * **AC 2.1 (Positive):** Film yang tampil sesuai dengan `with_genres={genreId}` dan navigasi ke detail film berjalan saat kartu diklik.
  * **AC 2.2 (Endless Scroll):** *Scroll* melewati item ke-20 otomatis mengambil halaman ke-2 tanpa memutus posisi *scroll*.
  * **AC 2.3 (Negative - Initial Error):** Jika halaman 1 gagal dimuat, tampilkan layar error penuh + tombol **"Retry"**.
  * **AC 2.4 (Negative - Append Error):** Jika gagal memuat halaman ke-2 dst (koneksi putus di tengah *scroll*), data halaman sebelumnya tetap bertahan dan muncul footer item **"Failed to load more — Retry"** di bagian bawah grid.
  * **AC 2.5 (Negative - Null Poster):** Jika `poster_path` bernilai `null`, tampilkan *fallback placeholder* tanpa crash.

### FR-03: Movie Primary Information (AC 3)
* **Deskripsi:** Menampilkan detail informasi utama dari film yang dipilih.
* **UI/UX Behavior:**
  * Header menampilkan **Backdrop Image** (`w780`) dengan gradasi gelap di bagian bawah, overlay **Poster** (`w500`), **Judul Film**, dan **Tagline** (jika ada).
  * Metadata bar: **Tanggal Rilis** (format `dd MMM yyyy`), **Durasi** (format `2h 15m` atau `N/A` jika `0/null`), **Skor Rating** (`★ 8.2 / 10`), dan **Status** (`Released`).
  * Daftar **Genre Chips** (`AssistChip` / `FlowRow`).
  * Bagian **Overview / Sinopsis** lengkap.
* **Acceptance Criteria:**
  * **AC 3.1 (Positive):** Seluruh informasi utama ter-render akurat sesuai `movieId`.
  * **AC 3.2 (Negative):** Jika `overview` kosong, tampilkan *"No synopsis available."*; jika `runtime` null/0, tampilkan *"N/A"*.

### FR-04: Movie YouTube Trailer (AC 5)
* **Deskripsi:** Pengguna dapat menonton trailer resmi film dari YouTube.
* **UI/UX Behavior:**
  * Tombol/kartu **"Watch Trailer"** ditampilkan di bawah header informasi utama.
  * Klik tombol membuka aplikasi YouTube (`vnd.youtube:{key}`) atau *fallback* ke browser (`https://www.youtube.com/watch?v={key}`).
* **Acceptance Criteria:**
  * **AC 5.1 (Positive):** Sistem memilih video dengan `site == "YouTube"` dan prioritas `type == "Trailer"` (fallback ke `"Teaser"` jika `"Trailer"` tidak tersedia).
  * **AC 5.2 (Negative - No Trailer):** Jika film tidak memiliki video YouTube, tombol trailer disembunyikan atau ditampilkan dalam keadaan *disabled* dengan label **"Trailer Unavailable"**.

### FR-05: User Reviews (Highlights + All Reviews with Endless Scrolling) (AC 4 & AC 6)
* **Deskripsi:** Halaman detail hanya menampilkan **3 ulasan sorotan**. Tombol **"See all reviews"** membuka layar **Reviews** terpisah yang memuat seluruh ulasan dengan *endless scrolling*.
* **UI/UX Behavior:**
  * Halaman detail: maksimal 3 ulasan (halaman pertama), lalu tombol **"See all reviews"** (hanya muncul bila ada 3 ulasan). Halaman detail **tidak** melakukan *endless scrolling*.
  * Layar **Reviews** (`MovieReviewsScreen`): `LazyColumn` berpaginasi dengan tombol back ke detail.
  * Setiap kartu ulasan menampilkan: **Nama Author**, **Avatar** (jika tersedia, fallback inisial), **Rating Author** (jika ada), **Tanggal Ulasan**, dan **Konten Ulasan** (dapat di-*expand/collapse* jika terlalu panjang).
* **Acceptance Criteria:**
  * **AC 4.1 (Highlights):** Detail menampilkan paling banyak 3 ulasan; "See all reviews" membuka daftar lengkap.
  * **AC 4.1b (Endless Scroll):** Di layar Reviews, ulasan dimuat per halaman dan otomatis mengambil halaman berikutnya saat pengguna men-*scroll* ke bawah.
  * **AC 4.2 (Negative - Empty Reviews):** Jika film belum memiliki ulasan (`results` kosong), tampilkan teks *"No user reviews yet."*
  * **AC 4.3 (Negative - Error):** Di detail, jika ulasan gagal dimuat tampilkan "Failed to load reviews" + **"Retry"** inline (hanya memuat ulang bagian ulasan). Di layar Reviews, jika halaman ke-2+ gagal dimuat, tampilkan **"Retry"** inline di bagian bawah daftar.

---

## 4. Non-Functional Requirements (NFR)
* **Architecture:** Strict separation of concerns (**Presentation** -> **Domain** -> **Data**). Layer `Domain` tidak boleh memakai class Android, Retrofit, maupun OkHttp (modul Android library tanpa UI; Koin di-expose lewat `:core:common`).
* **Concurrency:** Menggunakan `viewModelScope`, `Dispatchers.IO`, dan `async/await` untuk pemanggilan paralel (`getMovieDetail`, `getMovieTrailer` & `getReviewHighlights`).
* **Security:** `TMDB_BEARER_TOKEN` disimpan di `local.properties` dan diakses melalui `BuildConfig`, tidak pernah di-*hardcode* ke dalam repository Git.
* **Testability:** Unit test mencakup verifikasi DI graph (`koin-test` `.verify()`), `PagingSource`, `Mapper`, `Repository`, use case, dan `ViewModel` (*positive & negative cases*).
  * **Catatan:** Compose UI test dan instrumented test (`androidTest`) belum ada dan akan segera ditambahkan.