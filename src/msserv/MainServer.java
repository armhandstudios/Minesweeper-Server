package msserv;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MainServer
{

	public static void main(String[] args) throws ClassNotFoundException
	{
		int width = 5;
		int height = 5;
		int mines = 5;
		int portNum;
		ServerSocket serverSock;
		Socket clientSock;
		ObjectOutputStream out;
		ObjectInputStream in;
		
		//Set up start settings gui
		portNum = Integer.parseInt(JOptionPane.showInputDialog(null, "Port Number"));
		
		System.out.println("Initiating Socket...");
		try
		{
			serverSock = new ServerSocket(portNum);
			clientSock = serverSock.accept();
			Board b = new Board(width, height, mines);
			b.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			b.setSize(100 * width, 100 * height);
			b.setVisible(true);
			b.setTitle("Server");
			try
			{
				//Set up streams
				out = new ObjectOutputStream(clientSock.getOutputStream());
				out.flush();
				in = new ObjectInputStream(clientSock.getInputStream());
				
				//give client initial board
				out.writeObject(b.mapString());
				
				//listen for changes
				Thread thread = new Thread(new Runnable()
				{
					public void run()
					{
						boolean alreadyDisconnected = false;
						{
							do
							{
								boolean stillWaiting = false;
								while(!stillWaiting)
								{
									try 
									{
										Object obj = in.readObject();
										System.out.println("Read raw " + obj);
										if(obj != null)
										{
											b.processAction((String)obj);
											stillWaiting = true;
										}
									} 
									catch (ClassNotFoundException e) 
									{
										e.printStackTrace();
									}
									catch(SocketException e)
									{
										if(!alreadyDisconnected)
										{
											JOptionPane.showMessageDialog(null, "Other player disconnected");
											alreadyDisconnected = true;
										}
									}
									catch (IOException e) 
									{
										e.printStackTrace();
									}
									
								}
								b.draw();
							} while(b.getGameStatus() == 0);
						}
					}
				});
				thread.start();
				
				//play and send changes made
				do
				{
					b.playUntilChange();
					System.out.println("Sending " + b.mapString());
					out.writeObject(b.getLastAction());
					out.flush();
				} while(b.getGameStatus() == 0);
			}
			catch(EOFException e)
			{
				System.out.println("Server Disconnect");
			}
		} 
		catch(IOException e) 
		{
            System.out.println("Exception caught when trying to listen on port "
                + portNum + " or listening for a connection");
            System.out.println(e.getMessage());
        }
	}
}