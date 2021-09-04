package bweb;

import bwapi.*;
import bwem.*;

import java.util.Set;

public class Station {
    Base base;
    Set<TilePosition> defenses;
    Position resourceCentroid;
    boolean main;
    boolean natural;

    Station(Position _resourceCentroid, Set<TilePosition> _defenses, Base _base, boolean _main, boolean _natural) {
        resourceCentroid = _resourceCentroid;
        defenses = _defenses;
        base = _base;
        main = _main;
        natural = _natural;
    }

    //TODO: Implement equals operator?
//    boolean operator== (Station* s) {
//        return base == s->getBWEMBase();
//    }
}
