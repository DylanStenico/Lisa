package robocup2014.lisa.datatype;

import java.util.Arrays;

import android.graphics.Point;

/**
 * Created by luca on 2/12/14.
 */
public class Tile {

	public enum Floor {
		WHITE_FLOOR(0), SILVER_FLOOR(1), BLACK_FLOOR(2);
		
		private final int value;
		 
	    private Floor(int value){
	        this.value = value;
	    }
	    private int toInt(){
	    	return value;
	    }
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
		return !walls[dirToInt(dir)];
	}

	@Override
	public String toString() {
		return "Tile [floor=" + floor + ", victim=" + victim + ", walls="
				+ Arrays.toString(walls) + ", visited=" + visited
				+ ", obstacle=" + obstacle + ", priority=" + priority
				+ ", position=" + position + "]\n";
	}

	public boolean getVictim() {
		return victim;
	}

	public Floor getFloor() {
		return floor;
	}

    public boolean getObstacle() { return obstacle; }

    public boolean getWall(Direction dir) {
        return walls[dirToInt(dir)];
    }

	public void setWall(Direction dir, boolean value) {
		walls[dirToInt(dir)] = value;
	}

	public void setFloor(Floor value) {
		floor = value;
	}

	private int dirToInt(Direction dir) {
		int i = 0;

		if (dir == Direction.AHEAD)
			i = 0;
		else if (dir == Direction.RIGHT)
			i = 1;
		else if (dir == Direction.BACK)
			i = 2;
		else if (dir == Direction.LEFT)
			i = 3;
		return i;
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
