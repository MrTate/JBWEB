package bweb;

import bwapi.*;

public class Pathfinding {
    static PathCache unitPathCache;

    static int maxCacheSize = 10000;

    static void clearCache() {
        unitPathCache.indexList.clear();
        unitPathCache.pathCacheIndex = 0;
        unitPathCache.pathCache.clear();
    }

    boolean terrainWalkable(TilePosition tile) {
        return Map.isWalkable(tile);
    }

    boolean unitWalkable(TilePosition tile) {
        return Map.isWalkable(tile) && Map.isUsed(tile, 1, 1) == UnitType.None;
    }
}
