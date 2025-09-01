# Login API Client Setup

**Objective**
Create HTTP client for pCloud authentication endpoints with SSL support, server detection ([api.pcloud.com](http://api.pcloud.com) vs [eapi.pcloud.com](http://eapi.pcloud.com)) following test-driven development approach.

**Context**
Based on design specifications, implement the functionality with focus on correctness and integration with existing systems.

**Domain Implementation Rule**
⚠️ **CRITICAL**: Implement ALL domain-related code in a SINGLE FILE. Do not follow standard Android separation patterns.

* If this is authentication-related → Put everything in `auth.kt`
* Include: Models, Repository, UseCase, Screen, ViewModel - ALL in one file
* **Ignore Kotlin conventions** that separate layers into different files
* **Context window optimization**: AI needs all domain code visible simultaneously

**pCloud Server Auto-Detection**
🌍 **IMPLEMENTATION DETAILS**:

* **Primary Server**: `api.pcloud.com` (US) - try first
* **Fallback Server**: `eapi.pcloud.com` (EU) - retry on auth failure
* **Detection Logic**: HTTP client should automatically switch servers on 401/403 responses
* **State Management**: Remember successful server endpoint for session
* **Error Handling**: Distinguish between auth failure vs server mismatch

**Task**
Create HTTP client for pCloud authentication endpoints with SSL support using TDD methodology:

* Write tests covering functional requirements including server auto-detection
* Implement HTTP client with automatic US → EU server switching
* Add error handling for edge cases including network and auth failures
* Add logging and monitoring instrumentation
* Validate integration with existing systems

**Implementation Approach**

* Follow test-driven development with comprehensive test coverage
* Use existing patterns and conventions
* Ensure clean integration with existing architecture
* **Keep all domain code in single file for AI context optimization**

**Definition of Done**

* Tests written and passing for all functionality including server detection
* HTTP client implemented with automatic server switching
* SSL support working for both US and EU endpoints
* Error handling implemented for identified failure modes
* Integration with existing systems validated
* **ALL domain code consolidated in single auth.kt file**
* Code review completed and approved

**Labels**
backend, implementation
