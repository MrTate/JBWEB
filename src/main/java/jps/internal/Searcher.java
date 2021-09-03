package jps.internal;

import bwapi.*;
import bwapi.Position;
import jps.*;
import java.util.*;

//C++ TO JAVA CONVERTER WARNING: The original C++ template specifier was replaced with a Java generic specifier, which may not produce the same behavior:
//ORIGINAL LINE: template <typename GRID>
public class Searcher<GRID> {
	public Searcher(final GRID g) {
		this.grid = g;
		this.endNode = null;
		this.skip = 1;
		this.stepsRemain = 0;
		this.stepsDone = 0;
	}

	// single-call
	public final boolean findPath(ArrayList<TilePosition> path, Position start, Position end, int step)
	{
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: Result res = findPathInit(start, end);
		Result res = findPathInit(new jps.Position(start), new jps.Position(end));

		// If this is true, the resulting path is empty (findPathFinish() would fail, so this needs to be checked before)
		if (res == EMPTY_PATH)
		{
			return true;
		}

		while (true)
		{
			switch (res)
			{
			case NEED_MORE_STEPS:
				res = findPathStep(0);
				break; // the switch

			case FOUND_PATH:
				return findPathFinish(path, step);

			case NO_PATH:
			default:
				return false;
			}
		}
	}

	// incremental pathfinding
	public final Result findPathInit(Position start, Position end)
	{
		for (NodeGrid.iterator it = nodegrid.iterator(); it != nodegrid.end(); ++it)
		{
			it.second.clearState();
		}
		open.clear();
		endNode = null;
		stepsDone = 0;

		// If skip is > 1, make sure the points are aligned so that the search will always hit them
		start.x = (start.x / skip) * skip;
		start.y = (start.y / skip) * skip;
		end.x = (end.x / skip) * skip;
		end.y = (end.y / skip) * skip;

		if (start.equalsTo(end))
		{
			// There is only a path if this single position is walkable.
			// But since the starting position is omitted, there is nothing to do here.
			return grid(end.x, end.y) ? EMPTY_PATH : NO_PATH;
		}

		// If start or end point are obstructed, don't even start
		if (!grid(start.x, start.y) || !grid(end.x, end.y))
		{
			return NO_PATH;
		}

		endNode = getNode(end);
		Node startNode = getNode(start);
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(startNode && endNode);

//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if ! JPS_DISABLE_GREEDY
		// Try the quick way out first
		if (findPathGreedy(startNode))
		{
			return FOUND_PATH;
		}
//#endif

		open.push(startNode);

		return NEED_MORE_STEPS;
	}

	public final Result findPathStep(int limit)
	{
		stepsRemain = limit;
		do
		{
			if (open.empty())
			{
				return NO_PATH;
			}
			Node n = open.pop();
			n.setClosed();
			if (n == endNode)
			{
				return FOUND_PATH;
			}
			identifySuccessors(n);
		} while (stepsRemain >= 0);
		return NEED_MORE_STEPS;
	}

	public final boolean findPathFinish(ArrayList<TilePosition> path, int step)
	{
		return generatePath(path, step);
	}

	// misc
	public final void freeMemory()
	{
		NodeGrid v = new NodeGrid();
		nodegrid.swap(v);
		endNode = null;
		open.clear();
		// other containers known to be empty.
	}

	public final void setSkip(int s) {
		skip = Math.max(1, s);
	}

	public final size_t getStepsDone() {
		return new size_t(stepsDone);
	}

	public final int getNodesExpanded() {
		return nodegrid.size();
	}

	private final GRID grid;
	private Node endNode;
	private int skip;
	private int stepsRemain;
	private size_t stepsDone = new size_t();
	private OpenList open = new OpenList();

	private TreeMap<Position, Node> nodegrid = new TreeMap<Position, Node>();

	private Node getNode(final Position pos) {
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(grid(pos.x, pos.y));
		return nodegrid.put(pos, new Node(pos)).first.second;
	}

