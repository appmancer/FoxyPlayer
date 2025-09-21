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
