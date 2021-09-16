# JBWEB
Java Broodwar Easy Builder or JBWEB for short, is a BWEM based building placement addon and Java port of [BWEB](https://github.com/Cmccrave/BWEB). The purpose of this addon is to provide easily accessible building management.

BWEB started as a decision to create a standard and simple method for bots to optimize their building space and placement.

Current head is forked from [this commit](https://github.com/Cmccrave/BWEB/commit/a65a57115ef4b6161a477b2e38382b8d0b5b6c50) (v1.14.2)

### Differences between BWEB and JBWEB

* Uses [JavaJPS](https://github.com/MrTate/JavaJPS) library instead of porting JPS from BWEB.

### What does JBWEB do?
JBWEB has 3 classes of information, Walls, Blocks and Stations.

Walls are made by permutation through a list of UnitTypes in as many combinations as possible and measuring a BFS path length to find a wall that is either wall tight or minimizes enemy movement. Walls can be created using any `JBWAPI::UnitType` and have parameters to pass in; what `JBWAPI::UnitType` you want to be wall tight against, a vector of `JBWAPI::UnitType` for defenses, or a reserve path can be created to ensure your units can leave the `BWEM::Area` that the Wall is being created in.

Blocks are used for all building types and are modifiable for any race and build. Blocks are useful for maximizing your space for production buildings without trapping units.

Stations are placed on every `BWEM::Base` and include defense positions that provide coverage for all your workers.

### Why use JBWEB?
Building placement is a very important aspect of Broodwar. Decisions such as hiding tech, walling a choke or finding more optimal use of your space are possible using BWEB. Most Broodwar bots suffer from many issues stemming from building placement, such as; timeouts, building where it's not safe, trapping units, and lack of fast expand options due to poor wall placement.

### How do I install BWEB?
**Maven**

Add JitPack as a repository:
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Add JBWEB to your dependencies in `<dependencies></dependencies>`:
```
<dependency>
    <groupId>com.github.MrTate</groupId>
    <artifactId>JBWEB</artifactId>
    <version>v2.0.0</version>
</dependency>
```

**Gradle**

Add JitPack as a repository:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add JBWEB as a dependency:
```
dependencies {
        implementation 'com.github.MrTate:JBWEB:v2.0.0'
}
```
**Downloaded Copy**

1) Clone the repository or download a copy.
3) In your source code, create a JBWEB folder and store the files in there.
4) In Visual Studio, add the files to your project and edit the properties of your projects include directory to include BWEBs folder.

### How do I use JBWEB?

In any file in which you need to access BWEB, add the following include:
`import jbweb.*`

JBWEB is accessed through the JBWEB package. Map, Stations, Walls, Blocks and PathFinding is all accessed through their respective classes inside the JBWEB package.

- `Map` contains useful functions such as quick ground distance calculations, natural and main choke identifying and tracking of used tiles for buildings.
- `Stations` contains placements for `BWEM::Base`s, tracking the defense count and placements around them.
- `Walls` contains the ability to create walls at any `BWEM::Chokepoint` and `BWEM::Area`. These walls currently store the location of the `JBWAPI::UnitType::TileSize` of each segment of the created wall, defense placements and an optional opening called a door.
- `Blocks` contains placements for `JBWAPI::UnitType`s for a fairly optimized approach to placing production buildings. Blocks are placed with varying sizes and shapes to try and fit as many buildings into each `BWEM::Area` as possible.
- `PathFinding` provides the ability to take advantage of BWEBs blazing fast JPS to create paths.

JBWEB needs to be initialized after BWEM is initialized. I would suggest including the following functions in your code:

```
game = bwClient.getGame();
bwem = new BWEM(game);
bwem.initialize();
bwem.getMap().assignStartingLocationsToSuitableBases();

JBWEB.onStart(game, bwem);
Blocks.findBlocks();
```

All other JBWEB functions have full comments describing their use!
