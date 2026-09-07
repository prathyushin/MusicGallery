# Changelog

All notable Music Gallery changes are documented here.

## [5.1.0] — Flow Design System

### Design
- Unified the V5 visual language around Apple-inspired floating spatial navigation.
- Kept the deep dark / neon energy associated with modern music interfaces while maintaining MusicGallery's own identity.
- Formalized glassmorphism as a surface treatment for floating navigation and playback controls.
- Formalized minimalism for navigation, library management and search.
- Formalized controlled maximalism for artwork-led experiences and future Wrapped-style storytelling.
- Reserved selective brutalist typography and composition for experimental moments rather than the entire product.
- Preserved the ambient aura, immersive artwork treatment and Cover Flow-style Now Playing experience already present in V5.

### Playback & Platform
- Updated AndroidX Media3 from 1.9.0 to stable 1.11.0.
- Kept the existing local-library and playback architecture intact.
- Kept minimum Android version at API 28 and target SDK at 37.
- Version code increased to 52.

### Release
- Release name: **5.1.0**.
- Release branch: `v5.1.0-release`.
- CI release pipeline is intended to build and publish the installable debug APK for validation.

## [5.0.0-alpha02] — Glassmorphism & Aura Precision

### Added
- Glassmorphism & Aura UI hardening.
- Real-library ViewModel integration.
- Frosted navigation treatment.
- System inset handling.
- Live Cover Flow transforms.
- Ambient artwork blur.

## [3.1.0-alpha01] — The Listening Update

### Added
- Visible release version badge in the Home experience.
- Dedicated "What's new" card for each user-visible release direction.
- Centralized `AppVersion` object for user-facing release identity.
- Versioning policy for future Music Gallery updates.
- Refined copy and hierarchy across Home, Library, Podcasts and Search.

## Versioning rule

Every distributable update gets a new version code. User-visible changes should also increment the version name and be recorded in this file.
