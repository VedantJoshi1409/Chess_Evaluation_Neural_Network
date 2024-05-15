import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class Main {
    static int[] shiftSquares = {
            56, 57, 58, 59, 60, 61, 62, 63,
            48, 49, 50, 51, 52, 53, 54, 55,
            40, 41, 42, 43, 44, 45, 46, 47,
            32, 33, 34, 35, 36, 37, 38, 39,
            24, 25, 26, 27, 28, 29, 30, 31,
            16, 17, 18, 19, 20, 21, 22, 23,
            8, 9, 10, 11, 12, 13, 14, 15,
            0, 1, 2, 3, 4, 5, 6, 7};

    public static void main(String[] args) {
        convert(1000, 100000, 350, "lichess_db_eval.jsonl", "output.txt");
        fenToTensor(1000, -1, "output.txt", "tensors.txt");
    }

    static void convert(int statusUpdateIncrement, int amountLines, int skipAmount, String inputFileName, String outputFileName) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Entry entry;
            BufferedReader in = new BufferedReader(new FileReader(inputFileName));
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));
            String lineIn = "";
            int counter = 0;
            int skipCounter = 0;
            Board board;
            Board nextBoard;
            MoveList moveList;
            String move;
            int start;
            int end;

            while (true) {
                lineIn = in.readLine();
                if (lineIn == null) break;
                skipCounter++;
                if (skipCounter < skipAmount) continue;

                skipCounter = 0;
                counter++;
                if (counter % statusUpdateIncrement == 0) {
                    System.out.println(counter + " Lines Processed!");
                }
                if (counter > amountLines) {
                    break;
                }

                entry = objectMapper.readValue(lineIn, Entry.class);
                board = new Board(entry.fen);
                moveList = MoveGeneration.getMoves(board);

                for (Eval eval : entry.evals) {
                    for (Position pv : eval.pvs) {
                        move = pv.line.split(" ")[0];
                        start = BitMethods.stringMoveToInt(move.substring(0, 2));
                        end = BitMethods.stringMoveToInt(move.substring(2, 4));

                        for (long longMove : moveList.moves) {
                            if (MoveList.getStartSquare(longMove) == start) {
                                if (MoveList.getEndSquare(longMove) == end) {
                                    nextBoard = new Board(board);
                                    nextBoard.makeMove(longMove);
                                    out.write(nextBoard.boardToFen() + ", " + pv.cp);
                                    out.newLine();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void fenToTensor(int statusUpdateIncrement, int amountLines, String inputFileName, String outputFileName) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(inputFileName));
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));
            String lineIn = in.readLine();
            String[] split;
            int counter = 0;

            while (lineIn != null) {
                counter++;
                if (counter%statusUpdateIncrement == 0) {
                    System.out.println(counter + " Lines Processed!");
                }
                if (amountLines != -1) {
                    if (counter > amountLines) break;
                }

                split = lineIn.split(", ");
                out.write(boardToTensor(new Board(split[0])) + ", " + split[1]);
                out.newLine();
                lineIn = in.readLine();
            }
            out.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    static String boardToTensor(Board board) {
        String inputs = "";
        int fBase = shiftSquares[BitMethods.getLS1B(board.fKing)] * 64 * 5 * 2;
        int eBase = BitMethods.getLS1B(board.eKing) * 64 * 5 * 2 + 64 * 64 * 5 * 2;
        int currentPieceSquare;
        long currentBits;

        currentBits = board.fQueen;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 2); //2 is the code for queen
            inputs += ", " + (eBase + currentPieceSquare * 10 + 2 + 6);
        }

        currentBits = board.eQueen;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 2 + 6);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 2);
            //2 is the code for queen and the second 2 is since it is an enemy queen
        }

        currentBits = board.fRook;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 3);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 3 + 6);
        }

        currentBits = board.eRook;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 3 + 6);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 3);
        }

        currentBits = board.fBishop;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 4);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 4 + 6);
        }

        currentBits = board.eBishop;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 4 + 6);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 4);
        }

        currentBits = board.fKnight;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 5);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 5 + 6);
        }

        currentBits = board.eKnight;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 5 + 6);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 5);
        }

        currentBits = board.fPawn;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 6);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 6 + 6);
        }

        currentBits = board.ePawn;
        while (currentBits != 0) {
            currentPieceSquare = BitMethods.getLS1B(currentBits);
            currentBits &= ~(1L << currentPieceSquare);
            inputs += ", " + (fBase + shiftSquares[currentPieceSquare] * 10 + 6 + 6);
            inputs += ", " + (eBase + currentPieceSquare * 10 + 6);
        }

        return inputs.substring(2);
    }
}