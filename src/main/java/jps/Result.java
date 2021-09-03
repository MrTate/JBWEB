package jps;

import internal.Searcher;
import java.util.*;

//#endif // JPS_ASTAR_ONLY

//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JPS_VERIFY
//#endif // JPS_VERIFY

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
//ORIGINAL LINE: #define JPS_CHECKGRID(dx, dy) (grid(x+(dx), y+(dy)))
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
//ORIGINAL LINE: #define JPS_ADDPOS(dx, dy) do { *w++ = Pos(x+(dx), y+(dy)); } while(0)
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
//ORIGINAL LINE: #define JPS_ADDPOS_CHECK(dx, dy) do { if(JPS_CHECKGRID(dx, dy)) JPS_ADDPOS(dx, dy); } while(0)
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
//ORIGINAL LINE: #define JPS_ADDPOS_NO_TUNNEL(dx, dy) do { if(grid(x+(dx),y) || grid(x,y+(dy))) JPS_ADDPOS_CHECK(dx, dy); } while(0)


//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if ! ! JPS_ASTAR_ONLY
//#endif // JPS_ASTAR_ONLY
//-------------------------------------------------
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#undefine JPS_ADDPOS
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#undefine JPS_ADDPOS_CHECK
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#undefine JPS_ADDPOS_NO_TUNNEL
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#undefine JPS_CHECKGRID




// Public domain Jump Point Search implementation by False.Genesis
// Very fast pathfinding for uniform cost grids.
// Supports incremental pathfinding.

// Please keep the following source information intact when you use this file in your own projects:
// This file originates from: https://github.com/fgenesis/jps
// Based on the paper http://users.cecs.anu.edu.au/~dharabor/data/papers/harabor-grastien-aaai11.pdf
// by Daniel Harabor & Alban Grastien.
// Jumper (https://github.com/Yonaba/Jumper) and PathFinding.js (https://github.com/qiao/PathFinding.js)
// served as reference for this implementation.
// If you use this, attribution would be nice, but is not necessary.

// ====== COMPILE CONFIG ======

// If this is defined, compare all jumps against recursive reference implementation (only if _DEBUG is defined)
///#define JPS_VERIFY

// If this is defined, use standard A* instead of JPS (e.g. if you want to compare performance in your scenario)
///#define JPS_ASTAR_ONLY

// If this is defined, disable the greedy direct-short-path check that avoids the large area scanning that JPS does.
// Does not change optimality of results when left enabled
///#define JPS_DISABLE_GREEDY

// ============================

// Usage:
/*
// Define a class that overloads `operator()(x, y) const`, returning a value that can be treated as boolean.
// You are responsible for bounds checking!
// You want your operator() to be as fast as possible, as it will be called a LOT.

struct MyGrid
{
inline bool operator()(unsigned x, unsigned y) const
{
if(x < width && y < height) // Unsigned will wrap if < 0
... return true if terrain at (x, y) is walkable.
}
unsigned width, height;
};

// Then you can retrieve a path:

MyGrid grid;
// ... set grid width, height, and whatever
unsigned step = 0; // set this to 1 if you want a detailed single-step path
// (e.g. if you plan to further mangle the path yourself),
// or any other higher value to output every Nth position.
JPS::PathVector path; // The resulting path will go here.


// Single-call interface:
bool found = JPS::findPath(path, grid, startx, starty, endx, endy, step);


// Alternatively, if you want more control:

JPS::Searcher<MyGrid> search(grid);
while(true)
{
// ..stuff happening ...

// build path incrementally from waypoints
JPS::Position a, b, c, d = <...>; // set some waypoints
search.findPath(path, a, b);
search.findPath(path, b, c);
search.findPath(path, c, d);

// re-use existing pathfinder instance
if(!search.findPath(path2, JPS::Pos(startx, starty), JPS::Pos(endx, endy), step))
{
// ...handle failure...
}
// ... more stuff happening ...

// At convenient times, you can clean up accumulated nodes to reclaim memory.
// This is never necessary for correct function, but performance will drop if too many cached nodes exist.
if(mapWasReloaded)
search.freeMemory();
}

// Further remarks about the super lazy single-call function can be found at the bottom of this file.

// -------------------------------
// --- Incremental pathfinding ---
// -------------------------------

First, call findPathInit(Position start, Position end).
Don't forget to check the return value, as it may return:
- NO_PATH if one or both of the points are obstructed
- EMPTY_PATH if the points are equal and not obstructed
- FOUND_PATH if the initial greedy heuristic could find a path quickly.
If it returns NEED_MORE_STEPS then the next part can start.

Repeatedly call findPathStep(int limit) until it returns NO_PATH or FOUND_PATH.
For consistency, you will want to ensure that the grid does not change between subsequent calls;
if the grid changes, parts of the path may go through a now obstructed area.
If limit is 0, it will perform the pathfinding in one go. Values > 0 abort the search
as soon as possible after the number of steps was exceeded, returning NEED_MORE_STEPS.
Use getStepsDone() after some test runs to find a good value for the limit.

Lastly, generate the actual path points from a successful run via findPathFinish(PathVector& path, unsigned step = 0).
Like described above, path points are appended, and granularity can be adjusted with the step parameter.
Returns false if the pathfinding did not finish or generating the path failed.

*/



//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
//C++ TO JAVA CONVERTER TODO TASK: #define macros defined in multiple preprocessor conditionals can only be replaced within the scope of the preprocessor conditional:
///#define JPS_ASSERT(cond) assert(cond)
//#else
//C++ TO JAVA CONVERTER TODO TASK: #define macros defined in multiple preprocessor conditionals can only be replaced within the scope of the preprocessor conditional:
///#define JPS_ASSERT(cond)
//#endif



public enum Result {
	NO_PATH,
	FOUND_PATH,
	NEED_MORE_STEPS,
	EMPTY_PATH;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static Result forValue(int value) {
		return values()[value];
	}
}