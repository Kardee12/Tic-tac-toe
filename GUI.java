// ============================================================================
// Support multiplayer and single player (vs. computer) modes
// Taken From: http://programmingnotes.org/
// ============================================================================

import javafx.util.Pair;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class GUI extends JFrame implements ActionListener{
    private static final int MIN_ROWS = 1;
    private static final int MAX_ROWS = 20;
    private static final String TOKEN_X = "X";
    private static final String TOKEN_O = "O";
    private static final Font MAIN_FONT = new Font("Purisa", Font.BOLD, 18);
    private static final int X = 800, Y = 480, BG_COLOR = 190; // size of the game window

    private final JMenuItem mnuNewGame = new JMenuItem("  New Game");
    private final JMenuItem mnuExit = new JMenuItem("Quit");

    private JButton[][] cells;

    private final JPanel pnlSouth = new JPanel();
    private final JPanel pnlBottom = new JPanel();
    private final JPanel pnlPlayingField = new JPanel();

    private final JRadioButton selectX = new JRadioButton("User Plays X", false);
    private final JRadioButton selectO = new JRadioButton("User Plays O", false);
    private final JRadioButton selectComputer = new JRadioButton("User vs Computer", false);
    private final JRadioButton selectUser = new JRadioButton("User vs User", false);

    private String nextPlayer = TOKEN_X;// In User vs Computer, User goes first, and is X
    private boolean setTableEnabled = false;

    private enum GameMode {SINGLE_PLAYER, MULTI_PLAYER}

    private GameMode gameMode;

    private enum BoardStatus {X_WIN, O_WIN, DRAW, INCOMPLETE, NOT_STARTED}

    private static class Move {
        int position;
        int score;

        Move(int position, int score) {
            this.position = position;
            this.score = score;
        }
    }

    public GUI() {
//Setting window properties:
        JFrame window = new JFrame("Tic Tac Toe Game");
        window.setSize(X, Y);
        window.setLocation(300, 180);
        window.setResizable(true);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//------------  Sets up Panels and text fields  ------------------------//
// setting Panel layouts and properties
        JPanel pnlNorth = new JPanel();
        pnlNorth.setLayout(new FlowLayout(FlowLayout.CENTER));
        pnlSouth.setLayout(new FlowLayout(FlowLayout.CENTER));

        pnlNorth.setBackground(new Color(164, 20, 50));
        pnlSouth.setBackground(new Color(BG_COLOR, BG_COLOR, BG_COLOR));

        JPanel pnlTop = new JPanel();
        pnlTop.setBackground(new Color(BG_COLOR, BG_COLOR, BG_COLOR));
        pnlBottom.setBackground(new Color(BG_COLOR, BG_COLOR, BG_COLOR));

        pnlTop.setLayout(new FlowLayout(FlowLayout.CENTER));
        pnlBottom.setLayout(new FlowLayout(FlowLayout.CENTER));

// adding menu items to menu bar
        JMenuBar mnuMain = new JMenuBar();
        JMenuItem mnuGameTitle = new JMenuItem("|Tic Tac Toe|  ");
        mnuMain.add(mnuGameTitle);
        mnuGameTitle.setEnabled(false);
        mnuGameTitle.setFont(MAIN_FONT);
        mnuMain.add(mnuNewGame);
        mnuNewGame.setFont(MAIN_FONT);
        mnuMain.add(mnuExit);
        mnuExit.setFont(MAIN_FONT);//---->Menu Bar Complete

// adding Action Listener to all the Buttons and Menu Items
        mnuNewGame.addActionListener(this);
        mnuExit.addActionListener(this);

// adding everything needed to pnlNorth and pnlSouth
        pnlNorth.add(mnuMain);
        showGame();

// adding to window and Showing window
        window.add(pnlNorth, BorderLayout.NORTH);
        window.add(pnlSouth, BorderLayout.CENTER);

        setUpPlayingField();
        createGameModeScreen();

        window.setVisible(true);
    }// End GUI

    // ===========  Start Action Performed  ===============//
    public void actionPerformed(ActionEvent click) {
// get the mouse click from the user
        Object source = click.getSource();
        boolean btnEmptyClicked = false;

// check if a button was clicked on the gameboard
        if (getEmptyCellCount() > 0) {
            for (int i = 0; i < cells.length; i++) {
                for (int j = 0; j < cells.length; j++) {
                    if (source == cells[i][j]) {
                        btnEmptyClicked = true;
                        cells[i][j].setText(nextPlayer);

                        if (nextPlayer.equals(TOKEN_X)) {
                            nextPlayer = TOKEN_O;
                        } else {
                            nextPlayer = TOKEN_X;
                        }
                        cells[i][j].setEnabled(false);

                        if (gameMode == GameMode.SINGLE_PLAYER && getEmptyCellCount() > 0 && !isWinner(TOKEN_X) && !isWinner(TOKEN_O)) {
                            cpuMakeMoveMiniMax();
                        }

                        pnlPlayingField.requestFocus();
                    }
                }
            }

// if a button was clicked on the gameboard, check for a winner
            if (btnEmptyClicked) {
                if (isWinner(TOKEN_X)) {
                    if (gameMode == GameMode.MULTI_PLAYER) {
                        JOptionPane.showMessageDialog(null, "X wins!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Congrats, you won!");
                    }
                } else if (isWinner(TOKEN_O)) {
                    if (gameMode == GameMode.MULTI_PLAYER) {
                        JOptionPane.showMessageDialog(null, "O wins!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Sorry, you lost.");
                    }
                } else if (getEmptyCellCount() == 0) {
                    JOptionPane.showMessageDialog(null, "Cat's game!");
                }
            }
        } // if (getEmptyCellCount() > 0)

// check if the user clicks on a menu item
        if (source == mnuNewGame) {
            BoardStatus boardStatus = getBoardStatus();
            if (boardStatus == BoardStatus.INCOMPLETE) {
                int option = JOptionPane.showConfirmDialog(null, "If you start a new game," +
                                " your current game will be lost...\n" + "Are you sure you want to continue?"
                        , "New Game?", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    nextPlayer = null;
                    setTableEnabled = false;
                    setUpPlayingField();
                    createGameModeScreen();
                } else {
                    showGame();
                }
            } else {
                setUpPlayingField();
                createGameModeScreen();
            }
        } else if (source == mnuExit) {
            int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?",
                    "Quit", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }

        pnlSouth.setVisible(false);
        pnlSouth.setVisible(true);
    }// End Action Performed

    private class TokenSelectListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JRadioButton theButton = (JRadioButton) event.getSource();
            if (theButton.getText().equals("User Plays X")) {
                nextPlayer = TOKEN_X;
            }
            if (theButton.getText().equals("User Plays O")) {
                nextPlayer = TOKEN_O;
            }

// redisplay the gameboard to the screen
            pnlSouth.setVisible(false);
            pnlSouth.setVisible(true);
            redrawGameBoard();
        }
    }

    private class ModeSelectListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JRadioButton theButton = (JRadioButton) event.getSource();
            if (theButton.getText().equals("User vs Computer")) {
                gameMode = GameMode.SINGLE_PLAYER;
                nextPlayer = TOKEN_X;

                pnlSouth.setVisible(false);
                pnlSouth.setVisible(true);
                redrawGameBoard();
            } else if (theButton.getText().equals("User vs User")) {
                gameMode = GameMode.MULTI_PLAYER;

                createStartingPlayerPanel();
                pnlSouth.setVisible(false);
                pnlSouth.setVisible(true);
            }
        }
    }

    /*
    ----------------------------------
    Start of all the other methods. |
    ----------------------------------
     */
    private void redrawGameBoard() {
        pnlSouth.removeAll();
        showGame();

        for (int i = 0; i < cells.length; ++i) {
            for (int j = 0; j < cells.length; ++j) {
                cells[i][j].setText("");
                cells[i][j].setEnabled(setTableEnabled);
            }
        }
    }

    private void showGame() {
// shows the Playing Field
        pnlSouth.setLayout(new BorderLayout());
        pnlSouth.add(pnlPlayingField, BorderLayout.CENTER);
        pnlPlayingField.requestFocus();
    }

    private int getEmptyCellCount() {
        int count = 0;
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[0].length; col++) {
                if (cells[row][col].getText().isEmpty()) {
                    count++;
                }
            }
        }

        return count;
    }

    private BoardStatus getBoardStatus() {
        if (isWinner(TOKEN_X)) {
            return BoardStatus.X_WIN;
        } else if (isWinner(TOKEN_O)) {
            return BoardStatus.O_WIN;
        } else {
            int nonEmptyCount = 0;

            for (int col = 0; col < cells.length; col++) {
                for (int row = 0; row < cells.length; row++) {
                    String cellText = cells[row][col].getText();
                    if (!cellText.isEmpty()) {
                        nonEmptyCount++;
                    }
                }
            }

            if (nonEmptyCount == cells.length * cells.length) {
                return BoardStatus.DRAW;
            } else if (nonEmptyCount == 0) {
                return BoardStatus.NOT_STARTED;
            }
        }

        return BoardStatus.INCOMPLETE;
    }

    private boolean isWinner(String token) {
// Check columns
        int count = 0;
        for (int col = 0; col < cells.length; col++) {
            for (int row = 0; row < cells[0].length; row++) {
                if (cells[row][col].getText().equalsIgnoreCase(token)) {
                    count++;
                }
            }
            if (count == cells.length) {
                return true;
            } else {
                count = 0;
            }
        }

// Check rows
        count = 0;
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[0].length; col++) {
                if (cells[row][col].getText().equalsIgnoreCase(token)) {
                    count++;
                }
            }
            if (count == cells.length) {
                return true;
            } else {
                count = 0;
            }
        }

