# CacheScope 🔍

A developer-focused Android app that **benchmarks and visualizes** multi-layer caching strategies in real time.

Built to demonstrate deep understanding of Android caching architecture for senior engineering roles.

---

## What it does

Fetches GitHub user search results using **5 different cache strategies**, measures end-to-end latency for each, and presents analytics so you can see exactly which strategy is fastest — and why.

### 3 Screens

| Screen        | Purpose                                                      |
|---------------|--------------------------------------------------------------|
| **Benchmark** | Select a strategy, run a fetch, see latency + hit/miss badge |
| **Analytics** | Bar chart of avg latency per strategy + hit rate breakdown   |
| **Race**      | All 5 strategies fire simultaneously — see who wins live     |

---

## Architecture

```
UI (Compose) → ViewModel → BenchmarkRunner → CacheFactory → Cache Layer
                                          ↘ AnalyticsEngine → Room DB
```

### Cache Layers

| Layer   | Speed      | Persistence  | Notes                       |
|---------|------------|--------------|-----------------------------|
| Memory  | ~0ms       | None         | In-process HashMap          |
| Disk    | ~5ms       | App restarts | Raw JSON files              |
| Room    | ~10ms      | App restarts | Structured SQLite           |
| Hybrid  | ~0–10ms    | App restarts | Memory-first, Room fallback |
| Network | ~200–500ms | None         | Retrofit + GitHub API       |

### Key Design Patterns

- **Strategy Pattern** — `CacheFactory` resolves the right `CacheDataSource<T>` at runtime
- **Cache-aside** — `HybridCache` reads memory → falls back to Room → auto-warms memory on hit
- **Repository Pattern** — `UserRepository` abstracts network, `BenchmarkRunner` wraps timing
- **MVVM** — all screens backed by `@HiltViewModel` with `StateFlow`
- **Clean Architecture** — `domain/` layer has zero Android dependencies

---

## Tech Stack

- **Kotlin** + **Coroutines** + **Flow**
- **Jetpack Compose** (Material 3)
- **Hilt** for dependency injection
- **Room** for structured cache + analytics persistence
- **Retrofit** + **OkHttp** (with HTTP-level cache interceptor)
- **Kotlinx Serialization**

---

## Setup

1. Clone the repo
2. Open in Android Studio Ladybug or newer
3. Run on a device or emulator (minSdk 26)
4. No API keys required — uses GitHub's public search endpoint

---

## Resume Highlights

- Built multi-layer caching system (Memory/Disk/Room/Hybrid) with real-time latency benchmarking
- Implemented Strategy + Factory patterns for pluggable, runtime-selectable cache strategies
- Designed analytics pipeline persisting benchmark data to Room with Flow-based reactive UI
- Architected async "race mode" using Kotlin `async/await` in `coroutineScope` for concurrent strategy comparison
- Full MVVM + Clean Architecture with Hilt DI, zero coupling between layers

---

## Data Source

[GitHub REST API](https://docs.github.com/en/rest) — `/search/users` endpoint. Free, no auth required for basic usage.
