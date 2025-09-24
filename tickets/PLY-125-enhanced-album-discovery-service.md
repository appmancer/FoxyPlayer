# Enhanced Album Discovery Service

**SCOPE CLARIFICATION NEEDED**
This ticket is ambiguous about which service layer to enhance. We have identified two distinct services:

1. **Music Discovery Service** (Backend Layer): 
   - Location: `PCloudDiscovery.kt`
   - Function: Scans pCloud files, extracts metadata, writes to database
   - Pure backend operation, no UI connection

2. **Music Data Service** (UI Data Layer):
   - Location: `Albums.kt` (AlbumRepository) 
   - Function: Reads from database, provides UI-ready data
   - Connects database to UI components

**Awaiting clarification from Lieutenant Green on which service to enhance.**

**Original Objective**
Extend MusicDiscoveryService to include album-specific discovery methods that group songs by album metadata and provide album-centric data retrieval, leveraging the enhanced Room database architecture.

**Context**
Build on the established database foundation:

* Enhanced Room database v2 with album entities (PLY-124)
* AlbumEntity, AlbumTrackEntity junction tables (PLY-129, PLY-130)
* Repository pattern with proper data layer separation
* Albums-first navigation architecture research (PLY-127)

**Implementation Approach**

* Extend existing MusicDiscoveryService using established Room repository patterns
* Leverage AlbumRepository and enhanced database entities
* Use proper Android architecture layers (Repository → ViewModel → UI)
* Follow TDD methodology with comprehensive test coverage
* Integrate with existing Room database and migration infrastructure

**Task**
Implement album discovery functionality using the enhanced database architecture:

* Album grouping logic using AlbumEntity and AlbumTrackEntity
* Repository layer integration with Room database
* Service layer methods for album-centric data retrieval
* Error handling and validation using established patterns
* Integration testing with existing database layer

**Definition of Done**

* Album discovery methods implemented using Room entities
* Repository pattern integration validated
* Tests written covering album grouping and discovery
* Integration with enhanced database architecture confirmed
* All existing patterns and conventions followed
* Code review completed and approved
