package bweb;

import BWAPI.*;
import java.util.*;

public class Wall
{
	private BWAPI.UnitType tightType = new BWAPI.UnitType();
	private BWAPI.Position centroid = new BWAPI.Position();
	private BWAPI.TilePosition opening = new BWAPI.TilePosition();
	private BWAPI.TilePosition initialPathStart = new BWAPI.TilePosition();
	private BWAPI.TilePosition initialPathEnd = new BWAPI.TilePosition();
	private BWAPI.TilePosition pathStart = new BWAPI.TilePosition();
	private BWAPI.TilePosition pathEnd = new BWAPI.TilePosition();
	private BWAPI.TilePosition creationStart = new BWAPI.TilePosition();
	private TreeSet<BWAPI.TilePosition> defenses = new TreeSet<BWAPI.TilePosition>();
	private TreeSet<BWAPI.TilePosition> smallTiles = new TreeSet<BWAPI.TilePosition>();
	private TreeSet<BWAPI.TilePosition> mediumTiles = new TreeSet<BWAPI.TilePosition>();
	private TreeSet<BWAPI.TilePosition> largeTiles = new TreeSet<BWAPI.TilePosition>();
	private TreeSet<BWAPI.Position> notableLocations = new TreeSet<BWAPI.Position>();
	private Iterator<BWAPI.UnitType> typeIterator;
	private ArrayList<BWAPI.UnitType> rawBuildings = new ArrayList<BWAPI.UnitType>();
	private ArrayList<BWAPI.UnitType> rawDefenses = new ArrayList<BWAPI.UnitType>();
	private final ArrayList<BWEM.Area > accessibleNeighbors = new ArrayList<BWEM.Area >();
	private TreeMap<BWAPI.TilePosition, BWAPI.UnitType> currentLayout = new TreeMap<BWAPI.TilePosition, BWAPI.UnitType>();
	private TreeMap<BWAPI.TilePosition, BWAPI.UnitType> bestLayout = new TreeMap<BWAPI.TilePosition, BWAPI.UnitType>();
	private final BWEM.Area area;
	private final BWEM.ChokePoint choke;
	private final BWEM.Base base;
	private double chokeAngle;
	private double bestWallScore;
	private double jpsDist;
	private boolean pylonWall;
	private boolean openWall;
	private boolean requireTight;
	private boolean movedStart;
	private boolean pylonWallPiece;
	private boolean allowLifted;
	private boolean flatRamp;
	private bweb.Station closestStation;

	private Position findCentroid()
	{
		// Create current centroid using all buildings except Pylons
		var currentCentroid = Position(0, 0);
		var sizeWall = rawBuildings.size();
//C++ TO JAVA CONVERTER NOTE: 'auto' variable declarations are not supported in Java:
//ORIGINAL LINE: for (auto &[tile, type] : bestLayout)
		for ([,] : bestLayout)
		{
			if (type != UnitTypes.GlobalMembers.Protoss_Pylon)
			{
				currentCentroid += Position(tile) + Position(type.tileSize()) / 2;
			}
			else
			{
				sizeWall--;
			}
		}

		// Create a centroid if we only have a Pylon wall
		if (sizeWall == 0)
		{
			sizeWall = bestLayout.size();
//C++ TO JAVA CONVERTER NOTE: 'auto' variable declarations are not supported in Java:
//ORIGINAL LINE: for (auto &[tile, type] : bestLayout)
			for ([,] : bestLayout)
			{
				currentCentroid += Position(tile) + Position(type.tileSize()) / 2;
			}
		}
		return (currentCentroid / sizeWall);
	}

	private TilePosition findOpening()
	{
		if (!openWall)
		{
			return TilePositions.GlobalMembers.Invalid;
		}

		// Set any tiles on the path as reserved so we don't build on them
		var currentPath = findPathOut();
		var currentOpening = TilePositions.GlobalMembers.Invalid;

		// Check which tile is closest to each part on the path, set as opening
		var distBest = Double.MAX_VALUE;
		for (var pathTile : currentPath.getTiles())
		{
			final var closestChokeGeo = map.getClosestChokeTile(choke, Position(pathTile));
			final var dist = closestChokeGeo.getDistance(Position(pathTile));
			final var centerPath = Position(pathTile) + Position(16, 16);

			var angleOkay = true;
			var distOkay = false;

			// Check if the angle and distance is okay
//C++ TO JAVA CONVERTER NOTE: 'auto' variable declarations are not supported in Java:
//ORIGINAL LINE: for (auto &[tileLayout, typeLayout] : currentLayout)
			for ([,] : currentLayout)
			{
				if (typeLayout == UnitTypes.GlobalMembers.Protoss_Pylon)
				{
					continue;
				}

				final var centerPiece = Position(tileLayout) + Position(typeLayout.tileWidth() * 16, typeLayout.tileHeight() * 16);
				final var openingAngle = map.getAngle(new tangible.Pair<auto, auto>(centerPiece, centerPath));
				final var openingDist = centerPiece.getDistance(centerPath);

				if (Math.abs(chokeAngle - openingAngle) > 35.0)
				{
					angleOkay = false;
				}
				if (openingDist < 320.0)
				{
					distOkay = true;
				}
			}
			if (distOkay && angleOkay && BWAPI_ext.GlobalMembers.dist < distBest)
			{
				distBest = BWAPI_ext.GlobalMembers.dist;
				currentOpening = pathTile;
			}
		}

		// If we don't have an opening, assign closest path tile to wall centroid as opening
		if (!currentOpening.isValid())
		{
			for (var pathTile : currentPath.getTiles())
			{
				final var p = Position(pathTile);
				final var dist = centroid.getDistance(p);
				if (BWAPI_ext.GlobalMembers.dist < distBest)
				{
					distBest = BWAPI_ext.GlobalMembers.dist;
					currentOpening = pathTile;
				}
			}
		}

		return new auto(currentOpening);
	}

	private Path findPathOut()
	{
		// Check that the path points are possible to reach
		checkPathPoints();
		final var startCenter = Position(pathStart) + Position(16, 16);
		final var endCenter = Position(pathEnd) + Position(16, 16);

		// Get a new path
		bweb.Path newPath = new bweb.Path();
		allowLifted = false;
		newPath.bfsPath(new auto(endCenter), new auto(startCenter), (t) ->
		{
			return this.wallWalkable(t);
		}, false);
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return newPath;
		return new bweb.Path(newPath);
	}

