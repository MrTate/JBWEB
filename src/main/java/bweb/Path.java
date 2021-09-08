package bweb;

import bwapi.*;

import java.util.ArrayList;
import java.util.List;

public class Path {
    List<TilePosition> tiles;
    double dist;
    boolean reachable;
    TilePosition source, target;

    Path() {
        tiles = new ArrayList<>();
        dist = 0.0;
        reachable = false;
        source = TilePosition.Invalid;
        target = TilePosition.Invalid;
    }

    List<TilePosition> getTiles() {
        return tiles;
    }

    TilePosition getSource() {
        return source;
    }

    TilePosition getTarget() {
        return target;
    }

    double getDistance() {
        return dist;
    }

    boolean isReachable() {
        return reachable;
    }


//        const auto isBfsPathWalkable = [&](const int x, const int y) {
//            const TilePosition tile(x, y);
//        if (x > width || y > height || x < 0 || y < 0)
//            return false;
//        if (tile == source || tile == target)
//            return true;
//        if (Map::isWalkable(tile) && Map::isUsed(tile) == UnitTypes::None)
//        return true;
//        return false;
//    };


//        const auto isJspPathWalkable = [&](const int x, const int y) {
//            const TilePosition tile(x, y);
//        if (x > width || y > height || x < 0 || y < 0)
//            return false;
//        if (tile == source || tile == target)
//            return true;
//        if (passedWalkable(tile))
//            return true;
//        return false;
//    };


