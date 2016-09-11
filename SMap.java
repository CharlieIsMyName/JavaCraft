//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//This class keeps track of all information pertaining to the map, including the terrain,
//the units or buildings occupying different locations, etc. This class also has methods to
//return LinkedLists of points to be follows so that one can go from point A to point B on the 
//map (pathfinding).
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;
import java.awt.geom.*;
import java.io.*;
import java.util.*;

public class SMap {

    public static final int overGCost = 6; 	//overlapping G cost

    private Image mPic;
    private int ResX, ResY;
    private int mapX, mapY; 					//top left corner of the map pic;
    private int mapSizeX, mapSizeY;
    private boolean gridOn = false;
    private int gridSize = 32, numSquaresX, numSquaresY;
    private int[][] terGrid, originalTer;	  //plain-0, unwalkable-1, highground-2, wall-3, building-4, mineral-5
    private int[][] occupiedGrid; //0-nothing, 1-unit
    private int[][] overGGrid;	  //grid that keeps track of addiitonal Gcosts due to overlap in paths
    private Unit[][] unitGrid; 	  //stores the unit at a location
    private Building[][] buildGrid; 	//stores all buildings

    public String file;

    private boolean plain = false; //0
    private boolean tar = false;	//1
    private boolean highground = false; //2
    private boolean wall = false; //3

    private Camera cam;

    public SMap(Image i, int rx, int ry, Camera c, int msx, int msy, String file) {
        mapX = 0;
        mapY = 0;
        mPic = i;
        ResX = rx;
        ResY = ry;
        mapSizeX = msx;
        mapSizeY = msy;
        cam = c;
        this.file = file;

        numSquaresX = msx / gridSize;
        numSquaresY = msy / gridSize;

        terGrid = new int[numSquaresX][numSquaresY];
        originalTer = new int[numSquaresX][numSquaresY];
        occupiedGrid = new int[numSquaresX][numSquaresY];
        unitGrid = new Unit[numSquaresX][numSquaresY];
        buildGrid = new Building[numSquaresX][numSquaresY];
        overGGrid = new int[numSquaresX][numSquaresY];
    }

    public LinkedList<Point> findPath(ArrayList<Integer> walkable, int x1, int y1, int x2, int y2) {

        /**
         * **************************************************
         * //Finds a path using A* between 2 points on a grid and returns a
         * linked list of Point with first element //in the list as the starting
         * point and last element as destination. If no path exists returns //a
         * path to the closest grid element
         *
         * //Parameters should be grid coordinates, not coordinates on the
         * screen.
         *
         * //walkable - terrain values can be walked on
		****************************************************
         */
        int gridX1, gridY1, gridX2, gridY2;
        gridX1 = x1;
        gridX2 = x2;
        gridY1 = y1;
        gridY2 = y2;

		//If destination is unwalkable, then all unwalkable locations simply are given
        //a larger G cost, and once unit needs to move to an unwalkable location it stops
        boolean completePath = walkable.contains(terGrid[x2][y2]);

        LinkedList<Point> path = new LinkedList<Point>();
        Node dest = new Node(gridX2, gridY2, 0, 0, 0, null, null);
        Node start = new Node(gridX1, gridY1, 0, 0, 0, null, dest);
        NodeList open = new NodeList(getNumGrids());
        HashSet<String> closed = new HashSet<String>();
        boolean foundPath = false;

        open.insert(start);

        Node lowestH = start;
		//maxFCost is so that the pathfinder does not waste time looking at grids that
        //are unlikely to be helpful
        int maxFCost = isWalkable(walkable, new Point(gridX2, gridY2)) ? start.getF() * 4 : start.getF();

        while (open.size() > 0) {
            Node current = open.removeFirst();
            if (current.getH() < lowestH.getH()) {
                lowestH = current;
            }
            closed.add(current.getStrPoint());
            if (current.equals(dest)) {
                path = current.createPath(this, walkable);
                foundPath = true;
                break;
            }
            current.addNeighbours(walkable, maxFCost, this, open, closed, dest, completePath);
        }
        if (foundPath) {
            return path;
        }
        return lowestH.createPath(this, walkable);
    }

