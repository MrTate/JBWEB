package bweb;

import BWAPI.*;
import java.util.*;

//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Wall;

public class Path
{
	private ArrayList<BWAPI.TilePosition> tiles = new ArrayList<BWAPI.TilePosition>();
	private double dist;
	private boolean reachable;
	private BWAPI.TilePosition source = new BWAPI.TilePosition();
	private BWAPI.TilePosition target = new BWAPI.TilePosition();
	public Path()
	{
		tiles = new ArrayList<BWAPI.TilePosition>();
		BWAPI_ext.GlobalMembers.dist = 0.0;
		reachable = false;
		source = BWAPI.TilePositions.Invalid;
		target = BWAPI.TilePositions.Invalid;
	}

	/// <summary> Returns the vector of TilePositions associated with this Path. </summary>
	public final ArrayList<BWAPI.TilePosition> getTiles()
	{
		return new ArrayList<BWAPI.TilePosition>(tiles);
	}

	/// <summary> Returns the source (start) TilePosition of the Path. </summary>
	public final BWAPI.TilePosition getSource()
	{
		return new BWAPI.TilePosition(source);
	}

	/// <summary> Returns the target (end) TilePosition of the Path. </summary>
	public final BWAPI.TilePosition getTarget()
	{
		return new BWAPI.TilePosition(target);
	}

	/// <summary> Returns the distance from the source to the target in pixels. </summary>
	public final double getDistance()
	{
		return BWAPI_ext.GlobalMembers.dist;
	}

	/// <summary> Returns a check if the path was able to reach the target. </summary>
	public final boolean isReachable()
	{
		return reachable;
	}

	/// <summary> Creates a path from the source to the target using JPS and collision provided by BWEB based on walkable tiles and used tiles. </summary>
	public void createUnitPath(final Position s, final Position t)
	{
		target = TilePosition(t);
		source = TilePosition(s);

		// If this path does not exist in cache, remove last reference and erase reference
		var pathPoints = new tangible.Pair<BWAPI.TilePosition, BWAPI.TilePosition>(source, target);
		if (!GlobalMembers.unitPathCache.iteratorList.containsKey(pathPoints))
		{
			if (GlobalMembers.unitPathCache.pathCache.size() == GlobalMembers.maxCacheSize)
			{
				var last = GlobalMembers.unitPathCache.pathCache.getLast();
				GlobalMembers.unitPathCache.pathCache.removeLast();
				GlobalMembers.unitPathCache.iteratorList.remove(std::make_pair(last.getSource(), last.getTarget()));
			}
		}

		// If it does exist, set this path as cached version, update reference and push cached path to the front
		else
		{
			var oldPath = GlobalMembers.unitPathCache.iteratorList.get(pathPoints);
			BWAPI_ext.GlobalMembers.dist = oldPath.getDistance();
			tiles = oldPath.getTiles();
			reachable = oldPath.isReachable();

//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'erase' method in Java:
			GlobalMembers.unitPathCache.pathCache.erase(GlobalMembers.unitPathCache.iteratorList.get(pathPoints));
			GlobalMembers.unitPathCache.pathCache.addFirst(this);
			GlobalMembers.unitPathCache.iteratorList.put(pathPoints, GlobalMembers.unitPathCache.pathCache.iterator());
			return;
		}

		ArrayList<TilePosition> newJPSPath = new ArrayList<TilePosition>();
		final var width = Broodwar.mapWidth();
		final var height = Broodwar.mapHeight();

		final var isWalkable = (int x, int y) ->
		{
			TilePosition tile = new TilePosition(x, y);
			if (x > width || y > height || x < 0 || y < 0)
			{
				return false;
			}
			if (tile == source || tile == target)
			{
				return true;
			}
			if (map.isWalkable(tile) && map.isUsed(tile) == UnitTypes.GlobalMembers.None)
			{
				return true;
			}
			return false;
		};

		// If not reachable based on previous paths to this area
		if (target.isValid() && map.mapBWEM.GetArea(target) && isWalkable(source.x, source.y))
		{
			var checkReachable = GlobalMembers.unitPathCache.notReachableThisFrame.get(map.mapBWEM.GetArea(target));
			if (checkReachable >= Broodwar.getFrameCount() && Broodwar.getFrameCount() > 0)
			{
				reachable = false;
				BWAPI_ext.GlobalMembers.dist = Double.MAX_VALUE;
				return;
			}
		}

		// If we found a path, store what was found
		if (JPS.GlobalMembers.findPath(newJPSPath, isWalkable, source.x, source.y, target.x, target.y))
		{
			Position current = new Position(s);
			for (var t : newJPSPath)
			{
				BWAPI_ext.GlobalMembers.dist += (new Position(t)).getDistance(current);
				current = new Position(t);
				tiles.add(t);
			}
			reachable = true;

			// Update cache
			GlobalMembers.unitPathCache.pathCache.addFirst(this);
			GlobalMembers.unitPathCache.iteratorList.put(pathPoints, GlobalMembers.unitPathCache.pathCache.iterator());
		}

		// If not found, set destination area as unreachable for this frame
		else if (target.isValid() && map.mapBWEM.GetArea(target))
		{
			BWAPI_ext.GlobalMembers.dist = Double.MAX_VALUE;
			GlobalMembers.unitPathCache.notReachableThisFrame.put(map.mapBWEM.GetArea(target), Broodwar.getFrameCount());
			reachable = false;
		}
	}

