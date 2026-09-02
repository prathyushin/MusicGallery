# MusicGallery V3

A modern Android music and podcast player focused on a premium, fast and personal listening experience.

> **V3 goal:** go beyond the usual music-player UI with a unified music + podcast library, intelligent discovery, powerful playback, and a polished Apple-Music-inspired visual language.

## Product vision

MusicGallery V3 is being built around three principles:

- **Beautiful** — spacious layouts, expressive artwork, fluid transitions and adaptive theming.
- **Powerful** — serious local-music playback, library management, playlists, lyrics and queue controls.
- **Unified** — music and podcasts share one consistent player, history, queue and listening experience.

## V3 highlights

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
- Automatic/resumable download architecture planned

### Experience

- Premium, spacious UI inspired by modern music apps
- Dynamic artwork-driven surfaces
- Light and dark themes
- Material 3 / Jetpack Compose
- Persistent mini-player
- Dedicated full-screen Now Playing
- Music/podcast-aware controls
- Smooth navigation and subtle motion
- Designed for phones first, with responsive layouts for larger screens

## Architecture

The V3 foundation uses Kotlin, Jetpack Compose and AndroidX Media3. Music and podcast features are separated into domain/data layers so that playback remains shared while each content type can evolve independently.

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
- Room (planned/used as features are implemented)
- Coroutines + Flow
- Coil
- Android Architecture Components

## Upstream inspiration and attribution

MusicGallery V3 is being developed with permission to reuse code from the project's contributors where their licensing/permission permits it. The music-player foundation is based on work from **PixelPlayer** by PixelPlayerHQ, with podcast functionality informed by the feature set and architecture ideas of **uPod** by svenwiegand.

Before redistributing any copied upstream source, preserve the applicable copyright notices and license terms from those projects and from their third-party dependencies.

- PixelPlayer: https://github.com/PixelPlayerHQ/PixelPlayer
- uPod: https://github.com/svenwiegand/upod

## Status

**V3 is under active development.** The `v2` branch is preserved as the previous development line; new V3 work lives on the `v3` branch.

## Roadmap

- [x] V3 branch and project foundation
- [x] Premium navigation shell
- [x] Music + Podcasts top-level experience
- [ ] Port/upgrade the complete music playback and library engine
- [ ] Unified Media3 playback service
- [ ] Real local music scanning
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

MusicGallery V3 aims to compete on **experience**, not by copying another product. The visual direction takes cues from premium music applications while adding deeper customization, a stronger local-library experience and first-class podcasts.

## License

See the applicable source licenses and third-party notices before distribution. MusicGallery V3 itself should not be assumed to be covered by a single upstream license merely because it incorporates or derives from upstream projects.
