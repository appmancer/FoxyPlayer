# Temporary File Cleanup Audit and Fix

**Objective**
Audit all temporary files created by .tools/commands processes and ensure proper cleanup in pr-cleanup script.

**Context**
Captain Scarlet has identified inconsistent temporary file handling across various command scripts in .tools/commands. Need comprehensive audit and cleanup implementation to prevent file system pollution.

**Requirements**

* Audit all scripts in .tools/commands for temporary file creation patterns
* Identify files that are not being cleaned up by pr-cleanup
* Document temporary file lifecycle for each command
* Update pr-cleanup script to handle all temporary files properly
* Add safety checks to prevent deletion of important files
* Test cleanup process across all command workflows

**Success Criteria**

* Complete inventory of all temporary files created by commands
* pr-cleanup script properly removes all temporary files
* No temporary file pollution after command execution
* Documentation of temporary file patterns for future development
* Tested cleanup process across all command scenarios following test-driven development approach.

**Context**
Based on design specifications, implement the functionality with focus on correctness and integration with existing systems.

**Domain Implementation Rule**
⚠️ **CRITICAL**: Implement ALL domain-related code in a SINGLE FILE. Do not follow standard Android separation patterns.

* If this is authentication-related → Put everything in `auth.kt`
* If this is music discovery-related → Put everything in `music.kt`
* If this is playback-related → Put everything in `playback.kt`
* Include: Models, Repository, UseCase, Screen, ViewModel - ALL in one file
* **Ignore Kotlin conventions** that separate layers into different files
* **Context window optimization**: AI needs all domain code visible simultaneously

**Task**
Audit all temporary files created by .tools/commands processes and ensure proper cleanup in pr-cleanup script.

**Context**
Captain Scarlet has identified inconsistent temporary file handling across various command scripts in .tools/commands. Need comprehensive audit and cleanup implementation to prevent file system pollution.

**Requirements**

* Audit all scripts in .tools/commands for temporary file creation patterns
* Identify files that are not being cleaned up by pr-cleanup
* Document temporary file lifecycle for each command
* Update pr-cleanup script to handle all temporary files properly
* Add safety checks to prevent deletion of important files
* Test cleanup process across all command workflows

**Success Criteria**

* Complete inventory of all temporary files created by commands
* pr-cleanup script properly removes all temporary files
* No temporary file pollution after command execution
* Documentation of temporary file patterns for future development
* Tested cleanup process across all command scenarios using TDD methodology:
* Write tests covering functional requirements
* Implement core functionality following established patterns
* Add error handling for edge cases
* Add logging and monitoring instrumentation
* Validate integration with existing systems

**Implementation Approach**

* Follow test-driven development with comprehensive test coverage
* Use existing patterns and conventions
* Ensure clean integration with existing architecture
* **Keep all domain code in single file for AI context optimization**

**Definition of Done**

* Tests written and passing for all functionality
* Core functionality implemented following established patterns
* Error handling implemented for identified failure modes
* Integration with existing systems validated
* **ALL domain code consolidated in single file**
* Code review completed and approved

**Labels**
backend, implementation