	private boolean powerCheck(final UnitType type, final TilePosition here)
	{
		if (type != UnitTypes.GlobalMembers.Protoss_Pylon || pylonWall)
		{
			return true;
		}

		// TODO: Create a generic BWEB function that takes 2 tiles and tells you if the 1st tile will power the 2nd tile
//C++ TO JAVA CONVERTER NOTE: 'auto' variable declarations are not supported in Java:
//ORIGINAL LINE: for (auto &[tileLayout, typeLayout] : currentLayout)
		for ([,] : currentLayout)
		{
			if (typeLayout == UnitTypes.GlobalMembers.Protoss_Pylon)
			{
				continue;
			}

			if (typeLayout.tileWidth() == 4)
			{
				var powersThis = false;
				if (tileLayout.y - here.y == -5 || tileLayout.y - here.y == 4)
				{
					if (tileLayout.x - here.x >= -4 && tileLayout.x - here.x <= 1)
					{
						powersThis = true;
					}
				}
				if (tileLayout.y - here.y == -4 || tileLayout.y - here.y == 3)
				{
					if (tileLayout.x - here.x >= -7 && tileLayout.x - here.x <= 4)
					{
						powersThis = true;
					}
				}
				if (tileLayout.y - here.y == -3 || tileLayout.y - here.y == 2)
				{
					if (tileLayout.x - here.x >= -8 && tileLayout.x - here.x <= 5)
					{
						powersThis = true;
					}
				}
				if (tileLayout.y - here.y >= -2 && tileLayout.y - here.y <= 1)
				{
					if (tileLayout.x - here.x >= -8 && tileLayout.x - here.x <= 6)
					{
						powersThis = true;
					}
				}
				if (!powersThis)
				{
					return false;
				}
			}
			else
			{
				var powersThis = false;
				if (tileLayout.y - here.y == 4)
				{
					if (tileLayout.x - here.x >= -3 && tileLayout.x - here.x <= 2)
					{
						powersThis = true;
					}
				}
				if (tileLayout.y - here.y == -4 || tileLayout.y - here.y == 3)
				{
					if (tileLayout.x - here.x >= -6 && tileLayout.x - here.x <= 5)
					{
						powersThis = true;
					}
				}
				if (tileLayout.y - here.y >= -3 && tileLayout.y - here.y <= 2)
				{
					if (tileLayout.x - here.x >= -7 && tileLayout.x - here.x <= 6)
					{
						powersThis = true;
					}
				}
				if (!powersThis)
				{
					return false;
				}
			}
		}
		return true;
	}

	private boolean angleCheck(final UnitType type, final TilePosition here)
	{
		final var centerHere = Position(here) + Position(type.tileWidth() * 16, type.tileHeight() * 16);

		// If we want a closed wall, we don't care the angle of the buildings
		if (!openWall || (type == UnitTypes.GlobalMembers.Protoss_Pylon && !pylonWall && !pylonWallPiece))
		{
			return true;
		}

		// Check if the angle is okay between all pieces in the current layout
//C++ TO JAVA CONVERTER NOTE: 'auto' variable declarations are not supported in Java:
//ORIGINAL LINE: for (auto &[tileLayout, typeLayout] : currentLayout)
		for ([,] : currentLayout)
		{
			if (typeLayout == UnitTypes.GlobalMembers.Protoss_Pylon)
			{
				continue;
			}

			final var centerPiece = Position(tileLayout) + Position(typeLayout.tileWidth() * 16, typeLayout.tileHeight() * 16);
			final var wallAngle = map.getAngle(new tangible.Pair<auto, auto>(centerPiece, centerHere));

			if (Math.abs(chokeAngle - wallAngle) > 20.0)
			{
				return false;
			}
		}
		return true;
	}

	private boolean placeCheck(final UnitType type, final TilePosition here)
	{
		// Allow Pylon to overlap station defenses
		if (type == UnitTypes.GlobalMembers.Protoss_Pylon)
		{
			if (closestStation != null && closestStation.getDefenseLocations().find(here) != closestStation.getDefenseLocations().end())
			{
				return true;
			}
		}

		// Check if placement is valid
		if (map.isReserved(here, type.tileWidth(), type.tileHeight()) || !map.isPlaceable(type, here) || (!openWall && map.tilesWithinArea(area, here, type.tileWidth(), type.tileHeight()) == 0) || (openWall && map.tilesWithinArea(area, here, type.tileWidth(), type.tileHeight()) == 0 && (type == UnitTypes.GlobalMembers.Protoss_Pylon || (map.mapBWEM.GetArea(here) && choke.GetAreas().first != map.mapBWEM.GetArea(here) && choke.GetAreas().second != map.mapBWEM.GetArea(here)))))
		{
			return false;
		}
		return true;
	}

