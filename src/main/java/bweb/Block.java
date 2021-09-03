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
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above:
//ORIGINAL LINE: Block(BWAPI::TilePosition _tile, java.util.ArrayList<Piece> _pieces, boolean _proxy = false, boolean _defensive = false)
	public Block(TilePosition _tile, ArrayList<Piece> _pieces, boolean _proxy, boolean _defensive) {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: tile = _tile;
		tile = _tile;
		proxy = _proxy;
		defensive = _defensive;

		// Arrange pieces
		int rowHeight = 0;
		int rowWidth = 0;
		TilePosition here = tile;
		for (var p : _pieces) {
			if (p == Piece.Small) {
				smallTiles.add(here);
				here += BWAPI.TilePosition(2, 0);
				rowWidth += 2;
				rowHeight = Math.max(rowHeight, 2);
			}
			if (p == Piece.Medium) {
				mediumTiles.add(here);
				here += BWAPI.TilePosition(3, 0);
				rowWidth += 3;
				rowHeight = Math.max(rowHeight, 2);
			}
			if (p == Piece.Large) {
				if (Broodwar.self().getRace() == Races.Zerg && !Broodwar.canBuildHere(here, UnitType.Zerg_Hatchery)) {
					continue;
				}
				largeTiles.add(here);
				here += new TilePosition(4, 0);
				rowWidth += 4;
				rowHeight = Math.max(rowHeight, 3);
			}
			if (p == Piece.Addon) {
				smallTiles.add(here + new TilePosition(0, 1));
				here += new TilePosition(2, 0);
				rowWidth += 2;
				rowHeight = Math.max(rowHeight, 2);
			}
			if (p == Piece.Row) {
				w = Math.max(w, rowWidth);
				h += rowHeight;
				rowWidth = 0;
				rowHeight = 0;
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: here = tile + BWAPI::TilePosition(0, h);
				here = tile + new TilePosition(0, h);
			}
		}

		// In case there is no row piece
		w = Math.max(w, rowWidth);
		h += rowHeight;
	}

	/// <summary> Returns the top left TilePosition of this Block. </summary>
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: BWAPI::TilePosition getTilePosition() const
	public final TilePosition getTilePosition() {
		return tile;
	}

	/// <summary> Returns the set of TilePositions that belong to 2x2 (small) buildings. </summary>
	public final TreeSet<TilePosition> getSmallTiles() {
		return smallTiles;
	}

	/// <summary> Returns the set of TilePositions that belong to 3x2 (medium) buildings. </summary>
	public final TreeSet<TilePosition> getMediumTiles() {
		return mediumTiles;
	}

	/// <summary> Returns the set of TilePositions that belong to 4x3 (large) buildings. </summary>
	public final TreeSet<TilePosition> getLargeTiles() {
		return largeTiles;
	}

	/// <summary> Returns true if this Block was generated for proxy usage. </summary>
	public final boolean isProxy() {
		return proxy;
	}

	/// <summary> Returns true if this Block was generated for defensive usage. </summary>
	public final boolean isDefensive() {
		return defensive;
	}

	/// <summary> Returns the width of the Block in TilePositions. </summary>
	public final int width() {
		return w;
	}

	/// <summary> Returns the height of the Block in TilePositions. </summary>
	public final int height() {
		return h;
	}

	/// <summary> Inserts a 2x2 (small) building at this location. </summary>
	public void insertSmall(final TilePosition here) {
		smallTiles.add(here);
	}

	/// <summary> Inserts a 3x2 (medium) building at this location. </summary>
	public void insertMedium(final TilePosition here) {
		mediumTiles.add(here);
	}

	/// <summary> Inserts a 4x3 (large) building at this location. </summary>
	public void insertLarge(final TilePosition here) {
		largeTiles.add(here);
	}

	/// <summary> Draws all the features of the Block. </summary>
	public final void draw() {
		int color = Broodwar.self().getColor();
		int textColor = color == 185 ? textColor = Text.DarkGreen : Broodwar.self().getTextColor();

		// Draw boxes around each feature
		for (var tile : smallTiles) {
			Broodwar.drawBoxMap(Position(tile), Position(tile) + Position(65, 65), color);
			Broodwar.drawTextMap(Position(tile) + Position(52, 52), "%cB", textColor);
		}
		for (var tile : mediumTiles) {
			Broodwar.drawBoxMap(Position(tile), Position(tile) + Position(97, 65), color);
			Broodwar.drawTextMap(Position(tile) + Position(84, 52), "%cB", textColor);
		}
		for (var tile : largeTiles) {
			Broodwar.drawBoxMap(Position(tile), Position(tile) + Position(129, 97), color);
			Broodwar.drawTextMap(Position(tile) + Position(116, 84), "%cB", textColor);
		}
	}
}