    //Loads all the values onto the different grids
    public void initialize() {
        loadTerrain(file);
        for (int x = 0; x < unitGrid.length; x++) {
            for (int y = 0; y < unitGrid.length; y++) {
                occupiedGrid[x][y] = 0;
                overGGrid[x][y] = 0;
                unitGrid[x][y] = null;
                buildGrid[x][y] = null;
            }
        }
    }

    //Updates the grids to add a new unit
    public void addUnit(Unit u) {
        occupiedGrid[u.getGridX()][u.getGridY()] = 1;
        unitGrid[u.getGridX()][u.getGridY()] = u;
        if (u.getType().equals("mineral")) {
            for (int dx = -1; dx < 2; dx++) {
                for (int dy = -1; dy < 2; dy++) {
                    if (withinGrid(u.getGridX() + dx, u.getGridY() + dy)) {
                        terGrid[u.getGridX() + dx][u.getGridY() + dy] = 5;
                    }
                }
            }
            //This place looks empty from the picture
            terGrid[u.getGridX() + 1][u.getGridY() + 1] = originalTer[u.getGridX() + 1][u.getGridY() + 1];
        }
    }

    //Removes a unit from the grids
    public void removeUnit(Unit u) {
        setEmpty(u.getGridPos());
    }

    //Adds a building to the grids
    public void addBuilding(Building b) {
        for (int x = b.getGridX(); x < b.getGridX() + b.getWidth(); x++) {
            for (int y = b.getGridY(); y < b.getGridY() + b.getHeight(); y++) {
                terGrid[x][y]=4;
                buildGrid[x][y] = b;
            }
        }
    }

    //Removes a building from the grids

    public void removeBuilding(Building b) {
        for (int x = b.getGridX(); x < b.getGridX() + b.getWidth(); x++) {
            for (int y = b.getGridY(); y < b.getGridY() + b.getHeight(); y++) {
                terGrid[x][y] = originalTer[x][y];
                buildGrid[x][y] = null;
            }
        }
    }

    //Returns true if [x][y] is within the bounds of the grid
    public boolean withinGrid(int x, int y) {
        return x >= 0 && y >= 0 && x < numSquaresX && y < numSquaresY;
    }

    public boolean withinGrid(Point p) {
        return withinGrid(p.x, p.y);
    }

	//Adds a Gvalue to a location because this location has been
    //added to a path
    public void addGVal(Point p) {
        overGGrid[p.x][p.y] += overGCost;
    }

    public void removeGVal(int x, int y) {
        overGGrid[x][y] -= overGCost;
    }

    public void removeGVal(Point p) {
        overGGrid[p.x][p.y] -= overGCost;
    }

    //Removes the added Gvalues from each location in a LinkedList of Point
    public void removeGVals(LinkedList<Point> path) {
        if (path != null) {
            Point temp = null;
            while (path.size() > 0) {
                temp = path.removeFirst();
                //System.out.println("removing " + temp);
                removeGVal(temp);
            }
        }
    }

    //Returns if a unit can walk on point p (in terms of terrain)
    public boolean isWalkable(ArrayList<Integer> walkablePts, Point p) {
        return walkablePts.contains(terGrid[p.x][p.y]);
    }

    //Sets a grid location as empty (this is for removing Units)
    public void setEmpty(int x, int y) {
        occupiedGrid[x][y] = 0;
        unitGrid[x][y] = null;
    }

    public void setEmpty(Point p) {
        setEmpty(p.x, p.y);
    }

    //Checks if a unit is at location p
    public boolean isOccupied(Point p) {
        return occupiedGrid[p.x][p.y] == 1;
    }

    public boolean isBuilding(Point p) {
        return terGrid[p.x][p.y] == 4;
    }

    //Returns top left coordinate of the big map relative to the screen
    public int[] biltInformation() {
        mapX = (int) ResX / 2 - cam.getPos()[0];
        mapY = (int) ResY / 2 - cam.getPos()[1];
        int[] tem = new int[2];
        tem[0] = mapX;
        tem[1] = mapY;
        return tem;
    }

    //Converts map coordinates to screen coordinates
    public Point mapScreenCoord(Point p) {
        return new Point(mapX + p.x, mapY + p.y);
    }

    //Converts screen coordinates to map coordinates

    public Point screenMapCoord(Point p) {
        return new Point(p.x - mapX, p.y - mapY);
    }

