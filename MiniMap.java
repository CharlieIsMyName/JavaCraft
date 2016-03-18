//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//This class is for the small map that is drawn on the bottom right of the screen showing the entrire
//map as well as little dots for units and buildings. The dots are colour coded based on the player
//and enemy camps

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;
import java.awt.geom.*;
public class MiniMap{
	private int x,y,l,h;
	private SMap map;
	private Camera cam;
	private Image image;
	public MiniMap(Image i,SMap m,Camera c,int x,int y,int l,int h){
		map=m;
		cam=c;
		this.x=x;
		this.y=y;
		this.l=l;
		this.h=h;
		image=i;
	}
	public void moveCamera(int nx,int ny){
		if(withinMiniMap(nx,ny)==false){
			return;
		}
		Point newPos=miniMapToMapPos(nx,ny);
		cam.setPos(newPos.x,newPos.y);
	}
	public boolean withinMiniMap(int mx,int my){
		return mx>=x&&mx<x+l&&my>=y&&my<my+h;
	}
	public Point miniMapToMapPos(int mx,int my){
		double ratio=(double)(map.getMapSize().x)/(double)(l);  //since i know the map is always a square in starEditor
		int nx=(int)((mx-x)*ratio);
		int ny=(int)((my-y)*ratio);
		return new Point(nx,ny);
	}
	public Point maptoMiniMapPos(int mx,int my){
		double ratio=(double)(l)/(double)(map.getMapSize().x); 
		int nx=x+(int)((mx)*ratio);
		int ny=y+(int)((my)*ratio);
		return new Point(nx,ny);
	}
	public Point maptoMiniMapPos(Point p){
		return maptoMiniMapPos(p.x,p.y);
	}
	public Point gridtoMiniMapPos(int gx, int gy){
		return maptoMiniMapPos(map.gridMapCoord(new Point(gx,gy)));
	}
	public Point getPos(){
		return new Point(x,y);
	}
	public Image getImage(){
		return image;
	}
	public double ratioToMap(){		//greater than 1
		return (double)(map.getMapSize().x)/(double)(l);
	}
}