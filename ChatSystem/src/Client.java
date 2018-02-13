import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
    static Socket socket;
    static Scanner networkInput;
    static PrintWriter networkOutput;
    
    //the server, the port and the username
    static String server, username;
    static  int port = 5000;
    static InetAddress ip;
    
    public static Scanner getNetworkInput(){
        return networkInput;
    }

    public static void main(String[] args)  {
        // type name, portnumber, ip
        try {
            Scanner scan = new Scanner(System.in);
            System.out.print("Introduce the ip address: ");

            ip = InetAddress.getByName(scan.next());

            System.out.print("Introduce the port number: ");
            port = Integer.parseInt(scan.next());
            // ip = InetAddress.getLocalHost();
        }catch(UnknownHostException e){
            System.out.println("\nHost IP not found");
            System.exit(1);
        }

        join();
        Thread alive = new Thread(){
            public void run(){
                try {
                    networkOutput = new PrintWriter(socket.getOutputStream(), true);
                    while(true){
                    networkOutput.println("ALIVE");
                    Thread.sleep(60000);}
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };
        alive.start();
        MessageListener messageListener = new MessageListener();
        messageListener.start();
        sendMessages();
    }

    public static void join(){
        socket = null;
        try{
            socket = new Socket(ip, port);
            networkInput = new Scanner(socket.getInputStream());
            PrintWriter networkOutput = new PrintWriter(socket.getOutputStream(),true);
            Scanner userInput = new Scanner(System.in);
            //receive message from server
            String receive = "";
            int ok = 1;
            while(ok==1){
                ok = 0;
                System.out.print("Insert username: ");
                username = userInput.next();
                if(username.length()>11)
                    ok = 1;
                for(Character c:username.toCharArray()){
                    if(c=='_'||c=='-'||(c>64&&c<91)||(c>96&&c<123)||(c>=48&&c<=57)) {
                    }
                    else ok = 1;
                }
                if(ok == 0)
                {
                    //sent username to server
                    networkOutput.println("JOIN "+ username +", {"+ socket.getInetAddress()+"} {"+socket.getPort()+"} ");
                    //receive message from server
                    receive = networkInput.nextLine();
                    if(receive.equalsIgnoreCase("J_ERR")) {
                        ok = 1;
                    }
                    System.out.println("Server> "+receive);
                }
                else{
                    System.out.println("<Username incorrect>");
                }


            }


        }
        catch(IOException e){
            e.printStackTrace();
        }

    }
    public static void sendMessages(){

        try{

          //  Scanner networkInput = new Scanner(socket.getInputStream());
           networkOutput = new PrintWriter(socket.getOutputStream(), true);

            //set up stream for keyboard entry
            Scanner userInput = new Scanner(System.in);
            int ok=0;
            String message="", message2="DATA";
            do{
                    message = userInput.nextLine();
                    if (!message.equalsIgnoreCase("quit")) {
                        //send message to socket
                        if(!message2.equalsIgnoreCase("invisible") && ok==0) {
                            networkOutput.println(message2 + " " + username + ": " + message);
                        }
                        else{
                            ok=1;
                            if(message.equalsIgnoreCase("Visible")){
                                networkOutput.println("SERVER " + username);
                                networkOutput.println("VISIBLE " + username);
                                message2="DATA";
                                ok=0;
                            }
                            if(message.equalsIgnoreCase("Online")){
                                networkOutput.println("ONLINE "+ username);
                            }
                            if(message.equalsIgnoreCase("List")){
                                networkOutput.println("LIST "+ username);
                            }
                            if (message.equalsIgnoreCase("Commands")) {
                                networkOutput.println("COMMANDS "+ username);
                            }
                        }
                        if (message.equalsIgnoreCase("Invisible") && ok==0) {
                            System.out.println("You are invisible now, you can`t type messages anymore, but you`ll be able to read them.");
                            networkOutput.println("INVISIBLE " + username);
                            message2="INVISIBLE";
                        }
                        if(message.equalsIgnoreCase("Online")&&ok==0){
                            networkOutput.println("ONLINE "+ username);
                        }
                        if(message.equalsIgnoreCase("List")&&ok==0){
                            networkOutput.println("LIST "+ username);
                        }
                        if (message.equalsIgnoreCase("Commands")&&ok==0) {
                            networkOutput.println("COMMANDS "+ username);
                        }
                    }

            }while(!message.equalsIgnoreCase("quit"));

        }catch (IOException e){
            e.printStackTrace();
        }
        finally{
            try{
                System.out.println("\nClosing connection...");
                networkOutput.println("QUIT I am "+username);
                socket.close();
            }catch (IOException e){
                System.out.println("\nUnable to disconnect");
                System.exit(1);
            }
        }
    }
}
class MessageListener extends Thread{

    public void run(){
        while(true) {
            if (Client.getNetworkInput().hasNext()) {
                System.out.println(Client.getNetworkInput().nextLine());

            }
        }


    }
}
