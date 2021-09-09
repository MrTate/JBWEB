package bweb;

import bwapi.*;
import bwem.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathCache {
//    map<pair<TilePosition, TilePosition>, list<Path>::iterator> iteratorList;
    List<Path> pathCache = new ArrayList<>();
    HashMap<Area, Integer> notReachableThisFrame = new HashMap<>();
}
