//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//The unit class is able to construct a Marine, Medic, SCV, or Mineral, where mineral
//is a static unit that does not attack (but is "attacked" by SCV's). The unit class 
//keeps track of all the information of a unit, including its position, the path it is
//currently moving on, etc. 

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.applet.*;
import javax.sound.sampled.AudioSystem;

public class Unit{
	private SMap map; 						//the map the unit is in
	private GamePanel panel;				//the panel that the unit belongs to
	private MiniMap miniMap;				
	private Player owner;					//the player that created this unit
	private String type; 					//"marine", "medic", ..
	private int gridX, gridY; 				//unit's position is always stored as a grid position
	private int mapX,mapY; 					//the unit's position on the map
	//atkRange - the maximum distance that the unit can attack another unit from
	//sgtRange - the maximum distance that the unit can see another unit from
	//almRange - the maximum distance that will cause the unit to attack a unit from
	//returningRange - for SCV's, the maximum distance that will let an SCV return to a commmand centre
	private int atkRange, sgtRange, almRange, returningRange;
	private int attackSpeed; 	//the number of cycles required between each move/attack
	private ArrayList<Integer> walkableTerrain; 	//the terrain values that this unit can walk on
	private int damage; 					//the amount of damage it can deal	
	private int hp,maxHp,mana,maxMana;
	private SpriteList sprites; 			//the list of sprites for drawing the unit
	private int attackLvl, armourLvl;	 	//upgradable variables that increase the damage/armour of a unit	
	private int baseAtk,baseArmour; 	 	//the amount of damage/protection without additional variables	
	private int preAtkDelay; 				//the delay when the unit changes to attack
	private int postAtkDelay;				//the delay after the unit changes to attack
	private int atkMovingDelay;				//
	private int dir;						//the direction it's facing (0-15) (increasing: clockwise)
	private double adir; 					//the angular direction it's facing (in degrees, clockwise with 0 at 12)
	private int cp; 						//cp = camp (0 - player, 1 - enemy)
	private Point dest,oldDest; 			//the destination it needs to move to
	private Point tempDest;					//the next Point needed to be moved to to get to dest
	private boolean hasDest;				//where or not the unit has a destination
	public boolean haltWalking; 			//checks whether unit should stop walking
	private boolean isSelected; 			//if player selects this unit
	private String oldStatus, curStatus; 	//"moving","standing","atk","death"
	private boolean hasTarget, beingAttacked;
	private Unit target, attacker; 			//target can also be a "thing" (building), attacker is something attacking this
	private Building bTarget;				//the building it's attacking
	private int imgSize; 					//marine, medic: 64; svc: 72; tank, tower: 128
	private LinkedList<Point> path; 		//the path that this unit is currently following
	private int atkTargetCamp;				//determine which camp can be attacked by this unit
	private int atkCycleCounter;
	private int movingCounter; 
	private int deathCounter, endDeath; 	//deathCounter counts how long it's been dead, endDeath is 
											//how long until unit is removed from owner's unitlist
	private boolean autoAtk;				//When this is true, the unit will attack as soon as it
											//finds an enemy in the atkRange
	private Color[] cpColor=new Color[2];
	private int mMapUnitSize;				//size of the unit on the minimap
	private Image curImage;
	private Point moveDelta;				//the (dx,dy) needed to add to (mapX,mapY)
	private Image selectedCircle; 			//the circle that indicates the unit is selected
	private int unitSize;					//the size of the unit on the grid
	private boolean[][][] visGrid;			//the visible grid of the unit
	private boolean fakePath;				//when this is true, then the unit is moving towards some sort of
											//midpoint to the actual endpoint
	private int fakeH; 						//the H value of the temporary destination
	private Unit curMineral=null; 			//the current mineral being mined (SCV)
	private boolean loaded;		 			//when the SCV has gathered enough minerals			
	private int harCounter;					//harvesting counter (minerals)
	private int harCounterMax=20;
	private Building nearbyCommandCentre=null; 	//the closest command centre to the SCV
	private SpriteList minSprList;			//the SCV sprite list for minerals
	private boolean smoothMove=true; 		//if the unit is moving grid to grid
	private boolean smoothStopped=false; 	//if the unit is waiting for another unit to move before it continues
											//its unsmooth path

