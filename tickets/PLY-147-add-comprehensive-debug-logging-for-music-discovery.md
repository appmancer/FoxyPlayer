# PLY-147: Add Comprehensive Debug Logging for Music Discovery

## Overview
Add comprehensive debug logging throughout the music discovery pipeline to enable better troubleshooting and monitoring of the application's music discovery functionality.

## Requirements
1. Add debug logging to the music discovery service API calls
2. Add debug logging to database operations in the music domain
3. Add debug logging to UI data flow and binding
4. Ensure logging can be enabled/disabled for production
5. Include timing information for performance monitoring
6. Add error context logging for better debugging

## Implementation Areas
- **PCloudDiscovery.kt**: API request/response logging
- **Albums.kt**: Database operation logging  
- **HeuristicDiscovery.kt**: UI data flow logging
- **Test Coverage**: Comprehensive test suite for logging functionality

## Acceptance Criteria
- All major music discovery operations have debug logging
- Logging includes timing and performance metrics
- Error cases are properly logged with context
- Logging can be controlled for production environments
- Test suite validates logging functionality