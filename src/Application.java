

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Application {
   public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(2222);
            while (true){
                Socket socket = serverSocket.accept();
                Manipulator manipulator = new Manipulator(socket);
                System.out.println("open on " +socket.getLocalSocketAddress());
                new Thread(manipulator).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
   }
}
