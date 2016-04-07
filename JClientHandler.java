/**
 * JClientHandler.java
 *
 * This class handles communication between the client
 * and the server.  It runs in a separate thread but has a
 * link to a common list of sockets to handle broadcast.
 *
 */
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class JClientHandler implements Runnable
{
	static String BUZZ_IN_STR = "BUZZ_IN";
	int numPlayers = 3;
	private Socket connectionSock = null;
	private ArrayList<Socket> socketList;
	boolean[] sent;
	boolean[] buzzed;

	JClientHandler(Socket sock, ArrayList<Socket> socketList)
	{
		this.connectionSock = sock;
		this.socketList = socketList;	// Keep reference to master list
		sent = new boolean[numPlayers];
		buzzed = new boolean[numPlayers];
	}

	public void run()
	{
        		// Get data from a client and send it to everyone else
		try
		{
			System.out.println("Connection made with socket " + connectionSock);
			BufferedReader clientInput = new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));

			while (true)
			{
				// Get data sent from a client
				String clientText = clientInput.readLine();
				
				if (clientText != null && !clientText.contains("buzzed in."))
				{
					System.out.println("Received: " + clientText);
					
					if(!sent[0] && !sent[1] && !sent[2])
					{
						System.out.println("Player Count: " + socketList.size() + " / " + numPlayers);
					}
					else
					{
						if(clientText.equals("purple"))
						{
							System.out.println("Correct! :)");
							
							for (int i = 0; i < socketList.size(); ++i)
							{
								if(socketList.size() == numPlayers)
								{
									//DataOutputStream clientOutput = new DataOutputStream(this.socketList.get(i).getOutputStream());
									//clientOutput.writeBytes(clientText + "\n");
								}
							}
						}
						else
						{
							System.out.println("Incorrect :(");
							
							for (int i = 0; i < socketList.size(); ++i)
							{
								if(socketList.size() == numPlayers)
								{
									//DataOutputStream clientOutput = new DataOutputStream(this.socketList.get(i).getOutputStream());
									//clientOutput.writeBytes(clientText + "\n");
								}
							}
						}
					}
					// Turn around and output this data
					// to all other clients except the one
					// that sent us this information
					for (int i = 0; i < socketList.size(); ++i)
					{
						if(socketList.size() == numPlayers && !sent[i])
						{
							DataOutputStream clientOutput = new DataOutputStream(this.socketList.get(i).getOutputStream());
							clientOutput.writeBytes(clientText + "\n");
							sent[i] = true;
						}
					}
					
					clientInput = new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
				}
				else if (clientText != null && clientText.contains("buzzed in."))//someone has buzzed in
				{
					System.out.println(clientText);
					
					for (int i = 0; i < socketList.size(); ++i)
					{
						if(socketList.size() == numPlayers)
						{
							DataOutputStream clientOutput = new DataOutputStream(this.socketList.get(i).getOutputStream());
							clientOutput.writeBytes(clientText + "\n");
						}
					}
				}
				else
				{
				  // Connection was lost
				  System.out.println("Closing connection for socket " + connectionSock);
				   // Remove from arraylist
				   socketList.remove(connectionSock);
				   connectionSock.close();
				   break;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
			// Remove from arraylist
			socketList.remove(connectionSock);
		}
	}
} // ClientHandler for MTServer.java