	private boolean tightCheck(final UnitType type, final TilePosition here)
	{
		// If this is a powering pylon and we are not making a pylon wall, we don't care if it's tight
		if (type == UnitTypes.GlobalMembers.Protoss_Pylon && !pylonWall && !pylonWallPiece)
		{
			return true;
		}

		// Dimensions of current buildings UnitType
		final var dimL = (type.tileWidth() * 16) - type.dimensionLeft();
		final var dimR = (type.tileWidth() * 16) - type.dimensionRight() - 1;
		final var dimU = (type.tileHeight() * 16) - type.dimensionUp();
		final var dimD = (type.tileHeight() * 16) - type.dimensionDown() - 1;
		final var walkHeight = type.tileHeight() * 4;
		final var walkWidth = type.tileWidth() * 4;

		// Dimension of UnitType to check tightness for
		final var vertTight = (tightType == UnitTypes.GlobalMembers.None) ? 32 : tightType.height();
		final var horizTight = (tightType == UnitTypes.GlobalMembers.None) ? 32 : tightType.width();

		// Checks each side of the building to see if it is valid for walling purposes
		final var checkL = dimL < horizTight;
		final var checkR = dimR < horizTight;
		final var checkU = dimU < vertTight;
		final var checkD = dimD < vertTight;

		// Figures out how many extra tiles we can check tightness for
		final var extraL = pylonWall || !requireTight ? 0 : Math.max(0, (horizTight - dimL) / 8);
		final var extraR = pylonWall || !requireTight ? 0 : Math.max(0, (horizTight - dimR) / 8);
		final var extraU = pylonWall || !requireTight ? 0 : Math.max(0, (vertTight - dimU) / 8);
		final var extraD = pylonWall || !requireTight ? 0 : Math.max(0, (vertTight - dimD) / 8);

		// Setup boundary WalkPositions to check for tightness
		final var left = WalkPosition(here) - WalkPosition(1 + extraL, 0);
		final var right = WalkPosition(here) + WalkPosition(walkWidth + extraR, 0);
		final var up = WalkPosition(here) - WalkPosition(0, 1 + extraU);
		final var down = WalkPosition(here) + WalkPosition(0, walkHeight + extraD);

		// Used for determining if the tightness we found is suitable
		final var firstBuilding = currentLayout.size() == 0;
		final var lastBuilding = currentLayout.size() == (rawBuildings.size() - 1);
		var terrainTight = false;
		var parentTight = false;
		var p1Tight = 0;
		var p2Tight = 0;

		// Functions for each dimension check
		final var gapRight = (UnitType parent) ->
		{
			return (parent.tileWidth() * 16) - parent.dimensionLeft() + dimR;
		};
		final var gapLeft = (UnitType parent) ->
		{
			return (parent.tileWidth() * 16) - parent.dimensionRight() - 1 + dimL;
		};
		final var gapUp = (UnitType parent) ->
		{
			return (parent.tileHeight() * 16) - parent.dimensionDown() - 1 + dimU;
		};
		final var gapDown = (UnitType parent) ->
		{
			return (parent.tileHeight() * 16) - parent.dimensionUp() + dimD;
		};

		// Check if the building is terrain tight when placed here
		final var terrainTightCheck = (WalkPosition w, boolean check) ->
		{
			final var t = TilePosition(w);

			// If the walkposition is invalid or unwalkable
			if (tightType != UnitTypes.GlobalMembers.None && check && (!w.isValid() || !Broodwar.isWalkable(w)))
			{
				return true;
			}

			// If we don't care about walling tight and the tile isn't walkable
			if (!requireTight && !map.isWalkable(t))
			{
				return true;
			}

			// If there's a mineral field or geyser here
			if (map.isUsed(t).isResourceContainer())
			{
				return true;
			}
			return false;
		};

		// Iterate vertical tiles adjacent of this placement
		final var checkVerticalSide = (WalkPosition start, boolean check, gap) ->
		{
			for (var x = start.x - 1; x < start.x + walkWidth + 1; x++)
			{
				WalkPosition w = new WalkPosition(x, start.y);
				final var t = TilePosition(w);
				final var parent = map.isUsed(t);
				final var leftCorner = x < start.x;
				final var rightCorner = x >= start.x + walkWidth;

				// If this is a corner
				if (leftCorner || rightCorner)
				{

					// Check if it's tight with the terrain
					if (!terrainTight && terrainTightCheck(w, check) && leftCorner ? terrainTightCheck(w, checkL) : terrainTightCheck(w, checkR))
					{
						terrainTight = true;
					}

					// Check if it's tight with a parent
					if (!parentTight && find(rawBuildings.iterator(), rawBuildings.end(), parent) != rawBuildings.end() && (!requireTight || (gap(parent) < vertTight && (leftCorner ? gapLeft(parent) < horizTight : gapRight(parent) < horizTight))))
					{
						parentTight = true;
					}
				}
				else
				{

					// Check if it's tight with the terrain
					if (!terrainTight && terrainTightCheck(w, check))
					{
						terrainTight = true;
					}

					// Check if it's tight with a parent
					if (!parentTight && find(rawBuildings.iterator(), rawBuildings.end(), parent) != rawBuildings.end() && (!requireTight || gap(parent) < vertTight))
					{
						parentTight = true;
					}
				}

				// Check to see which node it is closest to (0 is don't check, 1 is not tight, 2 is tight)
				if (!openWall && !map.isWalkable(t) && w.getDistance(choke.Center()) < 4)
				{
					if (w.getDistance(choke.Pos(choke.end1)) < w.getDistance(choke.Pos(choke.end2)))
					{
						if (p1Tight == 0)
						{
							p1Tight = 1;
						}
						if (terrainTight)
						{
							p1Tight = 2;
						}
					}
					else if (p2Tight == 0)
					{
						if (p2Tight == 0)
						{
							p2Tight = 1;
						}
						if (terrainTight)
						{
							p2Tight = 2;
						}
					}
				}
			}
		};

		// Iterate horizontal tiles adjacent of this placement
		final var checkHorizontalSide = (WalkPosition start, boolean check, gap) ->
		{
			for (var y = start.y - 1; y < start.y + walkHeight + 1; y++)
			{
				WalkPosition w = new WalkPosition(start.x, y);
				final var t = TilePosition(w);
				final var parent = map.isUsed(t);
				final var topCorner = y < start.y;
				final var downCorner = y >= start.y + walkHeight;

				// If this is a corner
				if (topCorner || downCorner)
				{

					// Check if it's tight with the terrain
					if (!terrainTight && terrainTightCheck(w, check) && topCorner ? terrainTightCheck(w, checkU) : terrainTightCheck(w, checkD))
					{
						terrainTight = true;
					}

					// Check if it's tight with a parent
					if (!parentTight && find(rawBuildings.iterator(), rawBuildings.end(), parent) != rawBuildings.end() && (!requireTight || (gap(parent) < horizTight && (topCorner ? gapUp(parent) < vertTight : gapDown(parent) < vertTight))))
					{
						parentTight = true;
					}
				}
				else
				{

					// Check if it's tight with the terrain
					if (!terrainTight && terrainTightCheck(w, check))
					{
						terrainTight = true;
					}

					// Check if it's tight with a parent
					if (!parentTight && find(rawBuildings.iterator(), rawBuildings.end(), parent) != rawBuildings.end() && (!requireTight || gap(parent) < horizTight))
					{
						parentTight = true;
					}
				}

				// Check to see which node it is closest to (0 is don't check, 1 is not tight, 2 is tight)
				if (!openWall && !map.isWalkable(t) && w.getDistance(choke.Center()) < 4)
				{
					if (w.getDistance(choke.Pos(choke.end1)) < w.getDistance(choke.Pos(choke.end2)))
					{
						if (p1Tight == 0)
						{
							p1Tight = 1;
						}
						if (terrainTight)
						{
							p1Tight = 2;
						}
					}
					else if (p2Tight == 0)
					{
						if (p2Tight == 0)
						{
							p2Tight = 1;
						}
						if (terrainTight)
						{
							p2Tight = 2;
						}
					}
				}
			}
		};

		// For each side, check if it's terrain tight or tight with any adjacent buildings
		checkVerticalSide(up, checkU, gapUp);
		checkVerticalSide(down, checkD, gapDown);
		checkHorizontalSide(left, checkL, gapLeft);
		checkHorizontalSide(right, checkR, gapRight);

		// If we want a closed wall, we need all buildings to be tight at the tightness resolution...
		if (!openWall)
		{
			if (!lastBuilding && !firstBuilding) // ...to the parent if not first building
			{
				return parentTight;
			}
			if (firstBuilding) // ...to the terrain if first building
			{
				return terrainTight && p1Tight != 1 && p2Tight != 1;
			}
			if (lastBuilding) // ...to the parent and terrain if last building
			{
				return terrainTight && parentTight && p1Tight != 1 && p2Tight != 1;
			}
		}

		// If we want an open wall, we need this building to be tight at tile resolution to a parent or terrain
		else if (openWall)
		{
			return (terrainTight || parentTight);
		}
		return false;
	}

