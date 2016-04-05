/**
 * JServer.java
 *
 * This program implements a simple multithreaded chat server.  Every client that
 * connects to the server can broadcast data to all other clients.
 * The server stores an ArrayList of sockets to perform the broadcast.
 *
 * The JServer uses a ClientHandler whose code is in a separate file.
 * When a client connects, the MTServer starts a ClientHandler in a separate thread 
 * to receive messages from the client.
 *
 * To test, start the server first, then start multiple clients and type messages
 * in the client windows.
 *
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

public class JServer
{
	// Maintain list of all client sockets for broadcast
	private ArrayList<Socket> socketList;
	static String BUZZ_IN_STR = "BUZZ_IN";
	static int state = 0;//0 for people logging in, 1 for sending answer, 2 for listening for buzz
	static int num_contestants = 2;
	
	public JServer()
	{
		socketList = new ArrayList<Socket>();
	}

	private void getConnection()
	{
		// Wait for a connection from the client
		try
		{
			System.out.println("Waiting for client connections on port 7654.");
			ServerSocket serverSock = new ServerSocket(7654);
			// This is an infinite loop, the user will have to shut it down
			// using control-c

			int count = 0;//for number of clients

			while (true)
			{
				if(state == 0)
				{
					count++;

					Socket connectionSock = serverSock.accept();
					// Add this socket to the list
					socketList.add(connectionSock);
					// Send to ClientHandler the socket and arraylist of all sockets
					JClientHandler handler = new JClientHandler(connectionSock, this.socketList);
					Thread theThread = new Thread(handler);
					theThread.start();

					//DataOutputStream sends to client
					DataOutputStream out = new DataOutputStream(connectionSock.getOutputStream());
					out.writeBytes("You are number: " + count + "\n");
					out.flush();
					
					if(count == num_contestants)
					{
						state = 1;
					}
				}
				else if(state == 1)
				{
					//System.out.println("Total number of contestants reached. The game will begin.");
					
					for(int i = 0; i < socketList.size(); ++i)
					{
						//JClientHandler handler = new JClientHandler(socketList.get(i), this.socketList);
						//Thread theThread = new Thread(handler);
						//theThread.start();
						
						//DataOutputStream sends to client
						DataOutputStream out = new DataOutputStream(socketList.get(i).getOutputStream());
						out.writeBytes("answer: What do the colors blue and red create?");
					}
					
					state = 2;//listen for an answer
				}
				else if(state == 2)
				{
					//listening for a buzz
					//DataOutputStream out = new DataOutputStream(socketList.get(i).getOutputStream());
					//out.writeBytes("answer: What do the colors blue and red create?");
				}
			}
			// Will never get here, but if the above loop is given
			// an exit condition then we'll go ahead and close the socket
			//serverSock.close();
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		JServer server = new JServer();
		server.getConnection();
	}
} // MTServer
