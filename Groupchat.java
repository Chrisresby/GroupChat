
import java.net.*;
import java.io.*;
import java.util.*;


public class Groupchat
{
    private static final String TERMINATE = "Exit";
    static String name;
    static volatile boolean finished = false;

    public static void main(String[] args)
    {
        if(args.length != 2)
            System.out.println("Two arguments required: <multicast-host> <port-number>");

        else
        {
            try
            {
                InetAddress group = InetAddress.getByName(args[0]);
                int port = Integer.parseInt(args[1]);
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter your user name");
                name = sc.nextLine();
                MulticastSocket socket = new MulticastSocket(port);


                socket.setTimeToLive(0);

                socket.joinGroup(group);
                Thread t = new Thread(new ReadThread(socket, group, port));

                //Make a thread for reading messages
                t.start();

                //Sent to current group
                System.out.println("Start typing messages...\n");
                while(true)
                {
                    String message;
                    message = sc.nextLine();
                    if(message.equalsIgnoreCase(Groupchat.TERMINATE))
                    {
                        finished = true;
                        socket.leaveGroup(group);
                        socket.close();
                        break;
                    }
                    message = name + ": " + message;
                    byte[] buffer = message.getBytes();
                    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(datagram);
                }
            }
            catch(SocketException se)
            {
                System.out.println("Error creating socket");
                se.printStackTrace();
            }
            catch(IOException ie)
            {
                System.out.println("Error reading/writing from/to socket");
                ie.printStackTrace();
            }
        }
    }
}
class ReadThread implements Runnable
{
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private static final int MAX_LEN = 1000;

    ReadThread(MulticastSocket socket, InetAddress group, int port)
        {
            this.socket = socket;
            this.group = group;
            this.port = port;
        }
        @Override
        public void run()
        {
            while(!Groupchat.finished)
            {
                byte[] buffer = new byte[ReadThread.MAX_LEN];
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                String message;

                try
                {
                    socket.receive(datagram);
                    message = new String(buffer, 0, datagram.getLength(), "UTF-8");
                    if(!message.startsWith(Groupchat.name))
                        System.out.println(message);

                }
                catch(IOException e)
                {
                    System.out.println("Socket Closed!");

                }

            }
        }
}
