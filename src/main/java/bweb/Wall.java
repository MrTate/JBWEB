package bweb;

import bwapi.*;
import bwem.*;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class Wall {
    UnitType tightType;
    Position centroid;
    TilePosition opening, initialPathStart, initialPathEnd, pathStart, pathEnd, creationStart;
    TreeSet<TilePosition> defenses, smallTiles, mediumTiles, largeTiles;
    TreeSet<Position> notableLocations;
    List<UnitType> typeIterator;
    List<UnitType> rawBuildings, rawDefenses;
    List<Area> accessibleNeighbors;
    HashMap<TilePosition, UnitType> currentLayout, bestLayout;
    Area area;
    ChokePoint choke;
    Base base;
    double chokeAngle, bestWallScore, jpsDist;
    boolean pylonWall, openWall, requireTight, movedStart, pylonWallPiece, allowLifted, flatRamp;
    Station closestStation;

    Wall(Area _area, ChokePoint _choke, List<UnitType> _buildings, List<UnitType> _defenses, UnitType _tightType, boolean _requireTight, boolean _openWall) {
        area = _area;
        choke = _choke;
        rawBuildings = _buildings;
        rawDefenses = _defenses;
        tightType = _tightType;
        requireTight = _requireTight;
        openWall = _openWall;

        // Create Wall layout and find basic features
        initialize();
        addPieces();
        currentLayout = bestLayout;
        centroid = findCentroid();
        opening = findOpening();

        // Add defenses
        addDefenses();

        // Verify opening and cleanup Wall
        opening = findOpening();
        cleanup();
    }

    ChokePoint getChokePoint() {
        return choke;
    }

    Area getArea() {
        return area;
    }

    TreeSet<TilePosition> getDefenses() {
        return defenses;
    }

    TilePosition getOpening() {
        return opening;
    }

    Position getCentroid() {
        return centroid;
    }

    TreeSet<TilePosition> getLargeTiles() {
        return largeTiles;
    }

    TreeSet<TilePosition> getMediumTiles() {
        return mediumTiles;
    }

    TreeSet<TilePosition> getSmallTiles() {
        return smallTiles;
    }

    List<UnitType> getRawBuildings() {
        return rawBuildings;
    }

    List<UnitType> getRawDefenses() {
        return rawDefenses;
    }

    boolean isPylonWall() {
        return pylonWall;
    }

    /// Adds a piece at the TilePosition based on the UnitType.
    void addToWallPieces(TilePosition here, UnitType building) {
        if (building.tileWidth() >= 4)
            largeTiles.add(here);
        else if (building.tileWidth() >= 3)
            mediumTiles.add(here);
        else if (building != rawDefenses.get(rawDefenses.size()-1))
            defenses.add(here);
        else if (building.tileWidth() >= 2)
            smallTiles.add(here);
    }

    Position findCentroid() {
        // Create current centroid using all buildings except Pylons
        Position currentCentroid = new Position(0, 0);
        int sizeWall = rawBuildings.size();
        for (TilePosition tile : bestLayout.keySet()) {
            UnitType type = bestLayout.get(tile);
            if (type != UnitType.Protoss_Pylon) {
                currentCentroid = new Position(currentCentroid.x + tile.toPosition().x + type.tileSize().toPosition().x/2,
                        currentCentroid.y + tile.toPosition().y + type.tileSize().toPosition().y/2);
            } else {
                sizeWall--;
            }
        }

        // Create a centroid if we only have a Pylon wall
        if (sizeWall == 0) {
            sizeWall = bestLayout.size();
            for (TilePosition tile : bestLayout.keySet()) {
                UnitType type = bestLayout.get(tile);
                currentCentroid = new Position(currentCentroid.x + tile.toPosition().x + type.tileSize().toPosition().x/2,
                        currentCentroid.y + tile.toPosition().y + type.tileSize().toPosition().y/2);
            }
        }

        return new Position(currentCentroid.x/sizeWall, currentCentroid.y/sizeWall);
    }

    TilePosition findOpening() {
        if (!openWall) {
            return TilePosition.Invalid;
        }

        // Set any tiles on the path as reserved so we don't build on them
        Path currentPath = findPathOut();
        TilePosition currentOpening = TilePosition.Invalid;

        // Check which tile is closest to each part on the path, set as opening
        double distBest = Double.MAX_VALUE;
        for (TilePosition pathTile : currentPath.getTiles()){
            Position closestChokeGeo = Map.getClosestChokeTile (choke, new Position(pathTile));
            double dist = closestChokeGeo.getDistance(new Position(pathTile));
            Position centerPath = new Position(pathTile.x + 16, pathTile.y + 16);

            boolean angleOkay = true;
            boolean distOkay = false;

            // Check if the angle and distance is okay
            for (TilePosition tileLayout : currentLayout.keySet()) {
                UnitType typeLayout = currentLayout.get(tileLayout);
                if (typeLayout == UnitType.Protoss_Pylon) {
                    continue;
                }

                Position centerPiece = new Position(tileLayout.toPosition().x + typeLayout.tileWidth() * 16,
                        tileLayout.toPosition().y + typeLayout.tileHeight() * 16);
                double openingAngle = Map.getAngle(new Pair<>(centerPiece, centerPath));
                double openingDist = centerPiece.getDistance(centerPath);

                if (Math.abs(chokeAngle - openingAngle) > 35.0)
                    angleOkay = false;
                if (openingDist < 320.0)
                    distOkay = true;
            }
            if (distOkay && angleOkay && dist < distBest) {
                distBest = dist;
                currentOpening = pathTile;
            }
        }

        // If we don't have an opening, assign closest path tile to wall centroid as opening
        if (!currentOpening.isValid(Map.game)) {
            for (TilePosition pathTile : currentPath.getTiles()) {
                Position p = new Position(pathTile);
                double dist = centroid.getDistance(p);
                if (dist < distBest) {
                    distBest = dist;
                    currentOpening = pathTile;
                }
            }
        }

        return currentOpening;
    }

    Path findPathOut() {
        // Check that the path points are possible to reach
        checkPathPoints();
        Position startCenter = new Position(pathStart.toPosition().x + 16, pathStart.toPosition().y + 16);
        Position endCenter = new Position(pathEnd.toPosition().x + 16, pathEnd.toPosition().y + 16);

        // Get a new path
        Path newPath;
        allowLifted = false;
        newPath.bfsPath(endCenter, startCenter, [&](auto &t) { return this.wallWalkable(t); }, false);
        return newPath;
    }

    boolean powerCheck(UnitType type, TilePosition here) {
        if (type != UnitType.Protoss_Pylon || pylonWall)
            return true;

        // TODO: Create a generic BWEB function that takes 2 tiles and tells you if the 1st tile will power the 2nd tile
        for (TilePosition tileLayout : currentLayout.keySet()) {
            UnitType typeLayout = currentLayout.get(tileLayout);
            if (typeLayout == UnitType.Protoss_Pylon) {
                continue;
            }

            if (typeLayout.tileWidth() == 4) {
                boolean powersThis = false;
                if (tileLayout.y - here.y == -5 || tileLayout.y - here.y == 4) {
                    if (tileLayout.x - here.x >= -4 && tileLayout.x - here.x <= 1) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y == -4 || tileLayout.y - here.y == 3) {
                    if (tileLayout.x - here.x >= -7 && tileLayout.x - here.x <= 4) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y == -3 || tileLayout.y - here.y == 2) {
                    if (tileLayout.x - here.x >= -8 && tileLayout.x - here.x <= 5) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y >= -2 && tileLayout.y - here.y <= 1) {
                    if (tileLayout.x - here.x >= -8 && tileLayout.x - here.x <= 6) {
                        powersThis = true;
                    }
                }
                if (!powersThis) {
                    return false;
                }
            } else {
                boolean powersThis = false;
                if (tileLayout.y - here.y == 4) {
                    if (tileLayout.x - here.x >= -3 && tileLayout.x - here.x <= 2) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y == -4 || tileLayout.y - here.y == 3) {
                    if (tileLayout.x - here.x >= -6 && tileLayout.x - here.x <= 5) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y >= -3 && tileLayout.y - here.y <= 2) {
                    if (tileLayout.x - here.x >= -7 && tileLayout.x - here.x <= 6) {
                        powersThis = true;
                    }
                }
                if (!powersThis) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean angleCheck(UnitType type, TilePosition here) {
        Position centerHere = new Position(here.toPosition().x + type.tileWidth()*16,
                here.toPosition().y + type.tileHeight()*16);

        // If we want a closed wall, we don't care the angle of the buildings
        if (!openWall || (type == UnitType.Protoss_Pylon && !pylonWall && !pylonWallPiece)) {
            return true;
        }

        // Check if the angle is okay between all pieces in the current layout
        for (TilePosition tileLayout : currentLayout.keySet()) {
            UnitType typeLayout = currentLayout.get(tileLayout);
            if (typeLayout == UnitType.Protoss_Pylon)
                continue;

            Position centerPiece = new Position(tileLayout.toPosition().x + typeLayout.tileWidth()*16,
                    tileLayout.toPosition().y + typeLayout.tileHeight()*16);
            double wallAngle = Map.getAngle(new Pair<>(centerPiece, centerHere));

            if (Math.abs(chokeAngle - wallAngle) > 20.0) {
                return false;
            }
        }
        return true;
    }

    boolean placeCheck(UnitType type, TilePosition here) {
        // Allow Pylon to overlap station defenses
        if (type == UnitType.Protoss_Pylon) {
            if (closestStation != null && here != closestStation.getDefenseLocations().last()) {
                return true;
            }
        }

        // Check if placement is valid
        if (Map.isReserved(here, type.tileWidth(), type.tileHeight())
            || !Map.isPlaceable(type, here)
            || (!openWall && Map.tilesWithinArea(area, here, type.tileWidth(), type.tileHeight()) == 0)
            || (openWall && Map.tilesWithinArea(area, here, type.tileWidth(), type.tileHeight()) == 0 &&
                (type == UnitType.Protoss_Pylon || (Map.mapBWEM.getMap().getArea(here) != null &&
                        choke.getAreas().getFirst() != Map.mapBWEM.getMap().getArea(here) &&
                        choke.getAreas().getSecond() != Map.mapBWEM.getMap().getArea(here))))) {
            return false;
        }
        return true;
    }

    // Functions for each dimension check
//        const auto gapRight = [&](UnitType parent) {
//        return (parent.tileWidth() * 16) - parent.dimensionLeft() + dimR;
//    };
//        const auto gapLeft = [&](UnitType parent) {
//        return (parent.tileWidth() * 16) - parent.dimensionRight() - 1 + dimL;
//    };
//        const auto gapUp = [&](UnitType parent) {
//        return (parent.tileHeight() * 16) - parent.dimensionDown() - 1 + dimU;
//    };
//        const auto gapDown = [&](UnitType parent) {
//        return (parent.tileHeight() * 16) - parent.dimensionUp() + dimD;
//    };


    // Check if the building is terrain tight when placed here
//        const auto terrainTightCheck = [&](WalkPosition w, bool check) {
//            const auto t = TilePosition(w);
//
//        // If the walkposition is invalid or unwalkable
//        if (tightType != UnitTypes::None && check && (!w.isValid() || !Broodwar->isWalkable(w)))
//            return true;
//
//        // If we don't care about walling tight and the tile isn't walkable
//        if (!requireTight && !Map::isWalkable(t))
//        return true;
//
//        // If there's a mineral field or geyser here
//        if (Map::isUsed(t).isResourceContainer())
//        return true;
//        return false;
//    };


    // Iterate vertical tiles adjacent of this placement
//        const auto checkVerticalSide = [&](WalkPosition start, bool check, const auto gap) {
//        for (auto x = start.x - 1; x < start.x + walkWidth + 1; x++) {
//                const WalkPosition w(x, start.y);
//                const auto t = TilePosition(w);
//                const auto parent = Map::isUsed(t);
//                const auto leftCorner = x < start.x;
//                const auto rightCorner = x >= start.x + walkWidth;
//
//            // If this is a corner
//            if (leftCorner || rightCorner) {
//
//                // Check if it's tight with the terrain
//                if (!terrainTight && terrainTightCheck(w, check) && leftCorner ? terrainTightCheck(w, checkL) : terrainTightCheck(w, checkR))
//                    terrainTight = true;
//
//                // Check if it's tight with a parent
//                if (!parentTight && find(rawBuildings.begin(), rawBuildings.end(), parent) != rawBuildings.end() && (!requireTight || (gap(parent) < vertTight && (leftCorner ? gapLeft(parent) < horizTight : gapRight(parent) < horizTight))))
//                    parentTight = true;
//            }
//            else {
//
//                // Check if it's tight with the terrain
//                if (!terrainTight && terrainTightCheck(w, check))
//                    terrainTight = true;
//
//                // Check if it's tight with a parent
//                if (!parentTight && find(rawBuildings.begin(), rawBuildings.end(), parent) != rawBuildings.end() && (!requireTight || gap(parent) < vertTight))
//                    parentTight = true;
//            }
//
//            // Check to see which node it is closest to (0 is don't check, 1 is not tight, 2 is tight)
//            if (!openWall && !Map::isWalkable(t) && w.getDistance(choke->Center()) < 4) {
//                if (w.getDistance(choke->Pos(choke->end1)) < w.getDistance(choke->Pos(choke->end2))) {
//                    if (p1Tight == 0)
//                        p1Tight = 1;
//                    if (terrainTight)
//                        p1Tight = 2;
//                }
//                else if (p2Tight == 0) {
//                    if (p2Tight == 0)
//                        p2Tight = 1;
//                    if (terrainTight)
//                        p2Tight = 2;
//                }
//            }
//        }
//    };


    // Iterate horizontal tiles adjacent of this placement
//        const auto checkHorizontalSide = [&](WalkPosition start, bool check, const auto gap) {
//        for (auto y = start.y - 1; y < start.y + walkHeight + 1; y++) {
//                const WalkPosition w(start.x, y);
//                const auto t = TilePosition(w);
//                const auto parent = Map::isUsed(t);
//                const auto topCorner = y < start.y;
//                const auto downCorner = y >= start.y + walkHeight;
//
//            // If this is a corner
//            if (topCorner || downCorner) {
//
//                // Check if it's tight with the terrain
//                if (!terrainTight && terrainTightCheck(w, check) && topCorner ? terrainTightCheck(w, checkU) : terrainTightCheck(w, checkD))
//                    terrainTight = true;
//
//                // Check if it's tight with a parent
//                if (!parentTight && find(rawBuildings.begin(), rawBuildings.end(), parent) != rawBuildings.end() && (!requireTight || (gap(parent) < horizTight && (topCorner ? gapUp(parent) < vertTight : gapDown(parent) < vertTight))))
//                    parentTight = true;
//            }
//            else {
//
//                // Check if it's tight with the terrain
//                if (!terrainTight && terrainTightCheck(w, check))
//                    terrainTight = true;
//
//                // Check if it's tight with a parent
//                if (!parentTight && find(rawBuildings.begin(), rawBuildings.end(), parent) != rawBuildings.end() && (!requireTight || gap(parent) < horizTight))
//                    parentTight = true;
//            }
//
//            // Check to see which node it is closest to (0 is don't check, 1 is not tight, 2 is tight)
//            if (!openWall && !Map::isWalkable(t) && w.getDistance(choke->Center()) < 4) {
//                if (w.getDistance(choke->Pos(choke->end1)) < w.getDistance(choke->Pos(choke->end2))) {
//                    if (p1Tight == 0)
//                        p1Tight = 1;
//                    if (terrainTight)
//                        p1Tight = 2;
//                }
//                else if (p2Tight == 0) {
//                    if (p2Tight == 0)
//                        p2Tight = 1;
//                    if (terrainTight)
//                        p2Tight = 2;
//                }
//            }
//        }
//    };


//    boolean tightCheck(const UnitType type, const TilePosition here) {
//        // If this is a powering pylon and we are not making a pylon wall, we don't care if it's tight
//        if (type == UnitTypes::Protoss_Pylon && !pylonWall && !pylonWallPiece)
//            return true;
//
//        // Dimensions of current buildings UnitType
//        const auto dimL = (type.tileWidth() * 16) - type.dimensionLeft();
//        const auto dimR = (type.tileWidth() * 16) - type.dimensionRight() - 1;
//        const auto dimU = (type.tileHeight() * 16) - type.dimensionUp();
//        const auto dimD = (type.tileHeight() * 16) - type.dimensionDown() - 1;
//        const auto walkHeight = type.tileHeight() * 4;
//        const auto walkWidth = type.tileWidth() * 4;
//
//        // Dimension of UnitType to check tightness for
//        const auto vertTight = (tightType == UnitTypes::None) ? 32 : tightType.height();
//        const auto horizTight = (tightType == UnitTypes::None) ? 32 : tightType.width();
//
//        // Checks each side of the building to see if it is valid for walling purposes
//        const auto checkL = dimL < horizTight;
//        const auto checkR = dimR < horizTight;
//        const auto checkU = dimU < vertTight;
//        const auto checkD = dimD < vertTight;
//
//        // Figures out how many extra tiles we can check tightness for
//        const auto extraL = pylonWall || !requireTight ? 0 : max(0, (horizTight - dimL) / 8);
//        const auto extraR = pylonWall || !requireTight ? 0 : max(0, (horizTight - dimR) / 8);
//        const auto extraU = pylonWall || !requireTight ? 0 : max(0, (vertTight - dimU) / 8);
//        const auto extraD = pylonWall || !requireTight ? 0 : max(0, (vertTight - dimD) / 8);
//
//        // Setup boundary WalkPositions to check for tightness
//        const auto left =  WalkPosition(here) - WalkPosition(1 + extraL, 0);
//        const auto right = WalkPosition(here) + WalkPosition(walkWidth + extraR, 0);
//        const auto up =  WalkPosition(here) - WalkPosition(0, 1 + extraU);
//        const auto down =  WalkPosition(here) + WalkPosition(0, walkHeight + extraD);
//
//        // Used for determining if the tightness we found is suitable
//        const auto firstBuilding = currentLayout.size() == 0;
//        const auto lastBuilding = currentLayout.size() == (rawBuildings.size() - 1);
//        auto terrainTight = false;
//        auto parentTight = false;
//        auto p1Tight = 0;
//        auto p2Tight = 0;
//
//        // For each side, check if it's terrain tight or tight with any adjacent buildings
//        checkVerticalSide(up, checkU, gapUp);
//        checkVerticalSide(down, checkD, gapDown);
//        checkHorizontalSide(left, checkL, gapLeft);
//        checkHorizontalSide(right, checkR, gapRight);
//
//        // If we want a closed wall, we need all buildings to be tight at the tightness resolution...
//        if (!openWall) {
//            if (!lastBuilding && !firstBuilding)      // ...to the parent if not first building
//                return parentTight;
//            if (firstBuilding)                        // ...to the terrain if first building
//                return terrainTight && p1Tight != 1 && p2Tight != 1;
//            if (lastBuilding)                         // ...to the parent and terrain if last building
//                return terrainTight && parentTight && p1Tight != 1 && p2Tight != 1;
//        }
//
//        // If we want an open wall, we need this building to be tight at tile resolution to a parent or terrain
//        else if (openWall)
//            return (terrainTight || parentTight);
//        return false;
//    }
//
    boolean spawnCheck(UnitType type, TilePosition here) {
        // TODO: Check if units spawn in bad spots, just returns true for now
        checkPathPoints();
        Position startCenter = new Position(pathStart.toPosition().x + 16, pathStart.toPosition().y + 16);
        Position endCenter = new Position(pathEnd.toPosition().x + 16, pathEnd.toPosition().y + 16);
        Path pathOut;
        return true;
    }

    boolean wallWalkable(TilePosition tile) {
        // Checks for any collision and inverts the return value
        if (!tile.isValid(Map.game)
                || (Map.mapBWEM.getMap().getArea(tile) != null && Map.mapBWEM.getMap().getArea(tile) != area
                && Map.mapBWEM.getMap().getArea(tile) == accessibleNeighbors.get(accessibleNeighbors.size()-1))
            || Map.isReserved(tile, 1, 1) || !Map.isWalkable(tile)
            || (allowLifted && Map.isUsed(tile, 1, 1) != UnitType.Terran_Barracks && Map.isUsed(tile, 1, 1) != UnitType.None)
            || (!allowLifted && Map.isUsed(tile, 1, 1) != UnitType.None && Map.isUsed(tile, 1, 1) != UnitType.Zerg_Larva)
            || (openWall && (tile).getDistance(pathEnd) - 64.0 > jpsDist / 32)){
            return false;
        }
        return true;
    }

    void initialize() {
        // Clear failed counters
        Walls.failedPlacement = 0;
        Walls.failedAngle = 0;
        Walls.failedPath = 0;
        Walls.failedTight = 0;
        Walls.failedSpawn = 0;
        Walls.failedPower = 0;

        // Set BWAPI::Points to invalid (default constructor is None)
        centroid = Position.Invalid;
        opening = TilePosition.Invalid;
        pathStart = TilePosition.Invalid;
        pathEnd = TilePosition.Invalid;
        initialPathStart = TilePosition.Invalid;
        initialPathEnd = TilePosition.Invalid;

        // Set important terrain features
        bestWallScore = 0;
        accessibleNeighbors = area->AccessibleNeighbours();
        chokeAngle = Map.getAngle(make_pair(Position(choke->Pos(choke->end1)) + Position(4, 4), Position(choke->Pos(choke->end2)) + Position(4, 4)));
        pylonWall = count(rawBuildings.begin(), rawBuildings.end(), BWAPI::UnitTypes::Protoss_Pylon) > 1;
        creationStart = TilePosition(choke->Center());
        base = !area.getBases().isEmpty() ? area.getBases().get(0) : null;
        flatRamp = Map.game.isBuildable(new TilePosition(choke.getCenter()));
        closestStation = Stations.getClosestStation(new TilePosition(choke.getCenter()));

        // Check if a Pylon should be put in the wall to help the size of the Wall or away from the wall for protection
        Position p1 = choke->Pos(choke->end1);
        Position p2 = choke->Pos(choke->end2);
        pylonWallPiece = Math.abs(p1.x - p2.x) * 8 >= 320 || Math.abs(p1.y - p2.y) * 8 >= 256 || p1.getDistance(p2) * 8 >= 288;

        // Create a jps path for limiting BFS exploration using the distance of the jps path
        Path jpsPath;
        initializePathPoints();
        checkPathPoints();
        jpsPath.createUnitPath(Position(pathStart), Position(pathEnd));
        jpsDist = jpsPath.getDistance();

        // If we can't reach the end/start points, the Wall is likely not possible and won't be attempted
        if (!jpsPath.isReachable())
            return;

        // Create notable locations to keep Wall pieces within proxmity of
        if (base != null) {
            notableLocations = {base -> Center(), Position(initialPathStart) + Position(16, 16), (base -> Center() + Position(initialPathStart)) / 2};
        } else {
            notableLocations = {Position(initialPathStart) + Position(16, 16), Position(initialPathEnd) + Position(16, 16)};
        }

        // Sort all the pieces and iterate over them to find the best wall - by Hannes
        if (find(rawBuildings.begin(), rawBuildings.end(), UnitType.Protoss_Pylon) != rawBuildings.end()) {
            sort(rawBuildings.begin(), rawBuildings.end(), [](UnitType l, UnitType r) { return (l == UnitType.Protoss_Pylon) < (r == UnitType.Protoss_Pylon); }); // Moves pylons to end
            sort(rawBuildings.begin(), find(rawBuildings.begin(), rawBuildings.end(), UnitTypes::Protoss_Pylon)); // Sorts everything before pylons
        }
        else if (find(rawBuildings.begin(), rawBuildings.end(), UnitType.Zerg_Hatchery) != rawBuildings.end()) {
            sort(rawBuildings.begin(), rawBuildings.end(), [](UnitType l, UnitType r) { return (l == UnitType.Zerg_Hatchery) > (r == UnitType.Zerg_Hatchery); }); // Moves hatchery to start
            sort(find(rawBuildings.begin(), rawBuildings.end(), UnitType.Zerg_Hatchery), rawBuildings.begin()); // Sorts everything after hatchery
        }
        else
            sort(rawBuildings.begin(), rawBuildings.end());

        // If there is a base in this area and we're creating an open wall, move creation start within 10 tiles of it
        if (openWall && base) {
            Position startCenter = Position(creationStart) + Position(16, 16);
            double distBest = Double.MAX_VALUE;
            Position moveTowards = (Position(initialPathStart) + base->Center()) / 2;

            // Iterate 3x3 around the current TilePosition and try to get within 5 tiles
            while (startCenter.getDistance(moveTowards) > 320.0) {
                TilePosition initialStart = creationStart;
                for (int x = initialStart.x - 1; x <= initialStart.x + 1; x++) {
                    for (int y = initialStart.y - 1; y <= initialStart.y + 1; y++) {
                        TilePosition t = new TilePosition(x, y);
                        if (!t.isValid(Map.game)) {
                            continue;
                        }

                        Position p = new Position(t.toPosition().x + 16, t.toPosition().y + 16);
                        double dist = p.getDistance(moveTowards);

                        if (dist < distBest) {
                            distBest = dist;
                            creationStart = t;
                            startCenter = p;
                            movedStart = true;
                            break;
                        }
                    }
                }
            }
        }

        // If the creation start position isn't buildable, move towards the top of this area to find a buildable location
        while (openWall && !Map.game.isBuildable(creationStart)) {
            double distBest = Double.MAX_VALUE;
            TilePosition initialStart = creationStart;
            for (int x = initialStart.x - 1; x <= initialStart.x + 1; x++) {
                for (int y = initialStart.y - 1; y <= initialStart.y + 1; y++) {
                    TilePosition t = new TilePosition(x, y);
                    if (!t.isValid(Map.game)) {
                        continue;
                    }

                    Position p = new Position(t);
                    double dist = p.getDistance(new Position(area.getTop()));

                    if (dist < distBest) {
                        distBest = dist;
                        creationStart = t;
                        movedStart = true;
                    }
                }
            }
        }
    }

//    void initializePathPoints() {
//        auto line = make_pair(Position(choke->Pos(choke->end1)) + Position(4, 4), Position(choke->Pos(choke->end2)) + Position(4, 4));
//        auto perpLine = openWall ? Map::perpendicularLine(line, 160.0) : Map::perpendicularLine(line, 96.0);
//        auto lineStart = perpLine.first.getDistance(Position(area->Top())) > perpLine.second.getDistance(Position(area->Top())) ? perpLine.second : perpLine.first;
//        auto lineEnd = perpLine.first.getDistance(Position(area->Top())) > perpLine.second.getDistance(Position(area->Top())) ? perpLine.first : perpLine.second;
//        auto isMain = closestStation && closestStation->isMain();
//        auto isNatural = closestStation && closestStation->isNatural();
//
//        // If it's a natural wall, path between the closest main and end of the perpendicular line
//        if (isNatural) {
//            Station * closestMain = Stations::getClosestMainStation(TilePosition(choke->Center()));
//            initialPathStart = closestMain ? TilePosition(Map::mapBWEM.GetPath(closestStation->getBWEMBase()->Center(), closestMain->getBWEMBase()->Center()).front()->Center()) : TilePosition(lineStart);
//            initialPathEnd = TilePosition(lineEnd);
//        }
//
//        // If it's a main wall, path between a point between the roughly the choke and the area top
//        else if (isMain) {
//            initialPathEnd = (TilePosition(choke->Center()) + TilePosition(lineEnd)) / 2;
//            initialPathStart = (TilePosition(area->Top()) + TilePosition(lineStart)) / 2;
//        }
//
//        // Other walls
//        else {
//            initialPathStart = TilePosition(lineStart);
//            initialPathEnd = TilePosition(lineEnd);
//        }
//
//        pathStart = initialPathStart;
//        pathEnd = initialPathEnd;
//    }


//        const auto neighbourArea = [&](const BWEM::Area * area) {
//        for (auto subArea : area->AccessibleNeighbours()) {
//            if (area == subArea)
//                return true;
//        }
//        return false;
//    };


//        const auto notValidPathPoint = [&](const TilePosition testTile) {
//        return !testTile.isValid()
//                || !Map::isWalkable(testTile)
//                || Map::isReserved(testTile)
//                || Map::isUsed(testTile) != UnitTypes::None;
//    };


//    void Wall::checkPathPoints()
//    {
//        // Push the path start as far from the path end if it's not in a valid location
//        auto distBest = 0.0;
//        if (notValidPathPoint(pathStart)) {
//            for (auto x = initialPathStart.x - 4; x < initialPathStart.x + 4; x++) {
//                for (auto y = initialPathStart.y - 4; y < initialPathStart.y + 4; y++) {
//                    const TilePosition t(x, y);
//                    const auto dist = t.getDistance(initialPathEnd);
//                    if (notValidPathPoint(t))
//                        continue;
//
//                    if (dist > distBest) {
//                        pathStart = t;
//                        distBest = dist;
//                    }
//                }
//            }
//        }
//
//        // Push the path end as far from the path start if it's not in a valid location
//        distBest = 0.0;
//        if (notValidPathPoint(pathEnd)) {
//            for (auto x = initialPathEnd.x - 4; x < initialPathEnd.x + 4; x++) {
//                for (auto y = initialPathEnd.y - 4; y < initialPathEnd.y + 4; y++) {
//                    const TilePosition t(x, y);
//                    const auto dist = t.getDistance(initialPathStart);
//                    if (notValidPathPoint(t))
//                        continue;
//
//                    if (dist > distBest) {
//                        pathEnd = t;
//                        distBest = dist;
//                    }
//                }
//            }
//        }
//    }

    void addPieces() {
        // For each permutation, try to make a wall combination that is better than the current best
        do {
            currentLayout.clear();
            typeIterator = rawBuildings.begin();
            addNextPiece(creationStart);
        } while (Map.game.self().getRace() == Race.Zerg ? next_permutation(find(rawBuildings.begin(), rawBuildings.end(), UnitType.Zerg_Hatchery), rawBuildings.end())
            : next_permutation(rawBuildings.begin(), find(rawBuildings.begin(), rawBuildings.end(), UnitType.Protoss_Pylon)));

        for (TilePosition tile : bestLayout.keySet()) {
            UnitType type = bestLayout.get(tile);
            addToWallPieces(tile, type);
            Map.addReserve(tile, type.tileWidth(), type.tileHeight());
            Map.addUsed(tile, type);
        }
    }

//    void addNextPiece(TilePosition start) {
//        const auto type = *typeIterator;
//        const auto radius = (openWall || typeIterator == rawBuildings.begin()) ? 8 : 4;
//
//        for (auto x = start.x - radius; x < start.x + radius; x++) {
//            for (auto y = start.y - radius; y < start.y + radius; y++) {
//                const TilePosition tile(x, y);
//
//                if (!tile.isValid())
//                    continue;
//
//                const auto center = Position(tile) + Position(type.tileWidth() * 16, type.tileHeight() * 16);
//                const auto closestGeo = Map::getClosestChokeTile(choke, center);
//
//                // Open walls need to be placed within proximity of notable features
//                if (openWall) {
//                    auto closestNotable = Positions::Invalid;
//                    auto closestNotableDist = DBL_MAX;
//                    for (auto & pos : notableLocations) {
//                        auto dist = pos.getDistance(center);
//                        if (dist < closestNotableDist) {
//                            closestNotable = pos;
//                            closestNotableDist = dist;
//                        }
//                    }
//                    if (center.getDistance(closestNotable) >= 256.0 || center.getDistance(closestNotable) >= closestGeo.getDistance(closestNotable) + 48.0) {
//
//                        continue;
//                    }
//                }
//
//                // Try not to seal the wall poorly
//                if (!openWall && flatRamp) {
//                    auto dist = min({ Position(tile).getDistance(Position(choke->Center())),
//                            Position(tile + TilePosition(type.tileWidth(), 0)).getDistance(Position(choke->Center())),
//                            Position(tile + TilePosition(type.tileWidth(), type.tileHeight())).getDistance(Position(choke->Center())),
//                            Position(tile + TilePosition(0, type.tileHeight())).getDistance(Position(choke->Center())) });
//                    if (dist < 64.0)
//                        continue;
//                }
//
//                // Required checks for this wall to be valid
//                if (!powerCheck(type, tile)) {
//                    failedPower++;
//                    continue;
//                }
//                if (!angleCheck(type, tile)) {
//                    failedAngle++;
//                    continue;
//                }
//                if (!placeCheck(type, tile)) {
//                    failedPlacement++;
//                    continue;
//                }
//                if (!tightCheck(type, tile)) {
//                    failedTight++;
//                    continue;
//                }
//                if (!spawnCheck(type, tile)) {
//                    failedSpawn++;
//                    continue;
//                }
//
//                // 1) Store the current type, increase the iterator
//                currentLayout[tile] = type;
//                Map::addUsed(tile, type);
//                typeIterator++;
//
//                // 2) If at the end, score wall
//                if (typeIterator == rawBuildings.end())
//                    scoreWall();
//                else
//                    openWall ? addNextPiece(start) : addNextPiece(tile);
//
//                // 3) Erase this current placement and repeat
//                if (typeIterator != rawBuildings.begin())
//                    typeIterator--;
//
//                currentLayout.erase(tile);
//                Map::removeUsed(tile, type.tileWidth(), type.tileHeight());
//            }
//        }
//    }
//
//    void addDefenses() {
//        // Prevent adding defenses if we don't have a wall
//        if (bestLayout.empty())
//            return;
//
//        // Find the furthest non Pylon building to the chokepoint
//        auto furthest = 0.0;
//        for (auto &tile : largeTiles) {
//            const auto center = Position(tile) + Position(64, 48);
//            const auto closestGeo = Map::getClosestChokeTile(choke, center);
//            const auto dist = center.getDistance(closestGeo);
//        if (dist > furthest)
//            furthest = dist;
//    }
//        for (auto &tile : mediumTiles) {
//            const auto center = Position(tile) + Position(48, 32);
//            const auto closestGeo = Map::getClosestChokeTile(choke, center);
//            const auto dist = center.getDistance(closestGeo);
//        if (dist > furthest)
//            furthest = dist;
//    }
//
//        // Find the furthest Pylon building to the chokepoint if it's a Pylon wall
//        if (pylonWall) {
//            for (auto &tile : smallTiles) {
//                const auto center = Position(tile) + Position(32, 32);
//                const auto closestGeo = Map::getClosestChokeTile(choke, center);
//                const auto dist = center.getDistance(closestGeo);
//                if (dist > furthest)
//                    furthest = dist;
//            }
//        }
//
//        auto closestStation = Stations::getClosestStation(TilePosition(choke->Center()));
//        for (auto &building : rawDefenses) {
//
//            const auto start = TilePosition(centroid);
//            const auto width = building.tileWidth() * 32;
//            const auto height = building.tileHeight() * 32;
//            const auto openingCenter = Position(opening) + Position(16, 16);
//            const auto arbitraryCloseMetric = Broodwar->self()->getRace() == Races::Zerg ? 32.0 : 160.0;
//
//        // Iterate around wall centroid to find a suitable position
//        auto scoreBest = DBL_MAX;
//        auto tileBest = TilePositions::Invalid;
//        for (auto x = start.x - 12; x <= start.x + 12; x++) {
//            for (auto y = start.y - 12; y <= start.y + 12; y++) {
//                    const TilePosition t(x, y);
//                    const auto center = Position(t) + Position(width / 2, height / 2);
//                    const auto closestGeo = Map::getClosestChokeTile(choke, center);
//                    const auto overlapsDefense = closestStation && closestStation->getDefenseLocations().find(t) != closestStation->getDefenseLocations().end() && defenses.find(t) == defenses.end();
//
//                    const auto dist = center.getDistance(closestGeo);
//                    const auto tooClose = dist < furthest || center.getDistance(openingCenter) < arbitraryCloseMetric;
//                    const auto tooFar = center.getDistance(centroid) > 200.0;
//
//                if (!overlapsDefense) {
//                    if (!t.isValid()
//                            || Map::isReserved(t, building.tileWidth(), building.tileHeight())
//                            || !Map::isPlaceable(building, t)
//                            || Map::tilesWithinArea(area, t, building.tileWidth(), building.tileHeight()) == 0
//                            || tooClose
//                            || tooFar)
//                    continue;
//                }
//                    const auto score = dist + center.getDistance(openingCenter);
//
//                if (score < scoreBest) {
//                    Map::addUsed(t, building);
//                    auto &pathOut = findPathOut();
//                    if ((openWall && pathOut.isReachable()) || !openWall) {
//                        tileBest = t;
//                        scoreBest = score;
//                    }
//                    Map::removeUsed(t, building.tileWidth(), building.tileHeight());
//                }
//            }
//        }
//
//        // If tile is valid, add to wall
//        if (tileBest.isValid()) {
//            defenses.insert(tileBest);
//            Map::addReserve(tileBest, building.tileWidth(), building.tileHeight());
//        }
//
//        // Otherwise we can't place anymore
//        else
//            break;
//    }
//    }

    void scoreWall() {
        // Create a path searching for an opening
        Path pathOut = findPathOut();

        // If we want an open wall and it's not reachable, or we want a closed wall and it is reachable
        if ((openWall && !pathOut.isReachable()) || (!openWall && pathOut.isReachable())) {
            Walls.failedPath++;
            return;
        }

        // Find distance for each piece to the closest choke tile to the path start point
        double dist = 1.0;
        Position optimalChokeTile = pathStart.getDistance(new TilePosition(choke.getNodePosition(ChokePoint.Node.END1))) <
                pathStart.getDistance(new TilePosition(choke.getNodePosition(ChokePoint.Node.END2))) ?
                new Position(choke.getNodePosition(ChokePoint.Node.END1)) : new Position(choke.getNodePosition(ChokePoint.Node.END2));
        for (TilePosition tile : currentLayout.keySet()) {
            UnitType type = currentLayout.get(tile);
            Position center = Position(tile) + Position(type.tileWidth() * 16, type.tileHeight() * 16);
            double chokeDist = optimalChokeTile.getDistance(center);
            if (type == UnitType.Protoss_Pylon && !pylonWall && !pylonWallPiece) {
                dist += -chokeDist;
            } else {
                dist += chokeDist;
            }
        }

        // Calculate current centroid if a closed wall
        Position currentCentroid = findCentroid();
        Position currentOpening = new Position(findOpening().toPosition().x + 16, findOpening().toPosition().y + 16);

        // Score wall and store if better than current best layout
        double score = !openWall ? dist : 1.0 / dist;
        if (score > bestWallScore) {
            bestLayout = currentLayout;
            bestWallScore = score;
        }
    }

    void cleanup() {
        // Add a reserved path
        if (openWall && !bestLayout.isEmpty()) {
            Path currentPath = findPathOut();
            for (TilePosition tile : currentPath.getTiles()) {
                Map.addReserve (tile, 1, 1);
            }
        }

        // Remove used from tiles
        for (TilePosition tile : smallTiles){
            Map.removeUsed (tile, 2, 2);
        }
        for (TilePosition tile : mediumTiles){
            Map.removeUsed (tile, 3, 2);
        }
        for (TilePosition tile : largeTiles){
            Map.removeUsed (tile, 4, 3);
        }
        for (TilePosition tile : defenses){
            Map.removeUsed (tile, 2, 2);
        }
    }


    int getGroundDefenseCount() {
        // Returns how many visible ground defensive structures exist in this Walls defense locations
        int count = 0;
        for (TilePosition defense : defenses) {
            UnitType type = Map.isUsed(defense, 1, 1);
            if (type == UnitType.Protoss_Photon_Cannon
                    || type == UnitType.Zerg_Sunken_Colony
                    || type == UnitType.Terran_Bunker) {
                count++;
            }
        }
        return count;
    }

    int getAirDefenseCount() {
        // Returns how many visible air defensive structures exist in this Walls defense locations
        int count = 0;
        for (TilePosition defense : defenses) {
            UnitType type = Map.isUsed(defense, 1, 1);
            if (type == UnitType.Protoss_Photon_Cannon
                    || type == UnitType.Zerg_Spore_Colony
                    || type == UnitType.Terran_Missile_Turret) {
                count++;
            }
        }
        return count;
    }

    void draw() {
        TreeSet<Position> anglePositions;
        Color color = Map.game.self().getColor();
        Text textColor = color.id == 185 ? textColor = Text.DarkGreen : Map.game.self().getTextColor();

        // Draw boxes around each feature
        boolean drawBoxes = true;
        if (drawBoxes) {
            for (TilePosition tile : smallTiles) {
                Map.game.drawBoxMap(new Position(tile), Position(tile) + Position(65, 65), color);
                Map.game.drawTextMap(Position(tile) + Position(4, 4), "%cW", Map.game.self().getTextColor());
                anglePositions.add(Position(tile) + Position(32, 32));
            }
            for (TilePosition tile : mediumTiles) {
                Map.game.drawBoxMap(new Position(tile), Position(tile) + Position(97, 65), color);
                Map.game.drawTextMap(Position(tile) + Position(4, 4), "%cW", Map.game.self().getTextColor());
                anglePositions.add(Position(tile) + Position(48, 32));
            }
            for (TilePosition tile : largeTiles) {
                Map.game.drawBoxMap(new Position(tile), Position(tile) + Position(129, 97), color);
                Map.game.drawTextMap(Position(tile) + Position(4, 4), "%cW", Map.game.self().getTextColor());
                anglePositions.add(Position(tile) + Position(64, 48));
            }
            for (TilePosition tile : defenses) {
                Map.game.drawBoxMap(new Position(tile), Position(tile) + Position(65, 65), color);
                Map.game.drawTextMap(Position(tile) + Position(4, 4), "%cW", Map.game.self().getTextColor());
            }
        }

        // Draw angles of each piece
        boolean drawAngles = false;
        if (drawAngles) {
            for (Position pos1 : anglePositions) {
                for (Position pos2 : anglePositions) {
                    if (pos1 == pos2) {
                        continue;
                    }

                    Map.game.drawLineMap(pos1, pos2, color);
                    Map.game.drawTextMap(new Position((pos1.x + pos2.x)/ 2, (pos1.y + pos2.y)/ 2), "%c%.2f", textColor);
                }
            }
        }

        // Draw opening
        Map.game.drawBoxMap(Position(opening), Position(opening) + Position(33, 33), color, true);

        // Draw the line and angle of the ChokePoint
        Position p1 = choke.getNodePosition(ChokePoint.Node.END1).toPosition();
        Position p2 = choke.getNodePosition(ChokePoint.Node.END2).toPosition();
        Map.game.drawTextMap(new Position(choke.getCenter()), "%c%.2f", Text.Grey);
        Map.game.drawLineMap(new Position(p1), new Position(p2), Color.Grey);

        // Draw the path points
        Map.game.drawCircleMap(new Position(pathStart), 6, Color.Black, true);
        Map.game.drawCircleMap(new Position(pathEnd), 6, Color.White, true);
    }
}
