//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//The Player class allows one to create Units and Buildings and keep track of them
//If the player is the actual user, they can select Units/Buildings and choose when 
//to train new units, etc. Also keeps track of minerals
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.applet.*;
import javax.sound.sampled.AudioSystem;

public class Player {

    private ArrayList<Unit> units; 					//the units the player has
    private LinkedList<Unit> finishedUnits;			//the units that are going to be deleted (died)
    private LinkedList<Building> finishedBuildings;	//the buildings that are going to be deleted (destroyed)
    private ArrayList<Unit> selectedUnits;			//the units currently being selected
    private ArrayList<ArrayList<Unit>> controlGroups = new ArrayList<ArrayList<Unit>>();
    private ArrayList<Building> buildings;
    private Building selectedBuilding;
    private int cp; 								//the camp of the player (0-user, 1-AI)
    private GamePanel panel;
    private SMap map;
    private MiniMap miniMap;
    private SpriteDataBase spriteBase;
    private boolean[][][] visGrid;
    private int minerals; 							//the number of minerals user has
    private HashMap<String, Integer> trainCosts; 	//the costs associated with training different units
    private Random die = new Random();

    public Player(GamePanel p, int cp) {
        panel = p;
        map = panel.getMap();
        miniMap = panel.getMiniMap();
        spriteBase = p.getSpriteBase();
        this.cp = cp;

        trainCosts = new HashMap<String, Integer>();
        trainCosts.put("marine", 50);
        trainCosts.put("scv", 80);

        units = new ArrayList<Unit>();
        finishedUnits = new LinkedList<Unit>();
        finishedBuildings = new LinkedList<Building>();
        selectedUnits = new ArrayList<Unit>();
        buildings = new ArrayList<Building>();
        selectedBuilding = null;
        for (int i = 0; i < 10; i++) {
            controlGroups.add(null);
        }

        minerals = 100;
    }

    //Returns a random unit from the selected Units
    public Unit randomSelectedUnit() {
        if (selectedUnits.size() == 0) {
            return null;
        }
        return selectedUnits.get(die.nextInt(selectedUnits.size()));
    }

    //Resets the HP of all the units to proper range
    public void resetHp() {
        for (Unit u : units) {
            u.resetHp();
        }
    }

    //Adds d to the mineral count
    public void addMineralCount(int d) {
        minerals += d;
    }

    //Returns the nearest command centre to point p
    public Building nearestCommandCentre(Point p) {
        ArrayList<Building> cc = new ArrayList<Building>();
        ArrayList<Integer> dist = new ArrayList<Integer>();
        for (Building b : buildings) {
            if (b.getType().equals("command centre")) {
                cc.add(b);
                dist.add(dist(b.getMapCentre(), p));
            }
        }
        int min = 100000;
        for (int d : dist) {
            if (d < min) {
                min = d;
            }
        }
        return cc.get(dist.indexOf(min));

    }

    //Adds a new unit based on type and position
    public void addNewUnit(String type, Point pos) {
        if (pos == null) {
            return;
        }
        units.add(new Unit(this, panel, spriteBase, type, pos.x, pos.y, map, miniMap));
    }

    //If selected is true, then this added unit is also added to the selected list

    public void addNewUnit(String type, Point pos, boolean selected) {
        if (pos == null) {
            return;
        }
        Unit newUnit = new Unit(this, panel, spriteBase, type, pos.x, pos.y, map, miniMap);
        units.add(newUnit);
        if (selected) {
            selectedUnits.add(newUnit);
        }
    }

    //With a target specified, the unit starts moving to this destination right away

    public void addNewUnit(String type, Point pos, Point target) {
        if (pos == null) {
            return;
        }
        Unit newUnit = new Unit(this, panel, spriteBase, type, pos.x, pos.y, map, miniMap);
        units.add(newUnit);
        newUnit.updateDest(target, true);
    }

    //Removes a unit from the map and from all selected units
    //This is done when a unit dies. The unit is later removed from the unit list
    public void removeUnit(Unit u) {
        map.removeUnit(u);
        if (selectedUnits.contains(u)) {
            selectedUnits.remove(u);
        }
        for (ArrayList<Unit> group : controlGroups) {
            if (group != null && group.contains(u)) {
                group.remove(u);
            }
        }

    }

    //Sets a target for all selected units
    public void setTarget(Unit target) {
        for (Unit u : selectedUnits) {
            u.setTarget(target);
        }
    }

    //Selects a building target for all selected units

    public void setBTarget(Building k) {
        for (Unit u : selectedUnits) {
            u.setBTarget(k);
        }
    }

    //When a building dies. Like removeUnit
    public void removeBuilding(Building k) {
        if (selectedBuilding != null && selectedBuilding.equals(k)) {
            selectedBuilding = null;
        }
        map.removeBuilding(k);
        finishedBuildings.add(k);
    }

    //Adds a unit to the queue of units about to be remove from the unit list
    public void addFinishedUnit(Unit u) {
        finishedUnits.add(u);
    }

