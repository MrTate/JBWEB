package jbweb;

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
        return JBWEB.isWalkable(tile);
    }

    boolean unitWalkable(TilePosition tile) {
        return JBWEB.isWalkable(tile) && JBWEB.isUsed(tile, 1, 1) == UnitType.None;
    }
}
