import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
//charlie wang,kassem bazzi
//this class claculates and provides the user with the vision information
public class Vision{
	private SMap map;
	private Rectangle searchArea;		//an aproximate area of the real one when checking vision
	private boolean[][][] visGrid;
	private int[][] terGrid;		//terrain grid
	private Unit[][] unitGrid;		//unit grid same as the one in SMap
	private int gridSize;		//the size of the grid list(not a single grid in pixle)
	public Vision(SMap m){
		map=m;
		unitGrid=map.getUnitGrid();
		terGrid=map.getTerGrid();
		visGrid=new boolean[2][terGrid.length][terGrid.length];		//how to use visGrid: [cp][x][y]
		gridSize=terGrid.length;
		reset();
	}
	public void reset(){		//reset the visGrid to no vision at all
		for(int n=0;n<2;n++){
			for(int i=0;i<visGrid[n].length;i++){
				for(int j=0;j<visGrid[n][i].length;j++){
					visGrid[n][i][j]=false;
				}
			}
		}
	}
	public void update(){		//update the information of the vision grid
		reset();
		for(int x=0;x<unitGrid.length;x++){
			for(int y=0;y<unitGrid[x].length;y++){
				update(new Point(x,y));
			}
		}
	}
	public void update(Point p){		//update the vision information around a certain point
		if(unitGrid[p.x][p.y]==null){	// if there is no unit around then no vis
			return;
		}
		searchArea=findSearchArea(p);	//for efficency purpose
		for(int i=searchArea.x;i<searchArea.x+searchArea.width;i++){
			for(int j=searchArea.y;j<searchArea.y+searchArea.height;j++){
				if(ableToSee(p,new Point(i,j))){
					visGrid[unitGrid[p.x][p.y].getCamp()][i][j]=true;	
				}
			}
		}
	}
	public boolean [][][] getVisGrid(){
		return visGrid;
	}
	public Rectangle findSearchArea(Point p){	//get a approximate area for searching and updating vision in order to increase efficency
		int sgtRange=unitGrid[p.x][p.y].getSightRange();
		Rectangle tem=new Rectangle(p.x-sgtRange,p.y-sgtRange,2*sgtRange,2*sgtRange);	//trying to find the smallest rectangle that contains the circle
		if(tem.x<0){																	//of vision(with radius sightRange)
			tem.x=0;
		}
		if(tem.y<0){
			tem.y=0;
		}
		if(tem.x+tem.width>gridSize){
			tem.width=gridSize-tem.x;
		}
		if(tem.y+tem.height>gridSize){
			tem.height=gridSize-tem.y;
		}
		return tem;
	}
	public boolean ableToSee(Point ob,Point target){			//check if obeserver point can see the target point 
		int sgtRange=unitGrid[ob.x][ob.y].getSightRange();
		if(dist(ob.x,ob.y,target.x,target.y)>sgtRange){		//if not in sightRange
			return false;
		}
		if(terGrid[target.x][target.y]==2&&terGrid[ob.x][ob.y]!=2){		//if the target on highGround and ob not on highGround
			return false;
		}
		return true;
	}
	public int dist(int x1,int y1, int x2, int y2){			//calculate the distance between the point (x1,y1) to (x2,y2)
		return (int)(Math.pow(Math.pow((x1-x2),2)+Math.pow((y1-y2),2),0.5));
	}
	public void draw(Graphics g){			//draw the fog of war on the map using visGrid[0](which is the vision for the player)
		g.setColor(new Color(0,0,0,127));
		for(int x=0;x<gridSize;x++){
			for(int y=0;y<gridSize;y++){
				if(visGrid[0][x][y]==false){
					Point tem=map.gridScreenCoord(new Point(x,y));
					g.fillRect(tem.x,tem.y,map.getGridSize(),map.getGridSize());
				}
			}
		}
	}
}