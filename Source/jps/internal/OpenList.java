package jps.internal;

import jps.*;
import java.util.*;

public class OpenList
{
	public final void push(Node node)
	{
//C++ TO JAVA CONVERTER TODO TASK: The #define macro 'JPS_ASSERT' was defined in multiple preprocessor conditionals and cannot be replaced in-line:
		JPS_ASSERT(node);
		nodes.push_back(node);
		std::push_heap(nodes.begin(), nodes.end(), _compare);
	}
	public final Node pop()
	{
		std::pop_heap(nodes.begin(), nodes.end(), _compare);
		Node node = nodes.back();
		nodes.pop_back();
		return node;
	}
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: inline boolean empty() const
	public final boolean empty()
	{
		return nodes.empty();
	}
	public final void clear()
	{
		nodes.clear();
	}
	public final void fixup()
	{
		std::make_heap(nodes.begin(), nodes.end(), _compare);
	}

	protected static boolean _compare(Node a, Node b)
	{
		return a.f > b.f;
	}
	protected NodeVector nodes = new NodeVector();
}