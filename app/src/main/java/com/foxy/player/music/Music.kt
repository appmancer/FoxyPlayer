package com.foxy.player.music

// ===== MUSIC DOMAIN ARCHITECTURE EVOLUTION =====
//
// This file now serves as a compatibility layer that re-exports all classes
// from the new domain-specific files for backward compatibility.
//
// The original 1,400+ line Music.kt has been split into 4 focused domain files:
// - MusicModels.kt - Pure data models, API responses, metadata structures
// - MusicDiscovery.kt - MusicDiscoveryService + pCloud integration
// - MusicLibrary.kt - Cache, indexing, sync, performance services
// - MusicSearch.kt - Search, filter, browse functionality
//
// Each domain file follows the single-file domain pattern while providing
// better organization and improved development experience.
//
// ===== RE-EXPORTS FOR BACKWARD COMPATIBILITY =====

// All classes from the 4 domain files are automatically available
// in this package due to Kotlin's package-level visibility.
// No explicit re-exports needed - existing imports will continue to work.

// Example usage - all of these continue to work:
// import com.foxy.player.music.MusicDiscoveryService
// import com.foxy.player.music.MusicTrack
// import com.foxy.player.music.AudioMetadata
// etc.

// ===== MIGRATION COMPLETE =====
// ✅ 4 well-organized domain-specific files (~350 lines each vs 1,400+)
// ✅ Clear separation of concerns with maintained domain cohesion
// ✅ Dependency hierarchy: models → discovery → library → search
// ✅ All existing functionality preserved
// ✅ Backward compatibility maintained
