# Changelog

All notable Music Gallery changes are documented here.

## [4.4.0] — Fluid Listening

### Added
- Rebuilt Home around real device music and artwork-first presentation.
- Featured local track surface.
- Quick-pick and album carousels.
- Local category filtering.
- Persistent local favorites using SharedPreferences.
- Consistent editorial light/dark theme.
- Improved accessibility descriptions for artwork and playback controls.

### Playback
- Reliable Media3 playback from Android MediaStore content URIs.
- Pending playback command queue while MediaController connects.
- Queue index resolution by media ID to tolerate stale MediaStore entries.
- Play / pause / previous / next.
- Interactive seeking.
- Shuffle.
- Repeat off / all / one.
- Persistent mini-player.
- Full Now Playing surface with artwork ambience.

### UX
- Clear music-first hierarchy.
- Smoother mini-player and Now Playing transitions.
- Improved spacing, surfaces, touch targets and contrast.
- Honest empty states with no fabricated catalog content.

### Product policy
- Core music remains local-first.
- No fake songs, artists, albums, listening statistics, queue items, artwork or progress.
- Podcasts remain explicitly unpopulated until real feed/subscription functionality is ready.

## [4.3.0-alpha01] — Playback Reliability Rebuild

- Established the Media3 playback reliability pass.
- Added the first queued-command handling for the asynchronous MediaController connection.
- Introduced the artwork-first fluid listening direction.

## [4.2.x] — Real Library + Fluid UI Foundation

- Real Android MediaStore music library.
- Songs, albums and artists views.
- Real local metadata search.
- Media3 service playback foundation.
- Mini-player and Now Playing foundation.

## Versioning rule

Stable product releases use semantic versioning without alpha suffixes. Every distributable update gets a new version code, and user-visible changes are recorded in this file.

---

### Credits

**A HOXGOX Production**  
**Built by Pranav & Prathyusha**