	private int audioDelay, audioTimePassed; //audioTimePassed - the time passed once an attack audio has been played
											//once audioTimePassed > audioDelay, then another sound can be played

	public Unit(Player owner, GamePanel p, SpriteDataBase spriteBase,String type, int x, int y, SMap map, MiniMap miniMap){
		panel = p;
		this.owner = owner;
		this.type = type;
		this.map=map;
		this.miniMap=miniMap;		
		cp = owner.getCamp();
		gridX = x;
		gridY = y;
		
		Point mapPos = map.gridMapCoord(new Point(x,y));
		
		walkableTerrain = new ArrayList<Integer>();
		walkableTerrain.add(0);
		walkableTerrain.add(2);
		
		mapX = mapPos.x;
		mapY = mapPos.y;
		map.addUnit(this);
		
		cpColor[0]=new Color(0,255,0);
		cpColor[1]=new Color(255,0,0);
		sprites = spriteBase.get(type);
		audioDelay = 100;
		audioTimePassed = 100;
		
		dest = null;
		fakePath = false;
		oldDest = null;
		tempDest = null;
		isSelected = false;
		hasDest = false;
		path = null;
		curStatus = "standing";
		oldStatus = "standing";
		
		deathCounter = 0;
		endDeath = 100;
		
		dir = 0;
		curImage = null;
		moveDelta = null;
		
		visGrid=owner.getVisGrid();
		
		if (type.equals("marine")){ 
			atkRange = 600;
			sgtRange = 10;
			almRange = 8;
			attackSpeed = 20; 	//how many frames in an attack cycle
			maxHp = 100;
			hp = maxHp;
			maxMana = 100;
			mana = maxMana;
			baseAtk = 6;
			baseArmour = 0;
			attackLvl = 0;
			armourLvl = 0;
			preAtkDelay = 4;
			postAtkDelay = 20;
			atkMovingDelay = 12;
			imgSize = 64;
			atkTargetCamp=(cp+1)%2;
			atkCycleCounter=0;
			movingCounter=0;
			autoAtk=true;
			selectedCircle=new ImageIcon("./otherpics/circleS.png").getImage();
			mMapUnitSize=3;
			unitSize=32;
		}
		else if(type.equals("medic")){
			atkRange = 300;
			sgtRange = 10;
			almRange = 8;
			attackSpeed = 8; 	//how many frames in an attack cycle
			maxHp = 100;
			hp = maxHp;
			maxMana = 100;
			mana = maxMana;
			baseAtk = -1;
			baseArmour = 1;
			attackLvl = 0;
			armourLvl = 0;
			preAtkDelay = 4;
			postAtkDelay = 8;
			atkMovingDelay = 2;
			imgSize = 64;
			atkTargetCamp=cp;
			atkCycleCounter=0;
			movingCounter=0;
			autoAtk=true;
			selectedCircle=new ImageIcon("./otherpics/circleS.png").getImage();
			mMapUnitSize=3;
			unitSize=32;
		}
		else if(type.equals("scv")){
			minSprList=spriteBase.get("minPiece");
			walkableTerrain.add(5);
			atkRange = 64;
			sgtRange = 10;
			almRange = 1000;
			attackSpeed = 4; 	//how many frames in an attack cycle
			maxHp = 100;
			hp = maxHp;
			maxMana = 100;
			mana = maxMana;
			baseAtk = 0;
			baseArmour = 1;
			attackLvl = 0;
			armourLvl = 0;
			preAtkDelay = 4;
			postAtkDelay = 8;
			atkMovingDelay = 2;
			imgSize = 72;
			atkTargetCamp=-1;
			atkCycleCounter=0;
			movingCounter=0;
			autoAtk=true;
			selectedCircle=new ImageIcon("./otherpics/circleS.png").getImage();
			mMapUnitSize=3;
			unitSize=36;
			loaded=false;
			harCounter=0;
			returningRange=120;
		}
		else if(type.equals("mineral")){
			atkRange = 0;
			sgtRange = 0;
			almRange = 0;
			attackSpeed =4; 	//how many frames in an attack cycle
			maxHp=1000;
			hp = maxHp;
			baseAtk = 0;
			baseArmour = 0;
			attackLvl = 0;
			armourLvl = 0;
			preAtkDelay = 4;
			postAtkDelay = 8;
			atkMovingDelay = 2;
			imgSize = 64;
			atkTargetCamp=-1;
			atkCycleCounter=0;
			movingCounter=0;
			autoAtk=true;
			selectedCircle=new ImageIcon("./otherpics/circleS.png").getImage();
			mMapUnitSize=3;
			unitSize=64;
			loaded=false;
		}
	}
	
