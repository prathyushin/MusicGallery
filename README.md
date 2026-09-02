# MusicGallery 4.0

## The Personal Media Platform

MusicGallery 4.0 is the next major product generation of MusicGallery: a local-first Android listening system for personal music collections and podcasts.

> **V4 principle:** reliability before novelty. The app should feel like one continuous listening system rather than a collection of separate screens.

### Current V4 build

**MusicGallery 4.0.0-alpha01**  
Version code: **40**  
Minimum Android: **9 / API 28**  
Target Android: **16 / API 37**

This is a new V4 release branch based on the V3.1 foundation. Existing V1/V2/V3/V3.1 versions are not replaced or edited by V4.

## V4 product promise

**Your music and podcasts, organized beautifully, available reliably, and controlled by you.**

MusicGallery is designed as a premium personal media platform—not an Apple Music clone and not merely an offline file browser.

### Core differentiators

- Local music ownership
- Offline-first behavior
- First-class podcasts
- One persistent playback experience
- Material 3 and artwork-led UI
- No mandatory account
- Large-library organization
- Clear privacy and storage controls

## V4.0 alpha foundation

The V4 branch begins with an installable Android product line and the architecture needed for the larger V4 plan.

### Current foundation

- Android 9+ support
- Kotlin + Jetpack Compose
- Material 3
- Media3 ExoPlayer
- MediaSessionService playback architecture
- Local MediaStore music scanning
- Local music search
- Home / Library / Podcasts / Search navigation
- Persistent mini-player
- Podcast RSS foundation
- Version identity and What's New direction
- GitHub Actions installable APK build
- GitHub Release automation

### Playback architecture

The V4 playback direction moves the Player and MediaSession into `MusicPlaybackService`. The Activity communicates through a Media3 `MediaController` instead of owning an independent ExoPlayer instance.

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
Local / Podcast media
```

This is the foundation for background playback, notification controls, lock-screen controls, Bluetooth/headset actions, audio focus and queue continuity.

## V4 UX direction

The V4 interface follows the supplied Grand-Scale Product Blueprint and earlier immersive-audio research:

- Content-first hierarchy
- Artwork as the visual hero
- Quiet structural chrome
- Layered near-black dark mode
- Subtle artwork-derived accents
- Persistent mini-player
- Predictable mini-player → Now Playing expansion
- Contextual bottom sheets
- Consistent long-press actions
- Explicit empty, loading, error and offline states
- Phone-first responsive design with tablet/foldable adaptation
- Accessible controls and localized content descriptions

## V4.0 Core target

The full V4 Core target is intentionally larger than this first alpha build.

### Playback

- Background playback
- MediaSessionService
- System media notification
- Audio focus
- Becoming-noisy handling
- Headset/Bluetooth transport controls
- Queue reorder/remove
- Previous/next
- Shuffle
- Repeat off/all/one
- Saved playback position
- Playback restoration

### Music library

- Incremental MediaStore scanning
- Songs, albums, artists, genres and folders
- Sorting and filtering
- Album and artist pages
- Artwork caching
- Stale-row removal
- Manual rescan
- Large-library paging

### Personal organization

- Playlists
- Favorites
- Recently played
- Continue listening
- Recently added
- Queue persistence
- Contextual add-to-playlist actions
- Undo for destructive actions where practical

### Podcasts

- RSS URL import
- Feed validation
- Show pages
- Episode pages
- Subscriptions
- Episode progress
- Playback speed
- Skip controls
- Show notes
- Download state
- Recoverable feed/media errors

### Downloads

- Enqueue/cancel/retry/delete
- Wi-Fi-only option
- Storage usage
- Resumable WorkManager jobs
- App-managed storage
- Cleanup controls
- Offline playback

### Search

- Unified music + podcast search
- Tracks, albums, artists, playlists, shows and episodes
- Type filters
- Recent searches
- Direct Play / Add to Queue actions

### Settings

- Theme
- Dynamic color
- Scan controls
- Permission status
- Audio behavior
- Podcast defaults
- Download policy
- Storage management
- Privacy
- Licenses
- Version/build information
- User-consented diagnostic export

## Architecture plan

The intended V4 structure is modular and state-driven:

```text
app
core
├── model
├── playback
├── database
├── network
└── designsystem

feature
├── home
├── library
├── search
├── podcasts
├── downloads
├── playlists
└── settings
```

Compose screens observe immutable state through ViewModels and Flow. Repositories coordinate MediaStore, Room, network and playback. Room stores durable user-owned state while MediaStore remains the authority for shared local audio metadata. WorkManager handles rescans, feed refresh, downloads and cleanup.

## V4 delivery sequence

**Phase A — Architecture hardening**  
Module boundaries, Room schema/migrations, playback interface, MediaSessionService, notification, restoration contract and tests.

**Phase B — Listening core**  
Queue, shuffle, repeat, Now Playing, mini-player, history, favorites, playlists, audio focus and Bluetooth/lock-screen validation.

**Phase C — Library excellence**  
Incremental scan, categories, sorting/filtering, album/artist/folder pages, artwork strategy and large-library performance.

**Phase D — Podcast reliability**  
Subscriptions, episode persistence, progress, speed/skip controls, refresh jobs, downloads, storage management and offline tests.

**Phase E — Experience polish**  
Home personalization, unified search, settings, onboarding, accessibility, localization readiness, tablet adaptation and documentation.

**Phase F — Release candidate**  
Beta testing, crash/ANR review, battery/memory profiling, device matrix testing, migrations, privacy/licensing review and final QA.

## Versioning

V4 uses explicit release identities:

```text
4.0.0-alpha01
4.0.0-alpha02
4.0.0-alpha03
...
4.0.0-beta01
...
4.0.0
```

Every distributable build receives a new monotonically increasing Android version code. Meaningful user-visible changes must also be recorded in `CHANGELOG.md`.

### Release rule

An alpha is not described as stable. A stable `4.0.0` release is only ready after the playback, library, podcast, accessibility, privacy, licensing and release-quality gates pass.

## Version history

| Version | Role | Status |
|---|---|---|
| V1.0 | Original MusicGallery APK | Released baseline |
| V2.0 | Development generation | Historical development branch |
| V3.0 | Major product foundation | Historical development branch |
| V3.1.0-alpha01 | First genuinely usable release line | V3 alpha |
| **V4.0.0-alpha01** | **Personal Media Platform foundation** | **Current V4 alpha** |

## Upstream attribution

The project has documented reuse/inspiration relationships with PixelPlayerHQ/PixelPlayer and svenwiegand/uPod. The project owner has stated that permission was obtained from the relevant PixelPlayer contributor for code reuse. Applicable third-party licenses, copyright notices and attribution requirements must still be preserved before redistribution.

## Build / install

GitHub Actions builds an installable debug APK for the V4 branch and publishes the release artifact when the workflow completes successfully.

For local development:

```bash
gradle --no-daemon assembleDebug
```

The resulting debug APK is under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## License and privacy

MusicGallery is designed to keep core local playback account-free. Network access is used for explicit online functions such as podcast feeds, artwork and media downloads.

Before public redistribution, maintain a complete dependency/license inventory and preserve required upstream notices.

## Developer

**Built by Pratyush.**

## Documentation

- `CHANGELOG.md` — release-by-release changes
- `MusicGallery 4.0 Grand-Scale Product Blueprint` — product, UX, technical, quality, privacy and delivery specification
