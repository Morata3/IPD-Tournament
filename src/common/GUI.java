package common;

import jade.core.AID;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.*;

import agents.MainAgent;

public final class GUI extends JFrame implements ActionListener {

    private GameParametersStruct parameters = new GameParametersStruct();
    public JLabel leftPanelRoundsLabel;
    JLabel numberOfPlayers;
    public JLabel gamesPlayed;
    JLabel numberOfRounds;
    JSlider sliderRounds = new JSlider(JSlider.HORIZONTAL,parameters.MIN_ROUNDS,parameters.MAX_ROUNDS,parameters.INIT_ROUNDS);
    JList<String> list;
    JTable stadisticsTable = new JTable();
    private MainAgent mainAgent;
    private JPanel rightPanel;
    private JPanel extraInfo;
    private JTextArea rightPanelLoggingTextArea;
    private LoggingOutputStream loggingOutputStream;
    private Semaphore sem;

    public GUI() {
        initUI();
    }

    public GUI(MainAgent agent) {
        mainAgent = agent;
        initUI();
        loggingOutputStream = new LoggingOutputStream(rightPanelLoggingTextArea);
    }

    public void log(String s) {
        Runnable appendLine = () -> {
            rightPanelLoggingTextArea.append('[' + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] - " + s);
            rightPanelLoggingTextArea.setCaretPosition(rightPanelLoggingTextArea.getDocument().getLength());
        };
        SwingUtilities.invokeLater(appendLine);
    }

    public OutputStream getLoggingOutputStream() {
        return loggingOutputStream;
    }

    public void logLine(String s) {
        log(s + "\n");
    }

    public void setPlayersUI(String[] players) {
        DefaultListModel<String> listModel = new DefaultListModel<>();

        for (String s : players) {
            listModel.addElement(s);
        }
        list.setModel(listModel);
    }

    /**
     * This method catch the value select on the list of player and call sendKillAgent passing this value
     */
    public void removePlayer(){
        int index=0;
        DefaultListModel<String> listModel = new DefaultListModel<>();

        index = list.getSelectedIndex();
        listModel = (DefaultListModel<String>) list.getModel();
        listModel.remove(index);
        list.setModel(listModel);
        mainAgent.sendKillAgent(index);
    }

    /**
     * Method to create the table of stats
     * @param playerStats a map with the players and his stats
     */
    public void createTableStadisticsUI (HashMap<AID, Stats> playerStats){
        DefaultTableModel tableModel = (DefaultTableModel) stadisticsTable.getModel();
        if(tableModel.getColumnCount() == 0){
            tableModel.addColumn("Players");
            tableModel.addColumn("Games Won");
            tableModel.addColumn("Games Lost");
            tableModel.addColumn("Games Tied");
            tableModel.addColumn("Total points");
        }

        for(Stats player : playerStats.values()){
            tableModel.addRow(new String[] {player.getName(), String.valueOf(player.getGamesWon()), String.valueOf(player.getGamesLost()),String.valueOf(player.getGamesTied()), String.valueOf(player.getScore())});
        }
        tableModel.setRowCount(playerStats.size());
        stadisticsTable = new JTable(tableModel);
    }

    /**
     * It's use for delete a row of the table stats when a player is deleted
     * @param index of the player deleted (which coincides with the row of player)
     */
    public void deleteTableRow(int index){
        DefaultTableModel tableModel = (DefaultTableModel) stadisticsTable.getModel();
        tableModel.removeRow(index);
        tableModel.setRowCount(tableModel.getColumnCount() - 1);
    }

    /**
     * Update the table stats with the new values.
     * @param idPlayer to which the statistics belong
     * @param gamesWon the games won by the player
     * @param gamesLost the games lost by the player
     * @param gamesTied the games tied bu the player
     * @param score the average score of the player
     */
    public void setStadistics(int idPlayer,int gamesWon, int gamesLost, int gamesTied, double score){
        DefaultTableModel tableModel = (DefaultTableModel) stadisticsTable.getModel();

        tableModel.setValueAt(gamesWon,idPlayer,1);
        tableModel.setValueAt(gamesLost,idPlayer,2);
        tableModel.setValueAt(gamesTied,idPlayer,3);
        tableModel.setValueAt(String.format("%.2f",score),idPlayer,4);
        stadisticsTable = new JTable(tableModel);
    }