	//Checks if the enemy can see this unit.
	public boolean inEnemyVision(){	
		return visGrid[(cp+1)%2][gridX][gridY];
	}
	
	//Sets a mineral for the SCV
	public void setMineral(Unit u){
		curMineral=u;
	}
	
	//Lets unit do what it is currently doing. If a task is finished, then goes back to standing
	public void update(){
		audioTimePassed += 1;
		if (!curStatus.equals("death")){
			updateDeadStat();
		}
		updateStatus();
		if (curStatus.equals("moving")){ 	//if status = "moving", then hasDest
			if (path == null || haltWalking){
				haltWalking = false;
				stopMoving();
			}
			else{ 							//if it is currently following a path
				if (tempDest == null){
					if (path.size() == 0){
						if (!fakePath || !map.isWalkable(walkableTerrain,dest)){
							if (!getGridPos().equals(dest)){
								dir = updateDirInt(dest, true);
							}
							haltWalking = true;
						}
						else{
							updateDest(dest, autoAtk);
							
						}
					}
					else{
						tempDest = path.pop();
						moveDelta = getMDelta(tempDest);
						dir = updateDirInt(tempDest, true);
						if (map.isOccupied(tempDest) && !type.equals("scv")){
							Unit temp = map.getUnit(tempDest);
							if (temp.status().equals("standing")||temp.status().equals("atk")){
								haltWalking = true;
							}
							else if(temp.getTempDest() != null && temp.finalDest(tempDest)){
								haltWalking = true;
							}
							else{
								path.addFirst(tempDest);	//try again later
								tempDest = null;
							}
						}
						else if (map.isWalkable(walkableTerrain,tempDest)){
							map.setEmpty(gridX, gridY);
							if(!type.equals("scv")){
								map.removeGVal(gridX, gridY);	
							}
							updateGridCoord(tempDest);
							map.addUnit(this);
						}
						else{
							if (fakePath){
								tempDest = null;
								updateDest(dest, autoAtk);
							}
							else{
								haltWalking = true;
							}
						}
					}
				}
				else{
					moveToward(tempDest);
					if (reached(tempDest)){
						Point t = map.gridMapCoord(tempDest);
						mapX = t.x;
						mapY = t.y;
						tempDest = null;
						smoothMove = true;
						smoothStopped = false;
						if (fakePath && map.approxH(getGridPos(),dest) < fakeH){ 	//This prevents a unit
							updateDest(dest,autoAtk);								//from going unnecessarily to a 
						}															//"mid location"
					}		
				}
				checkMineralReturned();
			}
		}
		else if (curStatus.equals("standing")){
		}
		else if (curStatus.equals("atk")&&atkCycleCounter==0&&(target!=null||bTarget!=null)){
			if (!type.equals("scv") && audioTimePassed > audioDelay){
				audioTimePassed = 0;
				panel.playSound(this, "atk");
			}
			Point targetPos = target!=null ? target.getGridPos() : bTarget.getCentre();
			dir = updateDirInt(targetPos, true);
			if(target != null){ 	//target is a unit (otherwise, bTarget is Buildnig and target null)
				if(statChanged()==true){
					changeStatus("atk");
					atkCycleCounter+=preAtkDelay;
					movingCounter+=preAtkDelay;
				}
				else{
					atkCycleCounter+=postAtkDelay;
					movingCounter+=atkMovingDelay;
					if(type.equals("scv")){
						if (cp == 0){
							panel.playSound(this, "mining");
						}
						harCounter+=1;
						if(harCounter>harCounterMax){
							harCounter=0;
							loaded=true;
							returnMineral();
						}
					}
					else{
						target.damageDealt(baseAtk+attackLvl);	
					}
				}
				if (target.isDead()||targetLost(target)||target.inEnemyVision()==false){
					if(hasDest==false){
						changeStatus("standing");
					}
					else{
						changeStatus("moving");
					}
					hasTarget=false;
					target = null;
				}
			}
			else{
				if(statChanged()==true){
					changeStatus("atk");
					atkCycleCounter+=preAtkDelay;
					movingCounter+=preAtkDelay;
				}
				else{
					bTarget.damageDealt(baseAtk+attackLvl);
					atkCycleCounter+=postAtkDelay;
					movingCounter+=atkMovingDelay;
				}
				if (bTarget.isDead()||targetLost(bTarget)||bTarget.inEnemyVision()==false){
					if(hasDest==false){
						changeStatus("standing");
					}
					else{
						changeStatus("moving");
					}
					hasTarget=false;
					bTarget = null;
				}
				
			}
		}
		else if (curStatus.equals("death")){
			deathCounter += 1;
			if (deathCounter == endDeath){
				owner.addFinishedUnit(this);
			}
		}
		if(atkCycleCounter>0){
			atkCycleCounter--;
		}
		if(movingCounter>0){
			movingCounter--;
		}
		updateImage();
	}
	
