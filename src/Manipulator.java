
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Manipulator implements Runnable {
    PrintWriter writer;
    BufferedReader reader;
    Socket socket;
    MessageValidator validator;
    ResponseAndKeyCreator responseCreator;
    String username;
    int clientHash;
    int serverHash;


    Manipulator(Socket socket)throws Exception{
        this.socket = socket;
        writer = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        validator = new MessageValidator();
        responseCreator = new ResponseAndKeyCreator();
    }

    @Override
    public void run() {
        try {
            auth();
        } catch (Exception e){
            e.getStackTrace();
        }
    }
    void auth() throws Exception{
        username = takeMessage("CLIENT_USERNAME");
        System.out.println(username);
        sendMessage("SERVER_KEY_REQUEST", -1);
        int key = Integer.parseInt(takeMessage("CLIENT_KEY_ID"));
        System.out.println(key);
        int userHash = createHash(username);
        this.serverHash = (userHash + responseCreator.keys[key][0]) % 65536;
        this.clientHash = (userHash + responseCreator.keys[key][1]) % 65536;
        System.out.println("server Hash:"+serverHash + "  clientHash:" + clientHash);
        sendMessage("SERVER_CONFIRMATION", serverHash);
        int takenClientHash = Integer.parseInt(takeMessage("CLIENT_CONFIRMATION"));
        if (takenClientHash != clientHash){
            sendMessage("SERVER_LOGIN_FAILED", -1);
            return;
        }
        sendMessage("SERVER_OK", -1);

        move();

    }
    int createHash( String username){
        int res = 0;
        for (int i = 0;i < username.length();i++){
            res +=(int)username.charAt(i);
        }
        res = (res * 1000) % 65536;

        return res;
    }

    void move(){

    }

    void pick(){

    }

    void closeSocket() throws Exception{
        this.reader.close();
        this.writer.close();
        this.socket.close();
    }

    String takeMessage(String type) throws IOException {
        String newMessage = "";
        int actualSymbol;
        int nextSymbol;
        int maxMessageSize = validator.GetSizeOfMessage(type);
        while ((actualSymbol = reader.read()) != -1){
            if (newMessage.length() == maxMessageSize){
                break;
            }


            if (actualSymbol == '\u0007'){
                if ((nextSymbol = reader.read()) != -1){
                    if (nextSymbol == '\u0008'){
                        return newMessage;

                    }
                } else {

                }
            }


            newMessage += (char)actualSymbol;
        }
        if (!validator.Validation(newMessage,type)){
            return "";
        }
        System.out.println("TAKEN: "+ newMessage);
        return newMessage;

    }


    void sendMessage(String type, int key){
        String message;
        if (key == -1) {
            message = responseCreator.getResponse(type);
        } else {

            message = String.valueOf(key) + "\u0007" + "\u0008";
        }
        System.out.println("SEND:" + message);
        writer.print(message);
        writer.flush();
    }

}