	private boolean spawnCheck(final UnitType type, final TilePosition here)
	{
		// TODO: Check if units spawn in bad spots, just returns true for now
		checkPathPoints();
		final var startCenter = Position(pathStart) + Position(16, 16);
		final var endCenter = Position(pathEnd) + Position(16, 16);
		Path pathOut = new Path();
		return true;
	}

	private boolean wallWalkable(final TilePosition tile)
	{
		// Checks for any collision and inverts the return value
		if (!tile.isValid() || (map.mapBWEM.GetArea(tile) && map.mapBWEM.GetArea(tile) != area && find(accessibleNeighbors.iterator(), accessibleNeighbors.end(), map.mapBWEM.GetArea(tile)) == accessibleNeighbors.end()) || map.isReserved(tile) || !map.isWalkable(tile) || (allowLifted && map.isUsed(tile) != UnitTypes.GlobalMembers.Terran_Barracks && map.isUsed(tile) != UnitTypes.GlobalMembers.None) || (!allowLifted && map.isUsed(tile) != UnitTypes.GlobalMembers.None && map.isUsed(tile) != UnitTypes.GlobalMembers.Zerg_Larva) || (openWall && (tile).getDistance(pathEnd) - 64.0 > jpsDist / 32))
		{
			return false;
		}
		return true;
	}

	private void initialize()
	{
		// Clear failed counters
		GlobalMembers.failedPlacement = 0;
		GlobalMembers.failedAngle = 0;
		GlobalMembers.failedPath = 0;
		GlobalMembers.failedTight = 0;
		GlobalMembers.failedSpawn = 0;
		GlobalMembers.failedPower = 0;

		// Set BWAPI::Points to invalid (default constructor is None)
		centroid = BWAPI.Positions.Invalid;
		opening = BWAPI.TilePositions.Invalid;
		pathStart = BWAPI.TilePositions.Invalid;
		pathEnd = BWAPI.TilePositions.Invalid;
		initialPathStart = BWAPI.TilePositions.Invalid;
		initialPathEnd = BWAPI.TilePositions.Invalid;

		// Set important terrain features
		bestWallScore = 0;
		accessibleNeighbors = new ArrayList<BWEM.Area >(area.AccessibleNeighbours());
		chokeAngle = map.getAngle(std::make_pair(Position(choke.Pos(choke.end1)) + Position(4, 4), Position(choke.Pos(choke.end2)) + Position(4, 4)));
		pylonWall = count(rawBuildings.iterator(), rawBuildings.end(), BWAPI.UnitTypes.Protoss_Pylon) > 1;
		creationStart = TilePosition(choke.Center());
		base = !area.Bases().isEmpty() ? area.Bases().get(0) : null;
		flatRamp = Broodwar.isBuildable(TilePosition(choke.Center()));
		closestStation = stations.getClosestStation(TilePosition(choke.Center()));

		// Check if a Pylon should be put in the wall to help the size of the Wall or away from the wall for protection
		var p1 = choke.Pos(choke.end1);
		var p2 = choke.Pos(choke.end2);
		pylonWallPiece = Math.abs(p1.x - p2.x) * 8 >= 320 || Math.abs(p1.y - p2.y) * 8 >= 256 || p1.getDistance(p2) * 8 >= 288;

		// Create a jps path for limiting BFS exploration using the distance of the jps path
		Path jpsPath = new Path();
		initializePathPoints();
		checkPathPoints();
		jpsPath.createUnitPath(Position(pathStart), Position(pathEnd));
		jpsDist = jpsPath.getDistance();

		// If we can't reach the end/start points, the Wall is likely not possible and won't be attempted
		if (!jpsPath.isReachable())
		{
			return;
		}

		// Create notable locations to keep Wall pieces within proxmity of
		if (base != null)
		{
			notableLocations = {base.Center(), Position(initialPathStart) + Position(16,16), (base.Center() + Position(initialPathStart)) / 2};
		}
		else
		{
			notableLocations = {Position(initialPathStart) + Position(16,16), Position(initialPathEnd) + Position(16,16)};
		}

		// Sort all the pieces and iterate over them to find the best wall - by Hannes
		if (find(rawBuildings.iterator(), rawBuildings.end(), UnitTypes.GlobalMembers.Protoss_Pylon) != rawBuildings.end())
		{
			sort(rawBuildings.iterator(), rawBuildings.end(), (UnitType l, UnitType r) ->
			{
				return (l == UnitTypes.GlobalMembers.Protoss_Pylon) < (r == UnitTypes.GlobalMembers.Protoss_Pylon);
			}); // Moves pylons to end
			sort(rawBuildings.iterator(), find(rawBuildings.iterator(), rawBuildings.end(), UnitTypes.GlobalMembers.Protoss_Pylon)); // Sorts everything before pylons
		}
		else if (find(rawBuildings.iterator(), rawBuildings.end(), UnitTypes.GlobalMembers.Zerg_Hatchery) != rawBuildings.end())
		{
			sort(rawBuildings.iterator(), rawBuildings.end(), (UnitType l, UnitType r) ->
			{
				return (l == UnitTypes.GlobalMembers.Zerg_Hatchery) > (r == UnitTypes.GlobalMembers.Zerg_Hatchery);
			}); // Moves hatchery to start
			sort(find(rawBuildings.iterator(), rawBuildings.end(), UnitTypes.GlobalMembers.Zerg_Hatchery), rawBuildings.iterator()); // Sorts everything after hatchery
		}
		else
		{
			Collections.sort(rawBuildings);
		}

		// If there is a base in this area and we're creating an open wall, move creation start within 10 tiles of it
		if (openWall && base != null)
		{
			var startCenter = Position(creationStart) + Position(16, 16);
			var distBest = Double.MAX_VALUE;
			var moveTowards = (Position(initialPathStart) + base.Center()) / 2;

			// Iterate 3x3 around the current TilePosition and try to get within 5 tiles
			while (startCenter.getDistance(moveTowards) > 320.0)
			{
				final var initialStart = creationStart;
				for (int x = initialStart.x - 1; x <= initialStart.x + 1; x++)
				{
					for (int y = initialStart.y - 1; y <= initialStart.y + 1; y++)
					{
						TilePosition t = new TilePosition(x, y);
						if (!t.isValid())
						{
							continue;
						}

						final var p = Position(t) + Position(16, 16);
						final var dist = p.getDistance(moveTowards);

						if (BWAPI_ext.GlobalMembers.dist < distBest)
						{
							distBest = BWAPI_ext.GlobalMembers.dist;
							creationStart = t;
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: startCenter = p;
							startCenter.copyFrom(p);
							movedStart = true;
							break;
						}
					}
				}
			}
		}

		// If the creation start position isn't buildable, move towards the top of this area to find a buildable location
		while (openWall && !Broodwar.isBuildable(creationStart))
		{
			var distBest = Double.MAX_VALUE;
			final var initialStart = creationStart;
			for (int x = initialStart.x - 1; x <= initialStart.x + 1; x++)
			{
				for (int y = initialStart.y - 1; y <= initialStart.y + 1; y++)
				{
					TilePosition t = new TilePosition(x, y);
					if (!t.isValid())
					{
						continue;
					}

					final var p = Position(t);
					final var dist = p.getDistance(Position(area.Top()));

					if (BWAPI_ext.GlobalMembers.dist < distBest)
					{
						distBest = BWAPI_ext.GlobalMembers.dist;
						creationStart = t;
						movedStart = true;
					}
				}
			}
		}
	}

