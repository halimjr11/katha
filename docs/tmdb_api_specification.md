# API Specification — TMDB v3 Integration

## 1. Base Configuration & Authentication
* **Base URL:** `https://api.themoviedb.org/3/`
* **Image Base URL:** `https://image.tmdb.org/t/p/`
  * **Poster Size:** `w500` (`https://image.tmdb.org/t/p/w500{poster_path}`)
  * **Backdrop Size:** `w780` (`https://image.tmdb.org/t/p/w780{backdrop_path}`)
  * **Avatar Size:** `w185` (`https://image.tmdb.org/t/p/w185{avatar_path}`)
* **Authentication (Header via `TokenInterceptor`):**
  ```http
  Authorization: Bearer <TMDB_READ_ACCESS_TOKEN>
  Accept: application/json
  ```
* **Standard HTTP Error Responses:**
  * `401 Unauthorized`: Invalid/expired API token (`status_code: 7`).
  * `404 Not Found`: Resource / `movie_id` tidak ditemukan (`status_code: 34`).
  * `429 Too Many Requests`: Rate limit terlampaui.
  * `500 / 503`: Server error dari sisi TMDB.

> **Catatan implementasi:** parameter `language` dan `sort_by` tidak dikirim oleh `TmdbApiService`; default TMDB (`en-US`, `popularity.desc`) berlaku. Semua field DTO bersifat nullable dan dipetakan ke model domain non-null di `:data`.

---

## 2. Endpoint Specifications

### Endpoint 1: Get Official Movie Genres
* **Method & Path:** `GET /genre/movie/list`
* **Query Parameters:**
  * `language` (`String`, optional, default: `"en-US"`)
* **Response `200 OK`:**
  ```json
  {
    "genres": [
      { "id": 28, "name": "Action" },
      { "id": 12, "name": "Adventure" }
    ]
  }
  ```
* **Mapping Rule:** Map `genres` ke `List<Genre(id: Int, name: String)>`. Filter item yang memiliki `name.isNotBlank()`.

---

### Endpoint 2: Discover Movies by Genre (Paginated)
* **Method & Path:** `GET /discover/movie`
* **Query Parameters:**
  * `with_genres` (`Int`, **required**) — ID genre yang dipilih.
  * `page` (`Int`, **required**, min: `1`, max: `500`) — Nomor halaman untuk Paging 3.
  * `sort_by` (`String`, optional, default: `"popularity.desc"`).
  * `language` (`String`, optional, default: `"en-US"`).
* **Response `200 OK`:**
  ```json
  {
    "page": 1,
    "results": [
      {
        "id": 533535,
        "title": "Deadpool & Wolverine",
        "poster_path": "/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
        "backdrop_path": "/yDHYTfA3R0jFYba16jBB1ef8oIt.jpg",
        "release_date": "2024-07-24",
        "vote_average": 7.712,
        "overview": "A listless Wade Wilson toils away in civilian life..."
      }
    ],
    "total_pages": 450,
    "total_results": 9000
  }
  ```
* **Pagination Logic (`DiscoverMoviesPagingSource`):**
  * `prevKey = if (page == 1) null else page - 1`
  * `nextKey = if (page >= response.totalPages || response.results.isEmpty()) null else page + 1`

---

### Endpoint 3: Get Movie Primary Detail
* **Method & Path:** `GET /movie/{movie_id}`
* **Path Parameters:**
  * `movie_id` (`Int`, **required**)
* **Query Parameters:**
  * `language` (`String`, optional, default: `"en-US"`)
* **Response `200 OK`:**
  ```json
  {
    "id": 533535,
    "title": "Deadpool & Wolverine",
    "tagline": "Come together.",
    "overview": "A listless Wade Wilson toils away in civilian life...",
    "poster_path": "/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
    "backdrop_path": "/yDHYTfA3R0jFYba16jBB1ef8oIt.jpg",
    "release_date": "2024-07-24",
    "runtime": 128,
    "vote_average": 7.712,
    "vote_count": 5420,
    "status": "Released",
    "genres": [
      { "id": 28, "name": "Action" },
      { "id": 35, "name": "Comedy" }
    ]
  }
  ```
* **Mapping Rule:**
  * `runtime` (`Int?`) -> konversi ke `String` terformat (contoh: `128` -> `"2h 8m"`, `null` atau `0` -> `"N/A"`).
  * `vote_average` (`Double?`) -> format 1 desimal (`String.format(Locale.US, "%.1f", voteAverage ?: 0.0)`).

---

### Endpoint 4: Get Movie Videos (YouTube Trailer)
* **Method & Path:** `GET /movie/{movie_id}/videos`
* **Path Parameters:**
  * `movie_id` (`Int`, **required**)
* **Response `200 OK`:**
  ```json
  {
    "id": 533535,
    "results": [
      {
        "id": "65ca9e6e54a098018402f4c1",
        "key": "73_1biulkYk",
        "name": "Official Trailer",
        "site": "YouTube",
        "size": 1080,
        "type": "Trailer",
        "official": true
      }
    ]
  }
  ```
* **Selection Algorithm (Domain/Mapper Rule):**
  1. Filter `results` di mana `site.equals("YouTube", ignoreCase = true)` dan `key.isNotBlank()`.
  2. Cari item pertama dengan `type == "Trailer"` dan `official == true`.
  3. Jika tidak ada, fallback ke item pertama dengan `type == "Trailer"`.
  4. Jika masih tidak ada, fallback ke item pertama dengan `type == "Teaser"`.
  5. Jika tidak ditemukan sama sekali, kembalikan `null` (men-trigger AC 5.2).

---

### Endpoint 5: Get Movie User Reviews (Paginated)
* **Method & Path:** `GET /movie/{movie_id}/reviews`
* **Path Parameters:**
  * `movie_id` (`Int`, **required**)
* **Query Parameters:**
  * `page` (`Int`, **required**, min: `1`)
  * `language` (`String`, optional, default: `"en-US"`)
* **Response `200 OK`:**
  ```json
  {
    "id": 533535,
    "page": 1,
    "results": [
      {
        "id": "66a12f4c9b1234567890abcd",
        "author": "JohnDoe",
        "author_details": {
          "name": "John Doe",
          "username": "johndoe",
          "avatar_path": "/utEXl2EDiXBK6f41wCLsvDkLnl.jpg",
          "rating": 8.0
        },
        "content": "Great chemistry and non-stop entertainment!",
        "created_at": "2024-07-25T10:15:30.123Z"
      }
    ],
    "total_pages": 3,
    "total_results": 42
  }
  ```
* **Mapping Rule:**
  * `avatar_path`: Perhatikan edge case TMDB—kadang `avatar_path` diawali `/https://...` (Gravatar URL) atau path TMDB biasa (`/abc.jpg`). Jika diawali `"/http"`, hapus karakter `/` pertama; jika path normal, gabungkan dengan `"https://image.tmdb.org/t/p/w185"`.
  * `rating` (`Double?`): Bersifat opsional (`nullable`), tampilkan badge rating hanya jika `rating != null`.