	private void identifySuccessors(Node n) {
		Position[] buf = tangible.Arrays.initializeWithDefaultPositionInstances(8);
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JPS_ASTAR_ONLY
		final int num = findNeighborsAStar(n, buf[0]);
//#else
		final int num = findNeighbors(n, buf[0]);
//#endif
		for (int i = num - 1; i >= 0; --i) {
			// Invariant: A node is only a valid neighbor if the corresponding grid position is walkable (asserted in jumpP)
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JPS_ASTAR_ONLY
			Position jp = buf[i];
//#else
			Position jp = jumpP(buf[i], n.pos);
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JPS_VERIFY
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
			JPS_ASSERT(jp.equalsTo(jumpPRec(buf[i], n.pos)));
//#endif
			if (!jp.isValid()) {
				continue;
			}
//#endif
			// Now that the grid position is definitely a valid jump point, we have to create the actual node.
			Node jn = getNode(jp);
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
			JPS_ASSERT(jn && jn != n);
			if (jn.isClosed() == 0) {
				int extraG = heuristic.GlobalMembers.Euclidean(jn, n);
				int newG = n.g + extraG;
				if (jn.isOpen() == 0 || newG < jn.g) {
					jn.g = newG;
					jn.f = jn.g + heuristic.GlobalMembers.Manhattan(jn, endNode);
					jn.parent = n;
					if (jn.isOpen() == 0) {
						open.push(jn);
						jn.setOpen();
					} else {
						open.fixup();
					}
				}
			}
		}
	}

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean generatePath(java.util.ArrayList<BWAPI::TilePosition>& path, uint step) const
	private boolean generatePath(ArrayList<TilePosition> path, int step) {
		if (endNode == null) {
			return false;
		}
		size_t offset = path.size();
		if (step != 0) {
			Node next = endNode;
			Node prev = endNode.parent;
			if (prev == null) {
				return false;
			}
			do {
				final int x = next.pos.x;
				final int y = next.pos.y;
				int dx = prev.pos.x - x;
				int dy = prev.pos.y - y;
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
				JPS_ASSERT(!dx || !dy || Math.abs(dx) == Math.abs(dy)); // known to be straight, if diagonal
				final int steps = Math.max(Math.abs(dx), Math.abs(dy));
				dx /= Math.max(Math.abs(dx), 1);
				dy /= Math.max(Math.abs(dy), 1);
				dx *= (int)step;
				dy *= (int)step;
				int dxa = 0;
				int dya = 0;
				for (int i = 0; i < steps; i += step) {
					path.add(BWAPI.TilePosition(x + dxa, y + dya));
					dxa += dx;
					dya += dy;
				}
				next = prev;
				prev = prev.parent;
			} while (prev != null);
		} else {
			Node next = endNode;
			if (next.parent == null) {
				return false;
			}
			do {
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
				JPS_ASSERT(next != next.parent);
				path.add(BWAPI.TilePosition(next.pos.x, next.pos.y));
				next = next.parent;
			} while (next.parent != null);
		}
		std::reverse(path.iterator() + offset, path.end());
		return true;
	}

//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if ! JPS_DISABLE_GREEDY
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if ! JPS_DISABLE_GREEDY
	private boolean findPathGreedy(Node n) {
		Position midpos = GlobalMembers.npos;
		int x = n.pos.x;
		int y = n.pos.y;
		int ex = endNode.pos.x;
		int ey = endNode.pos.y;

	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(x != ex || y != ey); // must not be called when start==end
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(n != endNode);

		final int skip = this.skip;

		int dx = ex - x;
		int dy = ey - y;
		final int adx = Math.abs(dx);
		final int ady = Math.abs(dy);
		dx /= Math.max(adx, 1);
		dy /= Math.max(ady, 1);
		dx *= skip;
		dy *= skip;

		// go diagonally first
		if (x != ex && y != ey) {
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
			JPS_ASSERT(dx && dy);
			final int minlen = (int)Math.min(adx, ady);
			final int tx = x + dx * minlen;
			for (; x != tx;) {
				if (grid(x, y) && (grid(x + dx, y) || grid(x, y + dy))) { // prevent tunneling as well
					x += dx;
					y += dy;
				} else {
					return false;
				}
			}

			if (!grid(x, y)) {
				return false;
			}

//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: midpos = Pos(x, y);
			midpos.copyFrom(jps.GlobalMembers.Pos(x, y));
		}

		// at this point, we're aligned to at least one axis
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(x == ex || y == ey);

		if (!(x == ex && y == ey)) {
			while (x != ex) {
				if (!grid(x += dx, y)) {
					return false;
				}
			}

			while (y != ey) {
				if (!grid(x, y += dy)) {
					return false;
				}
			}

	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
			JPS_ASSERT(x == ex && y == ey);
		}

		if (midpos.isValid()) {
			Node mid = getNode(midpos);
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
			JPS_ASSERT(mid && mid != n);
			mid.parent = n;
			if (mid != endNode) {
				endNode.parent = mid;
			}
		} else {
			endNode.parent = n;
		}

		return true;
	}
//#endif

//#endif

