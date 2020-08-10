import java.awt.Point;
import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;

public class Game
{
  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Game Attributes
  private List<Card> envelope;  //order of solution is character, weapon then room
  private List<List<Card>> accusations;
  
  private Boolean gameOver = false;

  //Game Associations

 /* final Point CHARACTERLOC[] = {
          new Point(0,9),
          new Point(0, 14),
          new Point(6, 23),
          new Point(19, 23),
          new Point(24, 7),
          new Point(17, 0)
  };
  final char[] CHARACTERSYMBOL = {
          's',
          'm',
          'w',
          'g',
          'k',
          'p',
  };*/

  public static final Player PLAYERS[] = {
          new Player(new Point(0, 9), 's'),   //Miss Scarlett
          new Player(new Point(0, 14), 'm'),  //Colonel Mustard
          new Player(new Point(6, 23), 'w'),  //Mrs White
          new Player(new Point(19, 23), 'g'), //Mr Green
          new Player(new Point(24, 7), 'k'),  //Mrs Peacock
          new Player(new Point(17, 0), 'p')   //Professor Plum
  };

  public static final Weapon WEAPONS[] = {
          new Weapon('c'),  //candlestick
          new Weapon('d'),  //dagger
          new Weapon('l'),  //lead pipe
          new Weapon('r'),  //revolver
          new Weapon('o'),  //rope
          new Weapon('a')   //spanner
  };

  private Board board;
  private List<Tile> tiles;
  private Stack<Card> cards;

  /*final static char[] WEAPONSYMBOL = {
		  'c',
		  'd',
		  'l',
		  'r',
		  'o',
		  'a'
  };*/
  
  final static char[] ROOMSYMBOL = {
		  'K',
		  'B',
		  'C',
		  'G',
		  'Y',
		  'S',
		  'H',
		  'L',
		  'D'
  };

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Game() throws IOException
  {
    //initialise objects
    envelope = new ArrayList<Card>();
    accusations = new ArrayList<List<Card>>();
    //players = new ArrayList<Player>();
    tiles = new ArrayList<Tile>();
    cards = new Stack<Card>();

    //read board file
    String[] boardData;
    try {
      boardData = readBoardMapFile("src/boardMap");
    } catch(IOException e) {
      throw e;
    }  
    
    System.out.println("Welcome to a Text-Based Cluedo");
    System.out.println("Designed by Team 12");
    System.out.println();
    
    //create players
    Scanner s;// = new Scanner(System.in);
    int numPlayers = 0;
    boolean hasError = true;
    while(numPlayers < 3 || numPlayers > 6) {
        System.out.println("Please state number of players from 3 to 6:");
        s = new Scanner(System.in); //get input
        String input = s.next();
        try {
        numPlayers = parseInt(input);
        //check that input is valid
        } catch(NumberFormatException e) {
            System.out.println("ERROR: invalid character Please type down a character");
        }
    }


    for(int i = 0; i < 6; i++) {
    	if(i <= numPlayers-1) {
    	    PLAYERS[i].setIsActive(true);
    	    PLAYERS[i].setCanWin(true);
    		//players.add(new Player(CHARACTERLOC[i], true, CHARACTERSYMBOL[i]));
    	}
    	else {
            PLAYERS[i].setIsActive(false);
            PLAYERS[i].setCanWin(false);
    		//players.add(new Player(CHARACTERLOC[i], false, CHARACTERSYMBOL[i]));
    	}
    }

    System.out.println("creating board...");
    System.out.println();
    
    //set up board now
    createBoard(boardData); 
    System.out.println();

    initDeck();
    dealCards();    //give out deck to the players
      // testing that hands and solution are correct
   System.out.print("solution = ");
   for(int i = 0; i < 3; i++) {
       System.out.print(envelope.get(i)+" ");
   }
   System.out.println();
   for(Player p : PLAYERS) {
       if(p.getIsActive()) {
           System.out.print(p.getCharacter() + " hand = ");
           for (Card c : p.getHand()) {
               System.out.print(c.getName() + " ");
           }
           System.out.println();
       }
   }
  }
  
