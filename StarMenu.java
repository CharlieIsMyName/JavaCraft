//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//This class is the opening menu for the game, that allows the user to
//start the game or exit.
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class StarMenu extends JFrame implements ActionListener {

    private JButton newGame;
    private JButton instruct;
    private JButton controls;
    private JButton exit;

    private Game game;
    private Page controlPg;
    private Page instPg;
    private Page winPg;
    private Page losePg;

    private JLabel background;

    public StarMenu(Game g, int x, int y) {
        super("Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(x, y);

        game = g;
        setLayout(null);
        setResizable(false);

        controlPg = new Page(this, x, y, new ImageIcon("./otherpics/controls.png"), "Controls");
        instPg = new Page(this, x, y, new ImageIcon("./otherpics/instructions.png"), "Instructions");
        winPg = new Page(this, x, y, new ImageIcon("./otherpics/win.png"), "You Win!");
        losePg = new Page(this, x, y, new ImageIcon("./otherpics/lose.png"), "You Lose");

        newGame = new JButton("New Game");
        newGame.addActionListener(this);
        newGame.setSize(200, 30);
        newGame.setLocation(100, 165);
        add(newGame);

        instruct = new JButton("Instructions");
        instruct.addActionListener(this);
        instruct.setSize(200, 30);
        instruct.setLocation(100, 210);
        add(instruct);

        controls = new JButton("Controls");
        controls.addActionListener(this);
        controls.setSize(200, 30);
        controls.setLocation(100, 255);
        add(controls);

        exit = new JButton("Exit");
        exit.addActionListener(this);
        exit.setSize(200, 30);
        exit.setLocation(100, 300);
        add(exit);

        background = new JLabel(new ImageIcon("./otherpics/menu.png"));
        background.setSize(x, y);
        background.setLocation(0, 0);
        add(background);;

        //setVisible(true);
    }

    public void winPage() { 	//when the player wins, this page becomes visible
        winPg.setVisible(true);
    }

    public void losePage() {	//when the player loses, this page becomes visible
        losePg.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (isVisible()) {
            if (source == newGame) {
                game.setVisible(true);
                setVisible(false);
            } else if (source == controls) {
                controlPg.setVisible(true);
                setVisible(false);
            } else if (source == instruct) {
                instPg.setVisible(true);
                setVisible(false);
            } else if (source == exit) {
                System.exit(0);
            }
        }
    }

}
