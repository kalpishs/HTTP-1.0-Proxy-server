import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class ProxyServer 
{
	public static ConcurrentHashMap<String, ArrayList<Byte>> cache = new ConcurrentHashMap<String, ArrayList<Byte>>();
	public static int choice =1;
	public static void main(String[] args) throws IOException 
	{
		ServerSocket serverSocket = null;
        boolean listening = true;
        int port=8000;
        if(args.length==2)
	        {
	        	port=Integer.parseInt(args[0]);
	        	choice = Integer.parseInt(args[1]);
	        }
        else
	        {
	        	System.out.println("Taking Port Default 8000");
	        }
        try 
        {
            serverSocket = new ServerSocket(port);
        } 
        catch (IOException e) 
        {
            System.out.println("Port Error");
            System.exit(-1);
        }
        while (listening) 
        {
            new ProxyThread(serverSocket.accept()).start();            
        }
        serverSocket.close();
	}

}

