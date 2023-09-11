![image](https://github.com/JamesTKhan/gdx-recast/assets/10563814/4b6bfba4-6832-4a97-ad9f-51405d7d83c9)

# gdx-recast
gdx-recast is a library for libGDX that provides a bridge between libGDX and the Recast/Detour pathfinding libraries.
This is possible by using [recast4j](https://github.com/ppiastucki/recast4j), a Java port of Recast/Detour.

GWT is supported by using a forked version of recast4j, [gwt-recast](https://github.com/antzGames/gwt-recast4j) which has been modified to work with GWT.

NOTE: This library is still in development. API changes may occur at any time.

## Features
- NavMesh generation
- NavMesh saving / loading
- Pathfinding
- Detour Crowd (Experimental)
- GWT support
- Debug rendering for libGDX

## Usage

### Generating a NavMesh
```java
NavMeshGenSettings settings = NavMeshGenSettings.Builder.SettingsBuilder()
        .useTiles(true)
        .tileSizeX(128)
        .tileSizeZ(128)
        .build();

// NavMeshGenerator provides multiple constructors
NavMeshGenerator navMeshGenerator = new NavMeshGenerator(modelInstance);
NavMeshData data = navMeshGenerator.build(settings); // Contains NavMesh
```

### Saving / loading a generated NavMesh
```java
// Saving
NavMeshIO.save(navMeshData.getNavMesh(), Gdx.files.internal("navmesh.nav"));

// Loading
NavMesh navMesh = NavMeshIO.load(Gdx.files.internal("navmesh.nav"));
NavMeshData navMeshData = new NavMeshData(navMesh);
```

### Pathfinding
```java
private Pathfinder pathfinder;
...
pathfinder = new Pathfinder(navMeshData);
        
Array<float[]> paths = new Array<>(); // This will be populated with the path
pathfinder.getPath(new Vector3(-0.99f, 15.24f, 11.98f), new Vector3(17.63f,-2.37f,-21.86f), paths);
```

# Resources
[Recast Navigation Google Group](https://groups.google.com/g/recastnavigation)

[Recast4j](https://github.com/ppiastucki/recast4j)