	/// <summary> Creates a path from the source to the target using JPS, your provided walkable function, and whether diagonals are allowed. </summary>
	public void jpsPath(final Position s, final Position t, final function <boolean(const TilePosition)> passedWalkable)
	{
		jpsPath(s, t, passedWalkable, true);
	}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above:
//ORIGINAL LINE: void jpsPath(const Position s, const Position t, function <boolean(const TilePosition&)> passedWalkable, boolean diagonal = true)
	public void jpsPath(final Position s, final Position t, final function <boolean(const TilePosition)> passedWalkable, boolean diagonal)
	{
		target = TilePosition(t);
		source = TilePosition(s);

		// If this path does not exist in cache, remove last reference and erase reference
		var pathPoints = new tangible.Pair<BWAPI.TilePosition, BWAPI.TilePosition>(source, target);
		var thisCached = GlobalMembers.customPathCache.get(passedWalkable);

		if (thisCached.iteratorList.find(pathPoints) == thisCached.iteratorList.end())
		{
			if (thisCached.pathCache.size() == GlobalMembers.maxCacheSize)
			{
				var last = thisCached.pathCache.back();
				thisCached.pathCache.pop_back();
				thisCached.iteratorList.erase(std::make_pair(last.getSource(), last.getTarget()));
			}
		}

		// If it does exist, set this path as cached version, update reference and push cached path to the front
		else
		{
			var oldPath = thisCached.iteratorList[pathPoints];
			BWAPI_ext.GlobalMembers.dist = oldPath.getDistance();
			tiles = oldPath.getTiles();
			reachable = oldPath.isReachable();

			thisCached.pathCache.erase(thisCached.iteratorList[pathPoints]);
			thisCached.pathCache.push_front(this);
			thisCached.iteratorList[pathPoints] = thisCached.pathCache.begin();
			return;
		}

		ArrayList<TilePosition> newJPSPath = new ArrayList<TilePosition>();
		final var width = Broodwar.mapWidth();
		final var height = Broodwar.mapHeight();

		final var isWalkable = (int x, int y) ->
		{
			TilePosition tile = new TilePosition(x, y);
			if (x > width || y > height || x < 0 || y < 0)
			{
				return false;
			}
			if (tile == source || tile == target)
			{
				return true;
			}
			if (passedWalkable(tile))
			{
				return true;
			}
			return false;
		};

		// If not reachable based on previous paths to this area
		if (target.isValid() && map.mapBWEM.GetArea(target) && isWalkable(source.x, source.y))
		{
			var checkReachable = thisCached.notReachableThisFrame[map.mapBWEM.GetArea(target)];
			if (checkReachable >= Broodwar.getFrameCount() && Broodwar.getFrameCount() > 0)
			{
				reachable = false;
				BWAPI_ext.GlobalMembers.dist = Double.MAX_VALUE;
				return;
			}
		}

		// If we found a path, store what was found
		if (JPS.GlobalMembers.findPath(newJPSPath, isWalkable, source.x, source.y, target.x, target.y))
		{
			Position current = new Position(s);
			for (var t : newJPSPath)
			{
				BWAPI_ext.GlobalMembers.dist += (new Position(t)).getDistance(current);
				current = new Position(t);
				tiles.add(t);
			}
			reachable = true;

			// Update cache
			thisCached.pathCache.push_front(this);
			thisCached.iteratorList[pathPoints] = thisCached.pathCache.begin();
		}

		// If not found, set destination area as unreachable for this frame
		else if (target.isValid() && map.mapBWEM.GetArea(target))
		{
			BWAPI_ext.GlobalMembers.dist = Double.MAX_VALUE;
			thisCached.notReachableThisFrame[map.mapBWEM.GetArea(target)] = Broodwar.getFrameCount();
			reachable = false;
		}
	}

