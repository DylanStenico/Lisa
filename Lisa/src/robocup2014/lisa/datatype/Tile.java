package robocup2014.lisa.datatype;

import android.graphics.Point;

/**
 * Created by luca on 2/12/14.
 */
public class Tile {

	public enum Floor {
		WHITE_FLOOR, SILVER_FLOOR, BLACK_FLOOR;
	}

	public enum Direction {
		LEFT(0), AHEAD(1), RIGHT(2), BACK(3);
		
		private final int value;
		 
	    private Direction(int value) {
	        this.value = value;
	    }
	    public int toInt(){
	    	return value;
	    }
	    public static Direction toDirection(int arg0){
	    	Direction toRtn = Tile.Direction.AHEAD;
	    	if (arg0 == 0) toRtn = Tile.Direction.LEFT;
	        else if (arg0 == 1) toRtn = Tile.Direction.AHEAD;
	        else if (arg0 == 2) toRtn = Tile.Direction.RIGHT;
	        else if (arg0 == 3) toRtn = Tile.Direction.BACK;
	    	return toRtn;
	    }
	}

	private Floor floor;
	private boolean victim;
	private boolean[] walls;
	private boolean visited;
	private boolean obstacle;
	
	private int priority;
	private final Point position;
	
	public Tile(Point p) {
		
		walls = new boolean[4];
		for(int i = 0; i < 4; i++)
			walls[i] = false;
		floor = Floor.WHITE_FLOOR;
		victim = false;
		visited = false;
		position = p;
		obstacle = false;
	}

	public boolean isObstacle() {
		return obstacle;
	}

	public void setObstacle(boolean obstacle) {
		this.obstacle = obstacle;
	}

	public boolean isOpen(Direction dir) {
		return !walls[dir.toInt()];
	}

	@Override
	public String toString() {
		return position.toString();
	}

	public boolean getVictim() {
		return victim;
	}

	public Floor getFloor() {
		return floor;
	}

    public boolean getObstacle() { return obstacle; }

    public boolean getWall(Direction dir) {
        return walls[dir.toInt()];
    }

	public void setWall(Robot.Direction dir, boolean value) {
		walls[dir.toInt()] = value;
	}

	public void setFloor(Floor value) {
		floor = value;
	}
	public void setVictim(boolean isThere){
		victim = isThere;
	}
	public int getPriority(){
		return priority;
	}
	
	public void setPriority(int p){
		priority = p;
	}
	
	public Point getPoint(){
		return new Point(position.x, position.y);
	}

	public boolean isVisited() {
		return visited;
	}	
}