  public void gamePlay() {
	Scanner s = new Scanner(System.in);
	// while the game is not over
	while(!gameOver) {
        for(Player player : PLAYERS) { //go through all active player
            if(player.getIsActive()) {
                System.out.println("Current player is: "+player.getSymbol());
                int numMoves = rollDice();  //roll dice
                // list of player moves in a turn
                List<Point> moveLog = new ArrayList<>();
                //now let player move until run out of moves, no available moves or in new room and player accepted query
                System.out.println("numMoves = "+numMoves);
                boolean finishTurnEarly = false;    //for when no more available moves
                for(int i = 0; i < numMoves && !finishTurnEarly; i++) {
                    char inputChar = '\0';
                    while(true) {
                        System.out.println("Please select a direction to move:");
                        System.out.println("Forward: 'w',  Left: 'a', Right: 'd', Down: 's'");
                        inputChar = s.next().charAt(0);
                        System.out.println("char  = "+inputChar);
                        if(inputChar != 'w' && inputChar != 'a' && inputChar != 'd' && inputChar != 's') {
                            System.out.println("ERROR: invalid input!");
                            System.out.println();
                        }
                        else break;
                    }
                    int outcome = move(player, inputChar, moveLog);
                    switch (outcome) {
                        case 1: //invalid move
                            i--;
                            break;
                        case 2: //no valid moves
                            finishTurnEarly = true;
                            break;
                    }
                    System.out.println((numMoves-i)+" moves left");
                }
            }
        }
	}
	
	//move(PLAYERS[0], 's');
	//Player p = new Player(new Point(1, 9), 's');
	//move(p, 'a');
  }

public static void main(String[] args){
    //set up game
    Game game;
	try {
      game = new Game();
        // start game play
        game.gamePlay();
    } catch(IOException e) {
      System.out.println("ERROR: board map file could not be read");
    }
  }

  /**
   * Reads the board map file, TO DO: ADD TO UML
   * @param fileName  name of file to read
   * @return
   */
  private String[] readBoardMapFile(String fileName) throws IOException {
    //read board map text file and create board from data
    String[] data = new String[Board.HEIGHT];
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      //go through all lines of file and add it to string
      String str = "";  //line read from file
      int numRows = 0;
      while((str = br.readLine()) != null) {
        if(numRows > Board.HEIGHT) {  //if greater than 25 rows, then is not right board
          throw new IOException();
        }
        data[numRows] = str;
        numRows++;
      }
    } catch(IOException e) {
      throw e;
    }
    return data;
  }

  // line 79 "model.ump"
  /**
   * Setups and creates the board object to be used in the game
   * @param[] boardData list of strings that contains the characters of the board
   */
  public void createBoard(String[] boardData) {
    board = new Board(boardData);  //initialise board
    board.printBoard();
  }

    // partial turn play to test move method
    // need to add suggest, accuse refute...
    //public void turn(Player p){
       /* moveLog = new ArrayList<Point>();
        int movesLeft = rollDice();

        while(movesLeft != 0) {
            // if the move is valid
            if(validMove(p.getLocation())) {
                // add the move to the moveLog
                moveLog.add(p.getLocation());
                // call move
                move(p, 's');
            }
        }
        */
    //}

    // checks if players next move is a valid move
    public boolean validMove(Point newPos, Point oldPos, boolean checkingMainMove, List<Point> moveLog) {

        //check that is within board bounds
        if(newPos.y < 0
                || newPos.y >= Board.HEIGHT
                || newPos.x < 0
                || newPos.x >= Board.WIDTH
                || board.getGrid()[newPos.y][newPos.y].getUnderlyingSymbol() == '.') {
            if(checkingMainMove) {
                System.out.println("Invalid move! Goes out of bounds");
                board.printBoard();
                System.out.println();
            }
            return false;
        }

        //check if will go to a room tile
        if(board.getGrid()[newPos.y][newPos.x] instanceof RoomTile) {
            if(!(board.getGrid()[oldPos.y][oldPos.x] instanceof DoorTile) && !(board.getGrid()[oldPos.y][oldPos.x] instanceof RoomTile)) {//check if not coming from a door tile
                if(checkingMainMove) {
                    System.out.println("Invalid move! Goes into a room without first going through a door");   //if not, then it is invalid move
                    board.printBoard();
                    System.out.println();
                }
                return false;
            }
        }

        //check if currently in a room tile, if so can only move in room or door
        if(board.getGrid()[oldPos.y][oldPos.x] instanceof RoomTile) {
            if(!(board.getGrid()[newPos.y][newPos.x] instanceof DoorTile) && !(board.getGrid()[newPos.y][newPos.x] instanceof RoomTile)) {//check if not coming from a door tile
                if(checkingMainMove) {
                    System.out.println("Invalid move! Goes out of a room without first going through a door");   //if not, then it is invalid move
                    board.printBoard();
                    System.out.println();
                }
                return false;
            }
        }

        //check that it's not overlapping any previously made moves
        for(int i = 0; i < moveLog.size(); i++) {
            if(moveLog.get(i).equals(newPos)) {
                if(checkingMainMove) {
                    System.out.println("Invalid move! Goes to previously visited location");
                }
                return false;
            }
        }
        return true;
    }

    // moves the player by one tile, returns 0 if was a valid move, 1 is it was not a valid move, 2 if there's no move possible
    public int move(Player p, char dir, List<Point> moveLog){

        int x = 0;
        int y = 0;
        int currentRow = (int) p.getLocation().getX();
        int currentCol = (int) p.getLocation().getY();

        switch(dir) {
            case 'w': // move up
                y = -1;
                break;
            case 'd': // move right
                x = +1;
                break;
            case 's': // move down
                y = +1;
                break;
            case 'a': // move left
                x = -1;
                break;
            default:
                System.out.println("ERROR: Invalid input");
                break;
        }
        //check all directions that player can go
        boolean noMovesExist = true;
        noMovesExist = ( !validMove(new Point(currentCol-1, currentRow), new Point(currentCol, currentRow), false, moveLog) &&    //left
                       !validMove(new Point(currentCol+1, currentRow), new Point(currentCol, currentRow), false, moveLog) &&     //right
                       !validMove(new Point(currentCol, currentRow+1), new Point(currentCol, currentRow), false, moveLog) &&    //down
                       !validMove(new Point(currentCol, currentRow-1), new Point(currentCol, currentRow), false, moveLog));     //up

        if(noMovesExist) {
            System.out.println("No possible moves exist!, finishing turn");
            return 2;   //no possible moves
        }

        //check if current move is valid
        if(!validMove(new Point(currentCol+x, currentRow+y), new Point(currentCol, currentRow), true, moveLog)) {
            return 1;
        }


        // if the players next move choice has no token
        if (board.getGrid()[currentRow + y][currentCol + x].getToken() == null) {
            //update board
            // set the current tiles token as null
            board.getGrid()[currentRow][currentCol].setToken(null);

            // set the next moves tiles token as player
            board.getGrid()[currentRow + y][currentCol + x].setToken(p);

            //update player location
            p.setLocation(new Point(currentRow+y, currentCol+x));

            //add move to moveLog
            moveLog.add(new Point(currentCol+x, currentRow+y));

            // print updated board
            board.printBoard();
            System.out.println();

            return 0;    //was valid so return true
        }else {System.out.println("ERROR: Invalid move");}
        return 1;
    }


    //------------------------
  // INTERFACE
  //------------------------
  /* Code from template attribute_SetMany */
