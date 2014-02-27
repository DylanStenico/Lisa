package robocup2014.lisa.datatype;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

import robocup2014.lisa.datatype.Tile.Floor;
import robocup2014.lisa.dijkstra.compartor.PathComparator;
import robocup2014.lisa.dijkstra.compartor.PriorityComparator;
import android.graphics.Point;
import android.util.Log;

/**
 * Created by Forward on 02/13/14.
 */
public class Robot
{
    // *********************************************************
    // cardinal points
    public enum Direction
    {
	WEST(0), NORTH(1), EAST(2), SOUTH(3);
	private final int value;

	// constructor
	private Direction(int value)
	{
	    this.value = value;
	}

	// for casting from enum to int
	public int toInt()
	{
	    return value;
	}

	// for casting int to enum
	public static Direction toDirection(int arg0)
	{
	    Direction toRtn = Direction.SOUTH;
	    if (arg0 == 0)
		toRtn = Direction.WEST;
	    else if (arg0 == 1)
		toRtn = Direction.NORTH;
	    else if (arg0 == 2)
		toRtn = Direction.EAST;
	    else if (arg0 == 3) toRtn = Direction.SOUTH;
	    return toRtn;
	}
    }

    // *********************************************************
    // CONSTANTS
    // dimension of the two mazes
    public final int MAX_GROUND = 25,
	    MAX_AIR = 7;
    // startingPosition of the robot.
    public final Point startingPos;

    // DATA
    // the two mazes and the pointer of the current maze
    private Tile[][] mazeGround,
	    mazeAir,
	    maze;

    // data for the current maze
    private int dimension;
    private Point rampPoint;

    // first or second floor
    boolean level;

    // array that contaist the tiles to visit
    private Stack<Point> toVisit;

    // the current path
    Stack<Point> path;

    // lisa positioning data
    private Point position;
    private Direction direction;

    // *********************************************************

    // METHODS
    public Robot()
    {
	// create mazes
	mazeGround = new Tile[MAX_GROUND][MAX_GROUND];
	mazeAir = new Tile[MAX_AIR][MAX_AIR];
	maze = mazeGround;
	createMaze(mazeGround, MAX_GROUND);
	createMaze(mazeAir, MAX_AIR);

	// set ground maze
	dimension = MAX_GROUND;
	level = false;

	// set the enclosures
	setEnclosure(mazeGround, MAX_GROUND);
	setEnclosure(mazeAir, MAX_AIR);

	// set the current position to the center of the maze
	startingPos = new Point(dimension / 2, dimension/2);
	position = new Point(startingPos.x, startingPos.y);
	direction = Direction.NORTH;
	rampPoint = null;

	// create new path stack and add the starting position
	toVisit = new Stack<Point>();
	toVisit.push(position);
	path = new Stack<Point>();
    }