    //Converts map coordinates to grid position
    public Point mapGridPos(Point p) {
        return new Point((int) (p.x / gridSize), (int) (p.y / gridSize));
    }

    //Converts screen coordinates to grid position
    public Point screenGridPos(Point p) {
        return mapGridPos(screenMapCoord(p));
    }

    //Converts grid position to map coordinates
    public Point gridMapCoord(Point p) {
        return new Point(p.x * gridSize, p.y * gridSize);
    }

    public Point gridScreenCoord(Point p) {
        return mapScreenCoord(gridMapCoord(p));
    }

    //Returns map coordinates of the centre of a grid
    public Point gridCentre(Point p) {
        return new Point(gridSize / 2 + gridSize * p.x, gridSize / 2 + gridSize * p.y);
    }

	//Returns the unit located at point p
    //Assumes there is already a unit there! (if p is unoccupied, returns null)
    public Unit getUnit(Point p) {
        return unitGrid[p.x][p.y];
    }

    //Gets the building at a grid location
    public Building getBuilding(Point p) {
        return buildGrid[p.x][p.y];
    }

    public int gridMapLength(int d) {
        return d * gridSize;
    }

    public int getGridSize() {
        return gridSize;
    }

    //the number of squares in the grid
    public int getNumGrids() {
        return numSquaresX * numSquaresY;
    }

    public int getNumSquaresX() {
        return numSquaresX;
    }

    public int getNumSquaresY() {
        return numSquaresY;
    }

    public Image getImage() {
        return mPic;
    }

    public Point getMapSize() {
        return new Point(mapSizeX, mapSizeY);
    }

    public int[][] getTerGrid() {
        return terGrid;
    }

    public int[][] getOccGrid() {
        return occupiedGrid;
    }

    public int[][] getOverGGrid() {
        return overGGrid;
    }

    public Unit[][] getUnitGrid() {
        return unitGrid;
    }

    public Building[][] getBuildingGrid() {
        return buildGrid;
    }

    //Loads all the information from the given text file (about terrain)
    public Point[][] gridInformation() {
        Point tem;
        Point[][] ans = new Point[(int) (mapSizeX / gridSize) + (int) (mapSizeY / gridSize)][2];
        int counter = 0;
        for (int x = 0; x < (int) (mapSizeX / gridSize); x++) {
            tem = new Point(x * gridSize, 0);
            ans[counter][0] = mapScreenCoord(tem);
            tem = new Point(tem.x, mapSizeY);
            ans[counter][1] = mapScreenCoord(tem);
            counter++;
        }
        for (int y = 0; y < (int) (mapSizeY / gridSize); y++) {
            tem = new Point(0, y * gridSize);
            ans[counter][0] = mapScreenCoord(tem);
            tem = new Point(mapSizeX, tem.y);
            ans[counter][1] = mapScreenCoord(tem);
            counter++;
        }
        return ans;
    }

    public int approxH(Point start, Point dest) {
        return Math.abs(start.x - dest.x) * 10 + Math.abs(start.y - dest.y) * 10;
    }

	//Finds a good approximate destination to go to (a destination that is likely
    //to be traversed on the way to another destination)
    public Point findGoodDest(ArrayList<Integer> walkablePts, int appH, int x1, int y1, int x2, int y2) {
        int factor = 2;
        Point ans;
        if (appH < 500) { 	//and >300
            factor = 2;
        } else if (appH < 800) {
            factor = 3;
        } else {
            factor = 4;
        }
        ans = new Point(x1 + (x2 - x1) / factor, y1 + (y2 - y1) / factor);
        while (!(isWalkable(walkablePts, ans))) {
            ans.x += 1;
            ans.y += 1;
        }
        return ans;

    }

    //loads the map information from file to the grid
    public void loadTerrain(String file) {
        try {
            String tem;
            String[] tmp;
            int counter = 0;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((tem = reader.readLine()) != null) {
                tmp = tem.split(" ");
                for (int i = 0; i < terGrid[counter].length; i++) {
                    terGrid[counter][i] = Integer.parseInt(tmp[i]);
                    originalTer[counter][i] = terGrid[counter][i];
                }
                counter++;
            }
        } catch (IOException e) {
            System.out.println("rekt");
        }
    }
}