	//Given a point, updates status to moving if the point is not the current destination
	//The destination is a grid coordinate
	public void updateDest(Point pt, boolean autoAttack){
		if (map.withinGrid(pt)){
			fakePath = false;
			dest = pt;
			if(tempDest != null){
				if(!type.equals("scv")){
					map.removeGVal(tempDest);
									
				}
				map.removeGVal(tempDest);
				tempDest = null;
				smoothMove = false;
			}
			if(!type.equals("scv")){
				map.removeGVals(path);
			}
			Point pathDest = dest;
			fakeH = map.approxH(getGridPos(), dest);
			if (fakeH > 450){
				fakePath = true;
				pathDest = map.findGoodDest(walkableTerrain,fakeH,gridX, gridY, dest.x, dest.y);
			}
			path = map.findPath(walkableTerrain, gridX, gridY, pathDest.x, pathDest.y);
			changeStatus("moving");
			hasDest = true;
			autoAtk = autoAttack;
		}
	}
	
	//Changes the old status into the current status.
	public void updateStatus(){
		oldStatus = curStatus;
	}
	
	//This is for when medics heal a unit - prevents hp from going over max
	public void resetHp(){	
		if(hp>maxHp){
			hp=maxHp;
		}
	}
	
	//Changes grid coordinates to p
	public void updateGridCoord(Point p){
		gridX = p.x;
		gridY = p.y;
	}
	

	//Moves towards a point p
	//If the unit is within the grid, then it moves normally (smoothMove)
	//Otherwise, it needs to take an uneven path to get to the grid centre,
	//and then move smoothly
	public void moveToward(Point p){
		if(smoothMove){
			move(map.gridCentre(p));
		}
		else{
			moveTo(p);
		}
	}
	
	//The regular way of moving between grids
	public void move(Point p){
		mapX += 2*moveDelta.x;
		mapY += 2*moveDelta.y;
	}
	
	//Gets all units within a range of p
	public ArrayList<Unit> getAllUnits(Point p, int r){
		ArrayList<Unit> ans = new ArrayList<Unit>();
		for (int dx=-1*r; dx<r+1; dx++){
			for (int dy=-1*r; dy<r+1; dy++){
				if (map.withinGrid(p.x+dx,p.y+dy) && map.isOccupied(new Point(p.x+dx,p.y+dy))){
					ans.add(map.getUnit(new Point(p.x+dx,p.y+dy)));
				}
			}
		}
		return ans;
	}
	
