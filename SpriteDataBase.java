import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;
import java.awt.geom.*;
import javax.swing.SwingUtilities.*;
import java.io.*;
import java.util.*;
public class SpriteDataBase{
	private SpriteList marine,medic,tank,scv,mineral,minPiece;
	public SpriteDataBase(){
		marine=new SpriteList();
		medic=new SpriteList();
		tank=new SpriteList();
		scv=new SpriteList();
		mineral=new SpriteList();
		minPiece=new SpriteList();
		loadMarine();
		loadMedic();
		loadTank();
		loadScv();
		loadMin();
		loadMinPiece();
	}
	//load//
	public SpriteList load(SpriteList spl,String path,int rep,int loop,int cp,int delay,String tp,String st){	
		//load a series of image under path with certain criterias,add them into target spriteList.
		//rep--how many images for each direction; loop-which spot to set loop to
		Image []im;
		Sprite sp;
		String []names=reorder(PNGOnly(new File(path).list()));
		im=new Image[rep];
		for(int i=0;i<names.length/rep;i++){
			for(int j=0;j<rep;j++){
				im[j]=new ImageIcon(path+"/"+names[i*rep+j]).getImage();
			}
			if(names.length/rep==1){
				sp=new Sprite(im,tp,st,-1,cp,delay);
			}
			else{
				sp=new Sprite(im,tp,st,i,cp,delay);
			}
			sp.setLoop(loop);
			spl.add(sp);
		}
		return spl;
	}
	public void loadMarine(){	//load all the possible marine sprites
		marine=load(marine,"./unit/marine/player/atk",6,1,0,4,"marine","atk");
		marine=load(marine,"./unit/marine/player/death",8,7,0,4,"marine","death");
		marine=load(marine,"./unit/marine/player/moving",9,0,0,3,"marine","moving");
		marine=load(marine,"./unit/marine/player/standing",1,0,0,4,"marine","standing");
		marine=load(marine,"./unit/marine/enemy/atk",6,1,1,4,"marine","atk");
		marine=load(marine,"./unit/marine/enemy/death",8,7,1,4,"marine","death");
		marine=load(marine,"./unit/marine/enemy/moving",9,0,1,3,"marine","moving");
		marine=load(marine,"./unit/marine/enemy/standing",1,0,1,4,"marine","standing");
	}
	public void loadMedic(){	//load all the possible medic sprites
		medic=load(medic,"./unit/medic/player/healing",4,2,0,4,"medic","atk");	//since we are using atk mechnism for healing so st="atk"
		medic=load(medic,"./unit/medic/player/death",9,8,0,4,"medic","death");
		medic=load(medic,"./unit/medic/player/moving",7,0,0,4,"medic","moving");
		medic=load(medic,"./unit/medic/player/standing",1,0,0,4,"medic","standing");
		medic=load(medic,"./unit/medic/enemy/healing",4,2,0,4,"medic","atk");
		medic=load(medic,"./unit/medic/enemy/death",9,8,1,4,"medic","death");
		medic=load(medic,"./unit/medic/enemy/moving",7,0,1,4,"medic","moving");
		medic=load(medic,"./unit/medic/enemy/standing",1,0,1,4,"medic","standing");
	}
	public void loadScv(){	//load all the possible scv sprites
		scv=load(scv,"./unit/scv/player/harvesting",2,0,0,4,"scv","atk");
		scv=load(scv,"./unit/scv/player/standing",1,0,0,4,"scv","standing");
		scv=load(scv,"./unit/scv/player/moving",1,0,0,4,"scv","moving");
	}
	public void loadTank(){	//load all the possible tank sprites
		tank=load(tank,"./unit/tank/player/stank",1,0,0,4,"tank-base","siege");
		tank=load(tank,"./unit/tank/player/moving",3,0,0,4,"tank-base","moving");
		tank=load(tank,"./unit/tank/player/standing",1,0,0,4,"tank-base","standing");
		tank=load(tank,"./unit/tank/player/transformation",6,-1,0,12,"tank-base","transformation");
		tank=load(tank,"./unit/tank/enemy/stank",1,0,1,4,"tank-base","siege");
		tank=load(tank,"./unit/tank/enemy/moving",3,0,1,4,"tank-base","moving");
		tank=load(tank,"./unit/tank/enemy/standing",1,0,1,4,"tank-base","standing");
		tank=load(tank,"./unit/tank/enemy/transformation",6,-1,1,12,"tank-base","transformation");
		
		tank=load(tank,"./unit/tower/player/stower",1,0,0,4,"tank-tower","siege");
		tank=load(tank,"./unit/tower/player/tower",1,0,0,4,"tank-tower","standing");
		tank=load(tank,"./unit/tower/player/transformation",5,-1,0,12,"tank-tower","transformation");
		tank=load(tank,"./unit/tower/enemy/stower",1,0,1,4,"tank-tower","siege");
		tank=load(tank,"./unit/tower/enemy/tower",1,0,1,4,"tank-tower","standing");
		tank=load(tank,"./unit/tower/enemy/transformation",5,-1,1,12,"tank-tower","transformation");
	}
	public void loadMin(){	//load the sprite for the mineral patch
		mineral=load(mineral,"./unit/mineral",1,0,-1,4,"mineral","standing");
	}
	public void loadMinPiece(){	//load the sprite for the small mineral pieces that scv gets from the mineral patch
		minPiece=load(minPiece,"./unit/minPiece",1,0,0,4,"scv",null);
	}
	//load//
	public SpriteList get(String s){	//allow the user to get the set of sprites they want
		if (s.equals("marine")==true){
			return marine.clone();
		}
		if (s.equals("medic")==true){
			return medic.clone();
		}
		if (s.equals("tank")==true){
			return tank.clone();
		}
		if (s.equals("scv")==true){
			return scv.clone();
		}
		if(s.equals("mineral")){
			return mineral.clone();
		}
		if(s.equals("minPiece")){
			return minPiece.clone();
		}
		else{
			return null;
		}
	}
	public String []PNGOnly(String[]s){		//takes in a array of file names, and returns a array of file names that contains ".png"
		String []ans;
		int counter=0;
		for(int i=0;i<s.length;i++){
			if(s[i].substring(s[i].length()-3,s[i].length()).equals("png")==true){
				counter+=1;
			}
		}
		ans=new String[counter];
		counter=0;
		for(int i=0;i<s.length;i++){
			if(s[i].substring(s[i].length()-3,s[i].length()).equals("png")==true){
				ans[counter]=s[i];
				counter+=1;
			}
		}
		return ans;
	}
	public String []reorder(String []s){		//make sure the file name are in the correct order else the sprite will be messed up
		//because of the spliting programme i use gives the file like "str0.png", so i dont really have to "reorder" them, i just have to recreate
		//the file names using the way i used in the spliting programme
		int counter=0;
		String tem=s[0];
		String ans="";
		while(true){
			if(Character.isDigit(tem.charAt(counter))==false){
				ans=ans+tem.charAt(counter);	//get the string part of the name
			}
			else{
				break;
			}
			counter++;
		}
		String []fnl=new String[s.length];
		for(int i=0;i<s.length;i++){
			fnl[i]=ans+i+".png";	//adding a number after it to make the exact names as before but ordered in the array
		}
		return fnl;
	}
}