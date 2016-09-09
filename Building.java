//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//The Building class is able to construct a command centre or barracks.
//If the Building is a command centre then it can train new SCV's. Also, SCV's return
//minerals to it. Barracks can train Marines.
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;
import java.awt.geom.*;
import java.io.*;
import java.applet.*;
import javax.sound.sampled.AudioSystem;

public class Building {

    private int gridX, gridY;				//(gridX,gridY) is top left corner of the bulding
    private int gridW, gridH; 				//Building is a rectangle with width gridW and height gridH
    private GamePanel panel;
    private SMap map;
    private MiniMap miniMap;
    private Player owner;
    private int cp; 						//the camp the building belongs to
    private int imgSize;
	//barracks (construction), barracks(training), barracks (regular)
    //command centre(construction), command centre (regular)
    private Image[] barCon, barTrain, barReg, comCon, comReg;
    private Image curImage, buildImage, regImage, trainImage, selCircle;
    private Image curSelCircle, buildSelCircle, regSelCircle;
    private String type;
    private String curStatus; 				//"building", "standing"
    private String curTask; 				//"training", "research", ..
    private int trainingTime, amountTrained;
    private int buildingTime, amountBuilt; 	//time necessary, time complete
    private int minCost; 					//mineral cost to build it -- Player needs array of these
    private int hp, maxHp;
    private int mMapUnitSize;
    private Color[] cpColor;
    private Point exitPos; 					//the place where new trained units are put
    private String trainingUnit; 			//the unit(s) the building can train

    private AudioClip finishedTrainingSound;

    private Point drawDelta;
    private Point regCircleDelta, buildCircleDelta, curCircleDelta;

    //private []Building necessaryBuildings; //buildings necessary to create - for Player
    private boolean[][][] visGrid;

    public Building(Player owner, String type, int x, int y, GamePanel panel, SMap map, MiniMap miniMap) {
        this.owner = owner;
        gridX = x;
        gridY = y;
        this.type = type;
        this.cp = owner.getCamp();
        this.panel = panel;
        this.map = map;
        this.miniMap = miniMap;
        amountBuilt = 0;
        curStatus = "building";
        curTask = "none";

        visGrid = owner.getVisGrid();

        trainingTime = 1000;
        amountTrained = 0;

        cpColor = new Color[2];
        cpColor[0] = new Color(0, 255, 0);
        cpColor[1] = new Color(255, 0, 0);

        loadImages();

        selCircle = new ImageIcon("./otherpics/buildingCircle.png").getImage();

        if (type.equals("barracks")) {
            drawDelta = new Point(-32, 0);
            buildCircleDelta = new Point(-16, 0);
            regCircleDelta = new Point(-12, -2);
            gridW = 4;
            gridH = 4;
            minCost = 150;
            buildingTime = 100; 	//80 seconds
            maxHp = 800;
            mMapUnitSize = 4;
            buildImage = barCon[cp];
            regImage = barReg[cp];
            trainImage = barTrain[cp];
            trainingUnit = "marine";
            finishedTrainingSound = Applet.newAudioClip(getClass().getResource("./sound/marine/tmardy00.wav"));
        } else if (type.equals("command centre")) {
            drawDelta = new Point(0, 0);
            buildCircleDelta = new Point(-16, -16);
            regCircleDelta = new Point(-20, -18);
            gridW = 4;
            gridH = 4;
            minCost = 150;
            buildingTime = 100;
            maxHp = 800;
            mMapUnitSize = 4;
            buildImage = comCon[cp];
            regImage = comReg[cp];
            trainImage = regImage;
            trainingUnit = "scv";
            finishedTrainingSound = Applet.newAudioClip(getClass().getResource("./sound/scv/tscrdy00.wav"));
        }

        curImage = buildImage;
        curSelCircle = buildSelCircle;
        curCircleDelta = buildCircleDelta;
        map.addBuilding(this);
        hp = maxHp;

    }

    //If the enemy can spot the building
    public boolean inEnemyVision() {
        for (int x = gridX; x < gridX + gridW; x++) {
            for (int y = gridY; y < gridY + gridH; y++) {
                if (visGrid[(cp + 1) % 2][x][y]) {
                    return true;
                }
            }
        }
        return false;
    }

    //Updates the building by checking if is done training, etc.
    public void update() {
        if (curStatus.equals("standing")) {
            if (curTask.equals("training")) {
                amountTrained += 1;
                if (amountTrained == trainingTime) {
                    Point p = findAvailablePlace();
                    if (p != null) {
                        amountTrained = 0;
                        owner.addNewUnit(trainingUnit, findAvailablePlace());
                        finishedTrainingSound.play();
                        stopTraining();
                    }
                }
            }
        } else if (curStatus.equals("building")) {
            amountBuilt += 1;
            if (amountBuilt == buildingTime) {
                changeStatus("standing");
                changeTask("");
                curImage = regImage;
                curSelCircle = regSelCircle;
                curCircleDelta = regCircleDelta;
            }
        }
    }

    //Checks if the building is dead, and if so, removes itself from player's list
    public void updateDeadStat() {
        if (isDead()) {
            owner.removeBuilding(this);
            changeStatus("death");
        }
    }

    //Adds minerals to the owner by the specified amount
    private void addMineralCount(int amount) {
        owner.addMineralCount(amount);
    }

    public void addMineralCount() {
        owner.addMineralCount(8);
    }