//  public boolean addEnvelope(Card aEnvelope)
//  {
//    boolean wasAdded = false;
//    wasAdded = envelope.add(aEnvelope);
//    return wasAdded;
//  }
//
//  public boolean removeEnvelope(Card aEnvelope)
//  {
//    boolean wasRemoved = false;
//    wasRemoved = envelope.remove(aEnvelope);
//    return wasRemoved;
//  }
  /* Code from template attribute_SetMany */
//  public boolean addAccusation(String aAccusation)
//  {
//    boolean wasAdded = false;
//    wasAdded = accusations.add(aAccusation);
//    return wasAdded;
//  }

  public boolean removeAccusation(String aAccusation)
  {
    boolean wasRemoved = false;
    wasRemoved = accusations.remove(aAccusation);
    return wasRemoved;
  }
  /* Code from template attribute_GetMany */
  public Card getEnvelope(int index)
  {
    Card aEnvelope = envelope.get(index);
    return aEnvelope;
  }

  public Card[] getEnvelope()
  {
    Card[] newEnvelope = envelope.toArray(new Card[envelope.size()]);
    return newEnvelope;
  }

  public int numberOfEnvelope()
  {
    int number = envelope.size();
    return number;
  }

  public boolean hasEnvelope()
  {
    boolean has = envelope.size() > 0;
    return has;
  }

  public int indexOfEnvelope(Card aEnvelope)
  {
    int index = envelope.indexOf(aEnvelope);
    return index;
  }
  /* Code from template attribute_GetMany */
