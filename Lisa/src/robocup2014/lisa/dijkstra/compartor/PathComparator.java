package robocup2014.lisa.dijkstra.compartor;

import java.util.Comparator;

import robocup2014.lisa.datatype.Tile;


public class PathComparator implements Comparator<Tile> {

	@Override
	public int compare(Tile arg0, Tile arg1) {

		if(arg0.getPriority() < arg1.getPriority())
			return -1;

		else if(arg0.getPriority() > arg1.getPriority())
			return 1;
//		else{
//			if(arg0.isVisited() && !arg1.isVisited()){
//				return -1;
//			}
//			else if(!arg0.isVisited() && arg1.isVisited()){
//				return 1;
//			}
//		}
		return 0;
	}
}
