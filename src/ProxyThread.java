import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

class ProxyThread extends Thread 
{

    private final Socket clientSocket;

    public ProxyThread(Socket socket) 
    {
        this.clientSocket = socket;
    }

    public void run() 
    {
        try 
        {
        	
        	 String req_try = "";
             boolean endreq = false;
            // Read request
        	//CHANGE
        	int len = 0, currentlen=0, prev=0;
        	byte[] b = new byte[8196];
        	InputStream incommingIS = clientSocket.getInputStream();
            byte[] bnew = new byte[8196];
            while(!endreq)
            {
            	int j=0,i=prev;
            	currentlen = incommingIS.read(bnew);
                for(; i<prev+currentlen; ++i)
                {
                	b[i] = bnew[j++];
                }  
                len += currentlen;
                req_try+=new String(bnew, 0, currentlen);
                String check="\r\n\r\n";
                if (req_try.contains(check))
                {
                    endreq = true;
                }
                prev= currentlen;
            }

            if (len > 0) 
            {
            	String req=req_try,path = null;
                int port=80,port_new;;
                String[] patharray = req.split(" ");
                path = patharray[1];
                String local[]=new String[6];
                local[0]="localhost";
                local[1]="127.0.0.1";
                local[2]="iiit.ac.in";
                local[3]=".iiit.ac.in";
                local[4]="iiit.net";
                local[5]=".iiit.net";
                //////MISSING PART - CHANGE
                URL url_finding = new URL(path);
            	String host = url_finding.getHost();
            	String url = host+url_finding.getFile();
            	port_new = url_finding.getPort();
            	if(port_new==-1)
            		port = 80;
            	else
            		port=port_new;
	             if(url.length()-1!=url.indexOf('/'))
	            	{
	            		url+="/";
	            	}

                Socket socket=null;
                OutputStream incommingOS = clientSocket.getOutputStream();
                
               if(ProxyServer.cache.containsKey(url))
                {
            	   
            	    System.out.println("##### "+url+" Found in CACHE #####");
                	ArrayList<Byte> valuelist = (ArrayList<Byte>) ProxyServer.cache.get(url);
                	for(int i=0; i<valuelist.size(); i++)
                	{
                		for (int j=0; j < 8196; j++) 
                			{
                				if(i==valuelist.size())
                					break;
                	         	b[j] = valuelist.get(i);
                	         	i++;
                			}
                		incommingOS.write(b, 0, b.length);
                		
                	}
              
                }
                else
                {
                	
                	if(ProxyServer.choice==0)
                	{
                		if(host.contains(local[0]) || host.contains(local[1]) || host.contains(local[2]) || host.contains(local[3])|| host.contains(local[4])  || host.contains(local[5]))
                        	socket= new Socket(host, port);
                		System.out.println(" : ****Not Found in cache***** "+url);
                    	
                	}
                	else
                	{
                		if(!host.contains(local[0]) && !host.contains(local[1]) && !host.contains(local[2]) && !req.contains(local[3]) && !req.contains(local[4]) && !req.contains(local[5]))
                        	socket= new Socket("proxy.iiit.ac.in", 8080);
                        else
                        	socket= new Socket(host, port);
                		System.out.println(" : ****Not Found in cache***** "+url);
                	}
                    OutputStream outgoingOS = socket.getOutputStream();
                    outgoingOS.write(b, 0, len);
                    InputStream outgoingIS = socket.getInputStream();
                    ArrayList<Byte> incoming_data = new ArrayList<Byte>();
                    int firstreplypacketflag=0;
                    int successflag=0;
                    String reply_status=null;
                    String cache_control=null;
                    int nocacheflag=0;
                    for (int length; (length = outgoingIS.read(b)) != -1;) 
                    {
                    	
                    	reply_status=new String(b, 0, len);
                    	if(reply_status.contains("Cache-Control"))
            			{
                    		String cache_status = reply_status;
            				int cindex = cache_status.indexOf("Cache-Control");
                    		cache_status = cache_status.substring(cindex);
                			if((cache_status.indexOf("\n"))!=-1)
                    		{
                				cache_status = cache_status.substring(0, cache_status.indexOf("\n"));
                    			
                    		}
                			else
                				cache_status = cache_status.substring(0, cache_status.length());
                			if(cache_status.contains("private") || cache_status.contains("no-cache"))
                    				{
                    				System.out.println(cache_status+"Cache Flag Set");
                    				nocacheflag=1;
                    				}
            			}
                    	else if(reply_status.contains("Pragma"))
                    			{
		                    		String cache_status = reply_status;
                    				int cindex = cache_status.indexOf("Pragma");
		                    		cache_status = cache_status.substring(cindex);
		                    		//System.out.println(cindex+"================="+"\n"+cache_status);
		                    		cache_status = cache_status.substring(0, cache_status.indexOf("\n"));
		                    		System.out.println(cache_status);
		                    		if(cache_status.contains("private") || cache_status.contains("no-cache"))
		                    				{
		                    			System.out.println(cache_status+"Cache Flag Set");
	                    				nocacheflag=1;
		                    				}
                    			}
                        if(firstreplypacketflag==0)
	                        {	
                        		int in = reply_status.indexOf("\n");
                        		reply_status = reply_status.substring(0, in);
                        		int space = reply_status.indexOf(" ");
                        		reply_status = reply_status.substring(space+1);
	                        	firstreplypacketflag=1;
	                        	if(reply_status.contains("200")||reply_status.contains("30"))
	                        		successflag=1;
	                        }
                        if(successflag==1)
                        	{
                        	incommingOS.write(b, 0, length);
                        	for(int i=0; i<length; i++) 
	    	                    {
	    	                    	  incoming_data.add(new Byte(b[i]));
	    	                    }
                        	}
                        else
	                        {
                        	StringBuilder htmlBuilder = new StringBuilder();
                        	htmlBuilder.append("<html>");
                        	htmlBuilder.append("<head><title>Bad Response</title></head>");
                        	htmlBuilder.append("<body><h1>");
                        	htmlBuilder.append(reply_status);
                        	htmlBuilder.append("</h1></body>");
                        	htmlBuilder.append("</html>");
                        	String html = htmlBuilder.toString();
                        	byte[] error_code = html.getBytes();
                        	incommingOS.write(error_code, 0, error_code.length);
                        	successflag=0;
	                        break;	
	                        
	                        }
                      }
                   
                    outgoingIS.close();
                    outgoingOS.close();
                    socket.close();
                    
                    if(nocacheflag!=1)
                	   if (successflag==1) 
                		   {
                		   		ProxyServer.cache.put(url, incoming_data);
                		   }
                    }
               	
                incommingOS.close(); 
                incommingIS.close(); 
            } 
            else 
            {
            	System.out.println("Closing Input Stream");
                incommingIS.close();
            }
        } catch (IOException e) 
        {
           // e.printStackTrace();
        } 
        finally 
        {
            try 
            {
                clientSocket.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
    }
}