package common;

/**
 * This class is intended to provide a robust data structure gor players stats.
 */
public class Stats {
    private int gamesWon;
    private int gamesLost;
    private int gamesTied;
    private double score;
    private String playerName;

    public Stats(String playerName) {
        this.playerName = playerName;
        gamesWon = 0;
        gamesLost = 0;
        score = 0.00;
    }

    public String getName(){
        return this.playerName;
    }

    public int getGamesWon(){
        return this.gamesWon;
    }

    public int getGamesLost(){
        return this.gamesLost;
    }

    public int getGamesTied(){
        return this.gamesTied;
    }

    public double getScore(){
        return this.score;
    }

    public void setScore(double score){
        this.score = this.score + score;
    }

    public void setGamesWon(int gamesWon){
        if(gamesWon != 0){
            this.gamesWon = this.gamesWon +1;
        }else this.gamesWon=0;
    }

    public void setGamesLost(int gamesLost){
        if(gamesLost != 0){
            this.gamesLost = this.gamesLost +1;
        }else this.gamesWon=0;
    }

    public void setGamesTied(int gamesTied){
        if(gamesTied != 0){
            this.gamesTied = this.gamesTied +1;
        }else this.gamesTied=0;
    }

    public void restart(){
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesTied = 0;
        this.score = 0.00;
    }

}
