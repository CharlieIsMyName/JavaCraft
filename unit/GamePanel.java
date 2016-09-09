
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class GamePanel extends JPanel implements MouseMotionListener, MouseListener, KeyListener{
	
	private boolean []keys; 	//Boolean array of whether a keyboard key is being pushed
	private Unit []Units;
	
	public GamePanel(){
		setLayout(null);
		
		keys = new boolean[2000];
		addMouseMotionListener(this);
		addMouseListener(this);
		setFocusable(true);
		addKeyListener(this);
		
   
		setSize(850,650);
	}
	
    //Draws everything to do with the game on the screen
    public void paintComponent(Graphics g){
    	if (isVisible()){
			g.setColor(new Color(222,222,255)); 		
    		g.fillRect(0,0,getWidth(),getHeight());		//Draw the background colour of the game
    		drawUnits(g);
    	}
    }
    
    //Updates the events happening in the game.
    public void update(){
    	for (Unit u : units){
    		u.update();
    	}
    }
    
    public void drawUnits(Graphics g){
    	for (Unit u : units){
    		u.draw(g);
    	}
    }
    
    // ------------ MouseListener ------------------------------------------
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}    
    public void mouseClicked(MouseEvent e){}  
    public void mousePressed(MouseEvent e){}    
    	
    // ---------- MouseMotionListener ------------------------------------------
    public void mouseDragged(MouseEvent e){}
    public void mouseMoved(MouseEvent e){
    	//System.out.println(e.getX());
    }
    
    //KeyListener
    public void keyPressed(KeyEvent e) {
		int i = e.getKeyCode();
    	keys[i] = true;
    }
    public void keyReleased(KeyEvent e) {
    	int i = e.getKeyCode();
    	keys[i] = false;
    }
    public void keyTyped(KeyEvent e) {}
}