    //Deals d damage to its hp
    public void damageDealt(int d) {
        hp -= d;
        updateDeadStat();
    }

    //Changes its task to the new task
    public void changeTask(String newTask) {
        curTask = newTask;
    }

    //Trains a new unit 
    public void trainUnit() {
        changeTask("training");
        curImage = trainImage;
    }

    //Changes task back to nothing when done training a unit.
    public void stopTraining() {
        changeTask("");
        curImage = regImage;
    }

    //Finds an available place to put the new trained unit, and waits for one if doesn't find.
    public Point findAvailablePlace() {
        ArrayList<Point> tryPoints = new ArrayList<Point>();
        tryPoints.add(new Point(gridX - 1, gridY));
        tryPoints.add(new Point(gridX - 1, gridY + gridH - 1));
        tryPoints.add(new Point(gridX, gridY - 1));
        tryPoints.add(new Point(gridX, gridY + gridH));
        tryPoints.add(new Point(gridX + gridW - 1, gridY - 1));
        tryPoints.add(new Point(gridX + gridW - 1, gridY + gridH));
        tryPoints.add(new Point(gridX + gridW, gridY));
        tryPoints.add(new Point(gridX + gridW, gridY + gridH - 1));
        ArrayList<Integer> tempWalkableTerrain = new ArrayList<Integer>();
        tempWalkableTerrain.add(0);
        tempWalkableTerrain.add(2);
        for (Point p : tryPoints) {
            if (map.withinGrid(p) && map.isWalkable(tempWalkableTerrain, p) && !map.isOccupied(p)) {
                return p;
            }
        }
        return null;
    }

    //Changes status to the new status
    public void changeStatus(String newStat) {
        curStatus = newStat;
    }

    //Draws on the screen
    public void draw(Graphics g, GamePanel p) {
        Point screenCoords = getSCoord();
        g.drawImage(curImage, screenCoords.x + drawDelta.x, screenCoords.y + drawDelta.y, p);
    	//g.setColor(getColour());
        //g.fillRect(screenCoords.x, screenCoords.y, map.gridMapLength(gridW), map.gridMapLength(gridH));
    }

    //Draws a selected circle to indicate it's being selected
    public void drawSelectedCircle(Graphics g, GamePanel p) {
        if (curStatus != "death") {
            Point screenCoords = map.gridScreenCoord(new Point(gridX, gridY));
            g.drawImage(curSelCircle, screenCoords.x + curCircleDelta.x, screenCoords.y + curCircleDelta.y, p);
        }
    }

    //Returns true if it is contained within rectangle r
    public boolean inRect(Rectangle r) {
        Point mapCoord = map.gridMapCoord(new Point(gridX, gridY));
        return r.contains(mapCoord.x + mapWidth(), mapCoord.y + mapHeight());
    }

    public void drawOnMiniMap(Graphics g) {
        Point mPos = miniMap.gridtoMiniMapPos(gridX, gridY);
        g.setColor(cpColor[cp]);
        g.fillRect(mPos.x + mMapUnitSize / 2, mPos.y + mMapUnitSize / 2, mMapUnitSize, mMapUnitSize);
    }

    //Returns the screen coordinates
    public Point getSCoord() {
        return new Point(map.mapScreenCoord(map.gridMapCoord(new Point(gridX, gridY))));
    }

    public String status() {
        return curStatus;
    }

    public String task() {
        return curTask;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getCamp() {
        return cp;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public String getType() {
        return type;
    }

    public Point getCentre() {
        return new Point(gridX + gridW / 2, gridY + gridH / 2);
    }

    public Point getMapCentre() {
        return map.gridMapCoord(getCentre());
    }

    public int getWidth() {
        return gridW;
    }

    public int getHeight() {
        return gridH;
    }

    public int mapWidth() {
        return map.gridMapLength(gridW);
    }

    public int mapHeight() {
        return map.gridMapLength(gridH);
    }

    public String getTrainingUnit() {
        return trainingUnit;
    }

    public void loadImages() {
        barCon = new Image[2];
        barTrain = new Image[2];
        barReg = new Image[2];
        comReg = new Image[2];
        comCon = new Image[2];
        barCon[0] = new ImageIcon("./otherpics/playerBarracksUnderConstruction.png").getImage();
        barCon[1] = new ImageIcon("./otherpics/enemyBarracksUnderConstruction.png").getImage();
        barTrain[0] = new ImageIcon("./otherpics/playerBarracksOn.png").getImage();
        barTrain[1] = new ImageIcon("./otherpics/enemyBarracksOn.png").getImage();
        barReg[0] = new ImageIcon("./otherpics/playerBarracksOff.png").getImage();
        barReg[1] = new ImageIcon("./otherpics/enemyBarrackOff.png").getImage();
        comReg[0] = new ImageIcon("./otherpics/commandC.png").getImage();
        comReg[1] = new ImageIcon("./otherpics/enemyCommandC.png").getImage();
        comCon[0] = new ImageIcon("./otherpics/commandCUnderConstruction.png").getImage();
        comCon[1] = comCon[0];
        regSelCircle = new ImageIcon("./otherpics/regSelCircle.png").getImage();
        buildSelCircle = new ImageIcon("./otherpics/buildSelCircle.png").getImage();

    }

    //Checks if two buildings have the same top left corner
    public boolean equals(Building other) {
        return gridX == other.gridX && gridY == other.gridY;
    }
}
