package mycontroller;

import controller.CarController;
import mycontroller.FindPath.CoordinateSystem;
import world.Car;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.Direction;

public class MyAutoController extends CarController{	
	
		private enum drivingMode { SEARCHING, APPROACHING };
		
		private enum relativeDirection {FRONT, BACK, LEFT, RIGHT, ORIGIN};
		
		private drivingMode currentMode = drivingMode.SEARCHING;
		// How many minimum units the wall is away from the player.
		private int wallSensitivity = 1;
		
		private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.
		
		// Car Speed to move at
		private final int CAR_MAX_SPEED = 1;
		
		private Coordinate parcelPosition;
		
		private FindPath pathFinder = new FindPath();
		
		private ArrayList<Coordinate> blackList = new ArrayList<Coordinate>();
		
		public MyAutoController(Car car) {
			super(car);
		}
		
		// Coordinate initialGuess;
		// boolean notSouth = true;
		@Override
		public void update() {
			// Gets what the car can see
			HashMap<Coordinate, MapTile> currentView = getView();
			Coordinate currentPosition = new Coordinate(getPosition());
			ArrayList<CoordinateSystem> path = new ArrayList<FindPath.CoordinateSystem>();
			
			RadarDetection(currentView);
			
			//==============================================================
			
			// checkStateChange();
			//if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
				//applyForwardAcceleration();   // Tough luck if there's a wall in the way
			//}
			//===============================================================
			
			if (currentMode == drivingMode.APPROACHING) {
				//System.out.printf("======APROCHING MODE=======\n");
				//System.out.printf("target: ( %d, %d )\n", parcelPosition.x, parcelPosition.y);
				if(getSpeed() >= CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
					//applyBrake();   // Tough luck if there's a wall in the way
				}
				path = pathFinder.findPath(currentPosition.x, currentPosition.y, parcelPosition.x, parcelPosition.y);
				
				if (path != null) {
					for (CoordinateSystem coordSystem : path) {
						//System.out.printf("(%d, %d) -> ", coordSystem.coordinate.x, coordSystem.coordinate.y);
					}
					
					//System.out.print("\n");
					
					moveToParcel(path);
					isFollowingWall = false;
					
					
				}
				else {
					blackList.add(parcelPosition);
					PickUpParcel();
				}
				
				//System.out.print("BLACK LIST: ");
				for (Coordinate coordinate : blackList) {
					System.out.printf("(%d, %d)", coordinate.x, coordinate.y);
				}
				//System.out.print("\n");
			}
			else if (currentMode == drivingMode.SEARCHING){
				Searching(currentView);
			}
		}
		//========================================================================
		
		private relativeDirection getRelativeDirection(Direction d, Coordinate targetPosition) {
			Coordinate currentPositon = new Coordinate(getPosition());
			switch (d) {
			case NORTH:
				if (targetPosition.x > currentPositon.x) {return relativeDirection.RIGHT;}
				if (targetPosition.x < currentPositon.x) {return relativeDirection.LEFT;}
				if (targetPosition.y > currentPositon.y) {return relativeDirection.FRONT;}
				if (targetPosition.y < currentPositon.y) {return relativeDirection.BACK;}
				if (targetPosition.x == currentPositon.x && targetPosition.y == currentPositon.y) {return relativeDirection.ORIGIN;}
			case SOUTH:
				if (targetPosition.x > currentPositon.x) {return relativeDirection.LEFT;}
				if (targetPosition.x < currentPositon.x) {return relativeDirection.RIGHT;}
				if (targetPosition.y > currentPositon.y) {return relativeDirection.BACK;}
				if (targetPosition.y < currentPositon.y) {return relativeDirection.FRONT;}
				if (targetPosition.x == currentPositon.x && targetPosition.y == currentPositon.y) {return relativeDirection.ORIGIN;}
			case WEST:
				if (targetPosition.x > currentPositon.x) {return relativeDirection.BACK;}
				if (targetPosition.x < currentPositon.x) {return relativeDirection.FRONT;}
				if (targetPosition.y > currentPositon.y) {return relativeDirection.RIGHT;}
				if (targetPosition.y < currentPositon.y) {return relativeDirection.LEFT;}
				if (targetPosition.x == currentPositon.x && targetPosition.y == currentPositon.y) {return relativeDirection.ORIGIN;}
			case EAST:
				if (targetPosition.x > currentPositon.x) {return relativeDirection.FRONT;}
				if (targetPosition.x < currentPositon.x) {return relativeDirection.BACK;}
				if (targetPosition.y > currentPositon.y) {return relativeDirection.LEFT;}
				if (targetPosition.y < currentPositon.y) {return relativeDirection.RIGHT;}
				if (targetPosition.x == currentPositon.x && targetPosition.y == currentPositon.y) {return relativeDirection.ORIGIN;}

			default:
				return relativeDirection.ORIGIN;
			}
		}
		
