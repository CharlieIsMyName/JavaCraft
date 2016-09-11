//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//GamePanel keeps track of what's happening in the game, as well as updates and runs it
//according to an RTS styled game
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.applet.*;
import javax.sound.sampled.AudioSystem;

class GamePanel extends JPanel implements MouseMotionListener, MouseListener, KeyListener {

    private boolean[] keys; 			//Boolean array of whether a keyboard key is being pushed
    private Point selectedPt; 			//the point that the mouse clicks on
    private Point oldPt, curPt; 		//the old and current Pt that have been left clicked
    //these variables are used to determine if the user double clicked
    private boolean newPoint;			//if there is a new selected point
    private boolean atkMove; 			//keeps track of if A+left click is used
    private Image mPic = new ImageIcon("./otherpics/map.png").getImage(); 	//The picture of the map
    private Image miniPic = new ImageIcon("./otherpics/minimap.png").getImage();
    private Image minPic = new ImageIcon("./otherpics/mineral.png").getImage();
    private Game mainFrame;
    private SMap map;
    private Camera cam;
    private MiniMap miniMap;
    private boolean[] keysUsing; 			//Array of the keys being used

    JLabel mineralText; 					//JLabel that will display the number of minerals user has

    private SpriteDataBase spriteBase;		//Main sprite database of the game
    //Soundbase contains all the sounds that are played based on different situations and different types of units
    private HashMap<String, HashMap<String, ArrayList<AudioClip>>> soundBase;
    private Point selectingPoint; 			//Point being selected
    private Rectangle selectingRect; 		//The rectangle selecting the units
    private Rectangle ansRect;				//the final rectangle considered for selecting unis
    private boolean hasRect = false; 			//keeps track of if the user is dragging the mouse to make a rectangle
    private boolean mouseLeftButtonWasPressed = false;

    private Player goodGuy; 				//The user
    private Player badGuy;					//The AI
    private Player neutral;					//Things like minerals

    private Vision vision; 					//Keeps track of what can be seen by a player
    private Random die = new Random();

    private int audioDelay, audioTimePassed; //audioTimePassed must be > audioDelay to play a new sound
    private int soundsPlayed, atkSounds; 	//the number of sounds being played in this frame
    private int maxSounds; 					//the maximum number of sounds that can be played at once
    private boolean mineralSound; 			//keeps track of if a mineral sound has already been played
    //rx,ry - resolution, mx,my - map size

    public GamePanel(Game m, int rx, int ry, int mx, int my) {
        setLayout(null);
        addMouseMotionListener(this);
        addMouseListener(this);
        setFocusable(true);
        addKeyListener(this);

        cam = new Camera(rx, ry, mx, my);

        map = new SMap(mPic, rx, ry, cam, mx, my, "mapInfo.txt");
        map.initialize();

        vision = new Vision(map);

        miniMap = new MiniMap(miniPic, map, cam, 0, 446, 224, 224);

        mainFrame = m;
        setSize(rx, ry);

        spriteBase = new SpriteDataBase();
        loadSoundBase();
        audioTimePassed = 100;
        audioDelay = 100;
        soundsPlayed = 0;
        maxSounds = 4;
        mineralSound = false;

        keys = new boolean[10000];
        keysUsing = new boolean[10000];
        selectedPt = null;
        oldPt = null;
        curPt = null;
        selectingRect = null;
        selectingPoint = null;
        ansRect = null;
        atkMove = false;

        goodGuy = new Player(this, 0);
        badGuy = new Player(this, 1);
        neutral = new Player(this, -1);

        mineralText = new JLabel(goodGuy.getMinerals() + "hello");
        mineralText.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        mineralText.setSize(120, 40);
        mineralText.setLocation(1014, 9);
        mineralText.setForeground(Color.green);
        add(mineralText);

        addBase1(goodGuy, neutral);
        addBase2(badGuy, neutral);
        addBase3(badGuy, neutral);
        addGroup1(badGuy);
        addGroup2(badGuy);

    }

