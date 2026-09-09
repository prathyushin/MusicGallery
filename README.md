# Music Gallery 4.4.0

> **A HOXGOX Production**  
> **Built by Pranav & Prathyusha**

Music Gallery is a local-first Android music player built around one idea: **your music should feel as good as the player itself**.

Version **4.4.0** is the stable V4.4 product pass. It rebuilds the listening surface around real device music, reliable Media3 playback, artwork-led presentation, a persistent mini-player, and a full Now Playing experience.

---

## V4.4.0 — Fluid Listening

**Version:** `4.4.0`  
**Version code:** `45`  
**Minimum Android:** `9 / API 28`  
**Target Android:** `16 / API 37`  
**Release line:** `v4.4.0`

### Product rule

```text
REAL DEVICE MUSIC
        ↓
REAL MEDIA3 PLAYBACK
        ↓
ONE SYNCHRONIZED PLAYER STATE
        ↓
SMOOTH UI
        ↓
ARTWORK-FIRST EXPERIENCE
```

No fabricated songs. No fake listening statistics. No fake artwork. No demo queue pretending to be user data.

---

## What changed in 4.4.0

### Rebuilt Home

The Home screen is now a music destination rather than a database list.

- Editorial-style header and hierarchy
- Artwork-led featured track
- Quick-pick carousel
- Album carousel
- Library-derived song, album and artist counts
- Local-only content
- Category filtering
- Compact navigation
- Better spacing and touch targets
- Explicit empty states instead of placeholder catalog content

### Real playback, end to end

Music Gallery treats playback as the central product state.

```text
Compose UI
    ↓
PlaybackController
    ↓
MediaController
    ↓
MediaSessionService
    ↓
ExoPlayer / Media3
    ↓
Android MediaStore URI
```

Playback actions issued before the asynchronous `MediaController` connection completes are queued instead of being silently lost.

Supported controls:

- Play / pause
- Previous / next
- Seek
- Shuffle
- Repeat off / all / one
- Background playback through MediaSessionService
- System media-session integration
- Audio-becoming-noisy handling

### Persistent mini-player

The mini-player is part of the listening experience, not a decorative footer.

- Real current track
- Real artwork
- Real play/pause state
- Real next action
- Live progress indicator
- Tap to open Now Playing
- Follows track transitions

### Full Now Playing

The Now Playing surface is built around artwork and playback controls.

- Large artwork
- Soft blurred artwork ambience
- Track title and artist
- Live progress
- Interactive seek bar
- Previous / play-pause / next
- Shuffle
- Repeat
- Favorite
- Smooth entry and exit

### Better library

Library remains fully tied to Android MediaStore.

- Songs
- Albums
- Artists
- Real album artwork
- Real duration
- Real metadata
- Fast local search
- Locally persisted favorites

### Search that actually searches

Search is local and metadata-driven.

It searches:

- Song title
- Artist
- Album

There is no fake online catalog behind the search screen.

### Visual system

V4.4.0 moves Music Gallery toward a restrained premium music-app language:

- Strong typography
- Generous spacing
- Rounded but controlled surfaces
- Artwork as the primary visual element
- Subtle depth instead of heavy effects
- High-contrast text
- Consistent light/dark palettes
- Accessible descriptions on important artwork and controls
- Motion used to communicate state

The visual direction takes inspiration from the quality and interaction principles of modern music players, including the supplied Echo Nightly reference, **without copying its branding, artwork, source code, or exact interface**.

---

## Real-data policy

Music Gallery follows a strict rule:

> **If the device does not provide it, the app does not pretend it exists.**

The app must not manufacture:

- Songs
- Albums
- Artists
- Podcast episodes
- Listening history
- Play counts
- Queue entries
- Artwork
- Playback progress

When data is unavailable, Music Gallery uses an explicit empty, loading, or unavailable state.

---

## Music library

Music Gallery scans Android MediaStore for local audio marked as music.

Current track metadata includes:

| Field | Source |
|---|---|
| Title | Android MediaStore |
| Artist | Android MediaStore |
| Album | Android MediaStore |
| Duration | Android MediaStore |
| Media ID | Android MediaStore |
| Artwork | Android media artwork URI |
| Playback URI | Android MediaStore content URI |

The scanner keeps the core listening experience offline-first.

---

## Playback reliability

V4.4.0 fixes the asynchronous MediaController boundary that could make an early song tap appear to do nothing.

The controller now retains pending commands until the MediaController is ready. It also resolves the requested queue item by media ID after conversion to playable `MediaItem`s, preventing a stale-library index mismatch.

---

## Favorites

Favorites are persisted locally using Android `SharedPreferences`.

Only local MediaStore track IDs selected by the user are stored. No account or cloud service is required.

---

## Podcasts

The Podcasts destination is intentionally honest.

The UI is reserved for real RSS/podcast functionality as that subsystem is completed. V4.4.0 does **not** insert sample shows or fake episodes simply to make the screen look populated.

---

## Privacy

Core local playback does not require:

- A Music Gallery account
- A cloud music catalog
- A subscription

Network access is reserved for explicit online functionality such as future podcast/RSS features.

---

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- Android MediaStore
- AndroidX Media3
- ExoPlayer
- MediaSessionService
- Coil 3
- Kotlin Coroutines
- StateFlow
- GitHub Actions

---

## Build

From the project root:

```bash
gradle --no-daemon assembleDebug
```

Generated APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions builds an installable debug APK for the `v4.4.0` release line and publishes it to the GitHub Release when the build succeeds.

---

## Release verification

A CI build passing is necessary, but a release is not considered fully production-certified until the APK has also been exercised on a real Android device.

### Real-device checklist

- [ ] Audio permission flow
- [ ] Library scan
- [ ] Artwork loading
- [ ] Tap-to-play
- [ ] Play / pause
- [ ] Previous / next
- [ ] Seek
- [ ] Shuffle
- [ ] Repeat off / all / one
- [ ] Mini-player synchronization
- [ ] Now Playing synchronization
- [ ] Background playback
- [ ] Lock-screen/system media controls
- [ ] Favorite persistence
- [ ] Search
- [ ] Empty library state
- [ ] Large-library scrolling
- [ ] Light theme
- [ ] Dark theme
- [ ] Accessibility labels
- [ ] No fabricated media content

---

## Version history

| Version | Role | Status |
|---|---|---|
| V1.0 | Original MusicGallery baseline | Historical |
| V2.0 | Development generation | Historical |
| V3.0 | Major product foundation | Historical |
| V3.1.0-alpha01 | Usable release line | Historical |
| V4.0.0-alpha01 | Personal Media Platform foundation | Historical |
| V4.2.x | Real library + fluid UI foundation | Historical |
| V4.3.0-alpha01 | Playback reliability rebuild | Superseded |
| **V4.4.0** | **Fluid Listening + polished real-data experience** | **Current** |

---

## Credits

### A HOXGOX Production

**Built by Pranav & Prathyusha**

Music Gallery is developed as a focused local-first Android music experience, with the product direction centered on real device media, reliable playback, and a premium interface.

---

## Attribution

Music Gallery may contain or have been informed by open-source work and architectural ideas from third-party projects. Applicable licenses, notices, copyright statements, and attribution requirements must remain preserved when redistributing the project.

The Echo Nightly project was used as a UI/UX reference for interaction quality and modern music-player patterns; Music Gallery is an independent implementation.
