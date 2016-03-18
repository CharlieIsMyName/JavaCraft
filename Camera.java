//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//This class relocates the map so that it can be viewed on the screen, and allows
//the user to move around (since the map is bigger than the actual panel)

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;
import java.awt.geom.*;
public class Camera{
	private int CameraX,CameraY,ResX,ResY,mapX,mapY;
	private int speed=20;
	public Camera(int x,int y,int mpx,int mpy){		//Res--screen size mp--map size
		ResX=x; 	//size of map
		ResY=y;
		mapX=mpx;
		mapY=mpy;
		CameraX=0; 	//top left coordinate of the camera (screen) relative to big map
		CameraY=0;
		relocate();
	}
	//Moves the camera in the direction of the mouse
	public void move(int mx,int my){
		if (mx<=0){
			CameraX-=speed;
		}
		if (mx>=ResX){
			CameraX+=speed;
		}
		if (my<=0){
			CameraY-=speed;
		}
		if (my>=ResY){
			CameraY+=speed;
		}
		relocate();
	}
	//Relocates the camera
	public void relocate(){
		if ((CameraX-0.5*ResX)<0){
			CameraX=(int)ResX/2;
		}
		if ((CameraX+0.5*ResX)>mapX){
			CameraX=mapX-(int)ResX/2;
		}
		if ((CameraY-0.5*ResY)<0){
			CameraY=(int)ResY/2;
		}
		if ((CameraY+0.5*ResY)>mapY){
			CameraY=mapY-(int)ResY/2;
		}
	}
	public void setPos(int x,int y){
		CameraX=x;
		CameraY=y;
		relocate();
	}
	public int[] getPos(){
		int []tem=new int[2];
		tem[0]=CameraX;
		tem[1]=CameraY;
		return tem;
	}
	
	public int[] getSize(){
		int []tem=new int[2];
		tem[0]=ResX;
		tem[1]=ResY;
		return tem;
	}
	public Rectangle getCameraRect(){
		return new Rectangle(CameraX-ResX/2,CameraY-ResY/2,ResX,ResY);
	}
}