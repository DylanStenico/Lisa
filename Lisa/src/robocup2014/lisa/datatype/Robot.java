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
public class Robot {

	public enum Direction{
		WEST(0), NORTH(1), EAST(2), SOUTH(3);
		private final int value;
		 
	    private Direction(int value) {
	        this.value = value;
	    }
	    private int toInt(){
	    	return value;
	    }
	}
	public final int MAX_GROUND = 25,
			MAX_AIR = 7;
	public final int startingPos;
	private Tile[][] mazeGround,
	mazeAir,
	maze;

	// dati per il campo
	private int dimension;
	boolean level;

	// per le celle vuote che vediamo da raggiungere
	private Stack<Point> toVisit;
	
	//il percorso corrente che abbiamo deciso di intraprendere
	Stack<Point> path;
	
	// posizione di Lisa
	private Point position;
	private Direction direction;
	private Tile.Direction lastDirectionTaken;

	public Robot() {

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
		startingPos = dimension/2;
		position = new Point(startingPos,startingPos);
		direction = Direction.NORTH;
		// create new path stack and add the starting position
		toVisit = new Stack<Point>();
		toVisit.push(position);
		path = new Stack<Point>();
	}


	public Tile.Direction getNextDir() {
		//se ho finito il path ne devo creare uno nuovo 
        if(path.empty()){
            // create the two main points: the starting one and the ending one
            Point start = new Point(position.x, position.y);

            Point end;
            do{
                end = toVisit.pop();
            }while(maze[end.x][end.y].isVisited() && end.x == 12 && end.y == 12);
            // set all the priority in the maze
            dijkstra(start, end);
            
            // calculate the path to reach the starting point from the arrive point
            Comparator<Tile> comparator = new PathComparator();
            PriorityQueue<Tile> queue;

            while (!end.equals(start)) {
                queue = new PriorityQueue<Tile>(1, comparator);
                Log.d("laFigaGianluca end", end.toString());
                Log.d("laFigaGianluca start", start.toString());
                // aggiungi alla coda prioritaria ogni nuova direzione aperta
                if (maze[end.x][end.y].isOpen(Tile.Direction.AHEAD)) {
                    queue.add(maze[end.x][end.y + 1]);
                    Log.d("laFigaGianluca added", maze[end.x][end.y + 1].getPoint().toString());
                }
                if (maze[end.x][end.y].isOpen(Tile.Direction.RIGHT)) {
                    queue.add(maze[end.x + 1][end.y]);
                    Log.d("laFigaGianluca added", maze[end.x + 1][end.y].getPoint().toString());
                }
                if (maze[end.x][end.y].isOpen(Tile.Direction.LEFT)) {
                    queue.add(maze[end.x - 1][end.y]);
                    Log.d("laFigaGianluca added", maze[end.x - 1][end.y].getPoint().toString());
                }
                if (maze[end.x][end.y].isOpen(Tile.Direction.BACK)) {
                    queue.add(maze[end.x][end.y - 1]);
                    Log.d("laFigaGianluca added", maze[end.x][end.y - 1].getPoint().toString());
                }
                Point tmp;
                tmp = queue.remove().getPoint();
                Log.d("laFigaGianluca points", end.toString());
                path.add(end);
                end = tmp;
            }
        }
        //estrai il punto dove andare e convertilo in direzioni relative
        Point currentPoint = path.pop();
        Tile.Direction toRtn = null;
        int dirToGo = 0;
        int robotDir = direction.toInt();
        Log.d("laFigaGianluca", currentPoint.toString() + "  " + position.toString());
        if      (currentPoint.x == position.x + 1) dirToGo = Tile.Direction.RIGHT.toInt();
        else if (currentPoint.x == position.x - 1) dirToGo = Tile.Direction.LEFT.toInt();
        else if (currentPoint.y == position.y + 1) dirToGo = Tile.Direction.AHEAD.toInt();
        else if (currentPoint.y == position.y - 1) dirToGo = Tile.Direction.BACK.toInt();
        
        dirToGo = Math.abs((dirToGo - robotDir) % 4) + 1;
        toRtn = Tile.Direction.toDirection(dirToGo);
        lastDirectionTaken = toRtn;
        Log.d("laFigaGianluca", toRtn.toString());
        //direction = Math.abs((dirToGo - robotDir) % 4) + 1;
        return toRtn;
	}

