import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class MessageValidator {
    private Map<String, Pair<Pattern, Integer>> response;

 MessageValidator(){

     response = new HashMap<String, Pair<Pattern, Integer>>(){
         {
             put("CLIENT_USERNAME", new Pair(Pattern.compile("^[\\d\\D]{0,18}]"), 20));
             put("CLIENT_KEY_ID", new Pair(Pattern.compile("^[\\d]{1,3}"),5 ));
             put("CLIENT_CONFIRMATION",new Pair( Pattern.compile("^[\\d]{1,5}"), 7));
             put("CLIENT_OK", new Pair(Pattern.compile("OK [-]?[\\d]+ [-]?[\\d]+"), 12));
             put("CLIENT_RECHARGING" , new Pair( Pattern.compile("RECHARGING"), 12));
             put("CLIENT_FULL_POWER",new Pair( Pattern.compile("FULL_POWER"),12));
             put("CLIENT_MESSAGE",new Pair( Pattern.compile("[\\d\\D]{1,98}"),100));
         }
     };
 }

    public Boolean Validation(String  validation, String type){
     return response.get(type).t.matcher(validation).matches();
    }
    public Integer GetSizeOfMessage( String type){
     return response.get(type).u;
    }

}
