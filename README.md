# MusicGallery 4.2.1-alpha01

## The Personal Media Platform — Fluid Library UI

MusicGallery is a local-first Android music and personal media player focused on real device media, artwork-first presentation, continuous playback, and a fluid listening experience.

> **V4.2 principle:** real data → real functionality → polished UI. No fabricated songs, artists, albums, podcast episodes, listening statistics, artwork, queue items, or playback progress.

### Current release

**MusicGallery 4.2.1-alpha01**  
Version code: **43**  
Minimum Android: **9 / API 28**  
Target Android: **16 / API 37**

**Release:** `v4.2.1-alpha01`  
**Release title:** `MusicGallery 4.2.1-alpha01 — Fluid Library UI`

[Download the latest APK](https://github.com/prathyushin/MusicGallery/releases/download/v4.2.1-alpha01/MusicGallery-v4.2.1-alpha01-debug.apk) · [GitHub Release](https://github.com/prathyushin/MusicGallery/releases/tag/v4.2.1-alpha01)

## What V4.2.1 delivers

V4.2.1 is the current installable alpha release built on the real V4.2 playback and MediaStore foundation. The main change is the new **fluid, artwork-first interaction layer** while keeping the core playback architecture service-driven.

### Ready now

- Real local music from Android MediaStore
- Songs, albums and artists library views
- Real local search across title, artist and album metadata
- Real album artwork through device media URIs
- Fluid artwork-first visual hierarchy
- Floating bottom navigation dock
- Home → Library → Podcasts → Search flow
- Animated persistent mini-player
- Mini-player → full Now Playing transition
- Artwork-derived blurred ambience in Now Playing
- Live playback position
- Interactive seeking
- Previous / next controls
- Shuffle
- Repeat
- Media3 ExoPlayer playback
- MediaSessionService background playback architecture
- System media-session integration
- Runtime audio permission handling
- Explicit empty states instead of fake/demo content
- Edge-to-edge UI
- GitHub Actions build verification
- Installable GitHub Release APK

## Product promise

**Your music, organized beautifully, available reliably, and controlled by you.**

MusicGallery is designed as a premium personal media experience—not an Apple Music clone and not merely an offline file browser.

### Core identity

- Local music ownership
- Offline-first core listening
- Artwork-first presentation
- One continuous playback experience
- Fluid mini-player and Now Playing interaction
- No mandatory account
- Real device data as the source of truth
- Clear empty/loading/error states
- Privacy-conscious local-first architecture

## The V4.2.1 listening flow

```text
App launch
   ↓
Permission check
   ↓
MediaStore scan
   ↓
Home
   ├── Recently available local music
   ├── Album/artwork presentation
   └── Continue into Library
        ↓
Library
   ├── Songs
   ├── Albums
   └── Artists
        ↓
Tap a real track
   ↓
Media3 MediaController
   ↓
MusicPlaybackService
   ↓
ExoPlayer
   ↓
Persistent Mini Player
   ↓
Expand
   ↓
Fluid Now Playing
   ├── Artwork ambience
   ├── Live progress
   ├── Seek
   ├── Previous / Next
   ├── Shuffle
   └── Repeat
```

Search and Podcasts remain accessible through the floating navigation dock. Podcast functionality is intentionally not filled with fabricated episodes or sample content.

## Fluid UI direction

The V4.2.1 UI applies the supplied flow/Now Playing direction to the real library instead of replacing real functionality with presentation-only mock screens.

### Design principles

- **Artwork is the visual hero.**
- **Playback is continuous.** The mini-player and Now Playing surface are treated as one listening interaction.
- **Navigation stays compact.** The floating dock provides persistent access without dominating the screen.
- **Motion communicates state.** Expansion, collapse and playback changes use fluid transitions rather than disconnected screen changes.
- **Ambient presentation follows the artwork.** Now Playing uses blurred artwork ambience to create depth without inventing content.
- **Controls remain accessible.** Playback actions are kept clear and touch-friendly.
- **Data is honest.** If the device has no music, the UI shows an empty state instead of pretending that a catalog exists.

## Real-data policy

V4.2.1 follows a strict production-development rule:

> **No fake data.**

The following must never be hard-coded as if they were real user content:

- Songs
- Artists
- Albums
- Podcast episodes
- Listening statistics
- Recently played items
- Queue entries
- Artwork
- Playback position

When real data is unavailable, MusicGallery should show a useful empty, loading, offline or error state.

This rule exists specifically to prevent the earlier placeholder/demo experience from returning.

## Playback architecture

Playback authority remains outside the Compose Activity. The UI communicates with Media3 through a `MediaController`, while the service owns the player and media session.

```text
Compose UI
    ↓
PlaybackController
    ↓
MediaController
    ↓
MediaSessionService
    ↓
ExoPlayer
    ↓
Local MediaStore media
```

This architecture supports the continuous listening model and provides the foundation for background playback, system media controls, lock-screen controls, Bluetooth/headset actions, audio focus and queue continuity.

## Music library

MusicGallery reads local audio metadata from Android MediaStore.

Current library capabilities:

- Track title
- Artist
- Album
- Duration
- Media ID
- Artwork URI
- Songs view
- Albums view
- Artists view
- Local metadata search
- Playback from real device media

The library is intentionally local-first. MusicGallery does not require an account or cloud music catalog for core local playback.

## Search

V4.2.1 provides real local search rather than a decorative search screen.

Search currently matches available local music metadata including:

- Track title
- Artist
- Album

Podcast search and a broader unified search experience remain part of the next product-hardening stage.

## Podcasts

The application retains the podcast destination and RSS foundation, but V4.2.1 does **not** invent podcast episodes or sample feeds.

The next podcast stage is expected to add persistent subscriptions, feed refresh, episode persistence, progress, downloads and resilient network/error handling.

## Permissions

On first use, MusicGallery requests the Android media/audio permission appropriate for the device API level before scanning local music.

If permission is denied or no compatible media is available, the app should explain the state rather than display fabricated content.

## What changed from V4.1

V4.1 established the visual redesign direction but still contained too much placeholder/demo presentation. V4.2.1 moves the experience onto the real V4.2 data and playback foundation.

### V4.1 → V4.2.1

| Area | V4.1 direction | V4.2.1 |
|---|---|---|
| Music content | Placeholder/demo | Real MediaStore data |
| Artwork | Placeholder/gradient | Real device artwork |
| Search | Non-functional placeholder | Real local metadata search |
| Library | Demo content | Songs / Albums / Artists |
| Playback | UI-oriented mock state | Media3 service playback |
| Progress | Mock slider | Live position + seeking |
| Mini-player | Static presentation | Persistent animated player |
| Now Playing | Separate visual concept | Fluid mini-player expansion |
| Podcast content | Placeholder direction | No fabricated episodes |
| Navigation | Standard navigation concept | Floating fluid dock |
| Data integrity | Mixed/demo | Real data only |

## Architecture status

The current V4.2.1 release intentionally preserves the stable playback foundation rather than performing an unnecessary rewrite.

Core technologies:

- Kotlin
- Jetpack Compose
- Material 3
- Media3 / ExoPlayer
- MediaSessionService
- Android MediaStore
- Coil artwork loading
- StateFlow / Flow
- GitHub Actions

The supplied architecture direction calls for the visual layer to reflect existing real data and playback state while keeping the core media/repository architecture stable. V4.2.1 follows that approach.

## Release verification

The current release was built through GitHub Actions and published as an installable APK.

| Item | Verified value |
|---|---|
| Version | `4.2.1-alpha01` |
| Version code | `43` |
| Branch / tag | `v4.2.1-alpha01` |
| CI run | `#26` |
| Workflow result | `completed / success` |
| Release title | `MusicGallery 4.2.1-alpha01 — Fluid Library UI` |
| APK | `MusicGallery-v4.2.1-alpha01-debug.apk` |
| APK size | `71,591,411 bytes` |
| SHA-256 | `256118b2c69076e884c2c2559e46cf7d07adf76611765a352504f402e36ac2f2` |
| Head commit | `78ce6fdc49cae06e9cb91b5b68c571a52c2d2176` |

## Installation

### From GitHub Release

Download the latest debug APK from the [V4.2.1-alpha01 release](https://github.com/prathyushin/MusicGallery/releases/tag/v4.2.1-alpha01).

The direct APK is:

`MusicGallery-v4.2.1-alpha01-debug.apk`

### Local development

```bash
gradle --no-daemon assembleDebug
```

The generated debug APK is normally located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Current completion level

V4.2.1 is an **installable alpha**, not a production/stable release.

### Core listening experience

**Ready for real-device testing:**

- Real local library
- Real artwork
- Real search
- Real playback
- Live progress and seeking
- Mini-player
- Now Playing
- Fluid navigation
- No fabricated content

### Still being hardened

The following areas are not represented as complete merely because the Fluid UI is complete:

- Persistent favorites
- Persistent listening history
- Full playlist creation/editing
- Queue persistence and advanced queue editing
- Complete podcast subscription/feed/episode system
- Podcast downloads and offline management
- Full Settings implementation
- Large-library performance optimization
- Automated UI/instrumentation coverage
- Wider device and tablet/foldable validation
- Production signing and final release configuration
- Final crash/ANR, battery and memory profiling
- Full privacy/licensing release audit

## Recommended next development pass

The next stage should focus on **hardening and product completeness**, not returning to placeholder UI.

### Priority 1 — real user state

- Room-backed favorites
- Listening history
- Recently played
- Queue persistence
- Playlists
- Continue listening

### Priority 2 — library excellence

- Incremental MediaStore scanning
- Sorting and filtering
- Album pages
- Artist pages
- Folder/category support where appropriate
- Artwork caching
- Large-library paging/performance
- Manual rescan
- Stale-media cleanup

### Priority 3 — podcast reliability

- RSS import
- Feed validation
- Subscriptions
- Persistent episodes
- Episode progress
- Playback speed
- Skip controls
- Downloads
- Recoverable network/media errors

### Priority 4 — production quality

- Automated UI tests
- Playback/service tests
- Accessibility audit
- Tablet/foldable layouts
- Battery/memory profiling
- Crash/ANR review
- Migration testing
- Release signing
- Dependency/license inventory

## Version history

| Version | Role | Status |
|---|---|---|
| V1.0 | Original MusicGallery APK | Released baseline |
| V2.0 | Development generation | Historical development branch |
| V3.0 | Major product foundation | Historical development branch |
| V3.1.0-alpha01 | First genuinely usable release line | Historical alpha |
| V4.0.0-alpha01 | Personal Media Platform foundation | Historical V4 alpha |
| V4.2.0-alpha01 | Real Library Update / playback foundation | Previous V4.2 alpha |
| **V4.2.1-alpha01** | **Fluid Library UI + real data integration** | **Current installable alpha** |

Previous releases are preserved rather than silently rewritten.

## Upstream attribution

The project has documented reuse/inspiration relationships with PixelPlayerHQ/PixelPlayer and svenwiegand/uPod. The project owner has stated that permission was obtained from the relevant PixelPlayer contributor for code reuse.

Applicable third-party licenses, copyright notices and attribution requirements must remain preserved before redistribution.

## Privacy and networking

MusicGallery is designed around account-free local playback. Core local music playback does not require a cloud music account.

Network access is reserved for explicit online functions such as podcast feeds and future podcast/media downloads.

Before public redistribution, maintain a complete dependency/license inventory and preserve required upstream notices.

## Documentation

- `README.md` — current product and development status
- `CHANGELOG.md` — release-by-release changes
- `CHANGELOG_4.2.md` — V4.2 development history
- `MusicGallery 4.0 Grand-Scale Product Blueprint` — original V4 product, UX, technical and quality direction
- Supplied Now Playing / flow specifications — interaction and playback presentation direction

## Developer

**Built by Pratyush.**
