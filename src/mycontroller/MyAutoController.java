package mycontroller;

import controller.CarController;
import world.Car;
import java.util.HashMap;
import java.util.Iterator;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;

public class MyAutoController extends CarController{		
		// How many minimum units the wall is away from the player.
		private int wallSensitivity = 1;
		
		private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.
		
		// Car Speed to move at
		private final int CAR_MAX_SPEED = 1;
		
		private Coordinate targetPosition;
		
		public MyAutoController(Car car) {
			super(car);
		}
		
		// Coordinate initialGuess;
		// boolean notSouth = true;
		@Override
		public void update() {
			// Gets what the car can see
			HashMap<Coordinate, MapTile> currentView = getView();
			Iterator iter = currentView.entrySet().iterator();
			
			while (iter.hasNext()) {
				HashMap.Entry entry = (HashMap.Entry) iter.next();
				Coordinate coord = (Coordinate)entry.getKey();
				MapTile tile = (MapTile) entry.getValue();
				if (tile.getType() == MapTile.Type.TRAP) {
					targetPosition = coord;
					System.out.printf("Parcel found\n");
					break;
				}
				else {
					targetPosition = null;
					//System.out.printf("Parcel not found\n");
				}
			}
			
			// checkStateChange();
			if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
				applyForwardAcceleration();   // Tough luck if there's a wall in the way
			}
			
			if (targetPosition != null) {
				moveToParcel(targetPosition);
				//System.out.printf("!!!!!!!!!!!");
			}
			else {
				if (isFollowingWall) {
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
					// Start wall-following (with wall on left) as soon as we see a wall straight ahead
					if(checkWallAhead(getOrientation(),currentView)) {
						turnRight();
						isFollowingWall = true;
					}
				}
			}
		}
		
		private void moveToParcel(Coordinate parcelCoord) {
			WorldSpatial.Direction directionOfParcelOnX = null;
			WorldSpatial.Direction directionOfParcelOnY = null;
			WorldSpatial.Direction currentDirection = getOrientation();
			Coordinate currentPosition = new Coordinate(getPosition());
			
			if (currentPosition.x < parcelCoord.x) {
				directionOfParcelOnX = WorldSpatial.Direction.EAST;
			}
			else if (currentPosition.x > parcelCoord.x) {
				directionOfParcelOnX = WorldSpatial.Direction.WEST;
			}
			
			if (currentPosition.y < parcelCoord.y) {
				directionOfParcelOnY = WorldSpatial.Direction.NORTH;
			}
			else if (currentPosition.y > parcelCoord.y) {
				directionOfParcelOnY = WorldSpatial.Direction.SOUTH;
			}
			
			
			
			if (directionOfParcelOnX == null) {
				if (currentPosition.y < parcelCoord.y) {
					if (currentDirection == WorldSpatial.Direction.EAST) {
						turnLeft();
					}
					else if (currentDirection == WorldSpatial.Direction.WEST) {
						turnRight();
					}
				}
				
				if (currentPosition.y > parcelCoord.y) {
					if (currentDirection == WorldSpatial.Direction.EAST) {
						turnRight();
					}
					else if (currentDirection == WorldSpatial.Direction.WEST) {
						turnLeft();
					}
				}
			}
			
			if (directionOfParcelOnY == null) {
				if (currentPosition.x < parcelCoord.x) {
					if (currentDirection == WorldSpatial.Direction.NORTH) {
						turnRight();
					}
					else if (currentDirection == WorldSpatial.Direction.SOUTH) {
						turnLeft();
					}
				}
				
				if (currentPosition.x > parcelCoord.x) {
					if (currentDirection == WorldSpatial.Direction.NORTH) {
						turnLeft();
					}
					else if (currentDirection == WorldSpatial.Direction.SOUTH) {
						turnRight();
					}
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
				return checkNorth(currentView);
			case NORTH:
				return checkWest(currentView);
			case SOUTH:
				return checkEast(currentView);
			case WEST:
				return checkSouth(currentView);
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