//  public String getAccusation(int index)
//  {
//    String aAccusation = accusations.get(index);
//    return aAccusation;
//  }

  public String[] getAccusations()
  {
    String[] newAccusations = accusations.toArray(new String[accusations.size()]);
    return newAccusations;
  }

  public int numberOfAccusations()
  {
    int number = accusations.size();
    return number;
  }

  public boolean hasAccusations()
  {
    boolean has = accusations.size() > 0;
    return has;
  }

  public int indexOfAccusation(String aAccusation)
  {
    int index = accusations.indexOf(aAccusation);
    return index;
  }
  
  /* Code from template association_GetOne */ 
  public Board getBoard()
  {
    return board;
  }
  
  /* Code from template association_GetMany */
  /*ublic Player getPlayer(int index)
  {
    Player aPlayer = players.get(index);
    return aPlayer;
  }

  public List<Player> getPlayers()
  {
    List<Player> newPlayers = Collections.unmodifiableList(players);
    return newPlayers;
  }

  public int numberOfPlayers()
  {
    int number = players.size();
    return number;
  }

  public boolean hasPlayers()
  {
    boolean has = players.size() > 0;
    return has;
  }

  public int indexOfPlayer(Player aPlayer)
  {
    int index = players.indexOf(aPlayer);
    return index;
  }
//  /* Code from template association_GetMany */
//  public Tile getTile(int index)
//  {
//    Tile aTile = tiles.get(index);
//    return aTile;
//  }
//
//  public List<Tile> getTiles()
//  {
//    List<Tile> newTiles = Collections.unmodifiableList(tiles);
//    return newTiles;
//  }
//
//  public int numberOfTiles()
//  {
//    int number = tiles.size();
//    return number;
//  }
//
//  public boolean hasTiles()
//  {
//    boolean has = tiles.size() > 0;
//    return has;
//  }
//
//  public int indexOfTile(Tile aTile)
//  {
//    int index = tiles.indexOf(aTile);
//    return index;
//  }
  
  /* Code from template association_GetMany */
  public Card getCard(int index)
  {
    Card aCard = cards.get(index);
    return aCard;
  }

  public List<Card> getCards()
  {
    List<Card> newCards = Collections.unmodifiableList(cards);
    return newCards;
  }

  public int numberOfCards()
  {
    int number = cards.size();
    return number;
  }

  public boolean hasCards()
  {
    boolean has = cards.size() > 0;
    return has;
  }

  public int indexOfCard(Card aCard)
  {
    int index = cards.indexOf(aCard);
    return index;
  }
  /* Code from template association_SetUnidirectionalOne */
  public boolean setBoard(Board aNewBoard)
  {
    boolean wasSet = false;
    if (aNewBoard != null)
    {
      board = aNewBoard;
      wasSet = true;
    }
    return wasSet;
  }
  
  /* Code from template association_MinimumNumberOfMethod */
  public static int minimumNumberOfPlayers()
  {
    return 3;
  }
  /* Code from template association_MaximumNumberOfMethod */
  public static int maximumNumberOfPlayers()
  {
    return 6;
  }
  
  /* Code from template association_AddUnidirectionalMN */
  /*public boolean addPlayer(Player aPlayer)
  {
    boolean wasAdded = false;
    if (players.contains(aPlayer)) { return false; }
    if (numberOfPlayers() < maximumNumberOfPlayers())
    {
      players.add(aPlayer);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean removePlayer(Player aPlayer)
  {
    boolean wasRemoved = false;
    if (!players.contains(aPlayer))
    {
      return wasRemoved;
    }

    if (numberOfPlayers() <= minimumNumberOfPlayers())
    {
      return wasRemoved;
    }

    players.remove(aPlayer);
    wasRemoved = true;
    return wasRemoved;
  }
  /* Code from template association_SetUnidirectionalMN */
  /*public boolean setPlayers(Player... newPlayers)
  {
    boolean wasSet = false;
    ArrayList<Player> verifiedPlayers = new ArrayList<Player>();
    for (Player aPlayer : newPlayers)
    {
      if (verifiedPlayers.contains(aPlayer))
      {
        continue;
      }
      verifiedPlayers.add(aPlayer);
    }

    if (verifiedPlayers.size() != newPlayers.length || verifiedPlayers.size() < minimumNumberOfPlayers() || verifiedPlayers.size() > maximumNumberOfPlayers())
    {
      return wasSet;
    }

    players.clear();
    players.addAll(verifiedPlayers);
    wasSet = true;
    return wasSet;
  }
  /* Code from template association_AddIndexControlFunctions */
  /*public boolean addPlayerAt(Player aPlayer, int index)
  {  
    boolean wasAdded = false;
    if(addPlayer(aPlayer))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfPlayers()) { index = numberOfPlayers() - 1; }
      players.remove(aPlayer);
      players.add(index, aPlayer);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean addOrMovePlayerAt(Player aPlayer, int index)
  {
    boolean wasAdded = false;
    if(players.contains(aPlayer))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfPlayers()) { index = numberOfPlayers() - 1; }
      players.remove(aPlayer);
      players.add(index, aPlayer);
      wasAdded = true;
    } 
    else 
    {
      wasAdded = addPlayerAt(aPlayer, index);
    }
    return wasAdded;
  }
//  /* Code from template association_MinimumNumberOfMethod */
//  public static int minimumNumberOfTiles()
//  {
//    return 0;
//  }
//  /* Code from template association_AddUnidirectionalMany */
//  public boolean addTile(Tile aTile)
//  {
//    boolean wasAdded = false;
//    if (tiles.contains(aTile)) { return false; }
//    tiles.add(aTile);
//    wasAdded = true;
//    return wasAdded;
//  }
//
//  public boolean removeTile(Tile aTile)
//  {
//    boolean wasRemoved = false;
//    if (tiles.contains(aTile))
//    {
//      tiles.remove(aTile);
//      wasRemoved = true;
//    }
//    return wasRemoved;
//  }
//  /* Code from template association_AddIndexControlFunctions */
//  public boolean addTileAt(Tile aTile, int index)
//  {  
//    boolean wasAdded = false;
//    if(addTile(aTile))
//    {
//      if(index < 0 ) { index = 0; }
//      if(index > numberOfTiles()) { index = numberOfTiles() - 1; }
//      tiles.remove(aTile);
//      tiles.add(index, aTile);
//      wasAdded = true;
//    }
//    return wasAdded;
//  }
//
//  public boolean addOrMoveTileAt(Tile aTile, int index)
//  {
//    boolean wasAdded = false;
//    if(tiles.contains(aTile))
//    {
//      if(index < 0 ) { index = 0; }
//      if(index > numberOfTiles()) { index = numberOfTiles() - 1; }
//      tiles.remove(aTile);
//      tiles.add(index, aTile);
//      wasAdded = true;
//    } 
//    else 
//    {
//      wasAdded = addTileAt(aTile, index);
//    }
//    return wasAdded;
//  }
//  /* Code from template association_MinimumNumberOfMethod */
//  public static int minimumNumberOfCards()
//  {
//    return 0;
//  }
//  /* Code from template association_MaximumNumberOfMethod */
//  public static int maximumNumberOfCards()
//  {
//    return 21;
//  }
//  /* Code from template association_AddUnidirectionalOptionalN */
//  public boolean addCard(Card aCard)
//  {
//    boolean wasAdded = false;
//    if (cards.contains(aCard)) { return false; }
//    if (numberOfCards() < maximumNumberOfCards())
//    {
//      cards.add(aCard);
//      wasAdded = true;
//    }
//    return wasAdded;
//  }
//
//  public boolean removeCard(Card aCard)
//  {
//    boolean wasRemoved = false;
//    if (cards.contains(aCard))
//    {
//      cards.remove(aCard);
//      wasRemoved = true;
//    }
//    return wasRemoved;
//  }
//  /* Code from template association_SetUnidirectionalOptionalN */
//  public boolean setCards(Card... newCards)
//  {
//    boolean wasSet = false;
//    ArrayList<Card> verifiedCards = new ArrayList<Card>();
//    for (Card aCard : newCards)
//    {
//      if (verifiedCards.contains(aCard))
//      {
//        continue;
//      }
//      verifiedCards.add(aCard);
//    }
//
//    if (verifiedCards.size() != newCards.length || verifiedCards.size() > maximumNumberOfCards())
//    {
//      return wasSet;
//    }
//
//    cards.clear();
//    cards.addAll(verifiedCards);
//    wasSet = true;
//    return wasSet;
//  }
//  /* Code from template association_AddIndexControlFunctions */
//  public boolean addCardAt(Card aCard, int index)
//  {  
//    boolean wasAdded = false;
//    if(addCard(aCard))
//    {
//      if(index < 0 ) { index = 0; }
//      if(index > numberOfCards()) { index = numberOfCards() - 1; }
//      cards.remove(aCard);
//      cards.add(index, aCard);
//      wasAdded = true;
//    }
//    return wasAdded;
//  }
//
//  public boolean addOrMoveCardAt(Card aCard, int index)
//  {
//    boolean wasAdded = false;
//    if(cards.contains(aCard))
//    {
//      if(index < 0 ) { index = 0; }
//      if(index > numberOfCards()) { index = numberOfCards() - 1; }
//      cards.remove(aCard);
//      cards.add(index, aCard);
//      wasAdded = true;
//    } 
//    else 
//    {
//      wasAdded = addCardAt(aCard, index);
//    }
//    return wasAdded;
//  }
//
//  public void delete()
//  {
//    board = null;
//   // players.clear();
//    tiles.clear();
//    cards.clear();
//  }

    // line 76 "model.ump"

    /**
     * initialises deck and solution for a new game
     * @return
     */
    public void initDeck(){
        //create character cards
        Stack<Card> charCards = new Stack<>();
        charCards.push(new Card("Miss Scarlet"));
        charCards.push(new Card("Colonel Mustard"));
        charCards.push(new Card("Mrs White"));
        charCards.push(new Card("Mr Green"));
        charCards.push(new Card("Mrs Peacock"));
        charCards.push(new Card("Professor Plum"));
        Collections.shuffle(charCards);
        //create weapon cards
        Stack<Card> weaponCards = new Stack<>();
        weaponCards.push(new Card("candlestick"));
        weaponCards.push(new Card("dagger"));
        weaponCards.push(new Card("lead pipe"));
        weaponCards.push(new Card("revolver"));
        weaponCards.push(new Card("rope"));
        weaponCards.push(new Card("spanner"));
        Collections.shuffle(weaponCards);
        //create room cards
        Stack<Card> roomCards = new Stack<>();
        roomCards.push(new Card("kitchen"));
        roomCards.push(new Card("ballroom"));
        roomCards.push(new Card("conservatory"));
        roomCards.push(new Card("billard room"));
        roomCards.push(new Card("library"));
        roomCards.push(new Card("study"));
        roomCards.push(new Card("hall"));
        roomCards.push(new Card("lounge"));
        roomCards.push(new Card("dining room"));
        Collections.shuffle(roomCards);
        //create solution
        envelope = selectSolution(charCards, weaponCards, roomCards);

        //create deck noList<Card> deck = new ArrayList<>();
        int originalSize = charCards.size();
        for(int i = 0; i < originalSize; i++) { //adding characters
            cards.add(charCards.pop());
        }
        for(int i = 0; i < weaponCards.size(); i++) {   //adding weapons
            cards.add(weaponCards.pop());
        }
        for(int i = 0; i < roomCards.size(); i++) { //adding rooms
            cards.add(roomCards.pop());
        }
    }

    // line 77 "model.ump"

    /**
     * deals remaining cards of deck to player's hand
     */
    public void dealCards() {
        Collections.shuffle(cards);   //shuffle deck

        Iterator<Player> playerIterator =  Collections.unmodifiableList(Arrays.asList(PLAYERS)).iterator();
        while(!cards.isEmpty()) {   //loop through all active players until deck has no more cards
            if(!playerIterator.hasNext()) { //if reached end of players: reset iterator
                playerIterator  = Collections.unmodifiableList(Arrays.asList(PLAYERS)).iterator();
            }

            Player p = playerIterator.next();
            if(p.getIsActive()) {
                p.addCard(cards.pop());
            }
        }
    }

    // line 78 "model.ump"
    /**
     * creates the solution for the current game
     * @param charCards
     * @param weaponCards
     * @param roomCards
     * @return
     */
    public List<Card> selectSolution(Stack<Card> charCards, Stack<Card> weaponCards, Stack<Card> roomCards){
        List<Card> solution = new ArrayList<>();
        solution.add(charCards.pop());
        solution.add(weaponCards.pop());
        solution.add(roomCards.pop());
        return solution;
    }
  

    /**
     * teleports token instantly from one location to another
     * @param token
     * @param moveFrom
     * @param moveTo
     */
  public void teleport(Token token, Point moveFrom, Point moveTo) {
      board.getGrid()[(int)moveTo.getY()][(int)moveTo.getX()].setToken(token);   //move token to new position
      board.getGrid()[(int)moveFrom.getY()][(int)moveFrom.getX()].setToken(null);   //remove token from old position
  }
  

  // 2 dice roll
  public int rollDice(){
	  int randomNum = (int)((Math.random() * 12) + 2);
	  return randomNum;
  }

//  public String toString()
//  {
//    return super.toString() + "["+ "]" + System.getProperties().getProperty("line.separator") +
//            "  " + "board = "+(getBoard()!=null?Integer.toHexString(System.identityHashCode(getBoard())):"null");
//  }
}