import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author: catter1
 */
public class TicTacToe {

	static Scanner in = new Scanner(System.in);
	static Random rand = new Random();

	public static void main(String[] args) {		
		boolean nextGame = true;
		while (nextGame) {
			// Play one game
			oneGame();

			// Check if we want to continue
			System.out.print("Shall we play one more (y/n)? ");
			String next = in.nextLine();
			if ((next.charAt(0) == 'y') || (next.charAt(0) == 'Y'))
				nextGame = true;
			else
				nextGame = false;
		}
		System.out.println("See you around! Good games!");
	}


	public static void oneGame() {
		//create a 2D array as the board
		char[][] playField = {{' ',' ',' '},{' ',' ',' '},{' ',' ',' '}} ;
		//char[][][] fields = setFields(playField);

		printBoard(playField);
		// Game loop
		while (true) {
			//user player move
			getUserInput(playField);
			printBoard(playField);
			if (findWin('X',playField)) {
				System.out.println("You won! Congratulations. I guess...");
				break;
			}
			if (findTie(playField)) {
				System.out.println("It's a draw!");
				break;
			}
			//Computer player move
			System.out.println("My move:");
			getComputerInput(playField);
			printBoard(playField);
			if (findWin('O',playField)) {
				System.out.println("Ha! I DESTROYED you! Better luck next time!");
				break;
			}
			if (findTie(playField)) {
				System.out.println("We tied! Good game.");
				break;
			}
			// Next round
			System.out.println("Next turn.");
		}
	}

	public static void getUserInput(char[][] playField) {
		boolean valid = false;
		int row = 0;
		int col = 0;

		while (!valid) {
			System.out.print("Enter your move (row and col, no space): ");
			String move = in.nextLine();
			if (move.length() == 2) {
				row = (int) move.charAt(0) - 97;//'a' --> 0, 'b'--> 1, 'c' --> 2
				col = (int) move.charAt(1) - 49;//'1' --> 0, '2'--> 1, '3' --> 2

				if ((row >= 0 && row < playField.length) && (col >= 0 && col < playField[0].length)) {
					if (playField[row][col] == 0 || playField[row][col] == ' ') {
						playField[row][col] = 'X';
						valid = true;
					} else {
						System.out.println("This field is already taken, choose another!");
					}
				} 
				else {
					System.out.println("This field is invalid!");
				}
			} 
			else {
				System.out.println("Please only enter a correct field (a1, c3, etc) for your move!");
			}
		}
	}

	//In this setFields() method, we do something fancy for checking wins and comp. input.
	//We make a new 3D array. This contains an arrays of all the possible "lines".
	//For example - there's a list containing the data at a1, b2, and c3,
	//along with a tertiary array that contains the char, along with it's
	//location data so we may use it for computer input.
	//This method exists so we can easily check all lines at once for whatever is needed.
	public static char[][][] setFields(char[][] playField) { //2x2 - 4; 3x3 - 8; 4x4 - 10; 5x5 - 12
		int fieldNums = playField.length * 2 + 2;
		char[][][] fields = new char[fieldNums][playField.length][3];
		int count = 0;
		
		//rows
		for (int i = 0; i < playField.length; i++) {
			for (int j = 0; j < playField[i].length; j++) {
				fields[count][j][0] = playField[i][j];
				fields[count][j][1] = (char)i;
				fields[count][j][2] = (char)j;
			}
			count++;
		}
		
		//columns
		for (int i = 0; i < playField.length; i++) {
			for (int j = 0; j < playField.length; j++) {
				fields[count + i][j][0] = playField[j][i];
				fields[count + i][j][1] = (char)j;
				fields[count + i][j][2] = (char)i;
			}
		}
		
		//diagonal top left to bottom right
		count = count + 3;
		for (int i = 0; i < playField.length; i++) {
			fields[count][i][0] = playField[i][i];
			fields[count][i][1] = (char)i;
			fields[count][i][2] = (char)i;
		}
		
		//diagonal bottom left to top right
		count++;
		for (int i = 0; i < playField.length; i++) {
			int j = playField.length - i - 1;
			fields[count][i][0] = playField[i][j];
			fields[count][i][1] = (char)i;
			fields[count][i][2] = (char)j;
		}
		
		/* Print Fields - uncomment loop below for testing purposes*/
		
//		for (int i = 0; i < fields.length; i++) {
//			for (int j = 0; j < fields[i].length; j++) {
//				System.out.print(fields[i][j][0] + ": " + (int)fields[i][j][1] + ", " + (int)fields[i][j][2] + "   ");
//			}
//			System.out.println("");
//		}
		
		return fields;
	}
	
