package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import common.GUI;
import common.GameParametersStruct;
import common.Stats;

public class MainAgent extends Agent {

    private GUI gui;
    private AID[] playerAgents;
    HashMap<AID, Stats> playerStats = new HashMap<AID, Stats>();
    private GameParametersStruct parameters = new GameParametersStruct();
    private int games = 0;
    private boolean activate=true;

    @Override
    protected void setup() {
        gui = new GUI(this);
        System.setOut(new PrintStream(gui.getLoggingOutputStream()));

        updatePlayers();
        gui.logLine("Agent " + getAID().getName() + " is ready.");
    }

    /**
     * Send a ACLMessage to the player you want to remove with the keyword eliminated
     * @param index of the player who want to delete
     */
    public void sendKillAgent(int index){
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(playerAgents[index]);
        msg.setContent("Eliminated");
        send(msg);

        ACLMessage ack_kill = blockingReceive();
        gui.logLine(ack_kill.getContent());

        gui.deleteTableRow(index);
        updatePlayers();
    }

    /**
     * This method uses the AgentController class to suspend the main agent
     */
    public void suspend() {
        if(activate) {
            try {
                String agentName = this.getName().split("@")[0];
                AgentController mainController = this.getContainerController().getAgent(agentName);
                activate = false;
                mainController.suspend();
            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method uses the AgentController class to activate the main agent
     */
    public void activate(){
        if(!activate) {
            try {
                String agentName = this.getName().split("@")[0];
                AgentController mainController = this.getContainerController().getAgent(agentName);
                activate = true;
                mainController.activate();
            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }
    }
    
    public int updatePlayers() {
        gui.logLine("Updating player list");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            parameters.setN(result.length);
            if (result.length > 0) {
                gui.logLine("Found " + result.length + " players");
                gui.updateNumberOfPlayers(result.length);
            }
            playerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                playerAgents[i] = result[i].getName();
            }
        } catch (FIPAException fe) {
            gui.logLine(fe.getMessage());
        }
        String[] playerNames = new String[playerAgents.length];
        for (int i = 0; i < playerAgents.length; i++) {
            playerNames[i] = playerAgents[i].getName().split("@")[0];

            if(playerStats.isEmpty()) playerStats.put(playerAgents[i],new Stats(playerNames[i]));
            else {
                if(playerStats.containsKey(playerAgents[i])) continue;
                else playerStats.put(playerAgents[i],new Stats(playerNames[i]));
            }
        }
        gui.setPlayersUI(playerNames);
        gui.createTableStadisticsUI(playerStats);
        return 0;
    }

    /**
     * Reset the statistics of all players
     */
    public void restartPlayers(){
        playerStats.forEach(((aid, stats) -> {
            stats.restart();
        }));
        for (int index= 0; index < playerStats.size(); index ++){
            gui.setStadistics(index,0,0,0,0.00);
        }
    }

    public int newGame(HashMap<String, Integer> parameters) {
        this.parameters = new GameParametersStruct(parameters);
        addBehaviour(new GameManager());
        return 0;
    }

    /**
     * In this behavior this agent manages the course of a match during all the
     * rounds.
     */
    private class GameManager extends SimpleBehaviour {

        @Override
        public void action() {
            //Assign the IDs
            ArrayList<PlayerInformation> players = new ArrayList<>();
            int lastId = 0;
            for (AID a : playerAgents) {
                players.add(new PlayerInformation(a, lastId++));
            }

            //Initialize (inform ID)
            for (PlayerInformation player : players) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("Id#" + player.id + "#" + parameters.getN() + "," + parameters.getR());
                msg.addReceiver(player.aid);
                send(msg);
            }
            //Organize the matches
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) {
                    playGame(players.get(i), players.get(j));
                }
            }
            games = games +1;
            gui.gamesPlayed.setText("Game: " + games);
        }

