package jps;

import internal.Searcher;
import java.util.*;

public class Position implements Comparable<Position> {
	public final int compareTo(Position otherInstance) {
		if (lessThan(otherInstance)) {
			return -1;
		}
		else if (otherInstance.lessThan(this)) {
			return 1;
		}

		return 0;
	}

	public int x;
	public int y;

	public boolean equalsTo(final Position p) {
		return x == p.x && y == p.y;
	}

	public boolean notEqualsTo(final Position p) {
		return x != p.x || y != p.y;
	}

	// for sorting
	public boolean lessThan(final Position p) {
		return y < p.y || (y == p.y && x < p.x);
	}

	public final boolean isValid() {
		return x != -1;
	}
}