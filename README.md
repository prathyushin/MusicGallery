# MusicGallery V3.1

A modern Android music and podcast player focused on a premium, fast and personal listening experience.

> **V3.1 goal:** be the first genuinely usable release. Later versions will add substantial features while keeping a clear, visible version history.

## Current release

**Music Gallery 3.1.0-alpha01**  
Version code: **31**  
Minimum Android: **9 / API 28**  
Target Android: **16 / API 36**

### V3.1 — The First Usable Release

Implemented in this release line:

- Real local-device music scanning through Android MediaStore
- Android 9+ audio-library permission handling
- Real local audio playback through Media3 ExoPlayer
- Play/pause and next-track controls
- Persistent mini-player while browsing the app
- Local library list with song, artist and album metadata
- Fast local music search
- Home, Library, Podcasts and Search navigation
- Visible version identity and "What's new" surface
- RSS podcast feed parsing foundation
- Dedicated podcast area ready for subscriptions, episode persistence and downloads
- Material 3 + Jetpack Compose UI
- Android 9+ compatibility as a hard platform requirement
- GitHub Actions debug APK build pipeline

The goal of `3.1.0-alpha01` is not to pretend every planned feature is finished. It establishes a real, installable product foundation that can be tested and improved release by release.

## Release versioning policy

MusicGallery uses semantic-style version numbers so users can see that the app is actively evolving.

```text
3.1.0-alpha01
│ │ │ └── build/release iteration
│ │ └──── patch
│ └────── minor feature release
└──────── major product generation
```

Every meaningful update must bump the version number in **both** `app/build.gradle.kts` and `AppVersion`. The Home screen should also communicate the current release when the change is user-visible.

Examples:

- `3.1.0-alpha02` — next V3.1 development build
- `3.1.0` — first stable V3.1 release
- `3.2.0` — meaningful new feature generation
- `3.2.1` — focused bug-fix release

Do not reuse an existing version code. Version codes increase monotonically with every distributable build.

## Product vision

MusicGallery is built around three principles:

- **Beautiful** — spacious layouts, expressive artwork, fluid transitions and adaptive theming.
- **Powerful** — serious local-music playback, library management, playlists, lyrics and queue controls.
- **Unified** — music and podcasts share one consistent player, history, queue and listening experience.

## V3 foundation

### Music

Planned and progressively implemented:

- Local music library
- Songs, albums, artists, genres and folders
- Playlists and favorites
- Recently played and recently added
- Queue management
- Shuffle, repeat, gapless playback and crossfade
- Background playback and media controls
- Lyrics support
- Search
- Statistics and listening history
- Artwork-focused now-playing experience

### Podcasts

Planned and progressively implemented:

- Podcast discovery and RSS feed support
- Search and subscriptions
- Podcast library and show pages
- Episode lists with progress tracking
- Continue listening
- Downloads and offline playback
- Playback speed controls
- Sleep timer
- Show notes
- Episode queueing
- Podcast artwork and episode metadata

## Architecture

The V3 foundation uses Kotlin, Jetpack Compose and AndroidX Media3. Music and podcast features are separated into domain/data layers so playback can remain shared while each content type evolves independently.

Current foundation:

```text
app/src/main/java/com/prathyushin/musicgallery
├── library
│   └── MusicScanner
├── model
│   └── MediaModels
├── playback
│   ├── PlaybackController
│   └── MusicPlaybackService
├── podcast
│   └── PodcastFeedRepository
└── ui
    ├── MiniPlayer
    └── Theme
```

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Media3
- Coroutines
- Coil
- Android Architecture Components

## Upstream inspiration and attribution

MusicGallery V3 is being developed with permission to reuse code from the project's contributors where their licensing/permission permits it. The music-player foundation is based on work from **PixelPlayer** by PixelPlayerHQ, with podcast functionality informed by the feature set and architecture ideas of **uPod** by svenwiegand.

Before redistributing copied upstream source, preserve applicable copyright notices, license terms and third-party notices.

- PixelPlayer: https://github.com/PixelPlayerHQ/PixelPlayer
- uPod: https://github.com/svenwiegand/upod

## Development status

**V3.1 is an active alpha release line.** The objective is to ship a genuinely usable build first, then move feature-by-feature toward the full product vision.

The GitHub Actions workflow builds a debug APK on pushes to `v3.1`, making each iteration testable before the next version is promoted.

## Roadmap

- [x] V3.1 release identity and visible versioning
- [x] Premium navigation shell
- [x] Real local music scanning
- [x] Real local audio playback
- [x] Persistent mini-player
- [x] Local music search
- [x] RSS parsing foundation
- [x] Automated debug APK builds
- [ ] Media3 controller/service integration for persistent background playback
- [ ] Album/artist/genre/folder views
- [ ] Queue, shuffle and repeat
- [ ] Database-backed favorites, history and progress
- [ ] Podcast subscriptions and show persistence
- [ ] Podcast episode playback and progress
- [ ] Podcast downloads and offline playback
- [ ] Full Now Playing redesign
- [ ] Lyrics and synchronized playback integration
- [ ] Playlists and smart mixes
- [ ] Search across music and podcasts
- [ ] Settings and customization
- [ ] Android Auto support
- [ ] Widgets
- [ ] Release build and store-ready polish

## Design direction

MusicGallery aims to compete on **experience**, not by copying another product. The visual direction combines Android-native Material 3 interaction patterns with the content-first, artwork-led feel of premium music players, while keeping MusicGallery's music + podcast identity distinct.

## License

See the applicable source licenses and third-party notices before distribution. MusicGallery V3.1 should not be assumed to be covered by a single upstream license merely because it incorporates or derives from upstream projects.
