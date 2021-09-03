package bweb;

import BWAPI.*;
import java.util.*;

	public class PathCache
	{
		public TreeMap<tangible.Pair<TilePosition, TilePosition>, Iterator<Path>> iteratorList = new TreeMap<tangible.Pair<TilePosition, TilePosition>, Iterator<Path>>();
		public LinkedList<Path> pathCache = new LinkedList<Path>();
		public final TreeMap<BWEM.Area, Integer> notReachableThisFrame = new TreeMap<BWEM.Area, Integer>();
	}