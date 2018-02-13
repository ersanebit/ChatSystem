import com.sun.security.ntlm.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class ServerTCP {

    static ServerSocket serverSocket;
    static Socket client;
    static final int PORT = 5000;


    static ArrayList<ClientHandler> clients = new ArrayList<>();
    static Set<String> invClient = new TreeSet<String>();
    static Set<String> visClient = new TreeSet<String>();
    public static void main(String[] args) throws IOException {

        try {
            //initialize the serversocket
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server running...");
        } catch (IOException e) {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }

        do {
            try {
                //accepting a client connection
                client = serverSocket.accept();
                ClientHandler newClient = new ClientHandler(client, "thread");
                newClient.start();
            } catch (Exception e) {
                System.out.println("Unable to connect to server");
                System.exit(1);
            }

        } while (true);
    }

    //return the list of active clients
        public static String getActiveClients(){
            String list = "Online users :";
            for(String c: visClient){
                list += c;
                list += " ";
            }
            return list;
    }
    public static String getAllClients(){
        String list ="All users :";
        for(String c : invClient){
            list += c;
            list += " ";
        }
        return list;
    }

        public static void sendMessage(String message){
            for (ClientHandler c : clients) {
                c.getPrintWriter().println( message);
            }
    }
}

    class ClientHandler extends Thread
    {
        private Socket client;
        private Scanner input;
        private PrintWriter output;
        private String username = "";
        private boolean running;


        //constructor
        public ClientHandler(Socket socket, String username){
            client = socket;
            this.username = username;
            running = true;
            try{
                input = new Scanner(socket.getInputStream());
                output = new PrintWriter(socket.getOutputStream(), true);

            }catch(IOException e){
                e.printStackTrace();
            }
        }

        public void run(){

            while(running){
                long startTime = System.currentTimeMillis();
            String received = "QUIT";

            do{

             try {
                    if(input.hasNextLine()) {
                        received = input.nextLine();
                        Scanner messageScanner = new Scanner(received);
                        String key = messageScanner.next();
                        String name="";
                        switch (key) {
                            case "JOIN": {
                               name = received.substring(received.indexOf(" ") + 1, received.indexOf(","));
                                if (checkUsername(name)) {
                                    output.println("J_OK, to see commands type COMMANDS");
                                    username = name;
                                    ServerTCP.clients.add(this);
                                    ServerTCP.invClient.add(username);
                                    ServerTCP.visClient.add(username);
                                    for (ClientHandler c : ServerTCP.clients) {
                                        c.getPrintWriter().println(name+" has connected to the chat!");
                                    }

                                    System.out.println(received);
                                } else {
                                    output.println("J_ERR"); //send the J_ERR to the user, invalid name
                                    System.out.println(name + " was rejected.");
                                }
                            }
                            break;
                            case "DATA": {
                                String check="invisible";
                                name = received.substring(5,received.indexOf(":"));
                                String c1="list",c2="online",c3="commands";
                                System.out.println(received);
                                List <String> words = Arrays.asList(received.split(""));
                                if(!check.equalsIgnoreCase(received.substring(9))){
                                    if(!c1.equalsIgnoreCase(received.substring(5+name.length()+2))){
                                        if(!c2.equalsIgnoreCase(received.substring(5+name.length()+2))){
                                            if(!c3.equalsIgnoreCase(received.substring(5+name.length()+2))) {
                                                if(!check.equalsIgnoreCase(received.substring(5+name.length()+2)))
                                                ServerTCP.sendMessage(received);
                                            }
                                        }
                                    }

                                }

                            }
                            break;
                            case "QUIT": {


                            }
                            break;
                            case "ALIVE":
                                startTime = System.currentTimeMillis();
                                break;
                            case "INVISIBLE":
                                name = received.substring(10);
                                System.out.println(name);
                                String list = "Online users :";
                                for(String c: ServerTCP.visClient) {
                                    if (!name.equals(c)) {
                                        list += c;
                                        list += " ";
                                        System.out.println();
                                    }
                                    else{
                                        ServerTCP.visClient.remove(c);
                                    }
                                    output.println(list);
                                }
                                ServerTCP.sendMessage(name+" has left the chat!");
                                break;
                            case "SERVER":
                                name = received.substring(7);
                                System.out.println(name +" rejoined!");
                                ServerTCP.sendMessage(name+" has rejoined the chat!");
                                break;
                            case "VISIBLE":
                                name = received.substring(8);
                                System.out.println(name);
                                String listt = "Online users :";
                                for(String c: ServerTCP.invClient){
                                    if(name.equals(c)){
                                        ServerTCP.visClient.add(name);
                                    }
                                }
                                for(String c: ServerTCP.visClient) {
                                    listt += c;
                                }
                                System.out.println(listt);
                                output.println(listt);
                                break;

                            case "LIST":
                                String users = ServerTCP.getAllClients();
                                output.println(users);
                                break;
                            case "ONLINE":
                                String userss = ServerTCP.getActiveClients();
                                output.println(userss);
                                break;
                            case "COMMANDS":
                                output.println("You have the available commands:" +
                                        "\nInvisible: it will hide your name from the others as if youre we're offline and you " +
                                        "will be able to see the messages that they are typing." +
                                        "\nVisible: if you are invisible it will rejoin you in the chat." +
                                        "\nOnline: it will show you all the online clients." +
                                        "\nList: it will show you all the clients that have logged in till now." +
                                        "\nQuit: it will disconnect you from the server.");
                                break;
                            default: {
                                System.out.println(this.getUsername() + "send a weird message");
                                output.println("J_ERR");
                                break;
                            }

                        }

                    }
               }
             catch (Exception e){
                 e.printStackTrace();
             }
                long currentTime = System.currentTimeMillis();
                if(currentTime-startTime>60000)
                    received = "QUIT";



            }while(!received.startsWith("QUIT"));
            try
            {
                if (client!=null)
                {   stopRunning();
                    int i=-1;
                    for(ClientHandler c: ServerTCP.clients){
                        if(c.getUsername().equalsIgnoreCase(username))
                            i = ServerTCP.clients.indexOf(c);
                    }
                    ServerTCP.visClient.remove(username);
                    ServerTCP.clients.remove(i);

                    for (ClientHandler c : ServerTCP.clients) {
                        c.getPrintWriter().println(ServerTCP.getActiveClients());
                    }

                    System.out.println(username+" left the conversation");
                    ServerTCP.sendMessage(username+" has left the conversation!");

                    client.close();
                }
            }
            catch(IOException ioEx)
            {
                System.out.println("Unable to disconnect!");
            }

            }
        }

        private boolean checkUsername(String name) {
            //loop through user threads list
            for (String clientThread : ServerTCP.invClient) {
                if (clientThread.equalsIgnoreCase(name)) //the name exists
                    return false;
            }
            return true; //the username is valid
        }
        public void stopRunning(){
            running = false;
        }
        public String getUsername(){
            return username;
        }
        public PrintWriter getPrintWriter(){
            return output;
        }
    }




