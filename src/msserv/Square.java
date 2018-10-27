package msserv;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.awt.event.ActionEvent;

public class Square extends JButton implements Serializable
{
	public final int NO_MARK = 0;
	public final int FLAG = 1;
	public final int DUNNO = 2;
	
	
	boolean isClicked;
	boolean isMine;
	int locX;
	int locY;
	int marking;
	int nearbyMines = 0;
	
	
	public Square(boolean isMine, int x, int y)
	{
		super(" ");
		setBorder(BorderFactory.createRaisedBevelBorder());
		isClicked = false;
		this.isMine = isMine;
		marking = 0;
		locX = x;
		locY = y;
	}
	
	public Square(boolean isMine, int x, int y, boolean clicked)
	{
		super(" ");
		setBorder(BorderFactory.createRaisedBevelBorder());
		this.isClicked = clicked;
		this.isMine = isMine;
		marking = 0;
		locX = x;
		locY = y;
	}
	
	public void setNearbyMines(int x)
	{
		nearbyMines = x;
	}
	
	public void draw()
	{
		if(isClicked)
		{
			if(isMine)
			{
				setBackground(Color.RED);
				mySetText("X");
			}
			if(!isMine)
			{
				if(nearbyMines == 0)
				{
					setBackground(Color.LIGHT_GRAY);
				}
				else
				{
					setBackground(null);
				}
				mySetText(Integer.toString(nearbyMines));
			}
		}
		else
		{
			mySetText(getMarkChar(marking));
		}
	}
	
	public String getMarkChar(int mrkng)
	{
		setBackground(Color.YELLOW);
		switch(mrkng % 3)
		{
			case FLAG:
				return "F";
			case DUNNO:
				return "?";
			default:
				setBackground(null);
				return " ";
		}
	}
	
	public void drawFinal()
	{
		if(isClicked)
		{
			if(isMine)
			{
				mySetText("X");
			}
			if(!isMine)
			{
				mySetText(Integer.toString(nearbyMines));
			}
		}
		else
		{
			if(isMine)
			{
				mySetText("X");
			}
			if(!isMine)
			{
				mySetText("?");
			}
		}
	}
	public void mySetText(String s)
	{
		if(s.equals(" "))
		{
			//System.out.print("? ");
		}
		else
		{
			//System.out.print(s + " ");
		}
		setText(s);
	}
	
	public String toString()
	{
		String ret = "";
		if(isClicked)
		{
			ret += "c";
		}
		else
		{
			ret += "u";
		}
		if(isMine)
		{
			ret += "m";
		}
		else
		{
			ret += "f";
		}
		return ret;
	}
}

