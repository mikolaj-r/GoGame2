package com.gogame;

import java.util.Scanner;

public class App
{

    // GRASP: Controller
    // App steruje przebiegiem gry (tury, pętle, zakończenie gry),
    // ale nie zawiera logiki planszy
    public static int pointsBlack = 0;
    public static int pointsWhite = 0;

    // GRASP: Pure Fabrication
    // Metoda pomocnicza do walidacji wejścia użytkownika,
    // nie należy ani do Board, ani do Cell
    public static boolean validInput(String input) {
  

        if(input.equals("PASS"))
            return true;
        if (input.length() < 2 || input.length() > 3) return false;
        char col = input.charAt(0);
        if (col < 'A' || col > 'S')
            return false;


        String rowStr = input.substring(1);
        int row;
        try {
            row = Integer.parseInt(rowStr);
        } catch (NumberFormatException e) {
            return false; // not a number
        }

        if (row < 1 || row > 19) return false;

        return true;
    }

    public static void main( String[] args ) throws Exception {
        // GRASP: Creator
        // App tworzy Board, bo nim zarządza i go używa
        Board board = new Board();
        board.updateBreaths();
        boolean keepGoing = true;
        
        // GRASP: Controller
        // App decyduje czyja tura, a nie Board
        boolean turn = true;     //true - black, false - white
        
        boolean blackPass = false;
        boolean whitePass = false;
        char columnChar;
        int column;
        int row;
        String rowStr;
        String move = "";
        Scanner cin = new Scanner(System.in);
        System.out.println("Welcome to GO!");
        System.out.println("You can pass by typing PASS, if both players pass the game ends and the score is counted");
        System.out.println("Make a move by typing the cells column followed by its row, like A12 or g5 - ");
        board.show();
        while(keepGoing)
        {
            // GRASP: Controller
            // Centralna petla steruje przebiegiem gry
            if(turn)
            {
                System.out.println("Black to move!");
            }
            if(!turn)
            {
                System.out.println("White to move!");
            }
            boolean isInputCorrect = false;
            boolean isMoveCorrect = false;
            while(!isInputCorrect || !isMoveCorrect)
            {
                isInputCorrect = false;
                System.out.print("Your move : ");
                move = cin.nextLine().trim().toUpperCase();
                // GRASP: Pure Fabrication
                // Walidacja wejścia wydzielona poza Board
                if(validInput(move))
                    isInputCorrect = true;
                else
                    System.out.println("Incorrect input, try again");

                if(isInputCorrect)
                {
                    if (!move.equals("PASS"))
                    {
                        columnChar = move.charAt(0);
                        column = columnChar - 'A';
                        rowStr = move.substring(1);
                        row = Integer.parseInt(rowStr) - 1;
                         // GRASP: Information Expert
                        // Board zna stan planszy i decyduje o poprawności ruchu
                        // (checkMove)
                        if (!board.checkMove(row, column))
                            System.out.println("Incorrect input, try again");
                        if (board.checkMove(row, column))
                            isMoveCorrect = true;
                    }
                    else
                    {
                        isMoveCorrect = true;
                    }
                }
            }
            if(!move.equals("PASS")) {
                columnChar = move.charAt(0);
                column = columnChar - 'A';
                rowStr = move.substring(1);
                row = Integer.parseInt(rowStr) - 1;
                
                // GRASP: Information Expert
                // Board modyfikuje swój stan po ruchu
                // (move, updateBreaths, checkBoard)
                board.move(row, column, turn); //turn true - black
                board.updateBreaths();
                board.checkBoard();

                if(turn)
                    blackPass = false;
                if(!turn)
                    whitePass = false;

                turn = !turn;

            }
            else  
            {
                // GRASP: Controller
                // App zarządza logiką passów i zakończenia gry
                if(turn)
                    blackPass = true;
                if(!turn)
                    whitePass = true;

                turn = !turn;
            }

            if(whitePass && blackPass)
            {
                keepGoing = false;
            }
            board.show();
        }

        System.out.println("Both players passed their turns, the game is over!");


        //tu punktacja

        }

    }



