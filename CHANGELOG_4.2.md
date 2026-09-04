# MusicGallery 4.2.1-alpha01 — Fluid Library UI

## Goal
V4.2.1 is the next installable pass after V4.2.0-alpha01. It applies the supplied MusicGallery flow/UI direction to the real MediaStore-backed library without reintroducing demo content.

## UI flow
- Edge-to-edge Compose shell.
- Floating bottom dock for Home, Library, Podcasts and Search.
- Persistent mini player above the dock when real playback is active.
- Mini player expands into a full Now Playing surface with animated enter/exit instead of a hard navigation cut.
- Artwork remains the visual hero throughout the playback flow.
- Artwork is reused as a subtle blurred ambient layer in Now Playing.
- Live playback position is reflected in the mini player and Now Playing scrubber.
- Transport controls use the Media3 controller as the source of truth.
- Library uses Songs / Albums / Artists modes over actual scanned media.
- Search filters the actual local library by title, artist and album.
- Podcast area remains honest: no fabricated shows or episodes are displayed.

## Data rules
- No hard-coded demo tracks.
- No fake artist or album names.
- No fake artwork.
- No fake listening statistics or progress.
- Empty states are used when real data is unavailable.

## Architecture
The playback UI now observes `PlaybackController` state through Kotlin `StateFlow`. The existing Media3 `MediaSessionService`, MediaStore scanner and Track model remain the data/playback foundation.

## Source direction
The implementation follows the supplied V4/V4.1 design material: content-first artwork, strong hierarchy, a floating dock, continuous mini-player/Now Playing flow, dynamic/ambient visual treatment and state-driven motion. The supplied V3 technical specification remains the architectural baseline: UI observes centralized playback state and local media truth comes from MediaStore.

## Deliberately deferred
- Room-backed favorites, history and playlists.
- Full podcast discovery/subscriptions/downloads.
- Persistent custom theme settings.
- Tablet/foldable-specific layouts.
- Automated UI test suite.

These remain future work rather than simulated features.
