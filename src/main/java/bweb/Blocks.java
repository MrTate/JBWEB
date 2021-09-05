package bweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bwapi.*;
import bwem.*;

public class Blocks {
    static List<Block> allBlocks;
    HashMap<Area, Integer> typePerArea;
    HashMap<Piece, Integer> mainPieces;


    int countPieces(List<Piece> pieces, Piece type) {
        int count = 0;
        for (Piece piece : pieces) {
        if (piece == type)
            count++;
        }
        return count;
    }

    List<Piece> whichPieces(int width, int height, boolean faceUp, boolean faceLeft) {
        List<Piece> pieces = new ArrayList<>();

        // Zerg Block pieces
        if (Map.game.self().getRace() == Race.Zerg) {
            if (height == 2) {
                if (width == 2) {
                    pieces.add(Piece.Small);
                }
                if (width == 3) {
                    pieces.add(Piece.Medium);
                }
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 3) {
                if (width == 4) {
                    pieces.add(Piece.Large);
                }
            } else if (height == 4) {
                if (width == 3) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                }
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 6) {
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            }
        }

        // Protoss Block pieces
        if (Map.game.self().getRace() == Race.Protoss) {
            if (height == 2) {
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 4) {
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 5) {
                if (width == 4) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Small);
                }
                if (width == 8) {
                    if (faceLeft) {
                        if (faceUp) {
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Row);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Small);
                        } else {
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Small);
                            pieces.add(Piece.Row);
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Large);
                        }
                    } else {
                        if (faceUp) {
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Row);
                            pieces.add(Piece.Small);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Medium);
                        } else {
                            pieces.add(Piece.Small);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Row);
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Large);
                        }
                    }
                }
            } else if (height == 6) {
                if (width == 10) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Large);
                }
                if (width == 18) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                }
            } else if (height == 8) {
                if (width == 8) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                }
                if (width == 5) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                }
            }
        }

        // Terran Block pieces
        if (Map.game.self().getRace() == Race.Terran) {
            if (height == 2) {
                if (width == 3) {
                    pieces.add(Piece.Medium);
                }
                if (width == 6) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 4) {
                if (width == 3) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 6) {
                if (width == 3) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 3) {
                if (width == 6) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                }
            } else if (height == 4) {
                if (width == 6) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                }
                if (width == 9) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 5) {
                if (width == 6) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 6) {
                if (width == 6) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                }
            }
        }
        return pieces;
    }

    boolean canAddBlock(TilePosition here, int width, int height) {
        // Check if a block of specified size would overlap any bases, resources or other blocks
        for (int x = here.x - 1; x < here.x + width + 1; x++) {
            for (int y = here.y - 1; y < here.y + height + 1; y++) {
                    TilePosition t = new TilePosition(x, y);
                if (!t.isValid(Map.game) || !Map.mapBWEM.getMap().getTile(t).isBuildable() || Map.isReserved(t, 1, 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean canAddProxyBlock(TilePosition here, int width, int height) {
        // Check if a proxy block of specified size is not buildable here
        for (int x = here.x - 1; x < here.x + width + 1; x++) {
            for (int y = here.y - 1; y < here.y + height + 1; y++) {
                    TilePosition t = new TilePosition(x, y);
                if (!t.isValid(Map.game) || !Map.mapBWEM.getMap().getTile(t).isBuildable() || !Map.game.isWalkable(new WalkPosition(t))) {
                    return false;
                }
            }
        }
        return true;
    }

    void insertBlock(TilePosition here, List<Piece> pieces) {
        Block newBlock = new Block(here, pieces, false, false);
        allBlocks.add(newBlock);
        Map.addReserve(here, newBlock.width(), newBlock.height());
    }

    void insertProxyBlock(TilePosition here, List<Piece> pieces) {
        Block newBlock = new Block(here, pieces, true, false);
        allBlocks.add(newBlock);
        Map.addReserve(here, newBlock.width(), newBlock.height());
    }

    void insertDefensiveBlock(TilePosition here, List<Piece> pieces) {
        Block newBlock = new Block(here, pieces, false, true);
        allBlocks.add(newBlock);
        Map.addReserve(here, newBlock.width(), newBlock.height());
    }

    private boolean creepOnCorners(TilePosition here, int width, int height) {
        boolean b1 = Map.game.hasCreep(here);
        boolean b2 = Map.game.hasCreep(new TilePosition(here.x + width - 1, here.y));
        boolean b3 = Map.game.hasCreep(new TilePosition(here.x, here.y + height - 1));
        boolean b4 = Map.game.hasCreep(new TilePosition(here.x + width - 1, here.y + height - 1));
        return b1 && b2 && b3 && b4;
    }

    private void searchStart(Position start) {
        TilePosition tileStart = new TilePosition(start);
        TilePosition tileBest = TilePosition.Invalid;
        double distBest = Double.MAX_VALUE;
        List<Piece> piecesBest = new ArrayList<>();

        for (int i = 10; i > 0; i--) {
            for (int j = 10; j > 0; j--) {
                // Try to find a block near our starting location
                for (int x = tileStart.x - 15; x <= tileStart.x + 15; x++) {
                    for (int y = tileStart.y - 15; y <= tileStart.y + 15; y++) {
                        TilePosition tile = new TilePosition(x, y);
                        Position blockCenter = new Position(tile.x + i*16, tile.y + j*16);
                        double dist = blockCenter.getDistance(start);
                        boolean blockFacesLeft = (blockCenter.x < Map.getMainPosition().x);
                        boolean blockFacesUp = (blockCenter.y < Map.getMainPosition().y);

                        // Check if we have pieces to use
                        List<Piece> pieces = whichPieces(i, j, blockFacesUp, blockFacesLeft);
                        if (pieces.isEmpty()) {
                            continue;
                        }

                        // Check if we have creep as Zerg
                        Race race = Map.game.self().getRace();
                        if (race == Race.Zerg && !creepOnCorners(tile, i, j)) {
                            continue;
                        }

                        int smallCount = countPieces(pieces, Piece.Small);
                        int mediumCount = countPieces(pieces, Piece.Medium);
                        int largeCount = countPieces(pieces, Piece.Large);

                        if (!tile.isValid(Map.game)
                                || mediumCount < 1
                                || (race == Race.Zerg && smallCount == 0 && mediumCount == 0)
                                || (race == Race.Protoss && largeCount < 2)
                                || (race == Race.Terran && largeCount < 1)) {
                            continue;
                        }

                        if (dist < distBest && canAddBlock(tile, i, j)) {
                            piecesBest = pieces;
                            distBest = dist;
                            tileBest = tile;
                        }
                    }
                }

                if (tileBest.isValid(Map.game) && canAddBlock(tileBest, i, j)) {
                    if (Map.mapBWEM.getMap().getArea(tileBest) == Map.getMainArea()) {
                        for (Piece piece : piecesBest) {
                            int tmp = mainPieces.get(piece) + 1;
                            mainPieces.put(piece, tmp);
                        }
                    }
                    insertBlock(tileBest, piecesBest);
                }
            }
        }
    };

    void findMainStartBlocks() {
        Race race = Map.game.self().getRace();
        Position firstStart = Map.getMainPosition();
        Position secondStart = race != Race.Zerg ? (new Position(Map.getMainChoke().getCenter().x + Map.getMainPosition().x/2,
                Map.getMainChoke().getCenter().y + Map.getMainPosition().y/2)) : Map.getMainPosition();

        searchStart(firstStart);
        searchStart(secondStart);
    }

    void findMainDefenseBlock() {
        if (Map.game.self().getRace() == Race.Zerg)
        return;

        // Added a block that allows a good shield battery placement or bunker placement
        TilePosition tileBest = TilePosition.Invalid;
        TilePosition start = new TilePosition(Map.getMainChoke().getCenter());
        double distBest = Double.MAX_VALUE;
        for (int x = start.x - 12; x <= start.x + 16; x++) {
            for (int y = start.y - 12; y <= start.y + 16; y++) {
                TilePosition tile = new TilePosition(x, y);
                Position blockCenter = new Position(tile.toPosition().x + 80, tile.toPosition().y + 32);
                double dist = (blockCenter.getDistance(Map.getMainChoke().getCenter().toPosition()));

                if (!tile.isValid(Map.game)
                        || Map.mapBWEM.getMap().getArea(tile) != Map.getMainArea()
                        || dist < 96.0){
                    continue;
                }

                if (dist < distBest && canAddBlock(tile, 5, 2)) {
                    tileBest = tile;
                    distBest = dist;
                }
            }
        }

        if (tileBest.isValid(Map.game)) {
            List<Piece> p = new ArrayList<>();
            p.add(Piece.Small);
            p.add(Piece.Medium);
            insertDefensiveBlock(tileBest, p);
        }
    }

    void findProductionBlocks() {
        HashMap<Double, TilePosition> tilesByPathDist;
        int totalMedium = 0;
        int totalLarge = 0;

        // Calculate distance for each tile to our natural choke, we want to place bigger blocks closer to the chokes
        for (int y = 0; y < Map.game.mapHeight(); y++) {
            for (int x = 0; x < Map.game.mapWidth(); x++) {
                TilePosition t = new TilePosition(x, y);
                if (t.isValid(Map.game) && Map.game.isBuildable(t)) {
                    Position p = Position(x * 32, y * 32);
                    double dist = (Map.getNaturalChoke() && Map.game.self().getRace() != Race.Zerg) ? p.getDistance(Position(Map.getNaturalChoke().getCenter())) : p.getDistance(Map.getMainPosition());
                    tilesByPathDist.insert(make_pair(dist, t));
                }
            }
        }

        // Iterate every tile
        for (int i = 20; i > 0; i--) {
            for (int j = 20; j > 0; j--) {
                // Check if we have pieces to use
                List<Piece> pieces = whichPieces(i, j, false, false);
                if (pieces.isEmpty()) {
                    continue;
                }

                int smallCount = countPieces(pieces, Piece.Small);
                int mediumCount = countPieces(pieces, Piece.Medium);
                int largeCount = countPieces(pieces, Piece.Large);

                for (auto &[_, tile] : tilesByPathDist) {

                    // Protoss caps large pieces in the main at 12 if we don't have necessary medium pieces
                    if (Broodwar->self()->getRace() == Race.Protoss) {
                        if (largeCount > 0 && Map.mapBWEM.getMap().getArea(tile) == Map.getMainArea() && mainPieces[Piece::Large] >= 12 && mainPieces[Piece::Medium] < 10)
                        continue;
                    }

                    // Zerg only need 4 medium pieces and 2 small piece
                    if (Broodwar->self()->getRace() == Race.Zerg) {
                        if ((mediumCount > 0 && mainPieces[Piece.Medium] >= 4)
                                || (smallCount > 0 && mainPieces[Piece.Small] >= 2)) {
                            continue;
                        }
                    }

                    // Terran only need about 20 depot spots
                    if (Map.game.self().getRace() == Race.Terran) {
                        if (mediumCount > 0 && mainPieces[Piece.Medium] >= 20)
                            continue;
                    }

                    if (canAddBlock(tile, i, j)) {
                        insertBlock(tile, pieces);

                        totalMedium += mediumCount;
                        totalLarge += largeCount;

                        if (Map::mapBWEM.GetArea(tile) == Map.getMainArea()) {
                            for (auto &piece : pieces)
                            mainPieces[piece]++;
                        }
                    }
                }
            }
        }
    }

//    void findProxyBlock() {
//        // For base-specific locations, avoid all areas likely to be traversed by worker scouts
//        set<const BWEM::Area*> areasToAvoid;
//        for (auto &first : Map::mapBWEM.StartingLocations()) {
//        for (auto &second : Map::mapBWEM.StartingLocations()) {
//            if (first == second)
//                continue;
//
//            for (auto &choke : Map::mapBWEM.GetPath(Position(first), Position(second))) {
//                areasToAvoid.insert(choke->GetAreas().first);
//                areasToAvoid.insert(choke->GetAreas().second);
//            }
//        }
//
//        // Also add any areas that neighbour each start location
//        auto baseArea = Map::mapBWEM.GetNearestArea(first);
//        for (auto &area : baseArea->AccessibleNeighbours())
//        areasToAvoid.insert(area);
//    }
//
//        // Gather the possible enemy start locations
//        vector<TilePosition> enemyStartLocations;
//        for (auto &start : Map::mapBWEM.StartingLocations()) {
//        if (Map::mapBWEM.GetArea(start) != Map::getMainArea())
//        enemyStartLocations.push_back(start);
//    }
//
//        // Check if this block is in a good area
//            const auto goodArea = [&](TilePosition t) {
//        for (auto &start : enemyStartLocations) {
//            if (Map::mapBWEM.GetArea(t) == Map::mapBWEM.GetArea(start))
//            return false;
//        }
//        for (auto &area : areasToAvoid) {
//            if (Map::mapBWEM.GetArea(t) == area)
//            return false;
//        }
//        return true;
//    };
//
//        // Check if there's a blocking neutral between the positions to prevent bad pathing
//            const auto blockedPath = [&](Position source, Position target) {
//        for (auto &choke : Map::mapBWEM.GetPath(source, target)) {
//            if (Map::isUsed(TilePosition(choke->Center())) != UnitTypes::None)
//            return true;
//        }
//        return false;
//    };
//
//        // Find the best locations
//        TilePosition tileBest = TilePositions::Invalid;
//        auto distBest = DBL_MAX;
//        for (int x = 0; x < Broodwar->mapWidth(); x++) {
//            for (int y = 0; y < Broodwar->mapHeight(); y++) {
//                    const TilePosition topLeft(x, y);
//                    const TilePosition botRight(x + 8, y + 5);
//
//                if (!topLeft.isValid()
//                        || !botRight.isValid()
//                        || !canAddProxyBlock(topLeft, 8, 5))
//                    continue;
//
//                    const Position blockCenter = Position(topLeft) + Position(160, 96);
//
//                // Consider each start location
//                auto dist = 0.0;
//                for (auto &base : enemyStartLocations) {
//                        const auto baseCenter = Position(base) + Position(64, 48);
//                    dist += Map::getGroundDistance(blockCenter, baseCenter);
//                    if (blockedPath(blockCenter, baseCenter)) {
//                        dist = DBL_MAX;
//                        break;
//                    }
//                }
//
//                // Bonus for placing in a good area
//                if (goodArea(topLeft) && goodArea(botRight))
//                    dist = log(dist);
//
//                if (dist < distBest) {
//                    distBest = dist;
//                    tileBest = topLeft;
//                }
//            }
//        }
//
//        // Add the blocks
//        if (canAddProxyBlock(tileBest, 8, 5))
//            insertProxyBlock(tileBest, { Piece::Large, Piece::Large, Piece::Row, Piece::Small, Piece::Small, Piece::Small, Piece::Small });
//    }
//}
//
//    void eraseBlock(const TilePosition here)
//    {
//        for (auto &it = allBlocks.begin(); it != allBlocks.end(); ++it) {
//            auto &block = *it;
//            if (here.x >= block.getTilePosition().x && here.x < block.getTilePosition().x + block.width() && here.y >= block.getTilePosition().y && here.y < block.getTilePosition().y + block.height()) {
//                allBlocks.erase(it);
//                return;
//            }
//        }
//    }

    public void findBlocks() {
        findMainDefenseBlock();
        findMainStartBlocks();
        findProxyBlock();
        findProductionBlocks();
    }

    public void draw() {
        for (Block block : allBlocks) {
            block.draw();
        }
    }

    public static List<Block> getBlocks() {
        return allBlocks;
    }

    public Block getClosestBlock(TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Block bestBlock = null;
        for (Block block : allBlocks) {
            TilePosition tile = new TilePosition(block.getTilePosition().x + block.width()/2, block.getTilePosition().y + block.height()/2);
            double dist = here.getDistance(tile);

            if (dist < distBest) {
                distBest = dist;
                bestBlock = block;
            }
        }
        return bestBlock;
    }
}
