//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi

//Class for a page with a background and back button.

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class Page extends JFrame implements ActionListener{
	
	private JButton back;
	private StarMenu menu;
	private JLabel background;
	
	public Page(StarMenu m, int x, int y, ImageIcon pic, String name){
		super(name);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setSize(x,y);
    	
		menu = m;
		setLayout(null);
		setResizable(false);
		
    	back = new JButton("Back to Menu");
    	back.addActionListener(this);
    	back.setSize(200,30);
    	back.setLocation(450, 620);
    	add(back);
    	
		background = new JLabel(pic);
   		background.setSize(x,y);
   		background.setLocation(0,0);
   		add(background);; 
		
	}
	
	public void actionPerformed (ActionEvent e){
		Object source = e.getSource();
		if (isVisible()){
			if (source == back){
				menu.setVisible(true);
				setVisible(false);
			}
		}
	}
	
}