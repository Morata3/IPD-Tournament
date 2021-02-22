package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class SimpleAgent_PSI_11 extends Agent {

    private State state;
    private AID mainAgent;
    private int myId, opponentId;
    private ACLMessage msg;
    private int round = 0;
    private agentAction mylastAction = agentAction.C;
    private agentAction action;
    private int oponentIndex;
    private int myIndex;
    private int lastScore;

    protected void setup() {
        state = State.s0NoConfig;

        //Register in the yellow pages as a player
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        sd.setName("Game");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new Play());
        System.out.println("Simple Agent " + getAID().getName() + " is ready.");

    }

    /**
     * I have modified this method so that it will delete a player only if he is in the DFdomain,
     * also after deleting a player it sends a message to the main so that it can update the list of players.
     */
    protected void takeDown() {
        DFAgentDescription template = new DFAgentDescription();
        template.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        //Deregister from the yellow pages
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if(result.length >0){
                DFService.deregister(this);
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        ACLMessage ack_kill = new ACLMessage(ACLMessage.INFORM);
        ack_kill.addReceiver(mainAgent);
        ack_kill.setContent("Simple Player " + getAID().getName() + " terminating.");
        send(ack_kill);
    }

    private enum State {
        s0NoConfig, s1AwaitingGame, s2Round, s3AwaitingResult
    }
    private enum agentAction {
        C, D
    }


    private class Play extends CyclicBehaviour {
        @Override
        public void action() {
            System.out.println(getAID().getName() + ":" + state.name());
            msg = blockingReceive();
            if (msg != null) {
                System.out.println(getAID().getName() + " received " + msg.getContent() + " from " + msg.getSender().getName()); //DELETEME
                switch (state) {
                    case s0NoConfig:
                        if (msg.getContent().startsWith("Id#") && msg.getPerformative() == ACLMessage.INFORM) {
                            boolean parametersUpdated = false;
                            try {
                                parametersUpdated = validateSetupMessage(msg);
                            } catch (NumberFormatException e) {
                                System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                            }
                            if (parametersUpdated) state = State.s1AwaitingGame;

                        }
                        else if(msg.getContent().startsWith("Eliminated") && msg.getPerformative() == ACLMessage.REQUEST){
                            mainAgent = msg.getSender();
                            takeDown();
                        }
                        else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                        }
                        break;
                    case s1AwaitingGame:
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            if (msg.getContent().startsWith("Id#")) { //Game settings updated
                                try {
                                    validateSetupMessage(msg);
                                } catch (NumberFormatException e) {
                                    System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                                }
                            } else if (msg.getContent().startsWith("NewGame#")) {
                                round = 0;
                                boolean gameStarted = false;
                                try {
                                    gameStarted = validateNewGame(msg.getContent());
                                } catch (NumberFormatException e) {
                                    System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                                }
                                if (gameStarted) state = State.s2Round;
                            }
                        }
                        else if(msg.getContent().startsWith("Eliminated") && msg.getPerformative() == ACLMessage.REQUEST){
                            mainAgent = msg.getSender();
                            takeDown();
                        }
                        else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message IN state 1");
                        }
                        break;
                    case s2Round:
                        if (msg.getPerformative() == ACLMessage.REQUEST && msg.getContent().startsWith("Action")) {
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.addReceiver(mainAgent);
                            //Select action
                            if(lastScore <= 1) action = agentAction.D;
                            else action = mylastAction;

                            msg.setContent("Action#" + action);

                            System.out.println(getAID().getName() + " sent " + msg.getContent());
                            send(msg);
                            state = State.s3AwaitingResult;
                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("GameOver")) {
                            state = State.s1AwaitingGame;
                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message IN state 2:" + msg.getContent());
                        }
                        break;
                    case s3AwaitingResult:
                        if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Results#")) {
                            //Process results
                            if(myId>opponentId){
                                oponentIndex = 0;
                                myIndex = 1;
                            }
                            else{
                                oponentIndex = 1;
                                myIndex = 0;
                            }
                            lastScore = Integer.parseInt(msg.getContent().split("#")[3].split(",")[myIndex]);
                            mylastAction = action;
                            state = State.s2Round;
                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message IN state 3");
                        }
                        break;
                }
            }
        }


        /**
         * Validates and extracts the parameters from the setup message
         *
         * @param msg ACLMessage to process
         * @return true on success, false on failure
         */
        private boolean validateSetupMessage(ACLMessage msg) throws NumberFormatException {
            int tMyId;
            String msgContent = msg.getContent();

            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 3) return false;
            if (!contentSplit[0].equals("Id")) return false;
            tMyId = Integer.parseInt(contentSplit[1]);

            String[] parametersSplit = contentSplit[2].split(",");
            if (parametersSplit.length != 2) return false;
            mainAgent = msg.getSender();
            myId = tMyId;

            return true;
        }

        /**
         * Processes the contents of the New Game message
         *
         * @param msgContent Content of the message
         * @return true if the message is valid
         */
        public boolean validateNewGame(String msgContent) {
            int msgId0, msgId1;
            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 2) return false;
            if (!contentSplit[0].equals("NewGame")) return false;
            String[] idSplit = contentSplit[1].split(",");
            if (idSplit.length != 2) return false;
            msgId0 = Integer.parseInt(idSplit[0]);
            msgId1 = Integer.parseInt(idSplit[1]);
            if (myId == msgId0) {
                opponentId = msgId1;
                return true;
            } else if (myId == msgId1) {
                opponentId = msgId0;
                return true;
            }
            return false;
        }
    }
}

