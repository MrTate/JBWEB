package jps.internal;

import jps.*;
import java.util.*;

public class Node
{
	public Node(final Position p)
	{
		this.f = 0;
		this.g = 0;
		this.pos = new jps.Position(p);
		this.parent = null;
		this.flags = 0;
	}
	public int f;
	public int g;
	public final Position pos = new Position();
	public final Node parent;

	public final void setOpen()
	{
		flags |= 1;
	}
	public final void setClosed()
	{
		flags |= 2;
	}
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: inline uint isOpen() const
	public final int isOpen()
	{
		return flags & 1;
	}
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: inline uint isClosed() const
	public final int isClosed()
	{
		return flags & 2;
	}
	public final void clearState()
	{
		f = 0;
		g = 0, parent = null;
		flags = 0;
	}

	private int flags;

//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//	boolean operator ==(final Node o); // not implemented, nodes should not be compared
}