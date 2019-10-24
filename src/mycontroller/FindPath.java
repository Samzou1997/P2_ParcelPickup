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
 * Created by 默默 on 2018/3/27.
 */

public class FindPath {
	public enum TileType { START, END, ROAD, WALL}

    //public static final Type WALL = MapTile.Type.WALL;
    
    private HashMap<Coordinate,MapTile> map = World.getMap();
    

    public class coordinateSystem implements Comparable<coordinateSystem> {
        public TileType type;
        public Coordinate coordinate = new Coordinate(0,0);
        public coordinateSystem parent;
        public int g;
        public int h;
        public int f;

        @Override
        public int compareTo(coordinateSystem o) {
            return f - o.f;
        }
    }

    //public PriorityQueue<coordinateSystem> openMap = new PriorityQueue<>();

    //private Set<coordinateSystem> closeMap = new HashSet<>();
    //函数
    private int G(coordinateSystem start, coordinateSystem now) {
        //如果斜着走，近似模拟斜边距离的比例
        if (start.coordinate.x != now.coordinate.x && start.coordinate.y != now.coordinate.y) {
            return Math.abs(start.coordinate.x - now.coordinate.x) * 14;
        }
        return (Math.abs(start.coordinate.x - now.coordinate.x) + Math.abs(start.coordinate.y - now.coordinate.y)) * 10;
    }
    //函数H 曼哈顿距离*10
    private int H(coordinateSystem end, coordinateSystem now) {
        return (Math.abs(end.coordinate.x - now.coordinate.x) + Math.abs(end.coordinate.y - now.coordinate.y)) * 10;
    }

    private int F(coordinateSystem start, coordinateSystem now, coordinateSystem end) {
        return G(start, now) + H(end, now);
    }

    public ArrayList<coordinateSystem> findPath(int startX, int startY, int endX, int endY) {
    	ArrayList<coordinateSystem> pathList = new ArrayList<FindPath.coordinateSystem>();
    	
    	PriorityQueue<coordinateSystem> openMap = new PriorityQueue<>();
    	Set<coordinateSystem> closeMap = new HashSet<>();
    	
        coordinateSystem[][] coords = convert(map);
        coordinateSystem startCoord = coords[startX][startY];
        coordinateSystem endCoord = coords[endX][endY];
        startCoord.g = 0;
        startCoord.h = H(endCoord, startCoord);
        startCoord.f = startCoord.g + startCoord.h;
        //1.把起始格添加到开启列表。
        openMap.add(startCoord);
        //2. 重复如下的工作：
        while (!openMap.isEmpty()) {
            //a) 寻找开启列表中F值最低的格子。我们称它为当前格。
            coordinateSystem nowCoord = openMap.poll();
            //b) 把它切换到关闭列表。
            closeMap.add(nowCoord);
            if (nowCoord.equals(endCoord)) {
                break;
            }
            //c) 对相邻的8格中的每一个
            for (coordinateSystem next : getNearByCoords(coords, nowCoord)) {
                //如果它不可通过或者已经在关闭列表中，略过它。反之如下。
                if (this.map.get(next.coordinate).isType(MapTile.Type.WALL) || closeMap.contains(next)) {
                    continue;
                }
                //如果它不在开启列表中。把当前格作为这一格的父节点。记录这一格的F,G,和H值，把它添加开启列表中。
                if (!openMap.contains(next)) {
                    next.parent = nowCoord;
                    next.g = nowCoord.g + G(nowCoord, next);
                    next.h = H(endCoord, next);
                    next.f = next.g + next.h;
                    //计算值后再放入开启列表中
                    openMap.add(next);
                } else {//如果它已经在开启列表中，用G值为参考检查新的路径是否更好。更低的G值意味着更好的路径。如果是这样，就把这一格的父节点改成当前格，并且重新计算这一格的G和F值。如果你保持你的开启列表按F值排序，改变之后你可能需要重新对开启列表排序。
                    if (next.g > nowCoord.g + G(nowCoord, next)) {
                        next.parent = nowCoord;
                        next.g = nowCoord.g + G(nowCoord, next);
                        next.f = next.g + next.h;
                    }
                }
            }
        }

        if (openMap.isEmpty()) {
            System.out.println("没有找到路径");
            return null;
        } 
        else {
            printPath(pathList, endCoord);
            return pathList;
            //System.out.println();
            //startCoord.value = 'S';
            //endCoord.value = 'E';

            //int col = World.MAP_WIDTH;
            //int row = World.MAP_HEIGHT;
            //for (int i = 0; i < row; i++) {
                //for (int j = 0; j < col; j++) {
                    //System.out.print(coords[i][j].value + " ");
                //}
                //System.out.println();
        }
    }

    private void printPath(ArrayList<coordinateSystem> pathList, coordinateSystem end) {
        if (end.parent != null) {
            //end.parent.value = '#';
            printPath(pathList, end.parent);
        }
        //System.out.print("(" + end.coordinate.x + "," + end.coordinate.y + ")-> ");
        pathList.add(end);
    }

    private List<coordinateSystem> getNearByCoords(coordinateSystem[][] coords, coordinateSystem nowCoord) {
        List<coordinateSystem> coordList = new LinkedList<>();
        //上
        if (nowCoord.coordinate.x - 1 >= 0) {
            coordList.add(coords[nowCoord.coordinate.x - 1][nowCoord.coordinate.y]);
        }
        //右上
        //if (nowCoord.coordinate.x - 1 >= 0 && nowCoord.coordinate.y + 1 < coords[0].length) {
            //coordList.add(coords[nowCoord.coordinate.x - 1][nowCoord.coordinate.y + 1]);
        //}
        //右
        if (nowCoord.coordinate.y + 1 < coords[0].length) {
            coordList.add(coords[nowCoord.coordinate.x][nowCoord.coordinate.y + 1]);
        }
        //右下
        //if (nowCoord.coordinate.x + 1 < coords.length && nowCoord.coordinate.y + 1 < coords[0].length) {
            //coordList.add(coords[nowCoord.coordinate.x + 1][nowCoord.coordinate.y + 1]);
        //}
        //下
        if (nowCoord.coordinate.x + 1 < coords.length) {
            coordList.add(coords[nowCoord.coordinate.x + 1][nowCoord.coordinate.y]);
        }
        //左下
        //if (nowCoord.coordinate.x + 1 < coords.length && nowCoord.coordinate.y - 1 >= 0) {
            //coordList.add(coords[nowCoord.coordinate.x + 1][nowCoord.coordinate.y - 1]);
        //}
        //左
        if (nowCoord.coordinate.y - 1 >= 0) {
            coordList.add(coords[nowCoord.coordinate.x][nowCoord.coordinate.y - 1]);
        }
        //左上
        //if (nowCoord.coordinate.x - 1 >= 0 && nowCoord.coordinate.y - 1 >= 0) {
            //coordList.add(coords[nowCoord.coordinate.x - 1][nowCoord.coordinate.y - 1]);
        //}
        return coordList;
    }

    private coordinateSystem[][] convert(HashMap<Coordinate,MapTile> map) {
    	//Coordinate testCoordinate = new Coordinate(0, 21);
    	//map.get(testCoordinate).getType();
        int col = World.MAP_HEIGHT;
        int row = World.MAP_WIDTH;
        //System.out.printf("X: %d, Y: %d\n",col, row);
        
        coordinateSystem[][] coords = new coordinateSystem[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                coordinateSystem coord = new coordinateSystem();
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
                //coord.type = map.get(coord.coordinate).getType();
                coords[i][j] = coord;
            }
        }
        return coords;
    }

}

