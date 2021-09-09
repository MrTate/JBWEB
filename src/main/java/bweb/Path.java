package bweb;

import bwapi.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static bweb.Pathfinding.maxCacheSize;
import static bweb.Pathfinding.unitPathCache;

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

    void createUnitPath(Position s, Position t, Wall wall) {
        target = new TilePosition(t);
        source = new TilePosition(s);

        // If this path does not exist in cache, remove last reference and erase reference
        Pair<TilePosition, TilePosition> pathPoints = new Pair<>(source, target);
        if (unitPathCache.indexList.get(pathPoints) == null) {
            if (unitPathCache.pathCache.size() == maxCacheSize) {
                Path last = unitPathCache.pathCache.get(unitPathCache.pathCache.size()-1);
                unitPathCache.pathCache.remove(unitPathCache.pathCache.size()-1);
                unitPathCache.indexList.remove(new Pair<>(last.getSource(), last.getTarget()));
            }
        }

        // If it does exist, set this path as cached version, update reference and push cached path to the front
        else {
            Path oldPath = unitPathCache.indexList.get(pathPoints).get(unitPathCache.pathCacheIndex);
            dist = oldPath.getDistance();
            tiles = oldPath.getTiles();
            reachable = oldPath.isReachable();

            unitPathCache.pathCache.remove(unitPathCache.indexList.get(pathPoints).get(unitPathCache.pathCacheIndex));
            List<Path> tmpCache = new ArrayList<>();
            tmpCache.add(this);
            tmpCache.addAll(unitPathCache.pathCache);
            unitPathCache.pathCache = tmpCache;
            unitPathCache.pathCacheIndex = 0;
            return;
        }

        // If not reachable based on previous paths to this area
        if (target.isValid(Map.game) && Map.mapBWEM.getMap().getArea(target) != null && wall.wallWalkable(new TilePosition(source.x, source.y))) {
            int checkReachable = unitPathCache.notReachableThisFrame.get(Map.mapBWEM.getMap().getArea(target));
            if (checkReachable >= Map.game.getFrameCount() && Map.game.getFrameCount() > 0) {
                reachable = false;
                dist = Double.MAX_VALUE;
                return;
            }
        }

        // If we found a path, store what was found
        List<TilePosition> newJPSPath = JSP.findPath(newJPSPath, wall.wallWalkable(t.toTilePosition()), source.x, source.y, target.x, target.y);
        if (!newJPSPath.isEmpty()) {
            Position current = s;
            for (TilePosition tile : newJPSPath) {
                dist += new Position(tile).getDistance(current);
                current = new Position(tile);
                tiles.add(tile);
            }
            reachable = true;

            // Update cache
            List<Path> tmpCache = new ArrayList<>();
            tmpCache.add(this);
            tmpCache.addAll(unitPathCache.pathCache);
            unitPathCache.pathCache = tmpCache;
            unitPathCache.pathCacheIndex = 0;
        }

        // If not found, set destination area as unreachable for this frame
        else if (target.isValid(Map.game) && Map.mapBWEM.getMap().getArea(target) != null) {
            dist = Double.MAX_VALUE;
            unitPathCache.notReachableThisFrame.put(Map.mapBWEM.getMap().getArea(target), Map.game.getFrameCount());
            reachable = false;
        }
    }

    // P3 = function <bool(const TilePosition&)> isWalkable
    void bfsPath(Position s, Position t, Wall wall) {
        TilePosition source = new TilePosition(s);
        TilePosition target = new TilePosition(t);
        List<TilePosition> direction = new ArrayList<>();
        direction.add(new TilePosition(0, 1));
        direction.add(new TilePosition(1, 0));
        direction.add(new TilePosition(-1, 0));
        direction.add(new TilePosition(0, -1));

        if (source.equals(target)
                || source.equals(new TilePosition(0, 0))
                || target.equals(new TilePosition(0, 0)))
            return;

        TilePosition[][] parentGrid = new TilePosition[256][256];
        Queue<TilePosition> nodeQueue = new LinkedList<>();
        nodeQueue.add(source);
        parentGrid[source.x][source.y] = source;

        // While not empty, pop off top the closest TilePosition to target
        while (!nodeQueue.isEmpty()) {
            TilePosition tile = nodeQueue.peek();
            nodeQueue.remove();

            for (TilePosition d : direction) {
                TilePosition next = new TilePosition(tile.x + d.x, tile.y + d.y);

                if (next.isValid(Map.game)) {
                    // If next has a parent or is a collision, continue
                    if (!parentGrid[next.x][next.y].equals(new TilePosition(0, 0)) || !wall.wallWalkable(next))
                        continue;

                    // Check diagonal collisions where necessary
                    if ((d.x == 1 || d.x == -1) && (d.y == 1 || d.y == -1) && (!wall.wallWalkable(new TilePosition(tile.x + d.x, tile.y))
                            || !wall.wallWalkable(new TilePosition(tile.x, tile.y + d.y))))
                        continue;

                    // Set parent here
                    parentGrid[next.x][next.y] = tile;

                    // If at target, return path
                    if (next.equals(target)) {
                        createPath(s.toTilePosition(), t.toTilePosition(), parentGrid);
                        return;
                    }

                    nodeQueue.add(next);
                }
            }
        }
        reachable = false;
        dist = Double.MAX_VALUE;
    }
}