	private void initializePathPoints()
	{
		var line = std::make_pair(Position(choke.Pos(choke.end1)) + Position(4, 4), Position(choke.Pos(choke.end2)) + Position(4, 4));
		var perpLine = openWall ? map.perpendicularLine(line, 160.0) : map.perpendicularLine(line, 96.0);
		var lineStart = perpLine.first.getDistance(Position(area.Top())) > perpLine.second.getDistance(Position(area.Top())) ? perpLine.second : perpLine.first;
		var lineEnd = perpLine.first.getDistance(Position(area.Top())) > perpLine.second.getDistance(Position(area.Top())) ? perpLine.first : perpLine.second;
		var isMain = closestStation != null && closestStation.isMain();
		var isNatural = closestStation != null && closestStation.isNatural();

		// If it's a natural wall, path between the closest main and end of the perpendicular line
		if (isNatural)
		{
			Station closestMain = stations.getClosestMainStation(TilePosition(choke.Center()));
			initialPathStart = closestMain != null ? TilePosition(map.mapBWEM.GetPath(closestStation.getBWEMBase().Center(), closestMain.getBWEMBase().Center()).front().Center()) : TilePosition(lineStart);
			initialPathEnd = TilePosition(lineEnd);
		}

		// If it's a main wall, path between a point between the roughly the choke and the area top
		else if (isMain)
		{
			initialPathEnd = (TilePosition(choke.Center()) + TilePosition(lineEnd)) / 2;
			initialPathStart = (TilePosition(area.Top()) + TilePosition(lineStart)) / 2;
		}

		// Other walls
		else
		{
			initialPathStart = TilePosition(lineStart);
			initialPathEnd = TilePosition(lineEnd);
		}

//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: pathStart = initialPathStart;
		pathStart.copyFrom(initialPathStart);
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: pathEnd = initialPathEnd;
		pathEnd.copyFrom(initialPathEnd);
	}

	private void checkPathPoints()
	{
		final var neighbourArea = (BWEM.Area area) ->
		{
			for (var subArea : area.AccessibleNeighbours())
			{
				if (area == subArea)
				{
					return true;
				}
			}
			return false;
		};

		final var notValidPathPoint = (TilePosition testTile) ->
		{
			return !testTile.isValid() || !map.isWalkable(testTile) || map.isReserved(testTile) || map.isUsed(testTile) != UnitTypes.GlobalMembers.None;
		};

		// Push the path start as far from the path end if it's not in a valid location
		var distBest = 0.0;
		if (notValidPathPoint(pathStart))
		{
			for (var x = initialPathStart.x - 4; x < initialPathStart.x + 4; x++)
			{
				for (var y = initialPathStart.y - 4; y < initialPathStart.y + 4; y++)
				{
					TilePosition t = new TilePosition(x, y);
					final var dist = t.getDistance(initialPathEnd);
					if (notValidPathPoint(t))
					{
						continue;
					}

					if (BWAPI_ext.GlobalMembers.dist > distBest)
					{
						pathStart = t;
						distBest = BWAPI_ext.GlobalMembers.dist;
					}
				}
			}
		}

		// Push the path end as far from the path start if it's not in a valid location
		distBest = 0.0;
		if (notValidPathPoint(pathEnd))
		{
			for (var x = initialPathEnd.x - 4; x < initialPathEnd.x + 4; x++)
			{
				for (var y = initialPathEnd.y - 4; y < initialPathEnd.y + 4; y++)
				{
					TilePosition t = new TilePosition(x, y);
					final var dist = t.getDistance(initialPathStart);
					if (notValidPathPoint(t))
					{
						continue;
					}

					if (BWAPI_ext.GlobalMembers.dist > distBest)
					{
						pathEnd = t;
						distBest = BWAPI_ext.GlobalMembers.dist;
					}
				}
			}
		}
	}

	private void addPieces()
	{
		// For each permutation, try to make a wall combination that is better than the current best
		do
		{
			currentLayout.clear();
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: typeIterator = rawBuildings.begin();
			typeIterator.copyFrom(rawBuildings.iterator());
			addNextPiece(new BWAPI.TilePosition(creationStart));
		} while (Broodwar.self().getRace() == Races.GlobalMembers.Zerg ? next_permutation(find(rawBuildings.iterator(), rawBuildings.end(), UnitTypes.GlobalMembers.Zerg_Hatchery), rawBuildings.end()) : next_permutation(rawBuildings.iterator(), find(rawBuildings.iterator(), rawBuildings.end(), UnitTypes.GlobalMembers.Protoss_Pylon)));

//C++ TO JAVA CONVERTER NOTE: 'auto' variable declarations are not supported in Java:
//ORIGINAL LINE: for (auto &[tile, type] : bestLayout)
		for ([,] : bestLayout)
		{
			addToWallPieces(tile, type);
			map.addReserve(tile, type.tileWidth(), type.tileHeight());
			map.addUsed(tile, type);
		}
	}

