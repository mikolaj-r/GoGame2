package com.gogame;

import java.util.Scanner;

/**
 * Główna klasa aplikacji konsolowej gry Go.
 */

public class App
{

    // GRASP: Controller
    // App steruje przebiegiem gry (tury, pętle, zakończenie gry),
    // ale nie zawiera logiki planszy
    public static int pointsBlack = 0;
    public static int pointsWhite = 0;
    public static int finalPointsBlack;
    public static int finalPointsWhite;
    // GRASP: Pure Fabrication
    /**
     * Metoda pomocnicza do walidacji wejścia użytkownika,
     * nie należy ani do Board, ani do Cell
     *
     * @param input wejście użytkownika
     * @return true jeśli poprawne, false w przeciwnym razie
     */
    public static boolean validInput(String input) {


        if(input.equals("PASS"))
            return true;
        if(input.equals("SURRENDER"))
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





































    /**
     * Główna pętla gry.
     *
     * @param args argumenty linii poleceń
     * @throws Exception w przypadku błędu I/O
     */
    public static void main( String[] args ) throws Exception {
        // GRASP: Creator
        // App tworzy Board, bo nim zarządza i go używa
        Board board = new Board();
        Board board1 = new Board();
        board.updateBreaths();
        boolean keepGoing = true;

        // GRASP: Controller
        // App decyduje czyja tura, a nie Board
        boolean turn = true;     //true - black, false - white
        boolean tempturn;
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
                    if (!move.equals("PASS") && !move.equals("SURRENDER"))
                    {
                        columnChar = move.charAt(0);
                        column = columnChar - 'A';
                        rowStr = move.substring(1);
                        row = Integer.parseInt(rowStr) - 1;
                        // GRASP: Information Expert
                        // Board zna stan planszy i decyduje o poprawności ruchu
                        // (checkMove)
                        boolean canIMove = board.checkMove(row, column, turn);
                        if (!canIMove)
                            System.out.println("Incorrect input, try again");
                        if (canIMove)
                            isMoveCorrect = true;
                    }
                    else
                    {
                        isMoveCorrect = true;
                    }
                }
            }
            if(!move.equals("PASS")&& !move.equals("SURRENDER")) {
                columnChar = move.charAt(0);
                column = columnChar - 'A';
                rowStr = move.substring(1);
                row = Integer.parseInt(rowStr) - 1;

                // GRASP: Information Expert
                // Board modyfikuje swój stan po ruchu
                // (move, updateBreaths, checkBoard)

                //turn true - black
                board.copyToLast();
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
                if(move.equals("PASS"))
                {
                    if (turn)
                        blackPass = true;
                    if (!turn)
                        whitePass = true;

                    turn = !turn;
                }
                else
                {
                    keepGoing = false;
                }
            }

