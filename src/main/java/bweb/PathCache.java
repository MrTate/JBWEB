package bweb;

import bwapi.*;
import bwem.*;
import java.util.*;

	public class PathCache {
		public TreeMap<tangible.Pair<TilePosition, TilePosition>, Iterator<Path>> iteratorList = new TreeMap<tangible.Pair<TilePosition, TilePosition>, Iterator<Path>>();
		public LinkedList<Path> pathCache = new LinkedList<Path>();
		public final TreeMap<Area, Integer> notReachableThisFrame = new TreeMap<>();
	}