	private void addNextPiece(TilePosition start)
	{
//C++ TO JAVA CONVERTER TODO TASK: Iterators are only converted within the context of 'while' and 'for' loops:
		final var type = typeIterator;
		final var radius = (openWall || typeIterator == rawBuildings.iterator()) ? 8 : 4;

		for (var x = start.x - radius; x < start.x + radius; x++)
		{
			for (var y = start.y - radius; y < start.y + radius; y++)
			{
				TilePosition tile = new TilePosition(x, y);

				if (!tile.isValid())
				{
					continue;
				}

				final var center = Position(tile) + Position(type.tileWidth() * 16, type.tileHeight() * 16);
				final var closestGeo = map.getClosestChokeTile(choke, BWAPI_ext.GlobalMembers.center);

				// Open walls need to be placed within proximity of notable features
				if (openWall)
				{
					var closestNotable = Positions.GlobalMembers.Invalid;
					var closestNotableDist = Double.MAX_VALUE;
					for (var pos : notableLocations)
					{
						var dist = pos.getDistance(BWAPI_ext.GlobalMembers.center);
						if (BWAPI_ext.GlobalMembers.dist < closestNotableDist)
						{
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: closestNotable = pos;
							closestNotable.copyFrom(pos);
							closestNotableDist = BWAPI_ext.GlobalMembers.dist;
						}
					}
					if (BWAPI_ext.GlobalMembers.center.getDistance(closestNotable) >= 256.0 || BWAPI_ext.GlobalMembers.center.getDistance(closestNotable) >= closestGeo.getDistance(closestNotable) + 48.0)
					{

						continue;
					}
				}

				// Try not to seal the wall poorly
				if (!openWall && flatRamp)
				{
					var dist = min({Position(tile).getDistance(Position(choke.Center())), Position(tile + TilePosition(type.tileWidth(), 0)).getDistance(Position(choke.Center())), Position(tile + TilePosition(type.tileWidth(), type.tileHeight())).getDistance(Position(choke.Center())), Position(tile + TilePosition(0, type.tileHeight())).getDistance(Position(choke.Center()))});
					if (BWAPI_ext.GlobalMembers.dist < 64.0)
					{
						continue;
					}
				}

				// Required checks for this wall to be valid
				if (!powerCheck(new Iterator<BWAPI.UnitType>(type), new TilePosition(tile)))
				{
					GlobalMembers.failedPower++;
					continue;
				}
				if (!angleCheck(new Iterator<BWAPI.UnitType>(type), new TilePosition(tile)))
				{
					GlobalMembers.failedAngle++;
					continue;
				}
				if (!placeCheck(new Iterator<BWAPI.UnitType>(type), new TilePosition(tile)))
				{
					GlobalMembers.failedPlacement++;
					continue;
				}
				if (!tightCheck(new Iterator<BWAPI.UnitType>(type), new TilePosition(tile)))
				{
					GlobalMembers.failedTight++;
					continue;
				}
				if (!spawnCheck(new Iterator<BWAPI.UnitType>(type), new TilePosition(tile)))
				{
					GlobalMembers.failedSpawn++;
					continue;
				}

				// 1) Store the current type, increase the iterator
				currentLayout.put(tile, type);
				map.addUsed(tile, type);
//C++ TO JAVA CONVERTER TODO TASK: Iterators are only converted within the context of 'while' and 'for' loops:
				typeIterator++;

				// 2) If at the end, score wall
				if (typeIterator == rawBuildings.end())
				{
					scoreWall();
				}
				else
				{
					openWall ? addNextPiece(start) : addNextPiece(tile);
				}

				// 3) Erase this current placement and repeat
				if (typeIterator != rawBuildings.iterator())
				{
					typeIterator--;
				}

				currentLayout.remove(tile);
				map.removeUsed(tile, type.tileWidth(), type.tileHeight());
			}
		}
	}

	private void addDefenses()
	{
		// Prevent adding defenses if we don't have a wall
		if (bestLayout.isEmpty())
		{
			return;
		}

		// Find the furthest non Pylon building to the chokepoint
		var furthest = 0.0;
		for (var tile : largeTiles)
		{
			final var center = Position(tile) + Position(64, 48);
			final var closestGeo = map.getClosestChokeTile(choke, BWAPI_ext.GlobalMembers.center);
			final var dist = BWAPI_ext.GlobalMembers.center.getDistance(closestGeo);
			if (BWAPI_ext.GlobalMembers.dist > furthest)
			{
				furthest = BWAPI_ext.GlobalMembers.dist;
			}
		}
		for (var tile : mediumTiles)
		{
			final var center = Position(tile) + Position(48, 32);
			final var closestGeo = map.getClosestChokeTile(choke, BWAPI_ext.GlobalMembers.center);
			final var dist = BWAPI_ext.GlobalMembers.center.getDistance(closestGeo);
			if (BWAPI_ext.GlobalMembers.dist > furthest)
			{
				furthest = BWAPI_ext.GlobalMembers.dist;
			}
		}

		// Find the furthest Pylon building to the chokepoint if it's a Pylon wall
		if (pylonWall)
		{
			for (var tile : smallTiles)
			{
				final var center = Position(tile) + Position(32, 32);
				final var closestGeo = map.getClosestChokeTile(choke, BWAPI_ext.GlobalMembers.center);
				final var dist = BWAPI_ext.GlobalMembers.center.getDistance(closestGeo);
				if (BWAPI_ext.GlobalMembers.dist > furthest)
				{
					furthest = BWAPI_ext.GlobalMembers.dist;
				}
			}
		}

		var closestStation = stations.getClosestStation(TilePosition(choke.Center()));
		for (var building : rawDefenses)
		{

			final var start = TilePosition(centroid);
			final var width = building.tileWidth() * 32;
			final var height = building.tileHeight() * 32;
			final var openingCenter = Position(opening) + Position(16, 16);
			final var arbitraryCloseMetric = Broodwar.self().getRace() == Races.GlobalMembers.Zerg ? 32.0 : 160.0;

			// Iterate around wall centroid to find a suitable position
			var scoreBest = Double.MAX_VALUE;
			var tileBest = TilePositions.GlobalMembers.Invalid;
			for (var x = start.x - 12; x <= start.x + 12; x++)
			{
				for (var y = start.y - 12; y <= start.y + 12; y++)
				{
					TilePosition t = new TilePosition(x, y);
					final var center = Position(t) + Position(width / 2, height / 2);
					final var closestGeo = map.getClosestChokeTile(choke, BWAPI_ext.GlobalMembers.center);
					final var overlapsDefense = closestStation != null && closestStation.getDefenseLocations().find(t) != closestStation.getDefenseLocations().end() && defenses.find(t) == defenses.end();

					final var dist = BWAPI_ext.GlobalMembers.center.getDistance(closestGeo);
					final var tooClose = BWAPI_ext.GlobalMembers.dist < furthest || BWAPI_ext.GlobalMembers.center.getDistance(openingCenter) < arbitraryCloseMetric;
					final var tooFar = BWAPI_ext.GlobalMembers.center.getDistance(centroid) > 200.0;

					if (!overlapsDefense)
					{
						if (!t.isValid() || map.isReserved(t, building.tileWidth(), building.tileHeight()) || !map.isPlaceable(building, t) || map.tilesWithinArea(area, t, building.tileWidth(), building.tileHeight()) == 0 || tooClose || tooFar)
						{
							continue;
						}
					}
					final var score = BWAPI_ext.GlobalMembers.dist + BWAPI_ext.GlobalMembers.center.getDistance(openingCenter);

					if (score < scoreBest)
					{
						map.addUsed(t, building);
						var pathOut = findPathOut();
						if ((openWall && pathOut.isReachable()) || !openWall)
						{
							tileBest = t;
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: scoreBest = score;
							scoreBest.copyFrom(score);
						}
						map.removeUsed(t, building.tileWidth(), building.tileHeight());
					}
				}
			}

			// If tile is valid, add to wall
			if (tileBest.isValid())
			{
				defenses.add(tileBest);
				map.addReserve(tileBest, building.tileWidth(), building.tileHeight());
			}

			// Otherwise we can't place anymore
			else
			{
				break;
			}
		}
	}

