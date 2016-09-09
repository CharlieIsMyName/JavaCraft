import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*; 
import java.io.*; 
import javax.imageio.*; 

public class Starcraft extends JFrame implements ActionListener{
	private Timer myTimer;   
	private GamePanel game; 
	
	public Starcraft(){
		super("Starcraft Game");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setSize(900,650);
		
		myTimer = new Timer(5, this);	 // trigger every 50 ms
		myTimer.start();
		
		game = new GamePanel();
		add(game);
		
		setResizable(false);
		setVisible(true); 		//The game is not visible until the menu makes it visible.
	}
	
	public void actionPerformed(ActionEvent evt){
		Object source = evt.getSource();
		
		if(isVisible() && game!= null){
			game.update();
			game.repaint();
			if (game.gameOver()){
				remove(game);
				remove(menu);
				myTimer.stop();
				myTimer = new Timer(5,this);
				myTimer.start();
				game = new GamePanel();
				add(game);
				setVisible(false);
			}
		}
	}
	
	public static void main(String []args){
		Starcraft stargame  = new Starcraft();	
	}
}