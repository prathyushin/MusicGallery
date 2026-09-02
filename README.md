# MusicGallery V3.1

A modern Android music and podcast player focused on a premium, fast and personal listening experience.

> **V3.1 goal:** make every release visibly feel like progress — with a clear version identity, a polished listening shell, and a foundation that can keep growing without losing the app's personality.

## Current release

**Music Gallery 3.1.0-alpha01**  
Version code: **31**  
Minimum Android: **9 / API 28**  
Target Android: **16 / API 36**

### V3.1 — The Listening Update

- Release-aware version identity shown in the app
- "What's new" surface on Home
- Refined Home, Library, Podcasts and Search hierarchy
- Persistent mini-player shell
- Dedicated music + podcast navigation
- Shared visual language built with Material 3 and Jetpack Compose
- Android 9+ compatibility remains a hard platform requirement

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

- `3.1.0-alpha02` — another V3.1 development build
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

### Experience

- Premium, spacious UI inspired by modern music apps
- Dynamic artwork-driven surfaces
- Light and dark themes
- Material 3 / Jetpack Compose
- Persistent mini-player
- Dedicated full-screen Now Playing direction
- Music/podcast-aware controls
- Smooth navigation and subtle motion
- Phone-first responsive layouts

## Architecture

The V3 foundation uses Kotlin, Jetpack Compose and AndroidX Media3. Music and podcast features are separated into domain/data layers so playback can remain shared while each content type evolves independently.

Planned high-level modules:

```text
app
├── core
│   ├── playback
│   ├── database
│   ├── network
│   └── design
├── music
│   ├── library
│   ├── playlists
│   ├── lyrics
│   └── statistics
└── podcast
    ├── discovery
    ├── subscriptions
    ├── episodes
    └── downloads
```

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Media3
- Room (as database features are implemented)
- Coroutines + Flow
- Coil
- Android Architecture Components

## Upstream inspiration and attribution

MusicGallery V3 is being developed with permission to reuse code from the project's contributors where their licensing/permission permits it. The music-player foundation is based on work from **PixelPlayer** by PixelPlayerHQ, with podcast functionality informed by the feature set and architecture ideas of **uPod** by svenwiegand.

Before redistributing copied upstream source, preserve applicable copyright notices, license terms and third-party notices.

- PixelPlayer: https://github.com/PixelPlayerHQ/PixelPlayer
- uPod: https://github.com/svenwiegand/upod

## Development status

**V3.1 is an active alpha development line.** The `v2` branch remains preserved as the previous development line, while `v3` and `v3.1` track the newer product direction.

The current UI is a product shell while the deeper music-library, podcast, persistence and background-playback systems are progressively integrated.

## Roadmap

- [x] V3.1 release identity and visible versioning
- [x] Premium navigation shell
- [x] Music + Podcasts top-level experience
- [x] Persistent mini-player shell
- [ ] Port/upgrade the complete music playback and library engine
- [ ] Unified Media3 background playback service
- [ ] Real local music scanning for Android 9+
- [ ] Podcast RSS discovery and subscriptions
- [ ] Podcast episode downloads
- [ ] Database-backed history and progress
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