    //Updates the events happening in the game.
    public void update() {
        mineralText.setText(goodGuy.getMinerals() + "");
        mineralSound = false;
        soundsPlayed = 0;
        atkSounds = 0;
        audioTimePassed += 1;
        if (newPoint) { 							//If a new destination is selected
            goodGuy.updateUnitDest(selectedPt, atkMove);
            if (audioTimePassed > audioDelay) {
                playSound(goodGuy.randomSelectedUnit(), "moving");
                audioTimePassed = 0;
            }
            newPoint = false;
            if (atkMove) {
                atkMove = false;
            }
        }
        goodGuy.searchTargets(badGuy, neutral);
        badGuy.searchTargets(goodGuy, neutral);
        goodGuy.updateUnits();
        badGuy.updateUnits();
        neutral.updateUnits();
        goodGuy.resetHp();
        badGuy.resetHp();
        updateAllBuildings();
        updateSelectionArea();
        updateSelection();
        updateVision();
    }

    //Moves the miniMap according to p
    public void updateMiniMapInfo(Point p) {
        miniMap.moveCamera(p.x, p.y);
    }

    //Updates the units being selected by the rectangle
    public void updateSelection() {
        if (ansRect != null) {
            if (ansRect.height < 32) {
                ansRect.height = 32;
            }
            if (ansRect.width < 32) {
                ansRect.width = 32;
            }
            if (!goodGuy.noUnitsInRect(ansRect)) {
                goodGuy.selectUnits(ansRect);
                playSound(goodGuy.randomSelectedUnit(), "select");
            } else if (!goodGuy.noBuildingsInRect(ansRect)) {
                goodGuy.selectBuilding(ansRect);
            }
            ansRect = null;
        }
    }

    //Plays a sound given the unit (the type) and the situation that it is played in
    public void playSound(Unit u, String situation) {
        if (u != null) {
            if (soundsPlayed < maxSounds && (!situation.equals("atk") || atkSounds < 2) && soundBase.containsKey(u.getType())) {
                if (!situation.equals("mining") || !mineralSound) {
                    soundsPlayed += 1;
                    if (situation.equals("atk")) {
                        atkSounds += 1;
                    } else if (situation.equals("mining")) {
                        mineralSound = true;
                    }
                    ArrayList<AudioClip> soundList = soundBase.get(u.getType()).get(situation);
                    soundList.get(die.nextInt(soundList.size())).play();
                }
            }

        }
    }

    //Updates the area that is currently being selected
    public void updateSelectionArea() {
        if (mouseLeftButtonWasPressed && !(miniMap.withinMiniMap(mousePos().x, mousePos().y))) {
            Point tmp = map.screenMapCoord(mousePos());
            selectingRect = new Rectangle(selectingPoint.x, selectingPoint.y, tmp.x - selectingPoint.x, tmp.y - selectingPoint.y);
        } else if (miniMap.withinMiniMap(mousePos().x, mousePos().y)) {
            selectingPoint = null;
            selectingRect = null;
            ansRect = null;
            mouseLeftButtonWasPressed = false;
        }
    }

    //Updates all buildings, good or bad
    public void updateAllBuildings() {
        goodGuy.updateBuildings();
        badGuy.updateBuildings();
    }

    //Updates the vision
    public void updateVision() {
        vision.update();
    }
    ////////////////////////////////////////////
    //Drawing methods

    public void drawAllUnits(Graphics g) {
        badGuy.drawUnits(g);
        goodGuy.drawUnits(g);
        neutral.drawUnits(g);
    }

    public void drawAllBuildings(Graphics g) {
        goodGuy.drawBuildings(g);
        badGuy.drawBuildings(g);
    }

    public void drawMiniMapCamera(Graphics g) {
        int szX = (int) ((double) (cam.getSize()[0]) / miniMap.ratioToMap());
        int szY = (int) ((double) (cam.getSize()[1]) / miniMap.ratioToMap());
        Point tem = miniMap.maptoMiniMapPos(cam.getPos()[0], cam.getPos()[1]);
        g.setColor(Color.white);
        g.drawRect(tem.x - szX / 2, tem.y - szY / 2, szX, szY);
    }

    public void drawAllMiniMapUnits(Graphics g) {
        badGuy.drawMiniMapUnits(g);
        goodGuy.drawMiniMapUnits(g);
    }

    public void drawAllMiniMapBuildings(Graphics g) {
        goodGuy.drawMiniMapBuildings(g);
        badGuy.drawMiniMapBuildings(g);
    }