    public Tile.Direction getNextDir() throws Exception
    {
	// se ho finito il path ne devo creare uno nuovo
	if (path.empty())
	{
	    // create the two main points: the starting one and the ending one
	    Point start = new Point(position.x, position.y);
	    Point end;

	    // continue to pop the tile to go until it is not visited
	    do
	    {
		// if the toVisit stack is empty, it means that we have
		// explored all the maze
		if (!toVisit.isEmpty())
		{
		    end = toVisit.pop();
		}
		else
		{
		    throw new Exception("exploration completed");
		}
	    } while (maze[end.x][end.y].isVisited() && !maze[end.x][end.y].getPoint().equals(startingPos));

	    // set all the priority in the maze
	    dijkstra(start, end);

	    // calculate the path to reach the starting point from the arrive
	    // point
	    Comparator<Tile> comparator = new PathComparator();
	    PriorityQueue<Tile> queue;

	    while (!end.equals(start))
	    {
		// we add the end point to the path
		path.add(end);

		// crete a priorityqueue, that decides what tile is the best for
		// the path
		queue = new PriorityQueue<Tile>(1, comparator);
		// add to the priority queue all the tile next to an open walls
		if (maze[end.x][end.y].isOpen(Tile.Direction.AHEAD))
		{
		    queue.add(maze[end.x][end.y + 1]);
		    // Log.d("laFigaGianluca added", maze[end.x][end.y +
		    // 1].getPoint().toString());
		}
		if (maze[end.x][end.y].isOpen(Tile.Direction.RIGHT))
		{
		    queue.add(maze[end.x + 1][end.y]);
		    // Log.d("laFigaGianluca added", maze[end.x +
		    // 1][end.y].getPoint().toString());
		}
		if (maze[end.x][end.y].isOpen(Tile.Direction.LEFT))
		{
		    queue.add(maze[end.x - 1][end.y]);
		    // Log.d("laFigaGianluca added", maze[end.x -
		    // 1][end.y].getPoint().toString());
		}
		if (maze[end.x][end.y].isOpen(Tile.Direction.BACK))
		{
		    queue.add(maze[end.x][end.y - 1]);
		    // Log.d("laFigaGianluca added", maze[end.x][end.y -
		    // 1].getPoint().toString());
		}
		// now the second to last point becomes the end point
		end = queue.remove().getPoint();
	    }
	}

	// pop the point where to go
	Point currentPoint = path.pop();
	// the absolute direction where to go
	Direction absoluteDirToGo = null;
	// set the absolute direction
	if (currentPoint.x == position.x + 1)
	    absoluteDirToGo = Direction.EAST;
	else if (currentPoint.x == position.x - 1)
	    absoluteDirToGo = Direction.WEST;
	else if (currentPoint.y == position.y + 1)
	    absoluteDirToGo = Direction.NORTH;
	else if (currentPoint.y == position.y - 1) absoluteDirToGo = Direction.SOUTH;

	// calculate the relative direction based on the robot direction
	Tile.Direction dirToGo = realToRelative(direction, absoluteDirToGo);

	// update the robot direction
	direction = relativeToReal(direction, dirToGo);
	// update the robot position
	setPosition(currentPoint);
	Log.d("laFigaGianluca", toVisit.toString());
	printMaze();
	// return the relative direction the robot has to take
	return dirToGo;
    }

    // this method set all the tiles priority
    private void dijkstra(Point startingPoint, Point arrivePoint)
    {

	// reset the priority of each tile
	setMaxPriority();
	// create a new comparator, based on the priority of the tiles and a new
	// priorityqueue
	Comparator<Tile> comparator = new PriorityComparator();
	PriorityQueue<Tile> queue = new PriorityQueue<Tile>(1, comparator);

	// add to the queue the starting point, and set its priority on 0
	queue.add(maze[startingPoint.x][startingPoint.y]);
	maze[startingPoint.x][startingPoint.y].setPriority(0);

	// start the algorithm
	while (queue.size() > 0)
	{
	    // estract a currentTile from the queue
	    Tile currentTile = queue.remove();
	    // set best priority as the priority of the current tile + 1
	    int priority = currentTile.getPriority() + 1;

	    if (currentTile.isOpen(Tile.Direction.AHEAD) && maze[currentTile.getPoint().x][currentTile.getPoint().y + 1].getPriority() > priority)
	    {
		if (maze[currentTile.getPoint().x][currentTile.getPoint().y + 1].getFloor() != Floor.BLACK_FLOOR &&
			maze[currentTile.getPoint().x][currentTile.getPoint().y + 1].isVisited())
		{
		    maze[currentTile.getPoint().x][currentTile.getPoint().y + 1].setPriority(priority);
		    queue.add(maze[currentTile.getPoint().x][currentTile.getPoint().y + 1]);
		}
	    }
	    if (currentTile.isOpen(Tile.Direction.LEFT) && maze[currentTile.getPoint().x - 1][currentTile.getPoint().y].getPriority() > priority)
	    {
		if (maze[currentTile.getPoint().x - 1][currentTile.getPoint().y].getFloor() != Floor.BLACK_FLOOR &&
			maze[currentTile.getPoint().x - 1][currentTile.getPoint().y].isVisited())
		{
		    maze[currentTile.getPoint().x - 1][currentTile.getPoint().y].setPriority(priority);
		    queue.add(maze[currentTile.getPoint().x - 1][currentTile.getPoint().y]);
		}
	    }
	    if (currentTile.isOpen(Tile.Direction.RIGHT) && maze[currentTile.getPoint().x + 1][currentTile.getPoint().y].getPriority() > priority)
	    {
		if (maze[currentTile.getPoint().x + 1][currentTile.getPoint().y].getFloor() != Floor.BLACK_FLOOR &&
			maze[currentTile.getPoint().x + 1][currentTile.getPoint().y].isVisited())
		{
		    maze[currentTile.getPoint().x + 1][currentTile.getPoint().y].setPriority(priority);
		    queue.add(maze[currentTile.getPoint().x + 1][currentTile.getPoint().y]);
		}
	    }
	    if (currentTile.isOpen(Tile.Direction.BACK) && maze[currentTile.getPoint().x][currentTile.getPoint().y - 1].getPriority() > priority)
	    {
		if (maze[currentTile.getPoint().x][currentTile.getPoint().y - 1].getFloor() != Floor.BLACK_FLOOR &&
			maze[currentTile.getPoint().x][currentTile.getPoint().y - 1].isVisited())
		{
		    maze[currentTile.getPoint().x][currentTile.getPoint().y - 1].setPriority(priority);
		    queue.add(maze[currentTile.getPoint().x][currentTile.getPoint().y - 1]);
		}
	    }
	}
    }

