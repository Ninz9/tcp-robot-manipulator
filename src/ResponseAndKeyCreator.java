import java.util.HashMap;

public class ResponseAndKeyCreator {
    HashMap<String, String> response;
    public int[][] keys;
    ResponseAndKeyCreator(){
        response = new HashMap<>(){
            {
                put("SERVER_KEY_REQUEST", "107 KEY REQUEST");
                put("SERVER_KEY_OUT_OF_RANGE_ERROR", "303 KEY OUT OF RANGE");
                put("SERVER_LOGIN_FAILED", "300 LOGIN FAILED");
                put("SERVER_LOGIC_ERROR", "302 LOGIC ERROR");
                put("SERVER_SYNTAX_ERROR", "301 SYNTAX ERROR");
                put("SERVER_LOGOUT", "106 LOGOUT");
                put("SERVER_PICK_UP", "105 GET MESSAGE");
                put("SERVER_TURN_RIGHT", "104 TURN RIGHT");
                put("SERVER_TURN_LEFT", "103 TURN LEFT");
                put("SERVER_MOVE", "102 MOVE");
                put("SERVER_OK", "200 OK");
            }
        };
        keys = new int[][]{
                {23019, 32037},
                {32037, 29295},
                {18789, 13603},
                {16443, 29533},
                {19189, 21952}
        };

    }




    static String getResponse(ResponseAndKeyCreator responseCreator,String type){
        return responseCreator.response.get(type) + "\u0007" + "\u0008";
    }
}
