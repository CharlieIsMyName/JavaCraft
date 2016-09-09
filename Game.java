//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//This class is the main class for running the Starcraft Game.
//This game is a simple RTS based off of Starcraft. The player starts
//on the top left of the map with a base, and is expected to expand to the rest of the map
//and kill all enemy units. If the user is able to do this, they win the game.
//If the user loses all their units, then they lose.
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;
import java.awt.geom.*;
import javax.swing.SwingUtilities.*;
import java.io.*;
import java.util.ArrayList;

public class Game extends JFrame implements ActionListener {

    private Timer myTimer;   		//The game only uses one main timer.
    private StarMenu menu; 			//The main menu
    private GamePanel panel;		//The game's panel
    private int ResX = 1100; 			//size of camera
    private int ResY = 700;
    private int mapSizeX = 4096;		//The size of the entire map
    private int mapSizeY = 4096;
    private int delay = 16;  			//=60 frames/second

    public Game() {
        super("Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(ResX, ResY);
        myTimer = new Timer(delay, this);

        panel = new GamePanel(this, ResX, ResY, mapSizeX, mapSizeY);
        add(panel);

        setResizable(false);
        setVisible(false); 	//The game is not visible until the menu makes it visible.
        menu = new StarMenu(this, ResX, ResY);
        menu.setVisible(true);
    }

    public void start() {
        myTimer.start();
    }

    public int cycle() {
        return delay;
    }

    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        //System.out.println("im running");
        if (isVisible() && panel != null) {
            panel.moveCam();
            panel.update();
            panel.repaint();

            if (panel.gameOver()) {
                boolean won = panel.didPlayerWin();
                remove(panel);
                myTimer.stop();
                myTimer = new Timer(delay, this);

                panel = new GamePanel(this, ResX, ResY, mapSizeX, mapSizeY);
                add(panel);
                setVisible(false);
                if (won) {
                    menu.winPage();
                } else {
                    menu.losePage();
                }
            }
        }
    }

    public static void main(String[] arguments) {
        Game game = new Game();
    }
}
