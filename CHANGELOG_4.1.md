# MusicGallery 4.1.0-alpha01

## The Experience Update

MusicGallery 4.1 is a UX-first follow-up to V4. The goal is to fix the weak information hierarchy, button placement and navigation flow identified during review.

### Included in this alpha
- New floating bottom dock for Home, Library, Podcasts and Search.
- Cleaner Home hierarchy with Quick Picks and mood filters.
- Listening Insights area for listening time, top artist and favorites.
- Settings entry and organized settings groups for Appearance, Equalizer, Playback, Storage, Recommendations, Listening Habit, Notifications and About.
- Cleaner Library rows and reduced visual chrome.
- Podcast area positioned as a first-class destination.
- Search wording expanded toward music + podcast discovery.
- Updated version identity to 4.1.0-alpha01 / version code 41.
- Dedicated GitHub Actions pipeline that builds and verifies an installable APK and creates/uploads the 4.1 prerelease asset.

### Design reference carried forward
The V4 Now Playing design remains the target interaction model: one continuous mini-player/Now Playing surface, artwork-first hierarchy, dynamic accenting, 48dp+ touch targets, and a shared playback state. The provided Now Playing specification defines the expansion model and implementation direction.

### Next 4.1 work
1. Wire the new PlayerHost/Now Playing experience into the real MediaSessionService state.
2. Add real artwork rendering and artwork-derived dynamic accent extraction.
3. Persist listening statistics and generate recommendation mixes from actual listening history.
4. Implement functional Settings screens instead of navigation-only rows.
5. Add white, dark and user-customizable theme controls.
6. Add podcast subscriptions, episode progress and download management to the redesigned shell.
7. Build and verify the 4.1 APK before calling the release complete.