            if(whitePass && blackPass)
            {
                keepGoing = false;
            }
            board.show();
            if(!keepGoing) {
                if (!move.equals("SURRENDER")) {
                    //kopiuje kolory board do board1
                    for (int i = 0; i < 19; i++) {
                        for (int j = 0; j < 19; j++) {
                            board1.positions[i][j].color = board.positions[i][j].color;
                        }
                    }
                    System.out.println("Both players passed their turns, the game is over!");
                    System.out.println("First its black's turn to flag dead groups in its territories : ");
                    System.out.println("To flag a dead group simply type the coordinates of one of its cells, then the opponent will decide if they agree or not by typing Y or N. ");
                    System.out.println("When black types in ,,DONE'', it will be white's turn to flag groups as dead until white types ,,DONE''.");
                    System.out.println("PLEASE DO NOT MARK DEAD GROUPS OUTSIDE OF TERRITORIES");
                    System.out.println("At any point both players can type ,,PLAYON'' to start the game again.");
                    finalPointsBlack = pointsBlack;
                    finalPointsWhite = pointsWhite;
                    String flag = "";
                    String answer = "";
                    int correctAnswer = 0;
                    char flagColumnChar;
                    int flagColumn;
                    String flagRowStr = "";
                    int flagRow;
                    tempturn = turn;
                    while (!flag.equals("DONE") && !flag.equals("PLAYON")) {
                        turn = true;
                        board1.show();
                        System.out.print("Your input : ");
                        flag = cin.nextLine().trim().toUpperCase();
                        if (validInput(flag)) {
                            flagColumnChar = flag.charAt(0);
                            flagColumn = flagColumnChar - 'A';
                            flagRowStr = flag.substring(1);
                            flagRow = Integer.parseInt(flagRowStr) - 1;
                            if (board1.positions[flagRow][flagColumn].color != 0) {
                                int outrow = flagRow + 1;
                                System.out.print("The flag from black is " + flagColumnChar + outrow + ", now its white's turn to agree with Y or disagee with N, ");
                                turn = false;
                                correctAnswer = 0;
                                while (correctAnswer != 1) {
                                    System.out.print("Your input : ");
                                    answer = cin.nextLine().trim().toUpperCase();
                                    if (answer.equals("Y") || answer.equals("N")) {
                                        correctAnswer = 1;
                                        if (answer.equals("Y")) {
                                            board1.finalRemoveChain(flagRow, flagColumn); //potrzebne zeby poprawnie liczyc punkty przy PLAYON
                                        }

                                        System.out.println("Great! Time for black's next input");
                                    } else {
                                        System.out.println("Incorrect input, try Y or N");
                                    }
                                }
                            } else {
                                System.out.println("Choose a non-empty square!");
                            }
                        } else {
                            if (flag.equals("DONE") || flag.equals("PLAYON")) {
                            } else
                                System.out.println("Wrong input, type taken coordinates like A14, DONE, or PLAYON");
                        }
                    }
                    if (flag.equals("PLAYON")) {
                        System.out.println("Black decided to keep playing, the game will be continued!");
                        keepGoing = true;
                        turn = tempturn;
                    }
                    if (flag.equals("DONE")) {
                        System.out.println("Now it's white's turn to flag dead groups : ");

                        flag = "";
                        while (!flag.equals("DONE") && !flag.equals("PLAYON")) {
                            turn = false;
                            board1.show();
                            System.out.print("Your input : ");
                            flag = cin.nextLine().trim().toUpperCase();
                            if (validInput(flag)) {
                                flagColumnChar = flag.charAt(0);
                                flagColumn = flagColumnChar - 'A';
                                flagRowStr = flag.substring(1);
                                flagRow = Integer.parseInt(flagRowStr) - 1;
                                if (board1.positions[flagRow][flagColumn].color != 0) {
                                    int outrow = flagRow + 1;
                                    System.out.print("The flag from white is " + flagColumnChar + outrow + ", now its black's turn to agree with Y or disagee with N, ");
                                    turn = true;
                                    correctAnswer = 0;
                                    while (correctAnswer != 1) {
                                        System.out.print("Your input : ");
                                        answer = cin.nextLine().trim().toUpperCase();
                                        if (answer.equals("Y") || answer.equals("N")) {
                                            correctAnswer = 1;
                                            if (answer.equals("Y")) {
                                                board1.finalRemoveChain(flagRow, flagColumn); //potrzebne zeby poprawnie liczyc punkty przy PLAYON
                                            }
                                            System.out.println("Great! Time for white's next input");
                                        } else {
                                            System.out.println("Incorrect input, try Y or N");
                                        }
                                    }
                                } else {
                                    System.out.println("Choose a non-empty square!");
                                }
                            } else {
                                if (flag.equals("DONE") || flag.equals("PLAYON")) {
                                } else
                                    System.out.println("Wrong input, type taken coordinates like A14, DONE, or PLAYON");
                            }
                        }
                        if (flag.equals("PLAYON")) {
                            System.out.println("White decided to keep playing, the game will be continued!");
                            keepGoing = true;
                            turn = tempturn;
                        }
                        if (flag.equals("DONE")) {
                            System.out.println("Both white and black finished flagging, time to count up the points!");
                        }
                    }
                }
                else
                {
                    if(turn)
                    {
                        System.out.println("Black surrenders, the game is over!");
                    }
                    else
                    {
                        System.out.println("White surrenders, the game is over!");
                    }
                }
            }


            //koniec petli while keepgoing
        }

        //teraz liczenie punktow
        //tablica board byla nieruszona, wszystkie zmiany martwych robilem w board1 zeby na pewno miec dobrze zachowany board na koniec gry.
        //board jest tablica do zapisywania w bazie danych, board1 jest do liczenia pkt
        //co do liczenia, uzyje japonskiej punktacji z lekka modyfikacja : to uzytkownicy usuwają martwe pola w terytoriach.
        if(!move.equals("SURRENDER"))
        {


            board1.calculateTerritories();
            for(int i = 0; i < 19; i++)
            {
                for(int j = 0; j<19; j++)
                {
                    if(board1.territoryCache[i][j] == 1)
                        finalPointsWhite++;
                    if(board1.territoryCache[i][j] == 2)
                        finalPointsBlack++;
                }
            }
            System.out.println("Final scoring is as such : ");
            System.out.println("White's points - " + finalPointsWhite);
            System.out.println("Black's points - " + finalPointsBlack);
            if(finalPointsWhite>finalPointsBlack)
                System.out.println("White wins!");
            if(finalPointsWhite<finalPointsBlack)
                System.out.println("Black wins!");
            if(finalPointsWhite==finalPointsBlack)
                System.out.println("It's a draw!");







        }





    }

}