	/// <summary> Creates a path from the source to the target using BFS, your provided walkable function, and whether diagonals are allowed. </summary>
	public void bfsPath(final Position s, final Position t, final function <boolean(const TilePosition)> isWalkable)
	{
		bfsPath(s, t, isWalkable, true);
	}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above:
//ORIGINAL LINE: void bfsPath(const Position s, const Position t, function <boolean(const TilePosition&)> isWalkable, boolean diagonal = true)
	public void bfsPath(final Position s, final Position t, final function <boolean(const TilePosition)> isWalkable, boolean diagonal)
	{
		TilePosition source = new TilePosition(s);
		TilePosition target = new TilePosition(t);
		var maxDist = source.getDistance(target);
		final var width = Broodwar.mapWidth();
		final var height = Broodwar.mapHeight();
		ArrayList<TilePosition> direction = new ArrayList<TilePosition>(Arrays.asList({0, 1},{1, 0},{-1, 0},{0, -1}));

		if (source == target || source == TilePosition(0, 0) || target == TilePosition(0, 0))
		{
			return;
		}

		TilePosition[][] parentGrid = new TilePosition[256][256];

		// This function requires that parentGrid has been filled in for a path from source to target
		final var createPath = () ->
		{
			tiles.add(target);
			reachable = true;
			TilePosition check = parentGrid[target.x][target.y];
			BWAPI_ext.GlobalMembers.dist += (new Position(target)).getDistance(new Position(check));

			do
			{
				tiles.add(check);
				TilePosition prev = new TilePosition(check);
				check.copyFrom(parentGrid[check.x][check.y]);
				BWAPI_ext.GlobalMembers.dist += (new Position(prev)).getDistance(new Position(check));
			} while (check != source);

			// HACK: Try to make it more accurate to positions instead of tiles
			var correctionSource = new Position(*(tiles.end() - 1));
			var correctionTarget = new Position(*(tiles.iterator() + 1));
			BWAPI_ext.GlobalMembers.dist += s.getDistance(correctionSource);
			BWAPI_ext.GlobalMembers.dist += t.getDistance(correctionTarget);
			BWAPI_ext.GlobalMembers.dist -= 64.0;
		};

		LinkedList<TilePosition> nodeQueue = new LinkedList<TilePosition>();
		nodeQueue.emplace(source);
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: parentGrid[source.x][source.y] = source;
		parentGrid[source.x][source.y].copyFrom(source);

		// While not empty, pop off top the closest TilePosition to target
		while (!nodeQueue.isEmpty())
		{
			auto const tile = nodeQueue.peek();
			nodeQueue.poll();

//C++ TO JAVA CONVERTER NOTE: 'auto' variable declarations are not supported in Java:
//ORIGINAL LINE: for (auto const &d : direction)
			for (final int d : direction)
			{
				auto const next = tile + d;

				if (next.isValid())
				{
					// If next has parent or is a collision, continue
					if (parentGrid[next.x][next.y] != TilePosition(0, 0) || !isWalkable(next))
					{
						continue;
					}

					// Check diagonal collisions where necessary
					if ((d.x == 1 || d.x == -1) && (d.y == 1 || d.y == -1) && (!isWalkable(tile + TilePosition(d.x, 0)) || !isWalkable(tile + TilePosition(0, d.y))))
					{
						continue;
					}

					// Set parent here
					parentGrid[next.x][next.y] = tile;

					// If at target, return path
					if (next == target)
					{
						createPath();
						return;
					}

					nodeQueue.emplace(next);
				}
			}
		}
		reachable = false;
		BWAPI_ext.GlobalMembers.dist = Double.MAX_VALUE;
	}
}