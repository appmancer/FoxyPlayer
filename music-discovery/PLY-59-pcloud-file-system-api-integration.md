# pCloud File System API Integration

**Ticket ID**: PLY-59
**Assigned by**: Lieutenant Green  
**Status**: In Progress
**Type**: backend implementation

## Objective
Implement pCloud API integration for browsing and listing music files in user's cloud storage.

## Context
Foundation for Music Discovery Engine - need to connect to pCloud API to browse user's music files across folders and build file inventory.

## Requirements

* Integrate with existing authenticated API client from authentication domain
* Implement pCloud listfolder API for recursive directory traversal
* Filter results to audio file types (MP3, FLAC, WAV, etc.)
* Handle pagination for large directories
* Implement proper error handling for network and API errors
* Cache directory listings for performance

## Success Criteria

* Successfully browse pCloud folders recursively
* Filter and identify audio files accurately
* Handle large directories with pagination
* Integration with existing authentication system
* Comprehensive error handling and recovery following test-driven development approach.

## Domain Implementation Rule
⚠️ **CRITICAL**: Implement ALL domain-related code in a SINGLE FILE. Do not follow standard Android separation patterns.

* Since this is music discovery-related → Put everything in `music.kt`
* Include: Models, Repository, UseCase, Screen, ViewModel - ALL in one file
* **Ignore Kotlin conventions** that separate layers into different files
* **Context window optimization**: AI needs all domain code visible simultaneously

## Implementation Approach

* Follow test-driven development with comprehensive test coverage
* Use existing patterns and conventions
* Ensure clean integration with existing architecture
* **Keep all domain code in single file for AI context optimization**

## Definition of Done

* Tests written and passing for all functionality
* Core functionality implemented following established patterns
* Error handling implemented for identified failure modes
* Integration with existing systems validated
* **ALL domain code consolidated in single file**
* Code review completed and approved

## Labels
backend, implementation