package jbweb;

import bwapi.*;

import java.util.List;
import java.util.TreeSet;

public class Block {
    int w = 0;
    int h = 0;
    TilePosition tile;
    TreeSet<TilePosition> smallTiles;
    TreeSet<TilePosition> mediumTiles;
    TreeSet<TilePosition> largeTiles;
    boolean proxy = false;
    boolean defensive = false;

    Block() {}

    Block(TilePosition _tile, List<Piece> _pieces, boolean _proxy, boolean _defensive) {
        tile = _tile;
        proxy = _proxy;
        defensive = _defensive;

        // Arrange pieces
        int rowHeight = 0;
        int rowWidth = 0;
        TilePosition here = tile;
        for (Piece p : _pieces) {
            if (p == Piece.Small) {
                smallTiles.add(here);
                here = new TilePosition(here.x + 2, here.y);
                rowWidth += 2;
                rowHeight = Math.max(rowHeight, 2);
            }
            if (p == Piece.Medium) {
                mediumTiles.add(here);
                here = new TilePosition(here.x + 3, here.y);
                rowWidth += 3;
                rowHeight = Math.max(rowHeight, 2);
            }
            if (p == Piece.Large) {
                if (JBWEB.game.self().getRace() == Race.Zerg && !JBWEB.game.canBuildHere(here, UnitType.Zerg_Hatchery)) {
                    continue;
                }
                largeTiles.add(here);
                here = new TilePosition(here.x + 4, here.y);
                rowWidth += 4;
                rowHeight = Math.max(rowHeight, 3);
            }
            if (p == Piece.Addon) {
                TilePosition insertTile = new TilePosition(here.x, here.y + 1);
                smallTiles.add(insertTile);
                here = new TilePosition(here.x + 2, here.y);
                rowWidth += 2;
                rowHeight = Math.max(rowHeight, 2);
            }
            if (p == Piece.Row) {
                w = Math.max(w, rowWidth);
                h += rowHeight;
                rowWidth = 0;
                rowHeight = 0;
                here = new TilePosition(here.x, here.y + h);
            }
        }

        // In case there is no row piece
        w = Math.max(w, rowWidth);
        h += rowHeight;
    }

    public TilePosition getTilePosition() {
        return tile;
    }

    public TreeSet<TilePosition> getSmallTiles() {
        return smallTiles;
    }

    public TreeSet<TilePosition> getMediumTiles() {
        return mediumTiles;
    }

    public TreeSet<TilePosition> getLargeTiles() {
        return largeTiles;
    }

    public void insertSmall(TilePosition here) {
        smallTiles.add(here);
    }

    public void insertMedium(TilePosition here) {
        mediumTiles.add(here);
    }

    public void insertLarge(TilePosition here) {
        largeTiles.add(here);
    }

    public int width() {
        return w;
    }

    public int height() {
        return h;
    }

    void draw() {
        Color color = JBWEB.game.self().getColor();
        Text textColor = color.id == 185 ? textColor = Text.DarkGreen : JBWEB.game.self().getTextColor();

        // Draw boxes around each feature
        for (TilePosition tile : smallTiles) {
            Position p1 = new Position(tile.toPosition().x + 65, tile.toPosition().x + 65);
            Position p2 = new Position(tile.toPosition().x + 52, tile.toPosition().x + 52);
            JBWEB.game.drawBoxMap(new Position(tile), p1, color);
            JBWEB.game.drawTextMap(p2, "%cB", textColor);
        }
        for (TilePosition tile : mediumTiles) {
            Position p1 = new Position(tile.toPosition().x + 97, tile.toPosition().x + 65);
            Position p2 = new Position(tile.toPosition().x + 84, tile.toPosition().x + 52);
            JBWEB.game.drawBoxMap(new Position(tile), p1, color);
            JBWEB.game.drawTextMap(p2, "%cB", textColor);
        }
        for (TilePosition tile : largeTiles) {
            Position p1 = new Position(tile.toPosition().x + 129, tile.toPosition().x + 97);
            Position p2 = new Position(tile.toPosition().x + 116, tile.toPosition().x + 84);
            JBWEB.game.drawBoxMap(new Position(tile), p1, color);
            JBWEB.game.drawTextMap(p2, "%cB", textColor);
        }
    }
}
