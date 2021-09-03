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
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: resourceCentroid = _resourceCentroid;
		resourceCentroid = _resourceCentroid;
		defenses = new TreeSet<TilePosition>(_defenses);
		base = _base;
		main = _main;
		natural = _natural;
	}

	/// <summary> Returns the central position of the resources associated with this base including geysers. </summary>
	public final Position getResourceCentroid() {
		return resourceCentroid;
	}

	/// <summary> Returns the set of defense locations associated with this base. </summary>
	public final TreeSet<TilePosition> getDefenseLocations() {
		return defenses;
	}

	/// <summary> Returns the BWEM base associated with this BWEB base. </summary>
	public final Base getBWEMBase() {
		return base;
	}

	/// <summary> Returns the number of ground defenses associated with this Station. </summary>
	public final int getGroundDefenseCount() {
		int count = 0;
		for (var defense : defenses) {
			UnitType type = map.isUsed(defense);
			if (type == UnitType.Protoss_Photon_Cannon || type == UnitType.Zerg_Sunken_Colony || type == UnitType.Terran_Bunker) {
				count++;
			}
		}
		return count;
	}

	/// <summary> Returns the number of air defenses associated with this Station. </summary>
	public final int getAirDefenseCount() {
		int count = 0;
		for (var defense : defenses) {
			UnitType type = map.isUsed(defense);
			if (type == UnitType.Protoss_Photon_Cannon || type == UnitType.Zerg_Spore_Colony || type == UnitType.Terran_Missile_Turret) {
				count++;
			}
		}
		return count;
	}

	/// <summary> Returns true if the Station is a main Station. </summary>
	public final boolean isMain() {
		return main;
	}

	/// <summary> Returns true if the Station is a natural Station. </summary>
	public final boolean isNatural() {
		return natural;
	}

	/// <summary> Draws all the features of the Station. </summary>
	public final void draw() {
		int color = Broodwar.self().getColor();
		int textColor = color == 185 ? textColor = Text.DarkGreen : Broodwar.self().getTextColor();

		// Draw boxes around each feature
		for (var tile : defenses) {
			Broodwar.drawBoxMap(Position(tile), Position(tile) + Position(65, 65), color);
			Broodwar.drawTextMap(Position(tile) + Position(4, 52), "%cS", textColor);
		}
		Broodwar.drawBoxMap(Position(base.Location()), Position(base.Location()) + Position(129, 97), color);
	}

	public boolean equalsTo (Station s) {
		return base == s.getBWEMBase();
	}
}