	private void scoreWall()
	{
		// Create a path searching for an opening
		var pathOut = findPathOut();

		// If we want an open wall and it's not reachable, or we want a closed wall and it is reachable
		if ((openWall && !pathOut.isReachable()) || (!openWall && pathOut.isReachable()))
		{
			GlobalMembers.failedPath++;
			return;
		}

		// Find distance for each piece to the closest choke tile to the path start point
		var dist = 1.0;
		var optimalChokeTile = pathStart.getDistance(TilePosition(choke.Pos(choke.end1))) < pathStart.getDistance(TilePosition(choke.Pos(choke.end2))) ? Position(choke.Pos(choke.end1)) : Position(choke.Pos(choke.end2));
//C++ TO JAVA CONVERTER NOTE: 'auto' variable declarations are not supported in Java:
//ORIGINAL LINE: for (auto &[tile, type] : currentLayout)
		for ([,] : currentLayout)
		{
			final var center = Position(tile) + Position(type.tileWidth() * 16, type.tileHeight() * 16);
			final var chokeDist = optimalChokeTile.getDistance(BWAPI_ext.GlobalMembers.center);
			(type == UnitTypes.GlobalMembers.Protoss_Pylon && !pylonWall && !pylonWallPiece) ? BWAPI_ext.GlobalMembers.dist += -chokeDist : BWAPI_ext.GlobalMembers.dist += chokeDist;
		}

		// Calculate current centroid if a closed wall
		var currentCentroid = findCentroid();
		var currentOpening = Position(findOpening()) + Position(16, 16);

		// Score wall and store if better than current best layout
		final var score = !openWall ? BWAPI_ext.GlobalMembers.dist : 1.0 / BWAPI_ext.GlobalMembers.dist;
		if (score > bestWallScore)
		{
			bestLayout = new TreeMap<BWAPI.TilePosition, BWAPI.UnitType>(currentLayout);
			bestWallScore = score;
		}
	}

	private void cleanup()
	{
		// Add a reserved path
		if (openWall && !bestLayout.isEmpty())
		{
			var currentPath = findPathOut();
			for (var tile : currentPath.getTiles())
			{
				map.addReserve(tile, 1, 1);
			}
		}

		// Remove used from tiles
		for (var tile : smallTiles)
		{
			map.removeUsed(tile, 2, 2);
		}
		for (var tile : mediumTiles)
		{
			map.removeUsed(tile, 3, 2);
		}
		for (var tile : largeTiles)
		{
			map.removeUsed(tile, 4, 3);
		}
		for (var tile : defenses)
		{
			map.removeUsed(tile, 2, 2);
		}
	}

	public Wall(BWEM.Area _area, BWEM.ChokePoint _choke, ArrayList<BWAPI.UnitType> _buildings, ArrayList<BWAPI.UnitType> _defenses, BWAPI.UnitType _tightType, boolean _requireTight, boolean _openWall)
	{
		area = _area;
		choke = _choke;
		rawBuildings = new ArrayList<BWAPI.UnitType>(_buildings);
		rawDefenses = new ArrayList<BWAPI.UnitType>(_defenses);
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: tightType = _tightType;
		tightType.copyFrom(_tightType);
		requireTight = _requireTight;
		openWall = _openWall;

		// Create Wall layout and find basic features
		initialize();
		addPieces();
		currentLayout = new TreeMap<BWAPI.TilePosition, BWAPI.UnitType>(bestLayout);
		centroid = findCentroid();
		opening = findOpening();

		// Add defenses
		addDefenses();

		// Verify opening and cleanup Wall
		opening = findOpening();
		cleanup();
	}

	/// <summary> Adds a piece at the TilePosition based on the UnitType. </summary>
	public final void addToWallPieces(BWAPI.TilePosition here, BWAPI.UnitType building)
	{
		if (building.tileWidth() >= 4)
		{
			largeTiles.add(here);
		}
		else if (building.tileWidth() >= 3)
		{
			mediumTiles.add(here);
		}
		else if (find(rawDefenses.iterator(), rawDefenses.end(), building) != rawDefenses.end())
		{
			defenses.add(here);
		}
		else if (building.tileWidth() >= 2)
		{
			smallTiles.add(here);
		}
	}

	/// <summary> Returns the Chokepoint associated with this Wall. </summary>
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const BWEM::ChokePoint * getChokePoint() const
	public final BWEM.ChokePoint getChokePoint()
	{
		return choke;
	}