		private void moveToParcel(ArrayList<CoordinateSystem> path) {
			
			Coordinate targetCoordinate = path.get(1).coordinate;
			
			relativeDirection directionOfTarget = getRelativeDirection(getOrientation(), targetCoordinate);
			
			//System.out.printf("next point: (%d, %d) at %s\n", targetCoordinate.x, targetCoordinate.y, directionOfTarget.toString());
			
			switch (directionOfTarget) {
			case FRONT:
				System.out.println("MOVING FORWARD");
				break;
			case LEFT:
				System.out.println("TURNING LEFT");
				turnLeft();
				break;
			case RIGHT:
				System.out.println("TURNING RIGHT");
				turnRight();
				break;
			case BACK:
				break;
			case ORIGIN:
				break;
			default:
				break;
				
			}
		}
		
		private void Searching(HashMap<Coordinate, MapTile> currentView) {
			//System.out.printf("======SEARCHING MODE=======\n");
			if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
				applyForwardAcceleration();   // Tough luck if there's a wall in the way
			}
			if (isFollowingWall) {
				//System.out.printf("===Following===\n");
				// If wall no longer on left, turn left
				if(!checkFollowingWall(getOrientation(), currentView)) {
					turnLeft();
				} else {
					// If wall on left and wall straight ahead, turn right
					if(checkWallAhead(getOrientation(), currentView)) {
						turnRight();
					}
				}
			} else {
				//System.out.printf("===NOT Following===\n");
				// Start wall-following (with wall on left) as soon as we see a wall straight ahead
				if(checkWallAhead(getOrientation(),currentView)) {
					if (checkWallOnRight(getOrientation(), currentView)) {
						turnLeft();
					}
					else {
						turnRight();
						isFollowingWall = true;
					}
				}
			}
		}
		
		private void PickUpParcel() {
			this.parcelPosition = null;
			this.currentMode = drivingMode.SEARCHING;
		}
		
		private void RadarDetection(HashMap<Coordinate, MapTile> currentView) {
			Iterator iter = currentView.entrySet().iterator();
			
			
			//detect the parcel
			//============================================================
			while (iter.hasNext()) {
				HashMap.Entry entry = (HashMap.Entry) iter.next();
				Coordinate coord = (Coordinate)entry.getKey();
				MapTile tile = (MapTile) entry.getValue();
				if (tile.getType() == MapTile.Type.TRAP && blackList.contains(coord) == false) {
					parcelPosition = coord;
					currentMode = drivingMode.APPROACHING;
					//System.out.printf("Parcel found\n");
					break;
				}
			}
		}

		/**
		 * Check if you have a wall in front of you!
		 * @param orientation the orientation we are in based on WorldSpatial
		 * @param currentView what the car can currently see
		 * @return
		 */
		private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
			switch(orientation){
			case EAST:
				return checkEast(currentView);
			case NORTH:
				return checkNorth(currentView);
			case SOUTH:
				return checkSouth(currentView);
			case WEST:
				return checkWest(currentView);
			default:
				return false;
			}
		}
		
		/**
		 * Check if the wall is on your left hand side given your orientation
		 * @param orientation
		 * @param currentView
		 * @return
		 */
		private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
			
			switch(orientation){
			case EAST:
				//isFollowingWall = checkNorth(currentView);
				return checkNorth(currentView);
			case NORTH:
				//isFollowingWall = checkNorth(currentView);
				return checkWest(currentView);
			case SOUTH:
				//isFollowingWall = checkNorth(currentView);
				return checkEast(currentView);
			case WEST:
				//isFollowingWall = checkNorth(currentView);
				return checkSouth(currentView);
			default:
				return false;
			}	
		}
		
		private boolean checkWallOnRight(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
			
			switch(orientation){
			case EAST:
				return checkSouth(currentView);
			case NORTH:
				return checkEast(currentView);
			case SOUTH:
				return checkWest(currentView);
			case WEST:
				return checkNorth(currentView);
			default:
				return false;
			}	
		}
		
		/**
		 * Method below just iterates through the list and check in the correct coordinates.
		 * i.e. Given your current position is 10,10
		 * checkEast will check up to wallSensitivity amount of tiles to the right.
		 * checkWest will check up to wallSensitivity amount of tiles to the left.
		 * checkNorth will check up to wallSensitivity amount of tiles to the top.
		 * checkSouth will check up to wallSensitivity amount of tiles below.
		 */
		public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
			// Check tiles to my right
			Coordinate currentPosition = new Coordinate(getPosition());
			for(int i = 0; i <= wallSensitivity; i++){
				MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
				if(tile.isType(MapTile.Type.WALL)){
					return true;
				}
			}
			return false;
		}
		
		public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
			// Check tiles to my left
			Coordinate currentPosition = new Coordinate(getPosition());
			for(int i = 0; i <= wallSensitivity; i++){
				MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
				if(tile.isType(MapTile.Type.WALL)){
					return true;
				}
			}
			return false;
		}
		
		public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
			// Check tiles to towards the top
			Coordinate currentPosition = new Coordinate(getPosition());
			for(int i = 0; i <= wallSensitivity; i++){
				MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
				if(tile.isType(MapTile.Type.WALL)){
					return true;
				}
			}
			return false;
		}
		
		public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
			// Check tiles towards the bottom
			Coordinate currentPosition = new Coordinate(getPosition());
			for(int i = 0; i <= wallSensitivity; i++){
				MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
				if(tile.isType(MapTile.Type.WALL)){
					return true;
				}
			}
			return false;
		}
		
	}
