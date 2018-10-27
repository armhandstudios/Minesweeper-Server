package msserv;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;

public class Board extends JFrame
{
	private int gridX, gridY;
	private int totalMines;
	private int gameStatus;
	private int squaresLeft;
	private volatile boolean dirty;
	private volatile boolean clickInitiated;
	private ArrayList<ArrayList<Square>> map;
	private Scanner in;
	private String lastAction;
	

	public Board(int x, int y, int nMines)
	{
		//gui stuff
		super("Minesweeper Board");
		setLayout(new GridLayout(x, y));
		this.setMaximumSize(new Dimension((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
		
		
		//set vars
		gameStatus = 0;
		squaresLeft = x * y - nMines;
		gridX = x;
		gridY = y;
		totalMines = nMines;
		in = new Scanner(System.in);
		dirty = true;
		clickInitiated = false;
		setMap(generateMap());
	}
	
	public String getLastAction()
	{
		return lastAction;
	}
	
	public int getGameStatus()
	{
		return gameStatus;
	}
	
	public ArrayList<ArrayList<Square>> getMap()
	{
		return map;
	}
	
	public void setMap(ArrayList<ArrayList<Square>> readMap)
	{
		
		//remove all the current squares from the frame
		if(map != null)
		{
			for(ArrayList<Square> als : map)
			{
				for(Square ripSqr : als)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							removeSquare(ripSqr);
						}
					});
				}
			}
		}
		
		//set map to new map
		map = readMap;

