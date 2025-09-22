# Heuristic File Path Metadata Extraction

**Objective**
Implement heuristic metadata extraction from file paths and folder structures, integrating with the enhanced Room database architecture for proper album/track data management.

**Context**
Build on the established database foundation:

* Enhanced Room database v2 with album entities (PLY-124)
* AlbumEntity, AlbumTrackEntity junction tables, TrackEntity (PLY-129, PLY-130)
* Repository pattern with proper data layer separation
* Existing HeuristicDiscovery patterns for file path analysis

**Implementation Approach**

* Extend existing HeuristicDiscovery patterns for file path metadata extraction
* Extract artist/album/title from folder structure and filenames
* Integrate with Room entities for proper data persistence
* Use MetadataRepository pattern for database operations
* Follow established Android architecture layers (Repository → Service → Database)

**Task**
Implement heuristic metadata extraction using the enhanced database architecture:

* File path parsing logic for artist/album/title extraction
* Integration with Room entities (TrackEntity, AlbumEntity)
* Repository layer methods for heuristic metadata persistence
* Album-track relationship management using junction tables
* Confidence scoring for heuristic vs direct metadata
* Error handling and validation using established patterns

**Definition of Done**

* Heuristic extraction implemented using existing HeuristicDiscovery patterns
* Integration with Room entities and repository pattern validated
* Metadata properly persisted using AlbumEntity and TrackEntity relationships
* Confidence scoring system for metadata quality assessment
* Tests written covering file path extraction and database integration
* All existing architectural patterns and conventions followed
* Code review completed and approved