    public void setWalls(boolean left, boolean ahead, boolean right, boolean back)
    {
	// TODO se c'e' un muro in questa cella, bisogna aggiornare anche il
	if (!maze[position.x][position.y].isVisited())
	{
	    // update the walls, setting if they are or if they are not
	    maze[position.x][position.y].setWall(relativeToReal(direction, Tile.Direction.AHEAD), ahead);
	    maze[position.x][position.y].setWall(relativeToReal(direction, Tile.Direction.RIGHT), right);
	    maze[position.x][position.y].setWall(relativeToReal(direction, Tile.Direction.LEFT), left);
	    maze[position.x][position.y].setWall(relativeToReal(direction, Tile.Direction.BACK), back);

	    // if there is no wall in the current direction, add to the
	    // toVisitStack the cell we see
	    // and if there is the wall, set the same wall in the adiacent tile
	    if (!back)
	    {
		addTileToVisit(relativeToReal(direction, Tile.Direction.BACK));
	    }
	    else
	    {
		setAdiacentTile(relativeToReal(direction, Tile.Direction.BACK));
	    }
	    if (!right)
	    {
		addTileToVisit(relativeToReal(direction, Tile.Direction.RIGHT));
	    }
	    else
	    {
		setAdiacentTile(relativeToReal(direction, Tile.Direction.RIGHT));
	    }
	    if (!left)
	    {
		addTileToVisit(relativeToReal(direction, Tile.Direction.LEFT));
	    }
	    else
	    {
		setAdiacentTile(relativeToReal(direction, Tile.Direction.LEFT));
	    }
	    if (!ahead)
	    {
		addTileToVisit(relativeToReal(direction, Tile.Direction.AHEAD));
	    }
	    else
	    {
		setAdiacentTile(relativeToReal(direction, Tile.Direction.AHEAD));
	    }
	    // set the current tile as a visited tile
	    maze[position.x][position.y].setAsVisited();
	}
    }

    public void setFloor(Floor f)
    {
	maze[position.x][position.y].setFloor(f);
    }

    private void setEnclosure(Tile square[][], int dim)
    {

	for (int x = 0; x < dim; x++)
	{
	    for (int y = 0; y < dim; y++)
	    {
		if (x == 0)
		{
		    square[x][y].setWall(Direction.WEST, true);
		}
		else if (x == dim - 1)
		{
		    square[x][y].setWall(Direction.EAST, true);
		}
		if (y == 0)
		{
		    square[x][y].setWall(Direction.SOUTH, true);
		}
		else if (y == dim - 1)
		{
		    square[x][y].setWall(Direction.NORTH, true);
		}
	    }
	}
    }

