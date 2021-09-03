package bweb;

import bwapi.*;
import bwem.*;
import java.util.*;

public class Station {
	private final Base base;
	private TreeSet<TilePosition> defenses;
	private Position resourceCentroid;
	private boolean main;
	private boolean natural;

	public Station(Position _resourceCentroid, TreeSet<TilePosition> _defenses, Base _base, boolean _main, boolean _natural) {
		resourceCentroid = _resourceCentroid;
		defenses = new TreeSet<TilePosition>(_defenses);
		base = _base;
		main = _main;
		natural = _natural;
	}

	/// Returns the central position of the resources associated with this base including geysers.
	public final Position getResourceCentroid() {
		return resourceCentroid;
	}

	/// Returns the set of defense locations associated with this base.
	public final TreeSet<TilePosition> getDefenseLocations() {
		return defenses;
	}

	/// Returns the BWEM base associated with this BWEB base.
	public final Base getBWEMBase() {
		return base;
	}

	/// Returns the number of ground defenses associated with this Station.
	public final int getGroundDefenseCount() {
		int count = 0;
		for (TilePosition defense : defenses) {
			UnitType type = map.isUsed(defense);
			if (type == UnitType.Protoss_Photon_Cannon || type == UnitType.Zerg_Sunken_Colony || type == UnitType.Terran_Bunker) {
				count++;
			}
		}
		return count;
	}

	/// Returns the number of air defenses associated with this Station.
	public final int getAirDefenseCount() {
		int count = 0;
		for (TilePosition defense : defenses) {
			UnitType type = map.isUsed(defense);
			if (type == UnitType.Protoss_Photon_Cannon || type == UnitType.Zerg_Spore_Colony || type == UnitType.Terran_Missile_Turret) {
				count++;
			}
		}
		return count;
	}

	/// Returns true if the Station is a main Station.
	public final boolean isMain() {
		return main;
	}

	/// Returns true if the Station is a natural Station.
	public final boolean isNatural() {
		return natural;
	}

	/// Draws all the features of the Station.
	public final void draw() {
		Color color = Broodwar.game.self().getColor();
		Text textColor = color.id == 185 ? Text.DarkGreen: Broodwar.game.self().getTextColor();

		// Draw boxes around each feature
		for (TilePosition tile : defenses) {
			Broodwar.game.drawBoxMap(Position(tile), Position(tile) + Position(65, 65), color);
			Broodwar.game.drawTextMap(Position(tile) + Position(4, 52), "%cS", textColor);
		}
		Broodwar.game.drawBoxMap(Position(base.Location()), Position(base.Location()) + Position(129, 97), color);
	}

	public boolean equalsTo (Station s) {
		return base == s.getBWEMBase();
	}
}