	//-------------- Plain old A* search ----------------
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JPS_ASTAR_ONLY
	private int findNeighborsAStar(Node n, Position wptr) {
//C++ TO JAVA CONVERTER TODO TASK: Pointer arithmetic is detected on this variable, so pointers on this variable are left unchanged:
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: Position *w = wptr;
		Position * w = new Position(wptr);
		final int x = n.pos.x;
		final int y = n.pos.y;
		final int d = skip;
		do {
			if (grid(x + (-d),y) || grid(x,y + (-d))) {
				do {
					if ((grid(x + (-d), y + (-d)))) {
						do {
							*w++ = jps.GlobalMembers.Pos(x + (-d), y + (-d));
						} while (false);
					}
				} while (false);
			}
		} while (false);
		do {
			if ((grid(x + (0), y + (-d)))) {
				do {
					*w++ = jps.GlobalMembers.Pos(x + (0), y + (-d));
				} while (false);
			}
		} while (false);
		do {
			if (grid(x + (+d),y) || grid(x,y + (-d))) {
				do {
					if ((grid(x + (+d), y + (-d)))) {
						do {
							*w++ = jps.GlobalMembers.Pos(x + (+d), y + (-d));
						} while (false);
					}
				} while (false);
			}
		} while (false);
		do {
			if ((grid(x + (-d), y + (0)))) {
				do {
					*w++ = jps.GlobalMembers.Pos(x + (-d), y + (0));
				} while (false);
			}
		} while (false);
		do {
			if ((grid(x + (+d), y + (0)))) {
				do {
					*w++ = jps.GlobalMembers.Pos(x + (+d), y + (0));
				} while (false);
			}
		} while (false);
		do {
			if (grid(x + (-d),y) || grid(x,y + (+d))) {
				do {
					if ((grid(x + (-d), y + (+d)))) {
						do {
							*w++ = jps.GlobalMembers.Pos(x + (-d), y + (+d));
						} while (false);
					}
				} while (false);
			}
		} while (false);
		do {
			if ((grid(x + (0), y + (+d)))) {
				do {
					*w++ = jps.GlobalMembers.Pos(x + (0), y + (+d));
				} while (false);
			}
		} while (false);
		do {
			if (grid(x + (+d),y) || grid(x,y + (+d))) {
				do {
					if ((grid(x + (+d), y + (+d)))) {
						do {
							*w++ = jps.GlobalMembers.Pos(x + (+d), y + (+d));
						} while (false);
					}
				} while (false);
			}
		} while (false);
		stepsDone += 8;
		return int(w - wptr);
	}

//#else
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if ! JPS_ASTAR_ONLY
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: uint findNeighbors(const Node *n, Position *wptr) const
	private int findNeighbors(Node n, Position wptr) {
		Position w = wptr;
		final int x = n.pos.x;
		final int y = n.pos.y;
		final int skip = this.skip;

		if (n.parent == null) {
			// straight moves
			do {
				if ((grid(x + (-skip), y + (0)))) {
					do {
						*w++ = jps.GlobalMembers.Pos(x + (-skip), y + (0));
					} while (false);
				}
			} while (false);
			do {
				if ((grid(x + (0), y + (-skip)))) {
					do {
						*w++ = jps.GlobalMembers.Pos(x + (0), y + (-skip));
					} while (false);
				}
			} while (false);
			do {
				if ((grid(x + (0), y + (skip)))) {
					do {
						*w++ = jps.GlobalMembers.Pos(x + (0), y + (skip));
					} while (false);
				}
			} while (false);
			do {
				if ((grid(x + (skip), y + (0)))) {
					do {
						*w++ = jps.GlobalMembers.Pos(x + (skip), y + (0));
					} while (false);
				}
			} while (false);

			// diagonal moves + prevent tunneling
			do {
				if (grid(x + (-skip),y) || grid(x,y + (-skip))) {
					do {
						if ((grid(x + (-skip), y + (-skip)))) {
							do {
								*w++ = jps.GlobalMembers.Pos(x + (-skip), y + (-skip));
							} while (false);
						}
					} while (false);
				}
			} while (false);
			do {
				if (grid(x + (-skip),y) || grid(x,y + (skip))) {
					do {
						if ((grid(x + (-skip), y + (skip)))) {
							do {
								*w++ = jps.GlobalMembers.Pos(x + (-skip), y + (skip));
							} while (false);
						}
					} while (false);
				}
			} while (false);
			do {
				if (grid(x + (skip),y) || grid(x,y + (-skip))) {
					do {
						if ((grid(x + (skip), y + (-skip)))) {
							do {
								*w++ = jps.GlobalMembers.Pos(x + (skip), y + (-skip));
							} while (false);
						}
					} while (false);
				}
			} while (false);
			do {
				if (grid(x + (skip),y) || grid(x,y + (skip))) {
					do {
						if ((grid(x + (skip), y + (skip)))) {
							do {
								*w++ = jps.GlobalMembers.Pos(x + (skip), y + (skip));
							} while (false);
						}
					} while (false);
				}
			} while (false);

			return int(w - wptr);
		}

		// jump directions (both -1, 0, or 1)
		int dx = x - n.parent.pos.x;
		dx /= Math.max(Math.abs(dx), 1);
		dx *= skip;
		int dy = y - n.parent.pos.y;
		dy /= Math.max(Math.abs(dy), 1);
		dy *= skip;

		if (dx != 0 && dy != 0) {
			// diagonal
			// natural neighbors
			boolean walkX = false;
			boolean walkY = false;
			if ((walkX = grid(x + dx, y))) {
				*w++ = jps.GlobalMembers.Pos(x + dx, y);
			}
			if ((walkY = grid(x, y + dy))) {
				*w++ = jps.GlobalMembers.Pos(x, y + dy);
			}

			if (walkX || walkY) {
				do {
					if ((grid(x + (dx), y + (dy)))) {
						do
						{
							*w++ = jps.GlobalMembers.Pos(x + (dx), y + (dy));
						} while (false);
					}
				} while (false);
			}

			// forced neighbors
			if (walkY && !(grid(x + (-dx), y + (0)))) {
				do {
					if ((grid(x + (-dx), y + (dy)))) {
						do {
							*w++ = jps.GlobalMembers.Pos(x + (-dx), y + (dy));
						} while (false);
					}
				} while (false);
			}

			if (walkX && !(grid(x + (0), y + (-dy)))) {
				do {
					if ((grid(x + (dx), y + (-dy)))) {
						do
						{
							*w++ = jps.GlobalMembers.Pos(x + (dx), y + (-dy));
						} while (false);
					}
				} while (false);
			}
		}
		else if (dx) {
			// along X axis
			if ((grid(x + (dx), y + (0)))) {
				do {
					*w++ = jps.GlobalMembers.Pos(x + (dx), y + (0));
				} while (false);

				// Forced neighbors (+ prevent tunneling)
				if (!(grid(x + (0), y + (skip)))) {
					do {
						if ((grid(x + (dx), y + (skip)))) {
							do {
								*w++ = jps.GlobalMembers.Pos(x + (dx), y + (skip));
							} while (false);
						}
					} while (false);
				}
				if (!(grid(x + (0), y + (-skip)))) {
					do {
						if ((grid(x + (dx), y + (-skip)))) {
							do {
								*w++ = jps.GlobalMembers.Pos(x + (dx), y + (-skip));
							} while (false);
						}
					} while (false);
				}
			}
		}
		else if (dy) {
			// along Y axis
			if ((grid(x + (0), y + (dy)))) {
				do {
					*w++ = jps.GlobalMembers.Pos(x + (0), y + (dy));
				} while (false);

				// Forced neighbors (+ prevent tunneling)
				if (!(grid(x + (skip), y + (0)))) {
					do {
						if ((grid(x + (skip), y + (dy)))) {
							do {
								*w++ = jps.GlobalMembers.Pos(x + (skip), y + (dy));
							} while (false);
						}
					} while (false);
				}
				if (!(grid(x + (-skip), y + (0)))) {
					do {
						if ((grid(x + (-skip), y + (dy)))) {
							do {
								*w++ = jps.GlobalMembers.Pos(x + (-skip), y + (dy));
							} while (false);
						}
					} while (false);
				}
			}
		}

		return int(w - wptr);
	}
//#endif

//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if ! JPS_ASTAR_ONLY
	private Position jumpP(final Position p, final Position src) {
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(grid(p.x, p.y));

		int dx = p.x - src.x;
		int dy = p.y - src.y;
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(dx || dy);

		if (dx != 0 && dy != 0) {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return jumpD(p, dx, dy);
			return new jps.Position(jumpD(new jps.Position(p), dx, dy));
		} else if (dx) {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return jumpX(p, dx);
			return new jps.Position(jumpX(new jps.Position(p), dx));
		} else if (dy) {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return jumpY(p, dy);
			return new jps.Position(jumpY(new jps.Position(p), dy));
		}

		// not reached
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(false);
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return npos;
		return new jps.Position(GlobalMembers.npos);
	}

