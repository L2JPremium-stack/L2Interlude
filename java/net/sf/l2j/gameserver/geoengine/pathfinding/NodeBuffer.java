package net.sf.l2j.gameserver.geoengine.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.geoengine.geodata.GeoStructure;

/**
 * Safe bounded pathfinding buffer.
 *
 * This class still works with the existing aCis 360 GeoLocation / .PN logic, but
 * adds defensive checks so malformed pathnodes, impossible routes or overloaded
 * searches don't lock the game server.
 */
public class NodeBuffer
{
	private static final int MAX_Z_DIFF = GeoStructure.CELL_HEIGHT * 2;

	private final ReentrantLock _lock = new ReentrantLock();
	private final int _size;
	private final Node[][] _buffer;

	private int _cx = 0;
	private int _cy = 0;

	private int _gtx = 0;
	private int _gty = 0;
	private short _gtz = 0;

	private long _timeStamp = 0;
	private long _lastElapsedTime = 0;
	private int _lastIterations = 0;

	private Node _current = null;

	public NodeBuffer(int size)
	{
		if (size <= 0)
			throw new IllegalArgumentException("NodeBuffer size must be positive.");

		_size = size;
		_buffer = new Node[size][size];

		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				_buffer[x][y] = new Node();
	}

	public final Node findPath(int gox, int goy, short goz, int gtx, int gty, short gtz)
	{
		_timeStamp = System.currentTimeMillis();
		_lastIterations = 0;

		// Center the buffer around the requested route.
		_cx = gox + (gtx - gox - _size) / 2;
		_cy = goy + (gty - goy - _size) / 2;

		_gtx = gtx;
		_gty = gty;
		_gtz = gtz;

		_current = getNode(gox, goy, goz);
		if (_current == null || _current.getLoc() == null)
			return null;

		_current.setCost(getCostH(gox, goy, goz));

		while (_current != null && _lastIterations++ < Config.MAX_ITERATIONS)
		{
			if (isTargetReached(_current))
				return _current;

			expand();
			_current = _current.getChild();
		}

		return null;
	}

	public final List<Node> debugPath()
	{
		final List<Node> result = new ArrayList<>();

		if (_current != null)
		{
			for (Node n = _current; n != null && n.getParent() != null; n = n.getParent())
			{
				result.add(n);
				n.setCost(-n.getCost());
			}
		}

		for (Node[] nodes : _buffer)
		{
			for (Node node : nodes)
			{
				if (node.getLoc() == null || node.getCost() <= 0)
					continue;

				result.add(node);
			}
		}

		return result;
	}

	/**
	 * Historical API name kept for compatibility.
	 * @return true only when this buffer was successfully locked by the caller.
	 */
	public final boolean isLocked()
	{
		return _lock.tryLock();
	}

	public final void free()
	{
		try
		{
			_current = null;

			for (Node[] nodes : _buffer)
				for (Node node : nodes)
					if (node.getLoc() != null)
						node.free();

			_lastElapsedTime = System.currentTimeMillis() - _timeStamp;
		}
		finally
		{
			if (_lock.isHeldByCurrentThread())
				_lock.unlock();
		}
	}

	public final long getElapsedTime()
	{
		return _lastElapsedTime;
	}

	public final int getLastIterations()
	{
		return _lastIterations;
	}

	private boolean isTargetReached(Node node)
	{
		return node.getLoc().getGeoX() == _gtx && node.getLoc().getGeoY() == _gty && Math.abs(node.getLoc().getZ() - _gtz) < GeoStructure.CELL_HEIGHT;
	}

	private final void expand()
	{
		if (_current == null || _current.getLoc() == null)
			return;

		final byte nswe = _current.getLoc().getNSWE();
		if (nswe == 0)
			return;

		final int x = _current.getLoc().getGeoX();
		final int y = _current.getLoc().getGeoY();
		final short z = (short) _current.getLoc().getZ();

		if ((nswe & GeoStructure.CELL_FLAG_N) != 0)
			addNode(x, y - 1, z, Config.BASE_WEIGHT);

		if ((nswe & GeoStructure.CELL_FLAG_S) != 0)
			addNode(x, y + 1, z, Config.BASE_WEIGHT);

		if ((nswe & GeoStructure.CELL_FLAG_W) != 0)
			addNode(x - 1, y, z, Config.BASE_WEIGHT);

		if ((nswe & GeoStructure.CELL_FLAG_E) != 0)
			addNode(x + 1, y, z, Config.BASE_WEIGHT);

		if ((nswe & GeoStructure.CELL_FLAG_NW) != 0)
			addNode(x - 1, y - 1, z, Config.DIAGONAL_WEIGHT);

		if ((nswe & GeoStructure.CELL_FLAG_NE) != 0)
			addNode(x + 1, y - 1, z, Config.DIAGONAL_WEIGHT);

		if ((nswe & GeoStructure.CELL_FLAG_SW) != 0)
			addNode(x - 1, y + 1, z, Config.DIAGONAL_WEIGHT);

		if ((nswe & GeoStructure.CELL_FLAG_SE) != 0)
			addNode(x + 1, y + 1, z, Config.DIAGONAL_WEIGHT);
	}

	private final Node getNode(int x, int y, short z)
	{
		final int ix = x - _cx;
		if (ix < 0 || ix >= _size)
			return null;

		final int iy = y - _cy;
		if (iy < 0 || iy >= _size)
			return null;

		final Node result = _buffer[ix][iy];

		if (result.getLoc() == null)
			result.setLoc(x, y, z);

		return result;
	}

	private final void addNode(int x, int y, short z, int weight)
	{
		final Node node = getNode(x, y, z);
		if (node == null || node.getLoc() == null)
			return;

		final int nodeZ = node.getLoc().getZ();

		// Don't allow broken .PN/.L2D data to create stairs through walls/floors.
		if (Math.abs(nodeZ - z) > MAX_Z_DIFF)
			return;

		// Already inserted/expanded.
		if (node.getCost() >= 0)
			return;

		node.setParent(_current);

		final double penalty = node.getLoc().getNSWE() != (byte) 0xFF ? Config.OBSTACLE_MULTIPLIER : 1.0;
		node.setCost(getCostH(x, y, nodeZ) + weight * penalty);

		insertOpenNode(node);
	}

	private void insertOpenNode(Node node)
	{
		Node current = _current;
		int count = 0;

		while (current.getChild() != null && count++ < Config.MAX_ITERATIONS * 4)
		{
			if (current.getChild().getCost() > node.getCost())
			{
				node.setChild(current.getChild());
				break;
			}

			current = current.getChild();
		}

		if (count >= Config.MAX_ITERATIONS * 4)
			return;

		current.setChild(node);
	}

	private final double getCostH(int x, int y, int z)
	{
		final int dX = x - _gtx;
		final int dY = y - _gty;
		final int dZ = (z - _gtz) / GeoStructure.CELL_HEIGHT;

		return Math.sqrt(dX * dX + dY * dY + dZ * dZ) * Config.HEURISTIC_WEIGHT;
	}
}
