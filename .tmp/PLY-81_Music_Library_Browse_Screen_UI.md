# Music Library Hub - Material Design 3

**Objective**
Create the main music library browsing interface using Material Design 3 components and native Android patterns.

**Material Design 3 Requirements**

**Navigation Structure:**

* **Bottom Navigation Bar**: 5 tabs (Home, Songs, Artists, Folders, Settings)
* **Top App Bar**: Title + search icon + overflow menu
* **FAB**: Quick access to Now Playing screen

**Main Hub Layout:**

* **Material3 Cards** for each music category:
  * Songs card (track count, last updated)
  * Albums card (album count, metadata)
  * Artists card (artist count, sorting info)
  * Folders card (folder structure)
  * Favorites/Recent cards
* **Card styling**: Rounded corners, elevation, Material You dynamic colors

**UI Components:**

* **Typography**: Material3 text styles (Headline, Body, Caption)
* **Colors**: Material You adaptive color scheme
* **Spacing**: Material3 layout grid (16dp, 24dp spacing)
* **Icons**: Material Design icons throughout

**Navigation Behavior:**

* **Hub → Category drill-down**: Cards navigate to respective list views
* **FAB → Now Playing**: Expandable bottom sheet
* **Bottom nav persistence**: Always visible navigation

**Integration Points:**

* Connect to **MusicLibrary.kt** backend services
* Display real metadata counts from **MusicDiscovery.kt**
* Show pCloud sync status from **Authentication** system

**Success Criteria**

* Native Material3 visual design
* Smooth navigation between sections
* Real data integration (no hardcoded counts)
* Bottom navigation working
* FAB launches Now Playing
* Cards navigate to list views
* Material You dynamic theming applied

**Implementation Approach**

* Use Jetpack Compose with Material3 components
* Follow Android navigation patterns
* Integrate with existing backend services
* Apply Material You theming system

**Definition of Done**

* Material3 Hub screen implemented
* Bottom navigation functional
* Cards display real music library data
* FAB integration ready
* Navigation to list views working
* Material You theming applied
* All backend services connected