	//This computer "AI" doesn't just place randomly. It does a few things.
	//First, if it knows it can win, it will do that immediately.
	//If it can't, but the player can, it will block the player.
	//If the player has 2 win options, the computer will admit defeat, and
	//just block a random one.
	//If no one can win, the computer tries to take one of its placements and expand it
	//into a win condition. If the computer has the ability to make a move that
	//advances into TWO win conditions, it will immediately choose it.
	//Finally, if it cannot advance any strategy (or it is the first move),
	//the computer will randomly make a move in an empty space.
	public static void getComputerInput(char[][] playField) {
		char[][][] fields = setFields(playField);
		
		//Check if computer can win
		if (aiCompWin(playField, fields)) return;
		
		//Check if player can win
		if (aiPlayerWin(playField, fields)) return;
		
		//Create lines of 2 when possible
		if(aiAdvance(playField, fields)) return;
		
		//No strategies left? Randomly place
		if(aiRandomPlace(playField, fields)) return;
	}
	
	//Check if computer can win
	public static boolean aiCompWin(char[][] playField, char[][][] fields) {
		int sum = 0;
		int potx = 0;
		int poty = 0;
		boolean empty = false;
		
		for (int i = 0; i < fields.length; i++) {
			sum = 0;
			empty = false;
			for (int j = 0; j < fields[i].length; j++) {
				if (fields[i][j][0] == ' ' || fields[i][j][0] == 0) {
					potx = (int)fields[i][j][1];
					poty = (int)fields[i][j][2];
					empty = true;
				}
				if (fields[i][j][0] == 'O') {
					sum += 1;
				}
			}
			
			if (sum == 2 && empty == true) {
				//System.out.println("Detected my win condition!");
				playField[potx][poty] = 'O';
				return true;
			}
		}
		return false;
	}
	
	//Check if player can win
	public static boolean aiPlayerWin(char[][] playField, char[][][] fields) {
		ArrayList<Integer[]> pot = new ArrayList<Integer[]>();
		Random rd = new Random();
		int sum = 0;
		int potx = 0;
		int poty = 0;
		boolean empty = false;
		
		for (int i = 0; i < fields.length; i++) {
			sum = 0;
			empty = false;
			for (int j = 0; j < fields[i].length; j++) {
				if (fields[i][j][0] == ' ' || fields[i][j][0] == 0) {
					potx = (int)fields[i][j][1];
					poty = (int)fields[i][j][2];
					empty = true;
				}
				if (fields[i][j][0] == 'X') {
					sum += 1;
				}
			}
			
			if (sum == 2 && empty == true) {
				Integer potxy[] = {potx, poty};
				pot.add(potxy);
			}
		}
		
		//Block player from winning
		if (pot.size() >= 2) {
			int randint = rd.nextInt(pot.size());
			int placex = pot.get(randint)[0];
			int placey = pot.get(randint)[1];
			playField[placex][placey] = 'O';
			System.out.println("Wow, you trapped me... well done! Finish me off!");
			return true;
		} else {
			if (pot.size() == 1) {
				int placex = pot.get(0)[0];
				int placey = pot.get(0)[1];
				playField[placex][placey] = 'O';
				//System.out.println("Blocked off player from winning!");
				return true;
			}
		}
		
		return false;
	}
	
