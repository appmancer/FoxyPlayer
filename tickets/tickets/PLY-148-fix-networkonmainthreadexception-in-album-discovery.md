# Fix NetworkOnMainThreadException in Album Discovery

**Objective**
Critical threading issue preventing album discovery functionality. NetworkOnMainThreadException occurs in HeuristicMusicDiscovery.listAllFilesRecursively() when making pCloud API calls on main UI thread. Implement proper coroutine threading with IO dispatcher for API calls and Main dispatcher for UI updates. Add loading states during async operations. File: HeuristicMusicDiscovery.kt:84-96. Priority: HIGH - blocks core music discovery.

**Implementation Notes**

* Follow test-driven development approach
* Keep all domain code in single file for AI context optimization
* Use existing codebase patterns and conventions

**Definition of Done**

* Tests written and passing
* Core functionality implemented
* Integration validated with existing systems
* Code review completed

---
## Implementation Completed
- **Ticket**: PLY-148
- **PR**: https://github.com/appmancer/FoxyPlayer/pull/64
- **Domain**: tickets
- **TDD Cycles**: 2 completed
- **Tests**: 175 passing
- **Files Changed**: 5
- **Merged**: 2025-09-21T10:34:25+01:00
- **Branch**: feature/PLY-148-fix-networkonmainthreadexception-in-album-discovery (deleted)

This ticket has been completed and deployed through the Centro development workflow.
The implementation has been merged to dev branch and deployed to staging environment.
