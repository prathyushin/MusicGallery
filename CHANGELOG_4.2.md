# MusicGallery 4.2.0-alpha01 — The Real Library Update

## Goal
V4.2 is a rebuild after the V4.1 prototype feedback. It moves the experience away from placeholder content and toward a real daily-use local music player.

## Implemented
- Real MediaStore music scanning.
- Android 12 and Android 13+ audio permission flow retained.
- Real album-art content URIs from MediaStore album IDs.
- Artwork-first song rows and album cards using Coil.
- Real local Songs / Albums / Artists library modes.
- Local search across title, artist and album.
- Media3 queue playback instead of single-track-only playback.
- Next / previous / shuffle / repeat controls exposed through the playback client.
- Persistent MediaSessionService playback foundation retained for background and lock-screen integration.
- Artwork-first mini player and full Now Playing surface.
- Cleaner Home hierarchy based on the actual device library.
- Podcast destination retained as a first-class area, with discovery/subscription/download UX clearly marked for the next implementation pass rather than showing fake episodes.
- Installable debug APK workflow with GitHub Release attachment.

## Deliberately not faked
V4.2 does not invent songs, albums, podcast episodes, listening statistics or artwork when the device has no data. Empty states explain what is required instead.

## Review checklist
1. Does the first screen feel useful immediately after permission?
2. Is album artwork loading correctly for your files?
3. Is the library fast enough with a large collection?
4. Does tapping a song start playback reliably?
5. Do next/previous controls behave as expected?
6. Does the mini player open the full player naturally?
7. Does the full player feel like MusicGallery rather than a generic Material demo?
8. Is the Songs / Albums / Artists organization clear?
9. Is local search useful?
10. What should change before 4.2 beta?

## Next likely pass
- Live playback state observation from MediaController.
- Real progress/seek position in Now Playing.
- Persistent favorites, history and playlists with Room.
- Dynamic artwork-derived ambient theming.
- Real podcast RSS discovery, subscriptions, progress and downloads.
- Functional settings and appearance controls.
- Better adaptive tablet/foldable layouts.
- Automated UI tests and release validation.
