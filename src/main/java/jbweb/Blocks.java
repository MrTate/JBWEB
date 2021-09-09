package jbweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
        HashMap<Double, TilePosition> tilesByPathDist = new HashMap<>();
        int totalMedium = 0;
        int totalLarge = 0;

        // Calculate distance for each tile to our natural choke, we want to place bigger blocks closer to the chokes
        for (int y = 0; y < Map.game.mapHeight(); y++) {
            for (int x = 0; x < Map.game.mapWidth(); x++) {
                TilePosition t = new TilePosition(x, y);
                if (t.isValid(Map.game) && Map.game.isBuildable(t)) {
                    Position p = new Position(x * 32, y * 32);
                    double dist = (Map.getNaturalChoke() != null && Map.game.self().getRace() != Race.Zerg) ?
                            p.getDistance(new Position(Map.getNaturalChoke().getCenter())) : p.getDistance(Map.getMainPosition());
                    tilesByPathDist.put(dist, t);
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

                for (Double key : tilesByPathDist.keySet()) {
                    TilePosition tile = tilesByPathDist.get(key);

                    // Protoss caps large pieces in the main at 12 if we don't have necessary medium pieces
                    if (Map.game.self().getRace() == Race.Protoss) {
                        if (largeCount > 0 && Map.mapBWEM.getMap().getArea(tile) == Map.getMainArea() && mainPieces.get(Piece.Large) >= 12 && mainPieces.get(Piece.Medium) < 10) {
                            continue;
                        }
                    }

                    // Zerg only need 4 medium pieces and 2 small piece
                    if (Map.game.self().getRace() == Race.Zerg) {
                        if ((mediumCount > 0 && mainPieces.get(Piece.Medium) >= 4)
                                || (smallCount > 0 && mainPieces.get(Piece.Small) >= 2)) {
                            continue;
                        }
                    }

                    // Terran only need about 20 depot spots
                    if (Map.game.self().getRace() == Race.Terran) {
                        if (mediumCount > 0 && mainPieces.get(Piece.Medium) >= 20) {
                            continue;
                        }
                    }

                    if (canAddBlock(tile, i, j)) {
                        insertBlock(tile, pieces);
                        totalMedium += mediumCount;
                        totalLarge += largeCount;

                        if (Map.mapBWEM.getMap().getArea(tile) == Map.getMainArea()) {
                            for (Piece piece : pieces) {
                                int tmp = mainPieces.get(piece) + 1;
                                mainPieces.put(piece, tmp);
                            }
                        }
                    }
                }
            }
        }
    }

    // Check if this block is in a good area
    private boolean goodArea(TilePosition t, List<TilePosition> enemyStartLocations, HashSet<Area> areasToAvoid) {
        for (TilePosition start : enemyStartLocations) {
            if (Map.mapBWEM.getMap().getArea(t) == Map.mapBWEM.getMap().getArea(start)) {
                return false;
            }
        }
        for (Area area : areasToAvoid) {
            if (Map.mapBWEM.getMap().getArea(t) == area) {
                return false;
            }
        }
        return true;
    }

    // Check if there's a blocking neutral between the positions to prevent bad pathing
    private boolean blockedPath(Position source, Position target) {
        for (ChokePoint choke : Map.mapBWEM.getMap().getPath(source, target)) {
            if (Map.isUsed(new TilePosition(choke.getCenter()), 1, 1) != UnitType.None){
                return true;
            }
        }
        return false;
    }

    void findProxyBlock() {
        // For base-specific locations, avoid all areas likely to be traversed by worker scouts
        HashSet<Area> areasToAvoid = new HashSet<>();
        for (TilePosition first : Map.mapBWEM.getMap().getStartingLocations()) {
            for (TilePosition second : Map.mapBWEM.getMap().getStartingLocations()) {
                if (first == second) {
                    continue;
                }

                for (ChokePoint choke : Map.mapBWEM.getMap().getPath(new Position(first), new Position(second))) {
                    areasToAvoid.add(choke.getAreas().getFirst());
                    areasToAvoid.add(choke.getAreas().getSecond());
                }
            }

            // Also add any areas that neighbour each start location
            Area baseArea = Map.mapBWEM.getMap().getNearestArea(first);
            areasToAvoid.addAll(baseArea.getAccessibleNeighbors());
        }

        // Gather the possible enemy start locations
        List<TilePosition> enemyStartLocations = new ArrayList<>();
        for (TilePosition start : Map.mapBWEM.getMap().getStartingLocations()) {
            if (Map.mapBWEM.getMap().getArea(start) != Map.getMainArea()){
                enemyStartLocations.add(start);
            }
        }

        // Find the best locations
        TilePosition tileBest = TilePosition.Invalid;
        double distBest = Double.MAX_VALUE;
        for (int x = 0; x < Map.game.mapWidth(); x++) {
            for (int y = 0; y < Map.game.mapHeight(); y++) {
                TilePosition topLeft = new TilePosition(x, y);
                TilePosition botRight = new TilePosition(x + 8, y + 5);

                if (!topLeft.isValid(Map.game)
                        || !botRight.isValid(Map.game)
                        || !canAddProxyBlock(topLeft, 8, 5)) {
                    continue;
                }

                Position blockCenter = new Position(topLeft.toPosition().x + 160, topLeft.toPosition().y + 96);

                // Consider each start location
                double dist = 0.0;
                for (TilePosition base : enemyStartLocations) {
                    Position baseCenter = new Position(base.toPosition().x + 64, base.toPosition().y + 48);
                    dist += Map.getGroundDistance(blockCenter, baseCenter);
                    if (blockedPath(blockCenter, baseCenter)) {
                        dist = Double.MAX_VALUE;
                        break;
                    }
                }

                // Bonus for placing in a good area
                if (goodArea(topLeft, enemyStartLocations, areasToAvoid) &&
                        goodArea(botRight, enemyStartLocations, areasToAvoid)) {
                    dist = Math.log(dist);
                }

                if (dist < distBest) {
                    distBest = dist;
                    tileBest = topLeft;
                }
            }
        }

        // Add the blocks
        if (canAddProxyBlock(tileBest, 8, 5)) {
            List<Piece> p = new ArrayList<>();
            p.add(Piece.Large);
            p.add(Piece.Large);
            p.add(Piece.Row);
            p.add(Piece.Small);
            p.add(Piece.Small);
            p.add(Piece.Small);
            p.add(Piece.Small);
            insertProxyBlock(tileBest, p);
        }
    }

    void eraseBlock(TilePosition here) {
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : allBlocks) {
            if (here.x >= block.getTilePosition().x && here.x < block.getTilePosition().x + block.width() &&
                    here.y >= block.getTilePosition().y && here.y < block.getTilePosition().y + block.height()) {
                blocksToRemove.add(block);
            }
        }
        for (Block block : blocksToRemove) {
            allBlocks.remove(block);
        }
    }

    public void findBlocks() {
        findMainDefenseBlock();
        findMainStartBlocks();
        findProxyBlock();
        findProductionBlocks();
    }

    public static void draw() {
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
