package msserv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;

public class Main
{

	public static void main(String[] args)
	{
		int width = 5;
		int height = 5;
		int mines = 5;
	
		Board ob = new Board(width, height, mines);
		System.out.println("Board is " + ob.mapString());
		String oldboard = ob.mapString();
		
		Board b = new Board(width, height, mines);
		b.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		b.setSize(100 * width, 100 * height);
		b.setVisible(true);
		System.out.println("Board is " + b.mapString());
		b.setMap(b.stringToMap(oldboard));
		b.play();
	}
}