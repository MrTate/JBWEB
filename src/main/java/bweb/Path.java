package bweb;

import bwapi.*;

import java.util.ArrayList;
import java.util.List;

public class Path {
    List<TilePosition> tiles;
    double dist;
    boolean reachable;
    TilePosition source, target;

    Path() {
        tiles = new ArrayList<>();
        dist = 0.0;
        reachable = false;
        source = TilePosition.Invalid;
        target = TilePosition.Invalid;
    }

    List<TilePosition> getTiles() {
        return tiles;
    }

    TilePosition getSource() {
        return source;
    }

    TilePosition getTarget() {
        return target;
    }

    double getDistance() {
        return dist;
    }

    boolean isReachable() {
        return reachable;
    }
}