// Check diagonal 1
        count = 0;
        for (int i = 0; i < cells.length; i++) {
            if (cells[i][i].getText().equalsIgnoreCase(token)) {
                count++;
            }
            if (count == cells.length) {
                return true;
            }
        }

// Check diagonal 2
        count = 0;
        int col = cells.length - 1;
        for (int row = 0; row < cells.length; row++) {
            if (cells[row][col].getText().equalsIgnoreCase(token)) {
                count++;
            }
            if (count == cells.length) {
                return true;
            }
            col--;
        }

        return false;
    }

    private void createStartingPlayerPanel() {
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(selectX); // add plain to group
        radioGroup.add(selectO);
        radioGroup.clearSelection();

        pnlBottom.setBackground(new Color(BG_COLOR, BG_COLOR, BG_COLOR));

        JPanel radioPanel = new JPanel();
        radioPanel.setBackground(new Color(BG_COLOR, BG_COLOR, BG_COLOR));
        radioPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Who Goes First?"));

        setTableEnabled = true;

        selectX.setFont(MAIN_FONT);
        selectO.setFont(MAIN_FONT);
        selectX.addActionListener(new TokenSelectListener());
        selectO.addActionListener(new TokenSelectListener());

        radioPanel.setLayout(new GridLayout(2, 1));

        radioPanel.add(selectX);
        radioPanel.add(selectO);

        pnlSouth.removeAll();
        pnlSouth.setLayout(new GridLayout(2, 1, 2, 1));
        pnlSouth.add(radioPanel);
        pnlSouth.add(pnlBottom);
    }

    private void createGameModeScreen() {
        ButtonGroup radioGroup = new ButtonGroup(); // create ButtonGroup
        radioGroup.add(selectComputer); // add plain to group
        radioGroup.add(selectUser);
        radioGroup.clearSelection();

        JPanel radioPanel = new JPanel();
        radioPanel.setBackground(new Color(BG_COLOR, BG_COLOR, BG_COLOR));
        radioPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Select Game Mode"));

        setTableEnabled = true;

        selectComputer.setFont(MAIN_FONT);
        selectUser.setFont(MAIN_FONT);
        selectComputer.addActionListener(new ModeSelectListener());
        selectUser.addActionListener(new ModeSelectListener());

        radioPanel.setLayout(new GridLayout(2, 1));

        radioPanel.add(selectComputer);
        radioPanel.add(selectUser);

        pnlSouth.removeAll();
        pnlSouth.setLayout(new GridLayout(2, 1, 2, 1));
        pnlSouth.add(radioPanel);
        pnlSouth.add(pnlBottom);
    }

