package jps.internal;

import jps.*;

public class Node {
	public int f;
	public int g;
	public Position pos;
	public Node parent;
	private int flags;

	public Node(final Position p) {
		this.f = 0;
		this.g = 0;
		this.pos = p;
		this.parent = null;
		this.flags = 0;
	}

	public final void setOpen() {
		flags |= 1;
	}

	public final void setClosed() {
		flags |= 2;
	}

	public final int isOpen() {
		return flags & 1;
	}

	public final int isClosed() {
		return flags & 2;
	}

	public final void clearState() {
		f = 0;
		g = 0;
		parent = null;
		flags = 0;
	}
}