    //Adds a new building based on the type and position
    public void addNewBuilding(String type, Point pos) {
        if (pos == null) {
            return;
        }
        Building temp=new Building(this, type, pos.x, pos.y, panel, map, miniMap);
        buildings.add(temp);
    }

    //Trains a new unit according to the building selected, if player has enough minerals.
    public void trainUnit() {
        if (selectedBuilding == null || selectedBuilding.status().equals("building")
                || selectedBuilding.task().equals("training") || selectedBuilding.task().equals("medic")) {
            return;
        }
        String unitTrained = selectedBuilding.getTrainingUnit();
        if (minerals >= trainCosts.get(unitTrained)) {
            selectedBuilding.trainUnit();
            minerals -= trainCosts.get(unitTrained);
        }
    }
    
    public void trainMedic() {
        if (selectedBuilding == null || selectedBuilding.status().equals("building")
                || selectedBuilding.task().equals("training") || selectedBuilding.task().equals("medic")) {
            return;
        }
        
        if(selectedBuilding.getType().equals("command centre")){
            return;
        }
        
        String unitTrained = selectedBuilding.getTrainingUnit();            //medic has the same cost as marine
        if (minerals >= trainCosts.get(unitTrained)) {
            selectedBuilding.trainMedic();
            minerals -= trainCosts.get(unitTrained);
        }
    }

    public void cancelTraining(){
        if (selectedBuilding == null || selectedBuilding.status().equals("building")) {
            return;
        }
        
        if(selectedBuilding.task().equals("training") || selectedBuilding.task().equals("medic")){
            selectedBuilding.stopTraining();
            String unitTrained = selectedBuilding.getTrainingUnit();
            minerals += trainCosts.get(unitTrained);
        }
    }
    //Updates the destination of all selected units
    public void updateUnitDest(Point newDest, boolean atk) {
        for (Unit u : selectedUnits) {
            u.updateDest(newDest, atk);
        }
    }

    //Searches targets for all the units
    public void searchTargets(Player target, Player neutral) {
        for (Unit u : units) {
            u.searchForTarget(target, this, neutral);
        }
    }

    //Returns the vision grid
    public boolean[][][] getVisGrid() {
        return panel.getVisGrid();
    }

    //Updates all units and then removes the ones that are ready to be removed
    public void updateUnits() {
        for (Unit u : units) {
            u.update();
        }
        while (finishedUnits.size() > 0) {
            units.remove(finishedUnits.pop());
        }
    }

    //Updates all buildings

    public void updateBuildings() {
        for (Building k : buildings) {
            k.update();
        }
        while (finishedBuildings.size() > 0) {
            buildings.remove(finishedBuildings.pop());
        }
    }

    //Returns the type of the first unit found in a rectangle. If no units found, returns ""
    public String getFirstUnit(Rectangle r) {
        String ans = "";
        for (Unit u : units) {
            if (u.inRect(r)) {
                ans = u.getType();
                break;
            }
        }
        return ans;
    }

    //Empties selectedUnits and adds all units within a rect to the selectedUnits
    public void updateSelectedUnits(Rectangle r, String typ) {
        selectedUnits = new ArrayList<Unit>();
        selectedBuilding = null;
        for (Unit u : units) {
            if (u.inRect(r) && u.getType().equals(typ) && selectedUnits.size() < 12) {
                selectedUnits.add(u);
            }
        }
    }

    //Stops all selected units from moving.
    public void stopSelUnits() {
        for (Unit u : selectedUnits) {
            u.stopMoving();
        }
    }

    //Returns an ArrayList of Unit with the same elements as a
    public ArrayList<Unit> clone(ArrayList<Unit> a) {
        ArrayList<Unit> tem = new ArrayList<Unit>();
        for (Unit u : a) {
            tem.add(u);
        }
        return tem;
    }

    //Creates a new control group and adds all units in selectedUnits to it
    public void createControlGroup(boolean[] keys) {
        if (keys[KeyEvent.VK_1]) {
            controlGroups.set(1, clone(selectedUnits));
        }
        if (keys[KeyEvent.VK_2]) {
            controlGroups.set(2, clone(selectedUnits));
        }
        if (keys[KeyEvent.VK_3]) {
            controlGroups.set(3, clone(selectedUnits));
        }
        if (keys[KeyEvent.VK_4]) {
            controlGroups.set(4, clone(selectedUnits));
        }
        if (keys[KeyEvent.VK_5]) {
            controlGroups.set(5, clone(selectedUnits));
        }
    }

    //Returns true if player has at least one unit selected
    public boolean hasSelUnits() {
        //selectedUnits != null ?
        return selectedUnits.size() > 0;
    }