	//Create lines of 2 when possible
	public static boolean aiAdvance(char[][] playField, char[][][] fields) {
		ArrayList<Integer[]> advances = new ArrayList<Integer[]>();
		Random rd = new Random();
		int sum = 0;
		int potx = 0;
		int poty = 0;
		int emptycnt = 0;
		
		for (int i = 0; i < fields.length; i++) {
			sum = 0;
			emptycnt = 0;
			for (int j = 0; j < fields[i].length; j++) {
				if (fields[i][j][0] == ' ' || fields[i][j][0] == 0) {
					emptycnt += 1;
				}
				if (fields[i][j][0] == 'O') {
					sum += 1;
				}
			}
			
			if (sum == 1 && emptycnt == 2) {
				for (int j = 0; j < fields[i].length; j++) {
					if (fields[i][j][0] == ' ' || fields[i][j][0] == 0) {
						potx = (int)fields[i][j][1];
						poty = (int)fields[i][j][2];
						Integer potxy[] = {potx, poty};
						
						//Is this location able to help two lines?
						for (int k = 0; k < advances.size(); k++) {
							if (Arrays.equals(potxy, advances.get(k))) {
								int placex = advances.get(k)[0];
								int placey = advances.get(k)[1];
								playField[placex][placey] = 'O';
								//System.out.println("Expanded TWO lines!");
								return true;
							}
						}
						advances.add(potxy);
					}
				}
			}
		}
		
		if (advances.size() >= 1) {
			int randint = rd.nextInt(advances.size());
			int placex = advances.get(randint)[0];
			int placey = advances.get(randint)[1];
			playField[placex][placey] = 'O';
			//System.out.println("Expanded a line.");
			return true;
		}
		
		return false;
	}
	
	//No strategies left? Randomly place
	public static boolean aiRandomPlace(char[][] playField, char[][][] fields) {
		ArrayList<Integer[]> openings = new ArrayList<Integer[]>();
		Random rd = new Random();
		int potx = 0;
		int poty = 0;
		
		for (int i = 0; i < fields.length; i++) {
			for (int j = 0; j < fields[i].length; j++) {
				if (fields[i][j][0] == ' ' || fields[i][j][0] == 0) {
					potx = (int)fields[i][j][1];
					poty = (int)fields[i][j][2];
					Integer potxy[] = {potx, poty};
					openings.add(potxy);
				}
			}
		}
		int randint = rd.nextInt(openings.size());
		int placex = openings.get(randint)[0];
		int placey = openings.get(randint)[1];
		playField[placex][placey] = 'O';
		//System.out.println("Randomly placed.");
		return true;
	}

	
	
	
	public static boolean findTie(char[][] playField) {
		for (int i = 0; i < playField.length; i++) {
			for (int j = 0; j < playField[i].length; j++) {
				if (playField[i][j] == ' ' || playField[i][j] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	
	public static boolean findWin(char playerChar, char[][] playField) {
		int winSum = playField.length;
		int sum = 0;
		
		//Check win - This uses the setFields() method. Read the comments there
		//for more information on how it works (and why).
		char[][][] fields = setFields(playField);
		for (int i = 0; i < fields.length; i++) {
			sum = 0;
			for (int j = 0; j < fields[i].length; j++) {
				if(fields[i][j][0] == playerChar) {
					sum ++;
				}
			}
			if (sum == winSum) {
				return true;
			}
		}
		
		return false;
	}

	public static void printBoard(char[][] playField) {
		System.out.println("     1     2     3");
		System.out.println("  +-----+-----+-----+");
		System.out.println("a |  " + playField[0][0] + "  |  " + playField[0][1] + "  |  "
				+ playField[0][2] + "  |");
		System.out.println("  |-----+-----+-----|");
		System.out.println("b |  " + playField[1][0] + "  |  " + playField[1][1] + "  |  "
				+ playField[1][2] + "  |");
		System.out.println("  |-----+-----+-----|");
		System.out.println("c |  " + playField[2][0] + "  |  " + playField[2][1] + "  |  "
				+ playField[2][2] + "  |");
		System.out.println("  +-----+-----+-----+");

	}

}