	//Moves to a point p based off of map coordinates (when smoothMove is false)
	public void moveTo(Point p){
		smoothStopped = false;
		ArrayList<Unit> checkUnits = getAllUnits(p,2);
		Point tDest = map.gridMapCoord(p);
		int dx = tDest.x-mapX;
		int dy = tDest.y-mapY;
		double d = Math.pow(Math.pow((tDest.x-mapX),2)+Math.pow((tDest.y-mapY),2),0.5);
		double moveX = dx*Math.pow(8,0.5)/d;
		double moveY = dy*Math.pow(8,0.5)/d;
		
		mapX = mapX + (int)(moveX);
		mapY = mapY + (int)(moveY);	
	}
	
	//Returns true when the unit has reached the middle of a grid square
	public boolean reached(Point p){ 	//p is a grid coordinate
		return dist(getCentre(), map.gridCentre(p)) <= 3;
	}
	
	//Stops the unit from moving and changes its status to standing
	public void stopMoving(){
		if(!type.equals("scv")){
			map.removeGVal(getGridPos());	
		}
		changeStatus("standing");
		hasDest=false;
		if (tempDest != null){
			smoothMove = false;
		}
		tempDest = null;
		dest = null;
		moveDelta = null;
		path=null;
		autoAtk=true;
	}
	
	//Searches for a target. Note that medics search for targets within their own base,
	//SCV's search for neutral targets, etc. Targets can also include buildings.
	public void searchForTarget(Player other,Player player,Player neutral){
		if(autoAtk==false){
			return;
		}
		if(hasTarget){
			if (target != null){
				if(withinRange(atkRange,target)){
					if(!curStatus.equals("atk")){
						changeStatus("atk");
					}
					return;
				}	
			}
			else{
				if (withinRange(atkRange,bTarget)){
					if(!curStatus.equals("atk")){
						changeStatus("atk");
					}
					return;
				}
			}
		}
		for(Unit u : other.getUnits()){
			if(ableToAtk(u)&&withinRange(atkRange,u)&&!u.isDead()&&u.inEnemyVision()){	
				setTarget(u);
				return;
			}
		}
		for(Unit u : player.getUnits()){
			if(ableToAtk(u)&&withinRange(atkRange,u)&&!u.isDead()&&u.inEnemyVision()&&u!=this){	
				setTarget(u);
				return;
			}
		}
		if(type.equals("medic")){
			return;
		}
		for(Unit u:neutral.getUnits()){
			if(ableToAtk(u)&&withinRange(atkRange,u)&&!u.isDead()&&u.inEnemyVision()&&u!=this){	
				setTarget(u);
				curMineral=u;
				return;
			}
		}
		for(Building k : other.getBuildings()){		
			if(ableToAtk(k)&&withinRange(atkRange,k)&&!k.isDead()&&k.inEnemyVision()){	
				setBTarget(k);
				break;
			}
		}
	}
	
	//Returns when a target is no longer within the attack range
	public boolean targetLost(Unit u){
		return !(withinRange(atkRange,u));
	}
	public boolean targetLost(Building k){
		return !(withinRange(atkRange,k));
	}
	
	//Deals d damage to hp, reducing the damage by how much armour the unit has
	public void damageDealt(int d){
		hp=hp-d+baseArmour+armourLvl;
		updateDeadStat();
	}
	
	//Sets a new target (to attack) and updates the status
	public void setTarget(Unit other){
		if(ableToAtk(other)){
			hasTarget = true;
			target = other;
			changeStatus("atk");	
		}
	}
	//Equivalent to setTarget but for Building
	public void setBTarget(Building other){
		hasTarget = true;
		bTarget = other;
		changeStatus("atk");
	}
	
	//If gridCoords is true, converts other into map coordinates before passing it into updateDir.
	public double updateDir(Point other, boolean gridCoords){
		if (gridCoords){
			return updateDir(map.gridMapCoord(other));
		}
		return updateDir(other);
	}
	