	private Position jumpD(Position p, int dx, int dy) {
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(grid(p.x, p.y));
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(dx && dy);

		final Position endpos = endNode.pos;
		int steps = 0;

		while (true) {
			if (p.equalsTo(endpos)) {
				break;
			}

			++steps;
			final int x = p.x;
			final int y = p.y;

			if ((grid(x - dx, y + dy) && !grid(x - dx, y)) || (grid(x + dx, y - dy) && !grid(x, y - dy))) {
				break;
			}

			final boolean gdx = grid(x + dx, y);
			final boolean gdy = grid(x, y + dy);

			if (gdx && jumpX(jps.GlobalMembers.Pos(x + dx, y), dx).isValid()) {
				break;
			}

			if (gdy && jumpY(jps.GlobalMembers.Pos(x, y + dy), dy).isValid()) {
				break;
			}

			if ((gdx || gdy) && grid(x + dx, y + dy)) {
				p.x += dx;
				p.y += dy;
			} else {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: p = npos;
				p.copyFrom(GlobalMembers.npos);
				break;
			}
		}
		stepsDone += (int)steps;
		stepsRemain -= steps;
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return p;
		return new jps.Position(p);
	}

	private Position jumpX(Position p, int dx) {
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(dx);
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(grid(p.x, p.y));

		final int y = p.y;
		final Position endpos = endNode.pos;
		final int skip = this.skip;
		int steps = 0;

		int a = ~((!!grid(p.x, y + skip)) | ((!!grid(p.x, y - skip)) << 1));

		while (true) {
			final int xx = p.x + dx;
			final int b = (!!grid(xx, y + skip)) | ((!!grid(xx, y - skip)) << 1);

			if ((b & a) != 0 || p.equalsTo(endpos)) {
				break;
			}
			if (!grid(xx, y)) {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: p = npos;
				p.copyFrom(GlobalMembers.npos);
				break;
			}

			p.x += dx;
			a = ~b;
			++steps;
		}

		stepsDone += (int)steps;
		stepsRemain -= steps;
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return p;
		return new jps.Position(p);
	}

