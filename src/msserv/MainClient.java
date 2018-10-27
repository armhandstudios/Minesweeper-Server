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

public class MainClient 
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		int width = 5;
		int height = 5;
		int mines = 5;
		int portNum;
		String hostname = "localhost";
		Socket clientSock;
		ObjectOutputStream out;
		ObjectInputStream in;
		boolean stillWaiting = false;
		
		//get start settings
		hostname = JOptionPane.showInputDialog("Host Name");
		portNum = Integer.parseInt(JOptionPane.showInputDialog(null, "Port Number"));
		
		
		System.out.println("Looking for server...");
		try 
		{
			clientSock = new Socket(hostname, portNum); 
			
			Board b = new Board(width, height, mines);
			b.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			b.setSize(100 * width, 100 * height);
			b.setVisible(true);
			b.setTitle("Client");
			try
			{
				//set up input streams
				out = new ObjectOutputStream(clientSock.getOutputStream());
				out.flush();
				in = new ObjectInputStream(clientSock.getInputStream());
				
				//get initial map from the server
				try
				{
					Object obj = in.readObject();
					System.out.println("Read raw " + obj);
					b.setMap(b.stringToMap((String)obj));
					System.out.println("Read " + b.mapString());
					stillWaiting = true;
				}
				catch(ClassNotFoundException e)
				{
					stillWaiting = false;
				}
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
					System.out.println("Sending " + b.getLastAction());
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