		//add new squares to the component
		for(ArrayList<Square> als : map)
		{
			for(Square newSqr : als)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						addSquare(newSqr);
					}
				});
			}
		}
		dirty = true;
	}
	
	public void addSquare(Square s)
	{
		this.add(s);
	}
	
	public void removeSquare(Square s)
	{
		this.remove(s);
	}
	
	private ArrayList<ArrayList<Square>> generateMap()
	{
		//generate mines
		int[][] mineLocs = new int[gridX][gridY];
		Random rand = new Random();
		for(int i = 0; i < totalMines; i++)
		{
			int val = rand.nextInt(gridX * gridY);
			while(mineLocs[val%gridX][val/gridX] == 1)
			{
				val = rand.nextInt(gridX * gridY);
			}
			mineLocs[val%gridX][val/gridX] = 1;
		}
		
		//create map
		ArrayList<ArrayList<Square>> retMap = new ArrayList<ArrayList<Square>>();
		for(int i = 0; i < gridY; i++)
		{
			ArrayList<Square> al = new ArrayList<Square>();
			for(int j = 0; j < gridX; j++)
			{
				al.add(new Square(mineLocs[j][i] == 1, j, i));
			}
			retMap.add(al);
		}
		
		for(ArrayList<Square> al : retMap)
		{
			for(Square s : al)
			{
				s.addMouseListener(new MouseAdapter()
					{
						@Override
						public void mousePressed(MouseEvent e)
						{
						}
						
						@Override 
						public void mouseReleased(MouseEvent e)
						{
							if (SwingUtilities.isRightMouseButton(e)) 
							{
		                        s.marking++;
		                        lastAction = "r," + s.locX + "," + s.locY + ",";
		                    }
		                    else 
		                    {
		                        lastAction = "l," + s.locX + "," + s.locY + ",";
		                        gameStatus = processInput(s);
		                    }
							dirty = true;
							clickInitiated = true;
						}
						
						@Override
						public void mouseExited(MouseEvent e)
						{
						}
						
						@Override
						public void mouseEntered(MouseEvent e)
						{
						}
					});
			}
		}
		return retMap;
	}
	
	
	public void draw()
	{
		for(ArrayList<Square> al : map)
		{
			for(Square s : al)
			{
				s.draw();
			}
		}
	}
	
	public void drawFinal()
	{
		for(ArrayList<Square> al : map)
		{
			for(Square s : al)
			{
				s.drawFinal();
			}
		}
	}
	
	//get and process input
	public int getInput()
	{
	
		//get input
		boolean badInput = true;
		int xInput = 0, yInput = 0;
		while(badInput)
		{
			System.out.print("Click Where?: ");
			xInput = in.nextInt();
			yInput = in.nextInt();
			if(xInput < 0 || xInput >= gridX || yInput < 0 || yInput >= gridY)
			{
				
				System.out.print("Bad Input. ");
			}
			else
			{
				badInput = false;
			}
		}
		
		Square sqr = (Square)map.get(yInput).get(xInput);
		
		return processInput(sqr);
	}
	
	public int processInput(Square sqr)
	{
		Square actionSquare = sqr;
		//first turn reroll
		while(map.get(sqr.locY).get(sqr.locX).isMine && squaresLeft == gridX * gridY - totalMines)
		{
			setMap(generateMap());
			if(!(map.get(sqr.locY).get(sqr.locX).isMine && squaresLeft == gridX * gridY - totalMines))
			{
				lastAction += mapString();
				actionSquare = map.get(actionSquare.locY).get(actionSquare.locX);
			}
		}
		
		
		if(actionSquare.isClicked)
		{
			return 0;
		}
		int nearbyMines = getNearbyMines(actionSquare.locX, actionSquare.locY);
		actionSquare.setNearbyMines(nearbyMines);
		actionSquare.isClicked = true;
		
		//if no nearby mines, spread out till there are
		if(nearbyMines == 0)
		{
			for(int i = -1; i <= 1; i++)
			{
				for(int j = -1; j <= 1; j++)
				{
					int a = actionSquare.locX + i;
					int b = actionSquare.locY + j;
					if(a >= 0 && b >= 0 && a < gridX && b < gridY)
					{
						Square nbSqr = (Square)map.get(b).get(a);
						processInput(nbSqr);
					}
				}
			}
		}

		//evaluate win conditions
		squaresLeft--;
		if(actionSquare.isMine)
		{
			return -1;
		}
		if(squaresLeft == 0)
		{
			return 1;
		}
		
		return 0;
	}
	
	public int processAction(String msg)
	{
		String[] msgParts = msg.split(",");
		if(msgParts.length == 3 + 2 + gridY)
		{
			setMap(stringToMap(msg.substring(3 + msgParts[0].length() + msgParts[1].length() + msgParts[2].length())));
			processAction(msg.substring(0, 3 + msgParts[0].length() + msgParts[1].length() + msgParts[2].length()));
		}
		if(msgParts.length != 3)
		{
			return gameStatus;
		}
		//left click
		//if(msg.charAt(0) == 'l')
		if(msgParts[0].equals("l"))
		{
			//where
			//int x = Integer.parseInt(msg.substring(1, 2));
			//int y = Integer.parseInt(msg.substring(2, 3));
			int x = Integer.parseInt(msgParts[1]);
			int y = Integer.parseInt(msgParts[2]);
			
			processInput(map.get(y).get(x));
		}
		//right click
		//if(msg.charAt(0) == 'r')
		if(msgParts[0].equals("r"))
		{

			//int x = Integer.parseInt(msg.substring(1, 2));
			//int y = Integer.parseInt(msg.substring(2, 3));
			int x = Integer.parseInt(msgParts[1]);
			int y = Integer.parseInt(msgParts[2]);
			
			map.get(y).get(x).marking++;
		}
		dirty = false;
		draw();
		return gameStatus;
	}
	
	public void play()
	{
		while(gameStatus == 0)
		{
			if(dirty)
			{
				draw();
				dirty = false;
			}
			//gameStatus = getInput();	//for text version, not compatible while gui is running
		}
		if(gameStatus < 0)
		{
			drawFinal();
			System.out.println("You Lose.");
			setTitle("You Lose.");
		}
		if(gameStatus > 0)
		{
			drawFinal();
			System.out.println("You Win!");
			setTitle("You Win!");
		}
	}
	
	public void playUntilChange()
	{
		while(gameStatus == 0)
		{
			if(dirty)
			{
				draw();
				dirty = false;
			}
			if(clickInitiated)
			{
				clickInitiated = false;
				return;
			}
			//gameStatus = getInput();	//for text version, not compatible while gui is running
		}
		if(gameStatus < 0)
		{
			drawFinal();
			System.out.println("You Lose.");
			setTitle("You Lose.");
		}
		if(gameStatus > 0)
		{
			drawFinal();
			System.out.println("You Win!");
			setTitle("You Win!");
		}
		//return gameStatus;
	}
	
	public int playOnce()
	{
		if(gameStatus == 0)
		{
			if(dirty)
			{
				draw();
				dirty = false;
			}
			//gameStatus = getInput();	//for text version, not compatible while gui is running
		}
		if(gameStatus < 0)
		{
			drawFinal();
			System.out.println("You Lose.");
			setTitle("You Lose.");
		}
		if(gameStatus > 0)
		{
			drawFinal();
			System.out.println("You Win!");
			setTitle("You Win!");
		}
		return gameStatus;
	}
	
	public int getNearbyMines(int x, int y)
	{
		int nbMines = 0;
		for(int i = -1; i <= 1; i++)
		{
			for(int j = -1; j <= 1; j++)
			{
				int a = x + i;
				int b = y + j;
				if(a >= 0 && b >= 0 && a < gridX && b < gridY)
				{
					Square sqr = (Square)map.get(b).get(a);
					if(sqr.isMine)
					{
						nbMines++;
					}
				}
			}
		}
		return nbMines;
	}
	
	public String mapString()
	{
		String ret = "";
		ret += gridX + "," + gridY + ",";
		for(ArrayList<Square> al : map)
		{
			for(Square s : al)
			{
				ret += s.toString() + " ";
			}
			ret += ",";
		}
		return ret;
	}
	
	public ArrayList<ArrayList<Square>> stringToMap(String ohBoy)
	{
		ArrayList<ArrayList<Square>> newMap = new ArrayList<ArrayList<Square>>();
		String[] deets = ohBoy.split(",");
		int x = Integer.parseInt(deets[0]);
		int y = Integer.parseInt(deets[1]);
		for(int i = 2; i < deets.length; i++)
		{
			ArrayList<Square> al = new ArrayList<Square>();
			String[] sqrs = deets[i].split(" ");
			for(int j = 0; j < sqrs.length; j++)
			{
				int xLoc = j;
				int yLoc = i - 2;
				boolean isClicked = false;
				boolean isMine = false;
				if(sqrs[j].charAt(0) == 'c')
				{
					isClicked = true;
				}
				if(sqrs[j].charAt(1) == 'm')
				{
					isMine = true;
				}
				Square s = new Square(isMine, xLoc, yLoc, isClicked);
				s.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent e)
					{}
					
					@Override 
					public void mouseReleased(MouseEvent e)
					{
						if (SwingUtilities.isRightMouseButton(e)) 
						{
	                        s.marking++;
	                        lastAction = "r," + s.locX + "," + s.locY + ",";
	                    }
	                    else 
	                    {
	                        lastAction = "l," + s.locX + "," + s.locY + ",";
	                        gameStatus = processInput(s);
	                    }
						dirty = true;
						clickInitiated = true;
					}
					
					@Override
					public void mouseExited(MouseEvent e)
					{}
					
					@Override
					public void mouseEntered(MouseEvent e)
					{}
				});
				al.add(s);
			}
			newMap.add(al);
		}
		return newMap;
	}
	
}