	/// <summary> Returns the Area associated with this Wall. </summary>
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const BWEM::Area * getArea() const
	public final BWEM.Area getArea()
	{
		return area;
	}

	/// <summary> Returns the defense locations associated with this Wall. </summary>
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: java.util.TreeSet<BWAPI::TilePosition> getDefenses() const
	public final TreeSet<BWAPI.TilePosition> getDefenses()
	{
		return new TreeSet<BWAPI.TilePosition>(defenses);
	}

	/// <summary> Returns the TilePosition belonging to the opening of the wall. </summary>
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: BWAPI::TilePosition getOpening() const
	public final BWAPI.TilePosition getOpening()
	{
		return new BWAPI.TilePosition(opening);
	}

	/// <summary> Returns the TilePosition belonging to the centroid of the wall pieces. </summary>
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: BWAPI::Position getCentroid() const
	public final BWAPI.Position getCentroid()
	{
		return new BWAPI.Position(centroid);
	}

	/// <summary> Returns the TilePosition belonging to large UnitType buildings. </summary>
	public final TreeSet<BWAPI.TilePosition> getLargeTiles()
	{
		return new TreeSet<BWAPI.TilePosition>(largeTiles);
	}

	/// <summary> Returns the TilePosition belonging to medium UnitType buildings. </summary>
	public final TreeSet<BWAPI.TilePosition> getMediumTiles()
	{
		return new TreeSet<BWAPI.TilePosition>(mediumTiles);
	}

	/// <summary> Returns the TilePosition belonging to small UnitType buildings. </summary>
	public final TreeSet<BWAPI.TilePosition> getSmallTiles()
	{
		return new TreeSet<BWAPI.TilePosition>(smallTiles);
	}

	/// <summary> Returns the raw vector of the buildings the wall was initialzied with. </summary>
	public final ArrayList<BWAPI.UnitType> getRawBuildings()
	{
		return new ArrayList<BWAPI.UnitType>(rawBuildings);
	}

	/// <summary> Returns the raw vector of the defenses the wall was initialzied with. </summary>
	public final ArrayList<BWAPI.UnitType> getRawDefenses()
	{
		return new ArrayList<BWAPI.UnitType>(rawDefenses);
	}

	/// <summary> Returns true if the Wall only contains Pylons. </summary>
	public final boolean isPylonWall()
	{
		return pylonWall;
	}

	/// <summary> Returns the number of ground defenses associated with this Wall. </summary>
	public final int getGroundDefenseCount()
	{
		// Returns how many visible ground defensive structures exist in this Walls defense locations
		int count = 0;
		for (var defense : defenses)
		{
			var type = map.isUsed(defense);
			if (type == UnitTypes.GlobalMembers.Protoss_Photon_Cannon || type == UnitTypes.GlobalMembers.Zerg_Sunken_Colony || type == UnitTypes.GlobalMembers.Terran_Bunker)
			{
				count++;
			}
		}
		return count;
	}

	/// <summary> Returns the number of air defenses associated with this Wall. </summary>
	public final int getAirDefenseCount()
	{
		// Returns how many visible air defensive structures exist in this Walls defense locations
		int count = 0;
		for (var defense : defenses)
		{
			var type = map.isUsed(defense);
			if (type == UnitTypes.GlobalMembers.Protoss_Photon_Cannon || type == UnitTypes.GlobalMembers.Zerg_Spore_Colony || type == UnitTypes.GlobalMembers.Terran_Missile_Turret)
			{
				count++;
			}
		}
		return count;
	}

	/// <summary> Draws all the features of the Wall. </summary>
	public final void draw()
	{
		TreeSet<Position> anglePositions = new TreeSet<Position>();
		int color = Broodwar.self().getColor();
		int textColor = color == 185 ? textColor = Text.DarkGreen : Broodwar.self().getTextColor();

		// Draw boxes around each feature
		var drawBoxes = true;
		if (drawBoxes)
		{
			for (var tile : smallTiles)
			{
				Broodwar.drawBoxMap(Position(tile), Position(tile) + Position(65, 65), color);
				Broodwar.drawTextMap(Position(tile) + Position(4, 4), "%cW", Broodwar.self().getTextColor());
				anglePositions.add(Position(tile) + Position(32, 32));
			}
			for (var tile : mediumTiles)
			{
				Broodwar.drawBoxMap(Position(tile), Position(tile) + Position(97, 65), color);
				Broodwar.drawTextMap(Position(tile) + Position(4, 4), "%cW", Broodwar.self().getTextColor());
				anglePositions.add(Position(tile) + Position(48, 32));
			}
			for (var tile : largeTiles)
			{
				Broodwar.drawBoxMap(Position(tile), Position(tile) + Position(129, 97), color);
				Broodwar.drawTextMap(Position(tile) + Position(4, 4), "%cW", Broodwar.self().getTextColor());
				anglePositions.add(Position(tile) + Position(64, 48));
			}
			for (var tile : defenses)
			{
				Broodwar.drawBoxMap(Position(tile), Position(tile) + Position(65, 65), color);
				Broodwar.drawTextMap(Position(tile) + Position(4, 4), "%cW", Broodwar.self().getTextColor());
			}
		}

		// Draw angles of each piece
		var drawAngles = false;
		if (drawAngles)
		{
			for (var pos1 : anglePositions)
			{
				for (var pos2 : anglePositions)
				{
					if (pos1 == pos2)
					{
						continue;
					}
					final var angle = map.getAngle(new tangible.Pair<auto, auto>(pos1, pos2));

					Broodwar.drawLineMap(pos1, pos2, color);
					Broodwar.drawTextMap((pos1 + pos2) / 2, "%c%.2f", textColor, angle);
				}
			}
		}

		// Draw opening
		Broodwar.drawBoxMap(Position(opening), Position(opening) + Position(33, 33), color, true);

		// Draw the line and angle of the ChokePoint
		var p1 = choke.Pos(choke.end1);
		var p2 = choke.Pos(choke.end2);
		var angle = map.getAngle(new tangible.Pair<BWAPI.WalkPosition, BWAPI.WalkPosition>(p1, p2));
		Broodwar.drawTextMap(Position(choke.Center()), "%c%.2f", Text.Grey, angle);
		Broodwar.drawLineMap(Position(p1), Position(p2), Colors.GlobalMembers.Grey);

		// Draw the path points
		Broodwar.drawCircleMap(Position(pathStart), 6, Colors.GlobalMembers.Black, true);
		Broodwar.drawCircleMap(Position(pathEnd), 6, Colors.GlobalMembers.White, true);
	}
}