    public void drawMiniMap(Graphics g) {
        g.drawImage(miniMap.getImage(), miniMap.getPos().x, miniMap.getPos().y, this);
    }

    public void drawSelectingRect(Graphics g) {
        if (selectingRect == null) {
            return;
        }
        g.setColor(Color.green);
        Rectangle tem = normalize(selectingRect);
        Point pt = map.mapScreenCoord(new Point(tem.x, tem.y));
        g.drawRect(pt.x, pt.y, tem.width, tem.height);
    }

    //Draws a circle under any selected units
    public void drawAllSelectedCircles(Graphics g) {
        goodGuy.drawSelectedCircles(g);
    }

    //Draws everything to do with the game on the screen
    public void paintComponent(Graphics g) {
        if (isVisible()) {
            //Draw map//////////////////////////
            int[] tem = map.biltInformation();
            int grid = map.getGridSize();
            g.drawImage(map.getImage(), tem[0], tem[1], this);
            g.drawImage(minPic, 980, 20, this);
            drawAllSelectedCircles(g);
            drawAllBuildings(g);
            drawAllUnits(g);
            drawSelectingRect(g);
            drawVision(g);
            drawMiniMap(g);
            drawAllMiniMapUnits(g);
            drawAllMiniMapBuildings(g);
            drawMiniMapCamera(g);
        }
    }

    //Makes the parts of the map that don't have user's units as darker
    public void drawVision(Graphics g) {
        vision.draw(g);
    }

    public SMap getMap() {
        return map;
    }

    public MiniMap getMiniMap() {
        return miniMap;
    }

    public SpriteDataBase getSpriteBase() {
        return spriteBase;
    }

    //Returns position of mouse relative to the screen

    public Point mousePos() {
        Point tem = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(tem, this);
        return tem;
    }

    //forces a rectangle to have positive width and height
    public Rectangle normalize(Rectangle r) {
        if (r.width < 0) {
            r.x = r.x + r.width;
            r.width = r.width * (-1);
        }
        if (r.height < 0) {
            r.y = r.y + r.height;
            r.height = r.height * (-1);
        }
        return r;
    }
    ////////////////////////////////////////////
    //Getter methods

    public boolean[][][] getVisGrid() {
        return vision.getVisGrid();
    }

    //Finds the first mineral within a range of 64 of a point
    public Unit pointInMineral(Point p) {
        ArrayList<Unit> tem = neutral.getUnits();
        for (Unit u : tem) {
            if (u.withinRange(64, p)) {
                return u;
            }
        }
        return null;
    }

    //Returns true if the user double clicked the mouse (based on the number of clicks in MouseEvent and 
    //the old and new positions of the mouse
    public boolean doubleClicked(MouseEvent e) {
        if (e.getClickCount() >= 2 && noMovement()) {
            return true;
        }
        return false;
    }

    //Returns if there was movement in the mouse between two clicks
    public boolean noMovement() {
        if (curPt == null || oldPt == null) {
            return false;
        }
        return dist(curPt, oldPt) < 12;
    }

