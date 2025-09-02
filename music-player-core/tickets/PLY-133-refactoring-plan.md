# PLY-133 MusicModels.kt Refactoring Plan

## Current State
- **File**: `app/src/main/java/com/foxy/player/music/MusicModels.kt`
- **Size**: 1606 lines
- **Domain Sections**: 15 distinct domain areas
- **Issue**: Multiple responsibilities mixed in single file

## Refactoring Strategy

### Phase 1: Core Entity Models
**File**: `MusicEntities.kt` (~150 lines)
- Room entities (TrackEntity, AlbumEntity)
- Basic data classes (AudioMetadata, AudioFile, FolderListing)
- Core domain models

### Phase 2: Database Layer
**File**: `MusicDatabase.kt` (~400 lines)
- Room database configuration
- DAO interfaces and implementations
- Database provider patterns
- SQL foundation classes

### Phase 3: Repository Layer  
**File**: `MusicRepository.kt` (~200 lines)
- Repository interfaces and implementations
- Repository result types
- Data access patterns

### Phase 4: pCloud Integration
**File**: `PCloudModels.kt` (~150 lines)
- pCloud API response models
- pCloud-specific data structures
- API interface definitions

### Phase 5: Sync Service
**File**: `MusicSyncService.kt` (~400 lines)
- Background synchronization logic
- Sync configuration and results
- Progress tracking models

### Phase 6: UI & Search Models
**File**: `MusicUIModels.kt` (~300 lines)
- Search and browse models
- UI state management
- Paginated data layer
- Error handling for UI

## Implementation Approach

1. **TDD-First**: Write tests for each new file before extraction
2. **Incremental**: Move one domain at a time
3. **Validation**: Ensure all tests pass after each extraction
4. **Import Updates**: Update all dependent files

## Success Criteria

- [ ] All 15 domain sections extracted to appropriate files
- [ ] No functionality changes (pure refactor)
- [ ] All existing tests continue to pass
- [ ] Import statements updated throughout codebase
- [ ] Each new file under 400 lines
- [ ] Clear domain separation maintained