        private void playGame(PlayerInformation player1, PlayerInformation player2) {
            //Changing Rounds
            double max = 1;
            double min = 0.9;
	    double newRandomValue;
            int newRound;

            // With the timestamp we make a new seed for the random value, and then
            // we take a value between R (number of rounds) and 0.9*R
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long seed = timestamp.getTime();
            Random random = new Random(seed);
	    newRandomValue = min + (max - min) * random.nextDouble();
            newRound = (int) (parameters.getR() * newRandomValue);
	    gui.leftPanelRoundsLabel.setText("Round 0 / " + newRound);
            gui.logLine(newRound + " rounds for this game");


            //Assuming player1.id < player2.id
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(player1.aid);
            msg.addReceiver(player2.aid);
            msg.setContent("NewGame#" + player1.id + "," + player2.id);
            send(msg);

            String act1, act2;
            int score1=0,score2=0;
            double averageScore1=0,averageScore2=0;
            Stats statsPlayer1 = playerStats.get(player1.aid);
            Stats statsPlayer2 = playerStats.get(player2.aid);

            for(int round=1; round<=newRound; round++) {
                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent("Action");
                msg.addReceiver(player1.aid);
                send(msg);

                gui.logLine("Main Waiting for movement");
                ACLMessage move1 = blockingReceive();
                gui.logLine("Main Received " + move1.getContent() + " from " + move1.getSender().getName());
                act1 = move1.getContent().split("#")[1];

                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent("Action");
                msg.addReceiver(player2.aid);
                send(msg);

                gui.logLine("Main Waiting for movement");
                ACLMessage move2 = blockingReceive();
                gui.logLine("Main Received " + move2.getContent() + " from " + move2.getSender().getName());
                act2 = move2.getContent().split("#")[1];

                String result = parameters.getPayoff(act1 + "," + act2);
                String points1 = result.split(",")[0];
                String points2 = result.split(",")[1];

                msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(player1.aid);
                msg.addReceiver(player2.aid);
                msg.setContent("Results#" + player1.id + "," + player2.id + "#" + act1 + "," + act2 + "#" + points1 + "," + points2);
                send(msg);

                score1=score1 + Integer.parseInt(points1);
                score2=score2 + Integer.parseInt(points2);

                gui.leftPanelRoundsLabel.setText("Round "+ round + "/ " + newRound);
            }

            //We add the points and divide them by the number of rounds
            averageScore1=(double) score1 / parameters.getR();
            averageScore2=(double) score2 / parameters.getR();

            statsPlayer1.setScore(averageScore1);
            statsPlayer2.setScore(averageScore2);

            msg.addReceiver(player1.aid);
            msg.addReceiver(player2.aid);
            msg.setContent("GameOver#" + player1.id + "," + player2.id + "#"+ averageScore1 + "," + averageScore2);

            //We update the players' games
            if(averageScore1 > averageScore2){
                statsPlayer1.setGamesWon(1);
                statsPlayer2.setGamesLost(1);
            }
            else if (averageScore1 == averageScore2){
                statsPlayer1.setGamesTied(1);
                statsPlayer2.setGamesTied(1);
            }
            else{
                statsPlayer2.setGamesWon(1);
                statsPlayer1.setGamesLost(1);
            }
            gui.setStadistics(player1.id,statsPlayer1.getGamesWon(),statsPlayer1.getGamesLost(),statsPlayer1.getGamesTied(),statsPlayer1.getScore());
            gui.setStadistics(player2.id,statsPlayer2.getGamesWon(),statsPlayer2.getGamesLost(),statsPlayer2.getGamesTied(),statsPlayer2.getScore());

            send(msg);

        }

        @Override
        public boolean done() {
            return true;
        }
    }

    public class PlayerInformation {
        AID aid;
        int id;
        public PlayerInformation(AID a, int i) {
            aid = a;
            id = i;
        }

        @Override
        public boolean equals(Object o) {
            return aid.equals(o);
        }
    }

}