    /**
     * Update the variable number of players
     * @param N the new number of players
     */
    public void updateNumberOfPlayers(int N) {
        parameters.setN(N);
        numberOfPlayers.setText("Number of players: " + parameters.getN());
    }

    public void initUI() {
        setTitle("GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(1000, 600));
        setJMenuBar(createMainMenuBar());
        setContentPane(createMainContentPane());
        pack();
        setVisible(true);
    }

    private Container createMainContentPane() {
        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridy = 0;
        gc.weightx = 0.5;
        gc.weighty = 0.5;

        //CENTRAL PANEL
        gc.gridx = 1;
        gc.weightx = 8;
        pane.add(createCentralPanel(), gc);

        //RIGHT PANEL
        gc.gridx = 2;
        gc.weightx = 8;
        pane.add(createRightPanel(), gc);

        //LEFT PANEL
        gc.gridx = 0;
        gc.weightx = 2;
        pane.add(createLeftPanel(), gc);

        return pane;

    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        numberOfRounds = new JLabel("Number of rounds: ");

        sliderRounds.setPaintTicks(true);
        sliderRounds.setPaintTrack(true);
        sliderRounds.setPaintLabels(true);
        sliderRounds.setMajorTickSpacing(24);
        sliderRounds.setMinorTickSpacing(4);
        sliderRounds.addChangeListener(new ChangeListener());
        numberOfRounds.setText("Select number of rounds: " + parameters.getR());

        JButton leftPanelNewButton = new JButton("New Game");
        leftPanelNewButton.addActionListener(actionEvent -> mainAgent.newGame(parameters.getParameters()));
        JButton leftPanelStopButton = new JButton("Stop");
        leftPanelStopButton.addActionListener(actionEvent -> mainAgent.suspend());
        JButton leftPanelContinueButton = new JButton("Continue");
        leftPanelContinueButton.addActionListener(actionEvent -> mainAgent.activate());
        JButton leftPanelVerbose = new JButton("Verbose: " + (rightPanel.isVisible()?"on" :"off"));
        leftPanelVerbose.addActionListener(actionEvent ->
                {
                    rightPanel.setVisible(!rightPanel.isVisible());
                    leftPanelVerbose.setText("Verbose: " + (rightPanel.isVisible()?"on" :"off"));
                }
        );
        JButton leftPanelAbout = new JButton("Clear console");
        leftPanelAbout.addActionListener(actionEvent -> rightPanelLoggingTextArea.setText(""));

        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridx = 0;
        gc.weightx = 0.5;
        gc.weighty = 0;

        gc.gridy = 1;
        leftPanel.add(leftPanelContinueButton, gc);
        gc.gridy=2;
        leftPanel.add(leftPanelNewButton, gc);
        gc.gridy = 3;
        gc.weighty = 0.1;
        leftPanel.add(leftPanelStopButton, gc);
        gc.gridy=4;
        leftPanel.add(numberOfRounds,gc);
        gc.gridy = 5;
        gc.weighty = 0.5;
        leftPanel.add(sliderRounds,gc);
        gc.gridy = 6;
        gc.weighty=0;
        leftPanel.add(leftPanelVerbose, gc);
        gc.gridy=7;
        gc.weighty=1;
        leftPanel.add(leftPanelAbout, gc);

        return leftPanel;
    }

    private JPanel createCentralPanel() {
        JPanel centralPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;

        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridx = 0;

        gc.gridy = 0;
        gc.weighty = 1;
        centralPanel.add(createCentralTopSubpanel(), gc);
        gc.gridy = 1;
        gc.weighty = 0.5;
        centralPanel.add(createCentralBottomSubpanel(), gc);
        gc.gridy=2;
        centralPanel.add(infoPanel(),gc);

        return centralPanel;
    }

    private JPanel createCentralTopSubpanel() {
        JPanel centralTopSubpanel = new JPanel(new GridBagLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();

        listModel.addElement("Empty");
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);

        JLabel info1 = new JLabel("Selected player action");
        JButton updatePlayersButton = new JButton("Update players");
        updatePlayersButton.addActionListener(actionEvent -> mainAgent.updatePlayers());
        JButton leftPanelResetPlayers = new JButton("Reset All Players");
        leftPanelResetPlayers.addActionListener(actionEvent ->  mainAgent.restartPlayers());
        JButton leftPanelRemovePlayer = new JButton("Remove Player");
        leftPanelRemovePlayer.addActionListener(actionEvent -> removePlayer());
        leftPanelResetPlayers.addActionListener(this);


        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;
        gc.weighty = 0;
        gc.anchor = GridBagConstraints.CENTER;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridheight = 666;
        gc.fill = GridBagConstraints.BOTH;
        centralTopSubpanel.add(listScrollPane, gc);
        gc.gridx = 1;
        gc.gridheight = 1;
        gc.fill = GridBagConstraints.NONE;
        centralTopSubpanel.add(info1, gc);
        gc.gridy = 1;
        centralTopSubpanel.add(updatePlayersButton, gc);
        gc.gridy=2;
        centralTopSubpanel.add(leftPanelRemovePlayer, gc);
        gc.gridy=3;
        centralTopSubpanel.add(leftPanelResetPlayers,gc);

        return centralTopSubpanel;
    }

    private JPanel createCentralBottomSubpanel() {
        JPanel centralBottomSubpanel = new JPanel(new GridBagLayout());

        Object[] nullPointerWorkAround = {"P1/P2", "C", "D"};

        Object[][] data = {
                {"C", parameters.getPayoff("C,C"), parameters.getPayoff("C,D")},
                {"D", parameters.getPayoff("D,C"), parameters.getPayoff("D,D")}
        };

        JLabel payoffLabel = new JLabel("Payoff matrix");
        JTable payoffTable = new JTable(data, nullPointerWorkAround);
        payoffTable.setEnabled(false);

        JScrollPane player1ScrollPane = new JScrollPane(payoffTable);

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weighty = 0.5;
        centralBottomSubpanel.add(payoffLabel, gc);
        gc.gridy = 1;
        gc.gridx = 0;
        centralBottomSubpanel.add(player1ScrollPane, gc);

        return centralBottomSubpanel;
    }

    private JPanel createRightPanel() {
        rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weighty = 1d;
        c.weightx = 1d;

        rightPanelLoggingTextArea = new JTextArea("");
        rightPanelLoggingTextArea.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(rightPanelLoggingTextArea);
        rightPanel.add(jScrollPane, c);
        return rightPanel;
    }

    /**
     * Create a bottom panel which contents extra information
     * @return the layout of bottom panel
     */
    private JPanel infoPanel() {
        extraInfo = new JPanel(new GridBagLayout());

        leftPanelRoundsLabel = new JLabel("Round 0 / " + parameters.getR());
        numberOfPlayers = new JLabel("Number of players: " + parameters.getN());
        gamesPlayed = new JLabel("Game: 0");

        stadisticsTable.setEnabled(false);
        stadisticsTable.setFillsViewportHeight(true);
        JScrollPane stadisticsScroll = new JScrollPane(stadisticsTable);

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;
        gc.fill = GridBagConstraints.CENTER;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;

        gc.gridy=0;
        gc.gridx=0;
        gc.weighty=0;
        extraInfo.add(gamesPlayed,gc);
        gc.gridy=1;
        gc.gridx=0;
        extraInfo.add(leftPanelRoundsLabel, gc);
        gc.gridy=0;
        gc.gridx=2;
        gc.weighty=0.5;
        extraInfo.add(numberOfPlayers,gc);
        gc.gridx=1;
        gc.gridy=1;
        extraInfo.add(stadisticsScroll,gc);

        return extraInfo;
    }

    private JMenuBar createMainMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem exitFileMenu = new JMenuItem("Exit");
        exitFileMenu.setToolTipText("Exit application");
        exitFileMenu.addActionListener(this);

        JMenuItem newGameFileMenu = new JMenuItem("New Game");
        newGameFileMenu.setToolTipText("Start a new game");
        newGameFileMenu.addActionListener(this);

        menuFile.add(newGameFileMenu);
        menuFile.add(exitFileMenu);
        menuBar.add(menuFile);

        JMenu menuEdit = new JMenu("Edit");
        JMenuItem resetPlayerEditMenu = new JMenuItem("Reset Players");
        resetPlayerEditMenu.setToolTipText("Reset all player");
        resetPlayerEditMenu.setActionCommand("reset_players");
        resetPlayerEditMenu.addActionListener(this);

        JMenuItem parametersEditMenu = new JMenuItem("Parameters");
        parametersEditMenu.setToolTipText("Modify the parameters of the game");
        parametersEditMenu.addActionListener(actionEvent -> logLine("Parameters: " + JOptionPane.showInputDialog(new Frame("Configure parameters"), "Enter parameters N,S,R,I,P")));

        menuEdit.add(resetPlayerEditMenu);
        menuEdit.add(parametersEditMenu);
        menuBar.add(menuEdit);

        JMenu menuRun = new JMenu("Run");

        JMenuItem newRunMenu = new JMenuItem("New");
        newRunMenu.setToolTipText("Starts a new series of games");
        newRunMenu.addActionListener(this);

        JMenuItem stopRunMenu = new JMenuItem("Stop");
        stopRunMenu.setToolTipText("Stops the execution of the current round");
        stopRunMenu.addActionListener(actionEvent -> mainAgent.suspend());

        JMenuItem continueRunMenu = new JMenuItem("Continue");
        continueRunMenu.setToolTipText("Resume the execution");
        continueRunMenu.addActionListener(actionEvent -> mainAgent.activate());

        JMenuItem roundNumberRunMenu = new JMenuItem("Number Of rounds");
        roundNumberRunMenu.setToolTipText("Change the number of rounds");
        roundNumberRunMenu.addActionListener(actionEvent -> logLine( JOptionPane.showInputDialog(new Frame("Configure rounds"), "How many rounds?") + " rounds"));

        menuRun.add(newRunMenu);
        menuRun.add(stopRunMenu);
        menuRun.add(continueRunMenu);
        menuRun.add(roundNumberRunMenu);
        menuBar.add(menuRun);

        JMenu menuWindow = new JMenu("Window");

        JCheckBoxMenuItem toggleVerboseWindowMenu = new JCheckBoxMenuItem("Verbose", true);
        toggleVerboseWindowMenu.addActionListener(actionEvent -> rightPanel.setVisible(toggleVerboseWindowMenu.getState()));

        menuWindow.add(toggleVerboseWindowMenu);
        menuBar.add(menuWindow);

        JMenu menuAbout = new JMenu("Help");

        JMenuItem about = new JMenuItem("About");
        about.setToolTipText("Information about application");
        about.addActionListener(actionEvent -> JOptionPane.showMessageDialog(new Frame("About Application"),
                "Álvaro Sánchez García PSI-11","Author of Application",JOptionPane.INFORMATION_MESSAGE));

        menuAbout.add(about);
        menuBar.add(menuAbout);

        return menuBar;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            logLine("Button " + button.getText());
        } else if (e.getSource() instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) e.getSource();
            logLine("Menu " + menuItem.getText());
        }
    }

    public class LoggingOutputStream extends OutputStream {
        private JTextArea textArea;

        public LoggingOutputStream(JTextArea jTextArea) {
            textArea = jTextArea;
        }

        @Override
        public void write(int i) throws IOException {
            textArea.append(String.valueOf((char) i));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    public class ChangeListener implements javax.swing.event.ChangeListener {

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            JSlider source = (JSlider) changeEvent.getSource();
            if(!source.getValueIsAdjusting()){
                parameters.setR(source.getValue());
                numberOfRounds.setText("Select number of rounds: " + sliderRounds.getValue());
                leftPanelRoundsLabel.setText("Round 0 / " + parameters.getR());
                logLine(parameters.getR() + " rounds");
            }

        }
    }
}
