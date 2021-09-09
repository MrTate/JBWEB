package bweb;

import bwapi.*;
import bwem.*;

public class Pathfinding {
    static PathCache unitPathCache;
//    map<function <bool(const TilePosition&)>*, PathCache> customPathCache;
//
    static int maxCacheSize = 10000;

    static void clearCache() {
        unitPathCache.iteratorList.clear();
        unitPathCache.pathCache.clear();
        customPathCache.clear();
    }

//    void clearCache(function <bool(const TilePosition&)> passedWalkable) {
//        customPathCache[&passedWalkable].iteratorList.clear();
//        customPathCache[&passedWalkable].pathCache.clear();
//    }

    boolean terrainWalkable(TilePosition tile) {
        return Map.isWalkable(tile);
    }

    boolean unitWalkable(TilePosition tile) {
        return Map.isWalkable(tile) && Map.isUsed(tile, 1, 1) == UnitType.None;
    }
}