    //Returns the integer distance between two points
    public int dist(Point a, Point b) {
        return (int) (Math.pow(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2), 0.5));
    }

    //Returns true when the user or enemy has no more units
    public boolean gameOver() {
        return goodGuy.getUnits().size() == 0 || badGuy.getUnits().size() == 0;
    }

    //Returns true if the enemy has no more units
    public boolean didPlayerWin() {
        return badGuy.getUnits().size() == 0;
    }

    // ------------ MouseListener ------------------------------------------
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        if (selectingRect != null) {
            ansRect = selectingRect;		//this is the rectangle they selected
            selectingRect = null;
            selectingPoint = null;
            mouseLeftButtonWasPressed = false;
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            selectedPt = map.screenGridPos(mousePos());
            if (map.isBuilding(selectedPt) && map.getBuilding(selectedPt).getCamp() == 0) {
                goodGuy.selectBuilding(map.getBuilding(selectedPt));
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            selectedPt = map.screenGridPos(mousePos());
            oldPt = curPt;
            curPt = mousePos();
            if (miniMap.withinMiniMap(mousePos().x, mousePos().y)) {
                if (keysUsing[KeyEvent.VK_A]) {
                    selectedPt = map.mapGridPos(miniMap.miniMapToMapPos(mousePos().x, mousePos().y));
                    atkMove = true;
                    keysUsing[KeyEvent.VK_A] = false;
                    newPoint = true;
                } else {
                    updateMiniMapInfo(mousePos());
                }
            } else {
                if (keysUsing[KeyEvent.VK_A]) {
                    if (map.isOccupied(selectedPt) && map.getUnit(selectedPt).getCamp() == 1) {
                        goodGuy.setTarget(map.getUnit(selectedPt));
                    } else if (map.isBuilding(selectedPt) && map.getBuilding(selectedPt).getCamp() == 1) {
                        goodGuy.setBTarget(map.getBuilding(selectedPt));
                    } else {
                        atkMove = true;
                        newPoint = true;
                    }
                    keysUsing[KeyEvent.VK_A] = false;
                } else if (mouseLeftButtonWasPressed == false) {
                    selectingPoint = map.screenMapCoord(new Point(mousePos().x, mousePos().y));
                    selectingRect = new Rectangle(selectingPoint.x, selectingPoint.y, 32, 32);

                    mouseLeftButtonWasPressed = true;
                    String tmp = "";
                    if (keys[KeyEvent.VK_CONTROL] || doubleClicked(e)) {
                        tmp = goodGuy.getFirstUnit(selectingRect);
                        if (!tmp.equals("")) {
                            selectingPoint = null;
                            selectingRect = null;
                            mouseLeftButtonWasPressed = false;
                            goodGuy.updateSelectedUnits(cam.getCameraRect(), tmp);
                        }
                    }
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (keysUsing[KeyEvent.VK_A]) {
                keysUsing[KeyEvent.VK_A] = false;
            }
            if (miniMap.withinMiniMap(mousePos().x, mousePos().y)) {
                selectedPt = map.mapGridPos(miniMap.miniMapToMapPos(mousePos().x, mousePos().y));
                newPoint = true;
            } else {
                selectedPt = map.screenGridPos(mousePos());
                newPoint = true;
            }
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            selectedPt = map.screenGridPos(mousePos());
            System.out.println(selectedPt);
        }
    }

    // ---------- MouseMotionListener ------------------------------------------

    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            updateMiniMapInfo(mousePos());
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    //KeyListener
    public void keyPressed(KeyEvent e) {
        int i = e.getKeyCode();
        keys[i] = true;
        if (keys[KeyEvent.VK_A]) {
            keysUsing[KeyEvent.VK_A] = true;
        }
        if (keys[KeyEvent.VK_S]) {
            goodGuy.stopSelUnits();
        }
        if (keys[KeyEvent.VK_T]) {
            goodGuy.trainUnit();
        }
        if(keys[KeyEvent.VK_ESCAPE]){
            goodGuy.cancelTraining();
        }
        if(keys[KeyEvent.VK_M]){
            goodGuy.trainMedic();
        }
        
        if (keys[KeyEvent.VK_CONTROL] && goodGuy.hasSelUnits()) {
            goodGuy.createControlGroup(keys);
        } else if (keys[KeyEvent.VK_SHIFT] && goodGuy.hasSelUnits()) {
            goodGuy.addUnitsToControlGroup(keys);
        } else {
            goodGuy.selectUnits(keys);
        }
    }

    public void keyReleased(KeyEvent e) {
        int i = e.getKeyCode();
        keys[i] = false;
    }

    public void keyTyped(KeyEvent e) {
    }

    /////////////////////////////////////////////
    //Adds custom base to top left of the screen. 
    //This should be given to the actual player

    public void addBase1(Player p, Player neutral) {
        neutral.addNewUnit("mineral", new Point(1, 0));
        neutral.addNewUnit("mineral", new Point(3, 0));
        neutral.addNewUnit("mineral", new Point(5, 0));
        neutral.addNewUnit("mineral", new Point(4, 1));
        neutral.addNewUnit("mineral", new Point(0, 3));
        neutral.addNewUnit("mineral", new Point(0, 1));
        neutral.addNewUnit("mineral", new Point(2, 1));
        p.addNewBuilding("command centre", new Point(5, 7));
        p.addNewBuilding("barracks", new Point(13, 3));
        p.addNewBuilding("barracks", new Point(24, 3));
        p.addNewUnit("scv", new Point(2, 6));
        p.addNewUnit("scv", new Point(3, 9));
        //p.addNewUnit("scv", new Point(6, 6));
        p.addNewUnit("marine", new Point(7, 3));
        p.addNewUnit("marine", new Point(8, 3));
        p.addNewUnit("marine", new Point(9, 3));
        p.addNewUnit("marine", new Point(10, 3));
        p.addNewUnit("marine", new Point(9, 4));
        p.addNewUnit("marine", new Point(8, 1));

        p.addNewUnit("medic", new Point(18, 3));
        p.addNewUnit("medic", new Point(20, 6));
    }

    //Adds custom base to the bottom right of the screen.
    //This is a rather strong base, and should be given to the enemy.
    public void addBase2(Player p, Player neutral) {
        neutral.addNewUnit("mineral", new Point(126, 126));
        neutral.addNewUnit("mineral", new Point(124, 126));
        neutral.addNewUnit("mineral", new Point(122, 126));
        neutral.addNewUnit("mineral", new Point(121, 126));
        neutral.addNewUnit("mineral", new Point(118, 126));
        neutral.addNewUnit("mineral", new Point(126, 125));
        p.addNewBuilding("command centre", new Point(116, 117));
        p.addNewBuilding("barracks", new Point(122, 110));
        p.addNewBuilding("barracks", new Point(109, 112));
        p.addNewUnit("marine", new Point(109, 116));
        p.addNewUnit("marine", new Point(108, 116));
        p.addNewUnit("marine", new Point(107, 117));
        p.addNewUnit("marine", new Point(106, 116));
        p.addNewUnit("marine", new Point(105, 114));
        p.addNewUnit("marine", new Point(104, 115));
        p.addNewUnit("marine", new Point(104, 117));
        p.addNewUnit("marine", new Point(102, 118));
        p.addNewUnit("marine", new Point(106, 119));
        
        p.addNewUnit("medic", new Point(109, 114));
        p.addNewUnit("medic", new Point(106, 118));
        p.addNewUnit("medic", new Point(102, 116));
    }

    //A custom base for the bottom left of the map.
    //This base is convenient to take over. Also should be given to the enemy
    public void addBase3(Player p, Player neutral) {
        neutral.addNewUnit("mineral", new Point(12, 126));
        neutral.addNewUnit("mineral", new Point(11, 126));
        neutral.addNewUnit("mineral", new Point(9, 126));
        p.addNewUnit("marine", new Point(10, 117));
        p.addNewUnit("marine", new Point(5, 104));
        p.addNewUnit("marine", new Point(7, 106));
        p.addNewUnit("marine", new Point(10, 107));
        p.addNewUnit("marine", new Point(12, 106));
        p.addNewUnit("marine", new Point(24, 103));
        p.addNewUnit("marine", new Point(26, 108));
        p.addNewUnit("marine", new Point(29, 102));
        p.addNewUnit("marine", new Point(29, 105));
        
        p.addNewUnit("medic", new Point(4, 108));
        p.addNewUnit("medic", new Point(26, 105));
        p.addNewUnit("medic", new Point(27, 105));
        p.addNewBuilding("command centre", new Point(18, 119));
    }

    //Adds a group of marines to the middle of the screen (on the left)
    public void addGroup1(Player p) {
        p.addNewUnit("marine", new Point(9, 47));
        p.addNewUnit("marine", new Point(17, 53));
        p.addNewUnit("marine", new Point(21, 49));
        p.addNewUnit("marine", new Point(20, 48));
        p.addNewUnit("marine", new Point(22, 45));
        p.addNewUnit("marine", new Point(24, 47));
        
        p.addNewUnit("medic", new Point(21, 46));
        p.addNewUnit("medic", new Point(26, 45));
    }

    //Adds a group of marines in the middle of the screen
    public void addGroup2(Player p) {
        p.addNewUnit("marine", new Point(57, 44));
        p.addNewUnit("marine", new Point(56, 46));
        p.addNewUnit("marine", new Point(62, 46));
        p.addNewUnit("marine", new Point(62, 48));
        p.addNewUnit("marine", new Point(62, 50));
        p.addNewUnit("marine", new Point(56, 51));
        p.addNewUnit("marine", new Point(53, 48));
        p.addNewUnit("marine", new Point(54, 46));
        
        p.addNewUnit("medic", new Point(56, 49));
        p.addNewUnit("medic", new Point(62, 45));
        p.addNewUnit("medic", new Point(55, 48));

    }

    //Loads all the sounds into the soundBase.
    public void loadSoundBase() {
        soundBase = new HashMap<String, HashMap<String, ArrayList<AudioClip>>>();
        HashMap<String, ArrayList<AudioClip>> marineBase = new HashMap<String, ArrayList<AudioClip>>();
        HashMap<String, ArrayList<AudioClip>> medicBase = new HashMap<String, ArrayList<AudioClip>>();
        HashMap<String, ArrayList<AudioClip>> scvBase = new HashMap<String, ArrayList<AudioClip>>();
        String marineDir = "./sound/marine/";
        String medicDir = "./sound/medic/";
        String scvDir = "./sound/scv/";
        String[] temp1 = {"marine", "medic", "scv"};
        String[] temp2 = {"death", "atk", "moving", "select"};
        for (String situation : temp2) {
            marineBase.put(situation, new ArrayList<AudioClip>());
            medicBase.put(situation, new ArrayList<AudioClip>());
            scvBase.put(situation, new ArrayList<AudioClip>());
        }
        scvBase.put("mining", new ArrayList<AudioClip>());

        marineBase.get("death").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmadth00.wav")));
        marineBase.get("death").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmadth01.wav")));
        marineBase.get("atk").add(Applet.newAudioClip(getClass().getResource(marineDir + "marineFire.wav")));
        marineBase.get("moving").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmayes00.wav")));
        marineBase.get("moving").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmayes01.wav")));
        marineBase.get("moving").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmayes02.wav")));
        marineBase.get("moving").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmayes03.wav")));
        marineBase.get("select").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmawht00.wav")));
        marineBase.get("select").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmawht01.wav")));
        marineBase.get("select").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmawht02.wav")));
        marineBase.get("select").add(Applet.newAudioClip(getClass().getResource(marineDir + "tmawht03.wav")));

        medicBase.get("death").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmddth00.wav")));
        medicBase.get("atk").add(Applet.newAudioClip(getClass().getResource(medicDir + "healSound.wav")));
        medicBase.get("moving").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmdyes00.wav")));
        medicBase.get("moving").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmdyes01.wav")));
        medicBase.get("moving").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmdyes02.wav")));
        medicBase.get("moving").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmdyes03.wav")));
        medicBase.get("select").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmdwht00.wav")));
        medicBase.get("select").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmdwht01.wav")));
        medicBase.get("select").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmdwht02.wav")));
        medicBase.get("select").add(Applet.newAudioClip(getClass().getResource(medicDir + "tmdwht03.wav")));

        scvBase.get("death").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscdth00.wav")));
        scvBase.get("atk").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscyes00.wav")));
        scvBase.get("atk").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscyes01.wav")));
        scvBase.get("atk").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscyes02.wav")));
        scvBase.get("atk").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscyes03.wav")));
        scvBase.get("moving").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscpss00.wav")));
        scvBase.get("select").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscwht00.wav")));
        scvBase.get("select").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscwht01.wav")));
        scvBase.get("select").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscwht02.wav")));
        scvBase.get("select").add(Applet.newAudioClip(getClass().getResource(scvDir + "tscwht03.wav")));
        scvBase.get("mining").add(Applet.newAudioClip(getClass().getResource(scvDir + "/mining sound/edrrep00.wav")));
        scvBase.get("mining").add(Applet.newAudioClip(getClass().getResource(scvDir + "/mining sound/edrrep01.wav")));
        scvBase.get("mining").add(Applet.newAudioClip(getClass().getResource(scvDir + "/mining sound/edrrep02.wav")));
        scvBase.get("mining").add(Applet.newAudioClip(getClass().getResource(scvDir + "/mining sound/edrrep03.wav")));
        scvBase.get("mining").add(Applet.newAudioClip(getClass().getResource(scvDir + "/mining sound/edrrep04.wav")));

        soundBase.put("marine", marineBase);
        soundBase.put("medic", medicBase);
        soundBase.put("scv", scvBase);
    }

    public void addNotify() {
        super.addNotify();
        requestFocus();
        mainFrame.start();
    }

    public void moveCam() {
        Point tem = mousePos();
        cam.move(tem.x, tem.y);
    }
}
