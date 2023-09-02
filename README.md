# gdx-recast
gdx-recast is a library for libGDX that provides a bridge between libGDX and the Recast/Detour pathfinding libraries.
This is possible by using [recast4j](https://github.com/ppiastucki/recast4j), a Java port of Recast/Detour.

GWT is supported by using a forked version of recast4j, [gwt-recast](https://github.com/antzGames/gwt-recast4j) which has been modified to work with GWT.

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
NavMeshIO.save(navMeshData.getNavMesh(), Gdx.files.local("navmesh.nav"));

// Loading
NavMesh navMesh = NavMeshIO.load(Gdx.files.local("navmesh.nav"));
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