///////////////////////////////////////////////
    // MiniMax

    private void cpuMakeMoveMiniMax() {
// Get the next best move for the computer
        int depth = 10;
        if (cells.length > 3) {
            depth = 3;
        }

        Move move = minimax(depth, TOKEN_O);

        place(move.position, TOKEN_O);

// Disable the cell that we just wrote to
        Pair<Integer, Integer> rowCol = positionToRowCol(move.position);
        cells[rowCol.getKey()][rowCol.getValue()].setEnabled(false);

        nextPlayer = TOKEN_X; // User
    }

    private Move minimax(int depth, String player) {
// Get a list of all possible moves
        java.util.List<Integer> positions = listPossiblePositions();

// Player O (computer) is maximizing, player X is minimizing
        int bestScore = player.equals(TOKEN_O) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int bestPosition = -1;

        if (positions.isEmpty() || depth == 0) {
// Game is over, or depth reached, so evaluate score
            bestScore = evaluate();
        } else {
            int currentScore;

            for (int position : positions) {
// Try this move for the current player
                place(position, player);
                if (player.equals(TOKEN_O)) {
// O (computer) is the maximizing player
                    currentScore = minimax(depth - 1, TOKEN_X).score;
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestPosition = position;
                    }
                } else {
// X (human) is the minimizing player
                    currentScore = minimax(depth - 1, TOKEN_O).score;
                    if (currentScore < bestScore) {
                        bestScore = currentScore;
                        bestPosition = position;
                    }
                }

// Undo move
                clear(position);
            }
        }

        return new Move(bestPosition, bestScore);
    }

    private java.util.List<Integer> listPossiblePositions() {
        java.util.List<Integer> positions = new ArrayList<>();

// If the game is over, there are no possible moves
        BoardStatus boardStatus = getBoardStatus();
        if (boardStatus == BoardStatus.X_WIN || boardStatus == BoardStatus.O_WIN ||
                boardStatus == BoardStatus.DRAW) {
            return positions;
        }

// Search for empty cells and add to the list
        for (int col = 0; col < cells.length; col++) {
            for (int row = 0; row < cells.length; row++) {
                if (cells[row][col].getText().isEmpty()) {
                    positions.add(row * cells.length + col + 1);
                }
            }
        }

        return positions;
    }

    private int evaluate() {
// Return +1 if the computer wins, -1 if the player wins, or 0 otherwise
        int score = 0;
        BoardStatus boardStatus = getBoardStatus();
        switch (boardStatus) {
            case O_WIN:
                score = 1;
                break;
            case X_WIN:
                score = -1;
                break;
            case DRAW:
                break;
            default:
                break;
        }
        return score;
    }

    private void place(int position, String token) {
// If position is empty, place token and return true,
// else return false
        Pair<Integer, Integer> rowCol = positionToRowCol(position);
        int row = rowCol.getKey();
        int column = rowCol.getValue();

        if (cells[row][column].getText().isEmpty()) {
            cells[row][column].setText(token);
        } else {
            throw new IllegalArgumentException("Cell at position " + position + " is not empty.");
        }
    }

    private void clear(int position) {
        int column = position % cells.length - 1;
        if (column == -1) {
            column = cells.length - 1;
        }
        int row = position / cells.length;
        if (column == cells.length - 1) {
            row = (position - 1) / cells.length;
        }
        cells[row][column].setText("");
    }

    private Pair<Integer, Integer> positionToRowCol(int position) {
        int column = position % cells.length - 1;
        if (column == -1) {
            column = cells.length - 1;
        }

        int row = position / cells.length;
        if (column == cells.length - 1) {
            row = (position - 1) / cells.length;
        }

        return new Pair<>(row, column);
    }

    private static int requestBoardSize() {
        String inputValue = JOptionPane.showInputDialog(String.format("How many rows (between %d and %d) do you want on your board?", MIN_ROWS, MAX_ROWS));
        int rows = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                rows = Integer.parseInt(inputValue);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Value must be an integer!");
            }
            if (rows < MIN_ROWS || rows > MAX_ROWS) {
                JOptionPane.showInputDialog(String.format("Please enter a number between %d and %d.", MIN_ROWS, MAX_ROWS));
            } else {
                validInput = true;
            }
        }

        return rows;
    }

    private void setUpPlayingField() {
        int rows = requestBoardSize();
        cells = new JButton[rows][rows];

// setting up the playing field
        pnlPlayingField.removeAll();
        pnlPlayingField.setLayout(new GridLayout(cells.length, cells.length, 2, 2));
        pnlPlayingField.setBackground(Color.black);
        for (int i = 0; i < cells.length; ++i) {
            for (int j = 0; j < cells.length; ++j) {
                cells[i][j] = new JButton();
                cells[i][j].setBackground(new Color(220, 220, 220));
                if (cells.length < 7) {
                    cells[i][j].setFont(new Font("Rufscript", Font.BOLD, 100));
                } else {
                    cells[i][j].setFont(new Font("Rufscript", Font.BOLD, 70));
                }
                cells[i][j].setOpaque(true);
                cells[i][j].addActionListener(this);
                pnlPlayingField.add(cells[i][j]);
                cells[i][j].setEnabled(setTableEnabled);
            }
        }
    }
}