    private void addTileToVisit(Direction dir)
    {
	Point toVisitPoint = null;
	if (dir == Direction.NORTH)
	{
	    toVisitPoint = new Point(position.x, position.y + 1);
	}
	else if (dir == Direction.EAST)
	{
	    toVisitPoint = new Point(position.x + 1, position.y);
	}
	else if (dir == Direction.WEST)
	{
	    toVisitPoint = new Point(position.x - 1, position.y);
	}
	else if (dir == Direction.SOUTH)
	{
	    toVisitPoint = new Point(position.x, position.y - 1);
	}
	// check if the tile is already present in the toVisit stack
	int i = 0;
	while (i < toVisit.size())
	{
	    if (toVisit.get(i).equals(toVisitPoint) && !toVisitPoint.equals(startingPos))
	    {
		toVisit.remove(i);
	    }
	    i++;
	}
	if (!toVisitPoint.equals(startingPos))
	{
	    toVisit.push(toVisitPoint);
	}
    }

    public void setVictim(boolean thereIs)
    {
	maze[position.x][position.y].setVictim(thereIs);
    }

    private void setMaxPriority()
    {
	for (int x = 0; x < dimension; x++)
	{
	    for (int y = 0; y < dimension; y++)
	    {
		maze[x][y].setPriority(dimension * dimension);
	    }
	}
    }

    public void setLevel(boolean level)
    {
	if (this.level != level)
	{
	    if (!level)
	    {
		maze = mazeGround;
		dimension = MAX_GROUND;
		dimension = MAX_GROUND;
		if (rampPoint != null)
		{
		    position = new Point(rampPoint.x, rampPoint.y);
		    rampPoint = null;
		}
	    }
	    else
	    {
		maze = mazeAir;
		dimension = MAX_AIR;
		dimension = MAX_AIR;
		rampPoint = new Point(position.x, position.y);
		setPosition(new Point(dimension / 2, dimension / 2));
	    }
	    this.level = level;
	}
    }

    private void createMaze(Tile maze[][], int dim)
    {
	for (int x = 0; x < dim; x++)
	{
	    for (int y = 0; y < dim; y++)
	    {
		maze[x][y] = new Tile(new Point(x, y));
	    }
	}
    }

    public void setPosition(Point p)
    {
	position = new Point(p.x, p.y);
    }

    public void setObstacle()
    {
	maze[position.x][position.y].setObstacle(false);
    }

    public Tile[][] getMazeGround()
    {
	Tile toRtn[][] = new Tile[MAX_GROUND][MAX_GROUND];
	for (int x = 0; x < MAX_GROUND; x++)
	{
	    for (int y = 0; y < MAX_GROUND; y++)
	    {
		toRtn[x][y] = mazeGround[x][y];
	    }
	}
	return toRtn;
    }

    public Tile[][] getMazeAir()
    {
	Tile toRtn[][] = new Tile[MAX_AIR][MAX_AIR];
	for (int x = 0; x < MAX_AIR; x++)
	{
	    for (int y = 0; y < MAX_AIR; y++)
	    {
		toRtn[x][y] = mazeAir[x][y];
	    }
	}
	return toRtn;
    }

    public boolean getLevel()
    {
	return level;
    }

    public Point getPosition()
    {
	return new Point(position.x, position.y);
    }

    private void printMaze()
    {
	for (int y = dimension - 1; y >= 0; y--)
	{
	    String str = "";
	    for (int x = 0; x < dimension; x++)
	    {
		str += maze[x][y].getPriority() + " ";
		if (maze[x][y].getPriority() < 10)
		{
		    str += " ";
		}
	    }
	    Log.d("laFigaGianluca maze", str);
	}
    }

    public Robot.Direction getDirection()
    {
	return direction;
    }

    private void setAdiacentTile(Direction dir)
    {
	if (dir == Direction.WEST && position.x > 0)
	{
	    maze[position.x - 1][position.y].setWall(relativeToReal(dir, Tile.Direction.BACK), true);
	}
	if (dir == Direction.NORTH && position.y < dimension)
	{
	    maze[position.x][position.y + 1].setWall(relativeToReal(dir, Tile.Direction.BACK), true);
	}
	if (dir == Direction.EAST && position.x < dimension)
	{
	    maze[position.x + 1][position.y].setWall(relativeToReal(dir, Tile.Direction.BACK), true);
	}
	if (dir == Direction.SOUTH && position.y > 0)
	{
	    maze[position.x][position.y - 1].setWall(relativeToReal(dir, Tile.Direction.BACK), true);
	}
    }

