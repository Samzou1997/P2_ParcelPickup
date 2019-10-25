package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.badlogic.gdx.maps.tiled.TiledMap;

import tiles.MapTile;
import tiles.MapTile.Type;
import utilities.Coordinate;
import world.World;

/**
 * This is A star algorithm from https://blog.csdn.net/momo_ibeike/article/details/79712186
 * Reference https://blog.csdn.net/momo_ibeike/article/details/79712186
 */

public class FindPath {
	public enum TileType { START, END, ROAD, WALL}

    //public static final Type WALL = MapTile.Type.WALL;
    
    private HashMap<Coordinate,MapTile> map = World.getMap();
    

    public class CoordinateSystem implements Comparable<CoordinateSystem> {
        public TileType type;
        public Coordinate coordinate = new Coordinate(0,0);
        public CoordinateSystem parent;
        public int g;
        public int h;
        public int f;

        @Override
        public int compareTo(CoordinateSystem o) {
            return f - o.f;
        }
    }

    //public PriorityQueue<coordinateSystem> openMap = new PriorityQueue<>();

    //private Set<coordinateSystem> closeMap = new HashSet<>();
    private int G(CoordinateSystem start, CoordinateSystem now) {
        if (start.coordinate.x != now.coordinate.x && start.coordinate.y != now.coordinate.y) {
            return Math.abs(start.coordinate.x - now.coordinate.x) * 14;
        }
        return (Math.abs(start.coordinate.x - now.coordinate.x) + Math.abs(start.coordinate.y - now.coordinate.y)) * 10;
    }
    private int H(CoordinateSystem end, CoordinateSystem now) {
        return (Math.abs(end.coordinate.x - now.coordinate.x) + Math.abs(end.coordinate.y - now.coordinate.y)) * 10;
    }

    private int F(CoordinateSystem start, CoordinateSystem now, CoordinateSystem end) {
        return G(start, now) + H(end, now);
    }

    public ArrayList<CoordinateSystem> findPath(int startX, int startY, int endX, int endY) {
    	ArrayList<CoordinateSystem> pathList = new ArrayList<FindPath.CoordinateSystem>();
    	
    	PriorityQueue<CoordinateSystem> openMap = new PriorityQueue<>();
    	Set<CoordinateSystem> closeMap = new HashSet<>();
    	
        CoordinateSystem[][] coords = convert(map);
        CoordinateSystem startCoord = coords[startX][startY];
        CoordinateSystem endCoord = coords[endX][endY];
        startCoord.g = 0;
        startCoord.h = H(endCoord, startCoord);
        startCoord.f = startCoord.g + startCoord.h;
        openMap.add(startCoord);
        while (!openMap.isEmpty()) {
            CoordinateSystem nowCoord = openMap.poll();
            closeMap.add(nowCoord);
            if (nowCoord.equals(endCoord)) {
                break;
            }
            for (CoordinateSystem next : getNearByCoords(coords, nowCoord)) {
                if (this.map.get(next.coordinate).isType(MapTile.Type.WALL) || closeMap.contains(next)) {
                    continue;
                }
                if (!openMap.contains(next)) {
                    next.parent = nowCoord;
                    next.g = nowCoord.g + G(nowCoord, next);
                    next.h = H(endCoord, next);
                    next.f = next.g + next.h;
                    openMap.add(next);
                } else {
                    if (next.g > nowCoord.g + G(nowCoord, next)) {
                        next.parent = nowCoord;
                        next.g = nowCoord.g + G(nowCoord, next);
                        next.f = next.g + next.h;
                    }
                }
            }
        }

        if (openMap.isEmpty()) {
            System.out.println("No path found");
            return null;
        } 
        else {
            printPath(pathList, endCoord);
            return pathList;
        }
    }

    private void printPath(ArrayList<CoordinateSystem> pathList, CoordinateSystem end) {
        if (end.parent != null) {
            printPath(pathList, end.parent);
        }
        pathList.add(end);
    }

    private List<CoordinateSystem> getNearByCoords(CoordinateSystem[][] coords, CoordinateSystem nowCoord) {
        List<CoordinateSystem> coordList = new LinkedList<>();
        //up
        if (nowCoord.coordinate.x - 1 >= 0) {
            coordList.add(coords[nowCoord.coordinate.x - 1][nowCoord.coordinate.y]);
        }
        //right
        if (nowCoord.coordinate.y + 1 < coords[0].length) {
            coordList.add(coords[nowCoord.coordinate.x][nowCoord.coordinate.y + 1]);
        }
        //down
        if (nowCoord.coordinate.x + 1 < coords.length) {
            coordList.add(coords[nowCoord.coordinate.x + 1][nowCoord.coordinate.y]);
        }
        //left
        if (nowCoord.coordinate.y - 1 >= 0) {
            coordList.add(coords[nowCoord.coordinate.x][nowCoord.coordinate.y - 1]);
        }
        return coordList;
    }

    private CoordinateSystem[][] convert(HashMap<Coordinate,MapTile> map) {
        int col = World.MAP_HEIGHT;
        int row = World.MAP_WIDTH;
        
        CoordinateSystem[][] coords = new CoordinateSystem[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                CoordinateSystem coord = new CoordinateSystem();
                coord.coordinate.x = i;
                coord.coordinate.y = j;
                //System.out.printf("( %d, %d )\n",coord.coordinate.x, coord.coordinate.y);
                switch (map.get(coord.coordinate).getType()) {
				case WALL:
					coord.type = TileType.WALL;
				case ROAD:
					coord.type = TileType.ROAD;
				default:
					coord.type = TileType.ROAD;
				}
                coords[i][j] = coord;
            }
        }
        return coords;
    }

}

