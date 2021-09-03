package bweb;

import bwapi.*;
import java.util.*;

public class Block {
	private int w = 0;
	private int h = 0;
	private TilePosition tile;
	private TreeSet<TilePosition> smallTiles;
	private TreeSet<TilePosition> mediumTiles;
	private TreeSet<TilePosition> largeTiles;
	private boolean proxy = false;
	private boolean defensive = false;

	public Block() {
		this.w = 0;
		this.h = 0;
	}

	public Block(TilePosition _tile, java.util.ArrayList<Piece> _pieces, boolean _proxy) {
		this(_tile, _pieces, _proxy, false);
	}
	public Block(TilePosition _tile, java.util.ArrayList<Piece> _pieces) {
		this(_tile, _pieces, false, false);
	}

	public Block(TilePosition _tile, ArrayList<Piece> _pieces, boolean _proxy, boolean _defensive) {
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
				if (Broodwar.game.self().getRace() == Race.Zerg && !Broodwar.game.canBuildHere(here, UnitType.Zerg_Hatchery)) {
					continue;
				}
				largeTiles.add(here);
				here = new TilePosition(here.x + 4, here.y);
				rowWidth += 4;
				rowHeight = Math.max(rowHeight, 3);
			}
			if (p == Piece.Addon) {
				smallTiles.add(new TilePosition(here.x, here.y + 1));
				here = new TilePosition(here.x + 2, here.y);
				rowWidth += 2;
				rowHeight = Math.max(rowHeight, 2);
			}
			if (p == Piece.Row) {
				w = Math.max(w, rowWidth);
				h += rowHeight;
				rowWidth = 0;
				rowHeight = 0;
				here = new TilePosition(tile.x, tile.y + h);
			}
		}

		// In case there is no row piece
		w = Math.max(w, rowWidth);
		h += rowHeight;
	}

	/// Returns the top left TilePosition of this Block.
	public final TilePosition getTilePosition() {
		return tile;
	}

	/// Returns the set of TilePositions that belong to 2x2 (small) buildings.
	public final TreeSet<TilePosition> getSmallTiles() {
		return smallTiles;
	}

	/// Returns the set of TilePositions that belong to 3x2 (medium) buildings.
	public final TreeSet<TilePosition> getMediumTiles() {
		return mediumTiles;
	}

	/// Returns the set of TilePositions that belong to 4x3 (large) buildings.
	public final TreeSet<TilePosition> getLargeTiles() {
		return largeTiles;
	}

	/// Returns true if this Block was generated for proxy usage.
	public final boolean isProxy() {
		return proxy;
	}

	/// Returns true if this Block was generated for defensive usage.
	public final boolean isDefensive() {
		return defensive;
	}

	/// Returns the width of the Block in TilePositions.
	public final int width() {
		return w;
	}

	/// Returns the height of the Block in TilePositions.
	public final int height() {
		return h;
	}

	/// Inserts a 2x2 (small) building at this location.
	public void insertSmall(final TilePosition here) {
		smallTiles.add(here);
	}

	/// Inserts a 3x2 (medium) building at this location.
	public void insertMedium(final TilePosition here) {
		mediumTiles.add(here);
	}

	/// Inserts a 4x3 (large) building at this location.
	public void insertLarge(final TilePosition here) {
		largeTiles.add(here);
	}

	/// Draws all the features of the Block.
	public final void draw() {
		Color color = Broodwar.game.self().getColor();
		Text textColor = color.id == 185 ? Text.DarkGreen: Broodwar.game.self().getTextColor();

		// Draw boxes around each feature
		for (TilePosition tile : smallTiles) {
			Broodwar.game.drawBoxMap(tile.toPosition(), new Position(tile.x + 65, tile.y + 65), color);
			Broodwar.game.drawTextMap(new Position(tile.x + 52, tile.y + 52), "%cB", textColor);
		}
		for (TilePosition tile : mediumTiles) {
			Broodwar.game.drawBoxMap(tile.toPosition(), new Position(tile.x + 97, tile.y + 65), color);
			Broodwar.game.drawTextMap(new Position(tile.x + 84, tile.y + 52), "%cB", textColor);
		}
		for (TilePosition tile : largeTiles) {
			Broodwar.game.drawBoxMap(tile.toPosition(), new Position(tile.x + 129, tile.y + 97), color);
			Broodwar.game.drawTextMap(new Position(tile.x + 116, tile.y + 84), "%cB", textColor);
		}
	}
}