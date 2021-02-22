package common;
import java.util.HashMap;

/**
 * This class is intended to provide a robust data structure for game parameters.
 */
public class GameParametersStruct {

    static final int MIN_ROUNDS = 1;
    static final int MAX_ROUNDS = 100;
    static final int INIT_ROUNDS = 50;
    static final int NUMBER_ACTIONS = 2;
    static final String payoff_CC = "3,3";
    static final String payoff_DC = "5,0";
    static final String payoff_CD = "0,5";
    static final String payoff_DD = "1,1";

    private HashMap<String, Integer> parameters = new HashMap<String, Integer>();

    /**
     *  N --> number of players
     *  R --> number of rounds
     *  A --> number of actions
     *  W --> number of games won by the player
     *  L --> number of games lost by the player
     *  S --> average score of the player
     */
    public GameParametersStruct() {
        parameters.put("N",0);
        parameters.put("A",NUMBER_ACTIONS);
        parameters.put("W",0);
        parameters.put("R",INIT_ROUNDS);
        parameters.put("L",0);
        parameters.put("S",0);
    }
    public GameParametersStruct(HashMap<String, Integer> parameters) {
        this.parameters.put("N",parameters.get("N"));
        this.parameters.put("A",parameters.get("A"));
        this.parameters.put("W",parameters.get("W"));
        this.parameters.put("R",parameters.get("R"));
        this.parameters.put("L",parameters.get("L"));
        this.parameters.put("S",parameters.get("S"));

    }

    public  HashMap<String, Integer> getParameters(){
        return parameters;
    }

    public int getN(){
        return parameters.get("N");
    }

    public int getR(){
        return parameters.get("R");
    }

    public int getW(){
        return parameters.get("W");
    }

    public int getL(){
        return parameters.get("L");
    }

    public int getS(){
        return parameters.get("S");
    }


    public String getPayoff(String actions){
        switch (actions){
            case "C,C":
                return payoff_CC;
            case "D,C":
                return payoff_DC;
            case "C,D":
                return payoff_CD;
            case "D,D":
                return payoff_DD;
        }
        return "Bad actions";
    }

    public void setR(int numberOfrounds){
        parameters.replace("R",numberOfrounds);
    }

    public void setN(int numberOfplayers){
        parameters.replace("N",numberOfplayers);
    }

}