    // This function requires that parentGrid has been filled in for a path from source to target
    void createPath(TilePosition s, TilePosition t, TilePosition[][] parentGrid) {
        tiles.add(target);
        reachable = true;
        TilePosition check = parentGrid[target.x][target.y];
        dist += new Position(target).getDistance(new Position(check));

        do {
            tiles.add(check);
            TilePosition prev = check;
            check = parentGrid[check.x][check.y];
            dist += new Position(prev).getDistance(new Position(check));
        } while (check != source);

        // HACK: Try to make it more accurate to positions instead of tiles
        Position correctionSource = new Position(tiles.get(tiles.size()-2)); // Second to last tile
        Position correctionTarget = new Position(tiles.get(1)); // Second tile
        dist += s.getDistance(correctionSource.toTilePosition());
        dist += t.getDistance(correctionTarget.toTilePosition());
        dist -= 64.0;
    }

//    void createUnitPath(Position s, Position t) {
//        target = TilePosition(t);
//        source = TilePosition(s);
//
//        // If this path does not exist in cache, remove last reference and erase reference
//        auto pathPoints = make_pair(source, target);
//        if (unitPathCache.iteratorList.find(pathPoints) == unitPathCache.iteratorList.end()) {
//            if (unitPathCache.pathCache.size() == maxCacheSize) {
//                auto last = unitPathCache.pathCache.back();
//                unitPathCache.pathCache.pop_back();
//                unitPathCache.iteratorList.erase(make_pair(last.getSource(), last.getTarget()));
//            }
//        }
//
//        // If it does exist, set this path as cached version, update reference and push cached path to the front
//        else {
//            auto &oldPath = unitPathCache.iteratorList[pathPoints];
//            dist = oldPath->getDistance();
//            tiles = oldPath->getTiles();
//            reachable = oldPath->isReachable();
//
//            unitPathCache.pathCache.erase(unitPathCache.iteratorList[pathPoints]);
//            unitPathCache.pathCache.push_front(*this);
//            unitPathCache.iteratorList[pathPoints] = unitPathCache.pathCache.begin();
//            return;
//        }
//
//        vector<TilePosition> newJPSPath;
//        const auto width = Broodwar->mapWidth();
//        const auto height = Broodwar->mapHeight();
//
//        // If not reachable based on previous paths to this area
//        if (target.isValid() && Map::mapBWEM.GetArea(target) && isWalkable(source.x, source.y)) {
//        auto checkReachable = unitPathCache.notReachableThisFrame[Map::mapBWEM.GetArea(target)];
//        if (checkReachable >= Broodwar->getFrameCount() && Broodwar->getFrameCount() > 0) {
//            reachable = false;
//            dist = DBL_MAX;
//            return;
//        }
//    }
//
//        // If we found a path, store what was found
//        if (JPS::findPath(newJPSPath, isWalkable, source.x, source.y, target.x, target.y)) {
//        Position current = s;
//        for (auto &t : newJPSPath) {
//            dist += Position(t).getDistance(current);
//            current = Position(t);
//            tiles.push_back(t);
//        }
//        reachable = true;
//
//        // Update cache
//        unitPathCache.pathCache.push_front(*this);
//        unitPathCache.iteratorList[pathPoints] = unitPathCache.pathCache.begin();
//    }
//
//        // If not found, set destination area as unreachable for this frame
//        else if (target.isValid() && Map::mapBWEM.GetArea(target)) {
//        dist = DBL_MAX;
//        unitPathCache.notReachableThisFrame[Map::mapBWEM.GetArea(target)] = Broodwar->getFrameCount();
//        reachable = false;
//    }
//    }
//
//    void bfsPath(const Position s, const Position t, function <bool(const TilePosition&)> isWalkable, bool diagonal) {
//        TilePosition source = TilePosition(s);
//        TilePosition target = TilePosition(t);
//        auto maxDist = source.getDistance(target);
//        const auto width = Broodwar->mapWidth();
//        const auto height = Broodwar->mapHeight();
//        vector<TilePosition> direction{ { 0, 1 },{ 1, 0 },{ -1, 0 },{ 0, -1 } };
//
//        if (source == target
//                || source == TilePosition(0, 0)
//                || target == TilePosition(0, 0))
//            return;
//
//        TilePosition parentGrid[256][256];
//        queue<TilePosition> nodeQueue;
//        nodeQueue.emplace(source);
//        parentGrid[source.x][source.y] = source;
//
//        // While not empty, pop off top the closest TilePosition to target
//        while (!nodeQueue.empty()) {
//            auto const tile = nodeQueue.front();
//            nodeQueue.pop();
//
//            for (auto const &d : direction) {
//                auto const next = tile + d;
//
//                if (next.isValid()) {
//                    // If next has parent or is a collision, continue
//                    if (parentGrid[next.x][next.y] != TilePosition(0, 0) || !isWalkable(next))
//                        continue;
//
//                    // Check diagonal collisions where necessary
//                    if ((d.x == 1 || d.x == -1) && (d.y == 1 || d.y == -1) && (!isWalkable(tile + TilePosition(d.x, 0)) || !isWalkable(tile + TilePosition(0, d.y))))
//                        continue;
//
//                    // Set parent here
//                    parentGrid[next.x][next.y] = tile;
//
//                    // If at target, return path
//                    if (next == target) {
//                        createPath();
//                        return;
//                    }
//
//                    nodeQueue.emplace(next);
//                }
//            }
//        }
//        reachable = false;
//        dist = DBL_MAX;
//    }
//
//    void jpsPath(const Position s, const Position t, function <bool(const TilePosition&)> passedWalkable, bool diagonal) {
//        target = TilePosition(t);
//        source = TilePosition(s);
//
//        // If this path does not exist in cache, remove last reference and erase reference
//        auto &pathPoints = make_pair(source, target);
//        auto &thisCached = customPathCache[&passedWalkable];
//
//        if (thisCached.iteratorList.find(pathPoints) == thisCached.iteratorList.end()) {
//            if (thisCached.pathCache.size() == maxCacheSize) {
//                auto last = thisCached.pathCache.back();
//                thisCached.pathCache.pop_back();
//                thisCached.iteratorList.erase(make_pair(last.getSource(), last.getTarget()));
//            }
//        }
//
//        // If it does exist, set this path as cached version, update reference and push cached path to the front
//        else {
//            auto &oldPath = thisCached.iteratorList[pathPoints];
//            dist = oldPath->getDistance();
//            tiles = oldPath->getTiles();
//            reachable = oldPath->isReachable();
//
//            thisCached.pathCache.erase(thisCached.iteratorList[pathPoints]);
//            thisCached.pathCache.push_front(*this);
//            thisCached.iteratorList[pathPoints] = thisCached.pathCache.begin();
//            return;
//        }
//
//        List<TilePosition> newJPSPath = new ArrayList<>();
//        int width = Broodwar->mapWidth();
//        int height = Broodwar->mapHeight();
//
//        // If not reachable based on previous paths to this area
//        if (target.isValid() && Map::mapBWEM.GetArea(target) && isWalkable(source.x, source.y)) {
//        auto checkReachable = thisCached.notReachableThisFrame[Map::mapBWEM.GetArea(target)];
//        if (checkReachable >= Broodwar->getFrameCount() && Broodwar->getFrameCount() > 0) {
//            reachable = false;
//            dist = DBL_MAX;
//            return;
//        }
//    }
//
//        // If we found a path, store what was found
//        if (JPS::findPath(newJPSPath, isWalkable, source.x, source.y, target.x, target.y)) {
//        Position current = s;
//        for (auto &t : newJPSPath) {
//            dist += Position(t).getDistance(current);
//            current = Position(t);
//            tiles.push_back(t);
//        }
//        reachable = true;
//
//        // Update cache
//        thisCached.pathCache.push_front(*this);
//        thisCached.iteratorList[pathPoints] = thisCached.pathCache.begin();
//    }
//
//        // If not found, set destination area as unreachable for this frame
//        else if (target.isValid() && Map::mapBWEM.GetArea(target)) {
//        dist = DBL_MAX;
//        thisCached.notReachableThisFrame[Map::mapBWEM.GetArea(target)] = Broodwar->getFrameCount();
//        reachable = false;
//    }
//    }
}