	//Returns an appropriate angle such that a unit facing staight up would have to turn this
	//angle while moving dx to the right and dy down (dx,dy can be negative)
	private double updateDir(double dx,double dy){
		if (dx == 0){
			if (dy <= 0){
				return 0;
			}
			else{
				return 180;
			}
		}
		double ang = Math.toDegrees(Math.atan(Math.abs(dy/dx)));
		if (dx >= 0){
			if (dy <= 0){
				ang = 90-ang; 	
			}
			else{
				ang += 90;
			}
		}
		else{
			if (dy >= 0){
				ang = 270 - ang;
			}
			else{
				ang += 270;
			}
		}
		return ang;
		
	}
	//Returns angle so that the unit is facing another point
	public double updateDir(Point other){
		Point otherMapCoord = other;
		Point myMapCoord = map.gridMapCoord(getGridPos());
		double dx = otherMapCoord.x - myMapCoord.x;
		double dy = otherMapCoord.y - myMapCoord.y;
		return updateDir(dx, dy);
	}
	//Returns the angle as a number from 0-16, splitting the circle equally into 22.5 degrees
	public int updateDirInt(Point other, boolean gridCoords){
		if (gridCoords){
			return (int)(updateDir(other, true)*2/45);		
		}
		return (int)(updateDir(other)*2/45);
	}
	public int updateDirInt(double dx, double dy){
		return (int)(updateDir(dx, dy)*2/45);
	}
	
	//Returns the mineral that the SCV has to the nearest command centre
	public void returnMineral(){
		if(type.equals("scv")==false||loaded==false){
			return;
		}
		nearbyCommandCentre=owner.nearestCommandCentre(new Point(mapX,mapY));
		if(nearbyCommandCentre==null){
			return;
		}
		stopMoving();
		updateDest(nearbyCommandCentre.getCentre(),false);
	}
	
	//Checks if the mineral has been returned
	public void checkMineralReturned(){
		if(type.equals("scv")==false||loaded==false){
			return;
		}
		nearbyCommandCentre=owner.nearestCommandCentre(new Point(mapX,mapY));
		if(nearbyCommandCentre==null){
			return;
		}
		if(withinRange(returningRange,nearbyCommandCentre.getMapCentre())){
			stopMoving();
			nearbyCommandCentre.addMineralCount();
			loaded=false;
			//the line of adding mineral into the bank
			updateDest(curMineral.getGridPos(),true);
		}
	}
	
	//Checks if a unit is in a certain range
	public boolean withinRange(int range, Unit u){
		return dist(u)<=range;
	}
	public boolean withinRange(int range, Building k){
		return dist(k)<=range;
		//Change parameters - add a map thing, split map and use that instead of dist
	}
	public boolean withinRange(int range, Point p){
		return range>=dist(mapX,mapY,p.x,p.y);
	}
	
	//Changes the attacked status (when an enemy attacks or stops attacking the unit)
	public void setAttackedStatus(boolean stat){
		beingAttacked = stat;
	}
	
	//Sets te attacker to unit a
	public void setAttacker(Unit a){
		attacker = a;
	}
	
	//Returns true if the unit changed its status in the last frame
	public boolean statChanged(){
		return !(oldStatus.equals(curStatus));
	}
	
	//Draws a health bar of the unit to indicate how much life is left.
	//This will be drawn over their head
	public void drawHealthBar(Graphics g){
		if(cp==-1){
			return;
		}
		int length=unitSize-6;
		int height=5;
		int actLength=(int)((double)(length*hp)/(double)(maxHp));
		Point c=map.mapScreenCoord(new Point(mapX,mapY));
		g.setColor(Color.black);
		g.fillRect(c.x+3,c.y-8,length,height);
		g.setColor(cpColor[cp]);
		g.fillRect(c.x+3,c.y-8,actLength,height);
	}
	
	//Draws the unit on the screen with its health bar
	public void draw(Graphics g, GamePanel p){
		Point screenCoords = getSCoord();
		g.drawImage(curImage,screenCoords.x,screenCoords.y,p);
		if(loaded==true){
			Image tem=minSprList.getImage(getStatusArray(),dir,cp,type);
			g.drawImage(tem,screenCoords.x+20,screenCoords.y+20,p);
		}
		drawHealthBar(g);
	}
	
	//Draws the unit onto the miniMap
	public void drawOnMiniMap(Graphics g){
		Point mPos=miniMap.maptoMiniMapPos(mapX,mapY);
		g.setColor(cpColor[cp]);
		g.fillRect(mPos.x+mMapUnitSize/2,mPos.y+mMapUnitSize/2,mMapUnitSize,mMapUnitSize);
	}
	