	private Position jumpY(Position p, int dy) {
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(dy);
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(grid(p.x, p.y));

		final int x = p.x;
		final Position endpos = endNode.pos;
		final int skip = this.skip;
		int steps = 0;

		int a = ~((!!grid(x + skip, p.y)) | ((!!grid(x - skip, p.y)) << 1));

		while (true) {
			final int yy = p.y + dy;
			final int b = (!!grid(x + skip, yy)) | ((!!grid(x - skip, yy)) << 1);

			if ((a & b) != 0 || p.equalsTo(endpos)) {
				break;
			}
			if (!grid(x, yy)) {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created:
//ORIGINAL LINE: p = npos;
				p.copyFrom(GlobalMembers.npos);
				break;
			}

			p.y += dy;
			a = ~b;
		}

		stepsDone += (int)steps;
		stepsRemain -= steps;
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return p;
		return new jps.Position(p);
	}

	// Recursive reference implementation -- for comparison only
//#endif
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JPS_VERIFY
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: Position jumpPRec(const Position& p, const Position& src) const
	private Position jumpPRec(final Position p, final Position src) {
		int x = p.x;
		int y = p.y;
		if (!grid(x, y)) {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return npos;
			return new jps.Position(GlobalMembers.npos);
		}
		if (p.equalsTo(endNode.pos)) {
			return p;
		}

		int dx = x - src.x;
		int dy = y - src.y;
	//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(dx || dy);

		if (dx != 0 && dy != 0) {
			if ((grid(x - dx, y + dy) && !grid(x - dx, y)) || (grid(x + dx, y - dy) && !grid(x, y - dy))) {
				return p;
			}
		}
		else if (dx) {
			if ((grid(x + dx, y + skip) && !grid(x, y + skip)) || (grid(x + dx, y - skip) && !grid(x, y - skip))) {
				return p;
			}
		}
		else if (dy) {
			if ((grid(x + skip, y + dy) && !grid(x + skip, y)) || (grid(x - skip, y + dy) && !grid(x - skip, y))) {
				return p;
			}
		}

		if (dx != 0 && dy != 0) {
			if (jumpPRec(jps.GlobalMembers.Pos(x + dx, y), p).isValid()) {
				return p;
			}
			if (jumpPRec(jps.GlobalMembers.Pos(x, y + dy), p).isValid()) {
				return p;
			}
		}

		if (grid(x + dx, y) || grid(x, y + dy)) {
//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return jumpPRec(Pos(x + dx, y + dy), p);
			return new jps.Position(jumpPRec(jps.GlobalMembers.Pos(x + dx, y + dy), p));
		}

//C++ TO JAVA CONVERTER TODO TASK: The following line was determined to contain a copy constructor call - this should be verified and a copy constructor should be created:
//ORIGINAL LINE: return npos;
		return new jps.Position(GlobalMembers.npos);
	}

//#endif
}
// Single-call convenience function
//
// path: If the function returns true, the path is stored in this vector.
//       The path does NOT contain the starting position, i.e. if start and end are the same,
//       the resulting path has no elements.
//       The vector does not have to be empty. The function does not clear it;
//       instead, the new path positions are appended at the end.
//       This allows building a path incrementally.
//
// grid: expected to overload operator()(x, y), return true if position is walkable, false if not.
//
// step: If 0, only return waypoints.
//       If 1, create exhaustive step-by-step path.
//       If N, put in one position for N blocks travelled, or when a waypoint is hit.
//       All returned points are guaranteed to be on a straight line (vertically, horizontally, or diagonally),
//       and there is no obstruction between any two consecutive points.
//       Note that this parameter does NOT influence the pathfinding in any way;
//       it only controls the coarseness of the output path.
//
// skip: If you know your map data well enough, this can be set to > 1 to speed up pathfinding even more.
//       Warning: Start and end positions will be rounded down to the nearest <skip>-aligned position,
//       so make sure to give appropriate positions so they do not end up in a wall.
//       This will also skip through walls if they are less than <skip> blocks thick at any reachable position.
//C++ TO JAVA CONVERTER WARNING: The original C++ template specifier was replaced with a Java generic specifier, which may not produce the same behavior:
//ORIGINAL LINE: template <typename GRID>




