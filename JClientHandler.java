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

	int numPlayers = 3;
	private Socket connectionSock = null;
	private ArrayList<Socket> socketList;
	boolean[] sent;
	String[] buzzed;
	String[] players;
	int currentBuzzIndex = 0;
	int buzzedFilled = 0;
	boolean gameStarted = false;

	JClientHandler(Socket sock, ArrayList<Socket> socketList)
	{
		this.connectionSock = sock;
		this.socketList = socketList;	// Keep reference to master list
		sent = new boolean[numPlayers];
		buzzed = new String[numPlayers];
		players = new String[numPlayers];
		resetBuzzOrder();
	}

	//reset the order that people buzz in
	void resetBuzzOrder()
	{
		currentBuzzIndex = 0;
		buzzedFilled = 0;
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
					
					if(!sent[0] || !sent[1] || !sent[2])
					{
						System.out.println("Player Count: " + socketList.size() + " / " + numPlayers);

						// for loop goes through on each player's connection.
						for (int i = 0; i < socketList.size(); ++i)
						{
							if(!sent[i])//for sending names
							{
								sent[i] = true;
								players[i] = clientText;
							}
						}

					}
					else if(gameStarted)
					{
						if(clientText.equals("purple"))
						{
							System.out.println("Correct! :)");
							
							for (int i = 0; i < socketList.size(); ++i)
							{
								if(socketList.size() == numPlayers)
								{
									DataOutputStream clientOutput = new DataOutputStream(this.socketList.get(i).getOutputStream());
									clientOutput.writeBytes(buzzed[currentBuzzIndex] + " is correct!" + "\n");
								}
							}

							resetBuzzOrder();
						}
						else
						{
							System.out.println("Incorrect :(");
							
							for (int i = 0; i < socketList.size(); ++i)
							{
								if(socketList.size() == numPlayers)
								{
									DataOutputStream clientOutput = new DataOutputStream(this.socketList.get(i).getOutputStream());
									clientOutput.writeBytes(buzzed[currentBuzzIndex] + " is incorrect!" + "\n");

									if(currentBuzzIndex != 2)
									{
										currentBuzzIndex++;
									}
									else
									{
										resetBuzzOrder();
									}
								}
							}
						}
					}
					if(sent[0] && sent[1] && sent[2] && !gameStarted)
					{
						for (int i = 0; i < 3; ++i)
						{
							DataOutputStream clientOutput = new DataOutputStream(this.socketList.get(i).getOutputStream());
							clientOutput.writeBytes(" " + "\n");
							clientOutput.writeBytes("GAME_READY" + "\n");
						}
						
						gameStarted = true;
					}

					
					clientInput = new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
				}
				else if (clientText != null && clientText.contains("buzzed in."))//someone has buzzed in
				{
					System.out.println(clientText);//prints serverOutput.writeBytes(name + " buzzed in." + "\n")

					for(int i = 0; i < 3; ++i)
					{
						if(clientText.contains(players[i]))
						{
							buzzed[buzzedFilled] = players[i];
							buzzedFilled++;
						}
					}
					
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