	//Draws the circle to indicate the unit is being selected
	public void drawSelectedCircle(Graphics g, GamePanel p){
		if(curStatus!="death"){
			if(type.equals("marine")||type.equals("medic")||type.equals("scv")){
				Point screenCoords=map.mapScreenCoord(new Point(mapX,mapY));
				g.drawImage(selectedCircle,screenCoords.x+4,screenCoords.y+15,p);
			}
		}
	}
	//Returns true if the other unit is a possible target
	public boolean ableToAtk(Unit u){	
		if(u.getCamp()==atkTargetCamp){
			return true;
		}
		return false;
	}
	public boolean ableToAtk(Building k){	
		if(k.getCamp()==atkTargetCamp){
			return true;
		}
		return false;
	}
	
	//Updates the current image
	public void updateImage(){
		curImage = getImage();
	}

	public void updateDeadStat(){
		if (isDead()){
			panel.playSound(this,"death");
			owner.removeUnit(this);
			changeStatus("death");
		}
	}
	
	public boolean inRect(Rectangle r){
		return r.contains(mapX+unitSize,mapY+unitSize);
	}
	
	//Updates the status to newStatus
	public void changeStatus(String newStatus){
		oldStatus = curStatus;
		curStatus = newStatus;
	}
	
	//Returns the amount that the unit must move by to get to the next grid
	public Point getMDelta(Point p){
		return new Point(p.x-gridX, p.y-gridY);
	}

	public String status(){
		return curStatus;
	}
	
	public Image getImage(){
		return sprites.getImage(getStatusArray(),dir,cp,type);
	}
	
	public int getGridX(){
		return gridX;
	}
	public int getGridY(){
		return gridY;
	}
	public String getType(){
		return type;
	}
	//Returns the coordinates of the top left of the image in the screen
	//First, the coordinates are obtained
	//by converting the map coordinates to grid coordinates.
	//Then, coordinates are adjusted so that the
	//actual figure of the Unit is in the centre of the grid that it is occupying
	public Point getSCoord(){
		Point coords = map.mapScreenCoord(getMapCoord());	//top left of the unit
		int delta = (imgSize - map.getGridSize())/2;
		return new Point(coords.x - delta, coords.y - delta);
	}
	
	public Point getMapCoord(){
		return new Point(mapX,mapY);
	}
	public Point getGridPos(){
		return new Point(gridX,gridY);
	}
	public int getDamage(){	
		return damage;
	}
	public int getAtkTargetCamp(){	
		return atkTargetCamp;
	}
	public int getCamp(){	
		return cp;
	}
	
	public Point getCentre(){
		return new Point(mapX + map.getGridSize()/2, mapY + map.getGridSize()/2);
	}
	
	public int getSightRange(){		
		return sgtRange;
	}
	
	public Point getTempDest(){
		return tempDest;
	}
	
	//Checks if a point is the final destination
	public boolean finalDest(Point p){
		return p.equals(dest);
	}
	
	
	//Returns an array of old and current status
	public String[] getStatusArray(){
		String []temp = new String[2];
		temp[0] = oldStatus;
		temp[1] = curStatus;
		return temp;
	}
	
	public int dist(Unit u){
		return dist(u.getMapCoord().x,u.getMapCoord().y,mapX,mapY);
	}
	public int dist(Building k){
		return dist(map.gridMapCoord(k.getCentre()),new Point(mapX,mapY));
	}
	public int dist(int x1,int y1, int x2, int y2){
		return (int)(dDist(x1,y1,x2,y2));
	}
	public double dDist(int x1, int y1, int x2, int y2){
		return Math.pow(Math.pow((x1-x2),2)+Math.pow((y1-y2),2),0.5);
	}
	public int dist(Point p1, Point p2){
		return dist(p1.x,p1.y,p2.x,p2.y);
	}
	
	//Returns if the unit has 0 hp, and if so, updates old/current statuses
	public boolean isDead(){
		return hp <= 0;
	}
	
	public String toString(){
		String ans = "";
		ans += type + " located at (" + gridX + "," + gridY + ")";
		return ans;
	}
}