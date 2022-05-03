
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
            System.out.println(e);
        }
        finally {
            closeSocket();
            System.out.println("INFO: SERVER CLOSE CONNECTION WITH " + socket.getLocalSocketAddress());
        }
    }
    void auth() throws Exception{
        System.out.println("############################");
        username = takeMessage("CLIENT_USERNAME");
        System.out.println("INFO: USERNAME " + username);

        sendMessage("SERVER_KEY_REQUEST", -1);
        int key = Integer.parseInt(takeMessage("CLIENT_KEY_ID"));
        if (key < 0 || key > 4){
            sendMessage("SERVER_KEY_OUT_OF_RANGE_ERROR", -1);
            System.out.println("ERROR: KEY_OUT_OF_RANGE_ERROR");
            closeSocket();
        }
        System.out.println("INFO: KEY - " + key);
        int userHash = createHash(username);
        this.serverHash = (userHash + responseCreator.keys[key][0]) % 65536;
        this.clientHash = (userHash + responseCreator.keys[key][1]) % 65536;
        System.out.println("INFO: SERVER HASH - " + serverHash + "      clientHash: - " + clientHash);
        sendMessage("SERVER_CONFIRMATION", serverHash);
        int takenClientHash = Integer.parseInt(takeMessage("CLIENT_CONFIRMATION"));
        if (takenClientHash != clientHash){
            sendMessage("SERVER_LOGIN_FAILED", -1);
            System.out.println("ERROR: LOGIN_FAILED");
            closeSocket();
            return;
        }
        sendMessage("SERVER_OK", -1);
        System.out.println("INFO: AUTHENTICATION COMPLETED");
        startRobot();

    }
    int createHash( String username){
        int res = 0;
        for (int i = 0;i < username.length();i++){
            res +=(int)username.charAt(i);
        }
        res = (res * 1000) % 65536;

        return res;
    }

    void startRobot() throws Exception{
        Position goal = new Position(0, 0);
        Position firstMove = go("SERVER_MOVE");
        PrintPosition(firstMove);
        if (samePosition(firstMove, goal)){
            readMessage();
            return;
        }
        Position trend = new Position(0, 0);

        Position nextMove = new Position(0, 0);
        while (trend.x == 0 && trend.y == 0){
            nextMove = go("SERVER_MOVE");
            if (samePosition(firstMove, nextMove)){
                nextMove = go("SERVER_TURN_LEFT");
                PrintPosition(nextMove);
                continue;
            }
            trend = differencePosition(nextMove,firstMove);
        }

        while (nextMove.x != 0 || nextMove.y != 0){
            if (distance(sumPosition(nextMove, trend)) < distance(nextMove)){
                firstMove = nextMove.Clone();
                nextMove = go("SERVER_MOVE");
                PrintPosition(nextMove);
                if (!samePosition(firstMove, nextMove)){
                    continue;
                }
            }
            if (distance(sumPosition(nextMove, trend.Turn("LEFT"))) < distance(nextMove)){
                nextMove = go("SERVER_TURN_LEFT");
                trend = trend.Turn("LEFT");
                nextMove = go("SERVER_MOVE");
                PrintPosition(nextMove);
                if (!samePosition(firstMove, nextMove)) {
                    continue;
                }
            }
            nextMove = go("SERVER_TURN_RIGHT");
            trend = trend.Turn("RIGHT");
            nextMove = go("SERVER_MOVE");
            PrintPosition(nextMove);
        }

        System.out.println("INFO: LAST POSITION: " + nextMove.x +":" +nextMove.y);
        readMessage();

    }

    boolean samePosition(Position a, Position b){
        return  (a.x == b.x && a.y == b.y) ;
    }
    void PrintPosition (Position a){
        System.out.println("INFO: POSITION x:"+ a.x +" y:" + a.y);
    }
    Position sumPosition(Position a, Position b){
        return new Position(a.x + b.x, a.y + b.y);
    }
    Position differencePosition(Position a, Position b){
        return new Position(a.x - b.x, a.y - b.y);
    }
    int distance (Position position){
        return Math.abs(position.x) + Math.abs(position.y);
    }

    Position go(String type) throws Exception {
        sendMessage(type, -1);
        String message = takeMessage("CLIENT_OK");
        return parseMoveAnswer(message);
    }

    Position parseMoveAnswer(String message){
        String[] coordinates = message.split(" ");
        return new Position(Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
    }


    void readMessage() throws Exception{
        System.out.println("INFO: Robot reached the goal");
        sendMessage("SERVER_PICK_UP", -1);
        String message = takeMessage("CLIENT_MESSAGE");
        System.out.println("INFO: SECRET - " + message);
        sendMessage("SERVER_LOGOUT", -1);
    }

    void closeSocket() {
        try {
            this.reader.close();
            this.writer.close();
            this.socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    String takeMessage(String type) throws Exception {
        String newMessage = "";int actualSymbol;int nextSymbol;int maxMessageSize = validator.GetSizeOfMessage(type);
        while ((actualSymbol = reader.read()) != -1){
            this.socket.setSoTimeout(1000);
            if (actualSymbol == '\u0007'){
                if ((nextSymbol = reader.read()) != -1){
                    if (nextSymbol == '\b'){
                        if (validator.Validation(newMessage, "CLIENT_RECHARGING")){
                            socket.setSoTimeout(5000);
                            rechargingCheck();
                            newMessage = "";
                            continue;
                        }
                        if (!validator.Validation(newMessage,type)){
                            sendMessage("SERVER_SYNTAX_ERROR", -1);
                            System.out.println("ERROR: SERVER_SYNTAX_ERROR");
                            closeSocket();
                            return "";
                        }
                        return newMessage;
                    } else {
                        newMessage += (char)actualSymbol;
                        newMessage += (char)nextSymbol;
                        if (maxMessageSize - newMessage.length() == 0){
                            sendMessage("SERVER_SYNTAX_ERROR", -1);
                            System.out.println("ERROR: SERVER_SYNTAX_ERROR");
                            closeSocket();
                            return "";
                        }
                        continue;
                    }
                }
            }
            newMessage += (char)actualSymbol;
        }
        return newMessage;
    }


    public void rechargingCheck() throws Exception {

        String message = "";
        int actualSymbol;
        int nextSymbol;

        while((actualSymbol = reader.read()) != -1) {
            if (actualSymbol == '\u0007'){
                if ((nextSymbol = reader.read()) != -1){
                    if (nextSymbol == '\u0008')
                    {
                        if (!validator.Validation(message, "CLIENT_FULL_POWER")){
                            sendMessage("SERVER_LOGIC_ERROR", -1);
                            System.out.println("ERROR: SERVER_LOGIC_ERROR");
                            closeSocket();
                            return;
                        } else return;
                    }
                }

            }
            message += (char) actualSymbol;
        }

    }
    void sendMessage(String type, int key){
        String message;
        if (key == -1) {
            message = responseCreator.getResponse(type);
        } else {

            message = String.valueOf(key) + "\u0007" + "\u0008";
        }
        System.out.println( "INFO: SEND - " + message);
        writer.print(message);
        writer.flush();
    }

}