	// this method set all the tiles priority
	private void dijkstra(Point startingPoint, Point arrivePoint) {

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
		while (queue.size() > 0){
			// estract a currentTile from de queue
			Tile currentTile = queue.remove();
			// set best priority as the priority of the current tile + 1
			int priority = currentTile.getPriority() + 1;
//			Log.d("size laFigaGianluca", currentTile.getPoint().toString() + currentTile.isOpen(Tile.Direction.LEFT) + 
//																			 currentTile.isOpen(Tile.Direction.AHEAD) +
//																			 currentTile.isOpen(Tile.Direction.RIGHT) +
//																			 currentTile.isOpen(Tile.Direction.BACK));
			if (currentTile.isOpen(Tile.Direction.AHEAD)
					&& maze[currentTile.getPoint().x][currentTile.getPoint().y + 1]
							.getPriority() > priority) {
				if (maze[currentTile.getPoint().x][currentTile.getPoint().y + 1]
						.getFloor() != Floor.BLACK_FLOOR) {
					maze[currentTile.getPoint().x][currentTile.getPoint().y + 1]
							.setPriority(priority);
					queue.add(maze[currentTile.getPoint().x][currentTile
					                                         .getPoint().y + 1]);
				}
			}
			if (currentTile.isOpen(Tile.Direction.LEFT)
					&& maze[currentTile.getPoint().x - 1][currentTile
					                                      .getPoint().y].getPriority() > priority) {
				if (maze[currentTile.getPoint().x - 1][currentTile.getPoint().y]
						.getFloor() != Floor.BLACK_FLOOR) {
					maze[currentTile.getPoint().x - 1][currentTile.getPoint().y]
							.setPriority(priority);
					queue.add(maze[currentTile.getPoint().x - 1][currentTile
					                                             .getPoint().y]);
				}
			}
			if (currentTile.isOpen(Tile.Direction.RIGHT)
					&& maze[currentTile.getPoint().x + 1][currentTile
					                                      .getPoint().y].getPriority() > priority) {
				if (maze[currentTile.getPoint().x + 1][currentTile.getPoint().y]
						.getFloor() != Floor.BLACK_FLOOR) {
					maze[currentTile.getPoint().x + 1][currentTile.getPoint().y]
							.setPriority(priority);
					queue.add(maze[currentTile.getPoint().x + 1][currentTile
					                                             .getPoint().y]);
				}
			}
			if (currentTile.isOpen(Tile.Direction.BACK)
					&& maze[currentTile.getPoint().x][currentTile.getPoint().y - 1]
							.getPriority() > priority) {
				if (maze[currentTile.getPoint().x][currentTile.getPoint().y - 1]
						.getFloor() != Floor.BLACK_FLOOR) {
					maze[currentTile.getPoint().x][currentTile.getPoint().y - 1]
							.setPriority(priority);
					queue.add(maze[currentTile.getPoint().x][currentTile
					                                         .getPoint().y - 1]);
				}
			}
		}
	}

	public void setWalls(boolean left, boolean ahead, boolean right,
			boolean back) {
		//TODO control the robot direction to set the walls
		maze[position.x][position.y].setWall(Tile.Direction.AHEAD, ahead);
		maze[position.x][position.y].setWall(Tile.Direction.RIGHT, right);
		maze[position.x][position.y].setWall(Tile.Direction.LEFT, left);
		maze[position.x][position.y].setWall(Tile.Direction.BACK, back);

		if (!back)
			addToPath(Tile.Direction.BACK);
		if (!right)
			addToPath(Tile.Direction.RIGHT);
		if (!left)
			addToPath(Tile.Direction.LEFT);
		if (!ahead)
			addToPath(Tile.Direction.AHEAD);
	}

	public void setFloor(Floor f) {
		maze[position.x][position.y].setFloor(f);
	}

	private void setEnclosure(Tile square[][], int dim) {

		for (int x = 0; x < dim; x++) {
			for (int y = 0; y < dim; y++) {
				if (x == 0) {
					square[x][y].setWall(Tile.Direction.LEFT, true);
				} else if (x == dim - 1) {
					square[x][y].setWall(Tile.Direction.RIGHT, true);
				}
                if (y == 0) {
					square[x][y].setWall(Tile.Direction.BACK, true);
				} else if (y == dim - 1) {
					square[x][y].setWall(Tile.Direction.AHEAD, true);
				}
			}
		}
	}

	private void addToPath(Tile.Direction dir) {
		if (dir == Tile.Direction.AHEAD) {
			toVisit.push(new Point(position.x, position.y + 1));
		} else if (dir == Tile.Direction.RIGHT) {
			toVisit.push(new Point(position.x + 1, position.y));
		} else if (dir == Tile.Direction.LEFT) {
			toVisit.push(new Point(position.x - 1, position.y));
		} else if (dir == Tile.Direction.BACK) {
			toVisit.push(new Point(position.x, position.y - 1));
		}
	}

	public void setVictim(boolean thereIs) {
		maze[position.x][position.y].setVictim(thereIs);
	}

	private void setMaxPriority() {
		for (int x = 0; x < dimension; x++) {
			for (int y = 0; y < dimension; y++) {
				maze[x][y].setPriority(dimension * dimension);
			}
		}
	}

	public void setLevel(boolean level) {
		if(level){
			maze = mazeGround;
			dimension = MAX_GROUND;
			dimension = MAX_GROUND;
		}
		else{
			maze = mazeAir;
			dimension = MAX_AIR;
			dimension = MAX_AIR;
		}
		this.level = level;
	}

	private void createMaze(Tile maze[][], int dim) {
		for (int x = 0; x < dim; x++) {
			for (int y = 0; y < dim; y++) {
				maze[x][y] = new Tile(new Point(x, y));
			}
		}
	}
	public void setPosition(Point p){
        Log.d("handler", "x: " + p.x + " y: " + p.y);
		position = new Point(p.x, p.y);
	}

	public void setObstacle(){
		maze[position.x][position.y].setObstacle(false);
	}

	public Tile[][] getMazeGround(){
		Tile toRtn[][] = new Tile[MAX_GROUND][MAX_GROUND];
		for (int x = 0; x < MAX_GROUND; x++) {
			for (int y = 0; y < MAX_GROUND; y++) {
				toRtn[x][y] = mazeGround[x][y];
			}
		}
		return toRtn;
	}
	public Tile[][] getMazeAir(){
		Tile toRtn[][] = new Tile[MAX_AIR][MAX_AIR];
		for (int x = 0; x < MAX_AIR; x++) {
			for (int y = 0; y < MAX_AIR; y++) {
				toRtn[x][y] = mazeAir[x][y];
			}
		}
		return toRtn;
	}
	public boolean getLevel(){
		return level;
	}
	public Point getPosition(){
		return new Point(position.x, position.y);
	}
	private void printMaze(){
		for(int y = dimension -1; y >= 0; y--){
			String str = "";
			for(int x = 0; x < dimension; x++){
				str += maze[x][y].getPriority() + " ";
			}
			Log.d("laFigaGianluca maze", str);
		}
	}
}