    //Adds units to the control group specified
    public void addUnitsToControlGroup(boolean[] keys) {
        if (keys[KeyEvent.VK_1]) {
            if(controlGroups.get(1)==null){
                controlGroups.set(1, clone(selectedUnits));
            }
            addUnitsToControlGroup(1);
        }
        if (keys[KeyEvent.VK_2]) {
            if(controlGroups.get(2)==null){
                controlGroups.set(2, clone(selectedUnits));
            }
            addUnitsToControlGroup(2);
        }
        if (keys[KeyEvent.VK_3]) {
            if(controlGroups.get(3)==null){
                controlGroups.set(3, clone(selectedUnits));
            }
            addUnitsToControlGroup(3);
        }
        if (keys[KeyEvent.VK_4]) {
            if(controlGroups.get(4)==null){
                controlGroups.set(4, clone(selectedUnits));
            }
            addUnitsToControlGroup(4);
        }
        if (keys[KeyEvent.VK_5]) {
            if(controlGroups.get(5)==null){
                controlGroups.set(5, clone(selectedUnits));
            }
            addUnitsToControlGroup(5);
        }
    }

    //Adds units to the control group specified
    private void addUnitsToControlGroup(int index) {
        ArrayList<Unit> tem = controlGroups.get(index);
        for (Unit u : selectedUnits) {
            if (tem.size() >= 12) {
                break;
            }
            tem.add(u);
        }
    }

    //Selects the units within a specific control group.
    public void selectUnits(boolean[] keys) {
        boolean flag = false;
        if (keys[KeyEvent.VK_1] && controlGroups.get(1) != null) {
            selectedUnits = controlGroups.get(1);
            flag = true;
        }
        if (keys[KeyEvent.VK_2] && controlGroups.get(2) != null) {
            selectedUnits = controlGroups.get(2);
            flag = true;
        }
        if (keys[KeyEvent.VK_3] && controlGroups.get(3) != null) {
            selectedUnits = controlGroups.get(3);
            flag = true;
        }
        if (keys[KeyEvent.VK_4] && controlGroups.get(4) != null) {
            selectedUnits = controlGroups.get(4);
            flag = true;
        }
        if (keys[KeyEvent.VK_5] && controlGroups.get(5) != null) {
            selectedUnits = controlGroups.get(5);
            flag = true;
        }
        if (flag) {
            selectedBuilding = null;
        }
    }

    //Selects the units within a rectangle
    public void selectUnits(Rectangle r) {
        selectedUnits = new ArrayList<Unit>();
        selectedBuilding = null;
        for (Unit u : units) {
            if (selectedUnits.size() >= 12) {
                break;
            }
            if (u.inRect(r) && u.getCamp() == 0) {
                selectedUnits.add(u);
            }
        }
    }

    //Selects a building (when a building is selected, units cannot be selected)
    public void selectBuilding(Building k) {
        selectedBuilding = k;
        selectedUnits = new ArrayList<Unit>();
    }

    //Selects the first building found within a rectangle
    public void selectBuilding(Rectangle r) {
        selectedUnits = new ArrayList<Unit>();
        for (Building k : buildings) {
            if (k.inRect(r) && k.getCamp() == 0) {
                selectedBuilding = k;
                break;
            }
        }
    }

    //Returns if there are any units in a rectangle
    public boolean noUnitsInRect(Rectangle r) {
        for (Unit u : units) {
            if (u.inRect(r)) {
                return false;
            }
        }
        return true;
    }

    //Returns if there are any buildings in a rectangle

    public boolean noBuildingsInRect(Rectangle r) {
        for (Building k : buildings) {
            if (k.inRect(r)) {
                return false;
            }
        }
        return true;
    }

    ////////////////////////////////////////////
    //Draw methods
    public void drawUnits(Graphics g) {
        for (Unit u : units) {
            if (!(cp == 1 && u.inEnemyVision() == false)) {
                u.draw(g, panel);
            }
        }
    }

    public void drawSelectedCircles(Graphics g) {
        if (selectedUnits.size() > 0) {
            for (Unit u : selectedUnits) {
                u.drawSelectedCircle(g, panel);
            }
        } else if (selectedBuilding != null) {
            selectedBuilding.drawSelectedCircle(g, panel);
        }

    }

    public void drawMiniMapUnits(Graphics g) {
        for (Unit u : units) {
            if (!(cp == 1 && u.inEnemyVision() == false)) {
                u.drawOnMiniMap(g);
            }
        }
    }

    public void drawBuildings(Graphics g) {
        for (Building k : buildings) {
            k.draw(g, panel);
        }
    }

    public void drawMiniMapBuildings(Graphics g) {
        for (Building k : buildings) {
            k.drawOnMiniMap(g);
        }
    }

    ////////////////////////////////////////////
    //Getter methods

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    public int getCamp() {
        return cp;
    }

    public int getMinerals() {
        return minerals;
    }

    public int dist(int x1, int y1, int x2, int y2) {
        return (int) (Math.pow(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2), 0.5));
    }

    public int dist(Point p1, Point p2) {
        return dist(p1.x, p1.y, p2.x, p2.y);
    }
}