    private Direction relativeToReal(Direction robotDirection, Tile.Direction direction)
    {
	if (robotDirection == Direction.EAST)
	{
	    if (direction == Tile.Direction.LEFT)
	    {
		return Direction.NORTH;
	    }
	    if (direction == Tile.Direction.AHEAD)
	    {
		return Direction.EAST;
	    }
	    if (direction == Tile.Direction.RIGHT)
	    {
		return Direction.SOUTH;
	    }
	    if (direction == Tile.Direction.BACK)
	    {
		return Direction.WEST;
	    }
	}
	if (robotDirection == Direction.NORTH)
	{
	    if (direction == Tile.Direction.LEFT)
	    {
		return Direction.WEST;
	    }
	    if (direction == Tile.Direction.AHEAD)
	    {
		return Direction.NORTH;
	    }
	    if (direction == Tile.Direction.RIGHT)
	    {
		return Direction.EAST;
	    }
	    if (direction == Tile.Direction.BACK)
	    {
		return Direction.SOUTH;
	    }
	}
	if (robotDirection == Direction.WEST)
	{
	    if (direction == Tile.Direction.LEFT)
	    {
		return Direction.SOUTH;
	    }
	    if (direction == Tile.Direction.AHEAD)
	    {
		return Direction.WEST;
	    }
	    if (direction == Tile.Direction.RIGHT)
	    {
		return Direction.NORTH;
	    }
	    if (direction == Tile.Direction.BACK)
	    {
		return Direction.EAST;
	    }
	}
	if (robotDirection == Direction.SOUTH)
	{
	    if (direction == Tile.Direction.LEFT)
	    {
		return Direction.EAST;
	    }
	    if (direction == Tile.Direction.AHEAD)
	    {
		return Direction.SOUTH;
	    }
	    if (direction == Tile.Direction.RIGHT)
	    {
		return Direction.WEST;
	    }
	    if (direction == Tile.Direction.BACK)
	    {
		return Direction.NORTH;
	    }
	}
	return null;
    }

    private Tile.Direction realToRelative(Direction robotDirection, Direction dirToGo)
    {

	if (dirToGo == Direction.WEST)
	{
	    if (robotDirection == Direction.WEST)
	    {
		return Tile.Direction.AHEAD;
	    }
	    if (robotDirection == Direction.NORTH)
	    {
		return Tile.Direction.LEFT;
	    }
	    if (robotDirection == Direction.EAST)
	    {
		return Tile.Direction.BACK;
	    }
	    if (robotDirection == Direction.SOUTH)
	    {
		return Tile.Direction.RIGHT;
	    }
	}
	if (dirToGo == Direction.NORTH)
	{
	    if (robotDirection == Direction.WEST)
	    {
		return Tile.Direction.RIGHT;
	    }
	    if (robotDirection == Direction.NORTH)
	    {
		return Tile.Direction.AHEAD;
	    }
	    if (robotDirection == Direction.EAST)
	    {
		return Tile.Direction.LEFT;
	    }
	    if (robotDirection == Direction.SOUTH)
	    {
		return Tile.Direction.BACK;
	    }
	}
	if (dirToGo == Direction.EAST)
	{
	    if (robotDirection == Direction.WEST)
	    {
		return Tile.Direction.BACK;
	    }
	    if (robotDirection == Direction.NORTH)
	    {
		return Tile.Direction.RIGHT;
	    }
	    if (robotDirection == Direction.EAST)
	    {
		return Tile.Direction.AHEAD;
	    }
	    if (robotDirection == Direction.SOUTH)
	    {
		return Tile.Direction.LEFT;
	    }
	}
	if (dirToGo == Direction.SOUTH)
	{
	    if (robotDirection == Direction.WEST)
	    {
		return Tile.Direction.LEFT;
	    }
	    if (robotDirection == Direction.NORTH)
	    {
		return Tile.Direction.BACK;
	    }
	    if (robotDirection == Direction.EAST)
	    {
		return Tile.Direction.RIGHT;
	    }
	    if (robotDirection == Direction.SOUTH)
	    {
		return Tile.Direction.AHEAD;
	    }
	}
	return null;
    }
}
