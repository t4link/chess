package ru.pflb.chess;

import ru.pflb.chess.exception.NotImplementedException;

import static ru.pflb.chess.Color.BLACK;
import static ru.pflb.chess.Color.WHITE;
import static ru.pflb.chess.Piece.EMP;
import static ru.pflb.chess.Piece.OUT;
import static ru.pflb.chess.PieceType.KING;
import static ru.pflb.chess.PieceType.ROOK;
import static ru.pflb.chess.Square.*;

/**
 * @author <a href="mailto:8445322@gmail.com">Ivan Bonkin</a>.
 */
public class Board {

    // массивы быстрого доступа, позволяющие получать позиции всех имеющихся у каждой стороны фигур без обхода всей доски
    private int[] kingPos120 = {0, 0};
    private int[][] rookPos120 = {
            {0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0}
    };
    // число имеющихся ладей у кажой из сторон
    private int rooksNb[] = {0, 0};

    private Color sideToMove;

    private int[][] offset = {
        // KING
        { -11, -10, -9, -1, 1,  9, 10, 11 },
        // ROOK
        { -10,  -1,  1, 10, 0,  0,  0,  0 }
    };

    private Piece[] mailbox120 = {
        OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, // 0-9
        OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, // 10-19
        OUT, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, OUT, // 20-29
        OUT, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, OUT, // 30-39
        OUT, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, OUT, // 40-49
        OUT, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, OUT, // 50-59
        OUT, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, OUT, // 60-69
        OUT, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, OUT, // 70-79
        OUT, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, OUT, // 80-89
        OUT, EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP, OUT, // 90-99
        OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, // 100-109
        OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT, OUT  // 110-119
    };

    public Board(String fen) {
        String[] fenParts = fen.split("\\s");
        for (int square = 98, fenIndex = 0; fenIndex < fenParts[0].length(); fenIndex++, square--) {
            char c = fenParts[0].charAt(fenIndex);
            switch (c) {
                case 'K':
                    mailbox120[square] = new Piece(KING, WHITE);
                    kingPos120[WHITE.getCode()] = square;
                    break;
                case 'R':
                    mailbox120[square] = new Piece(ROOK, WHITE);
                    for (int i = 0; i < rookPos120[WHITE.getCode()].length; i++) {
                        if (rookPos120[WHITE.getCode()][i] == 0) {
                            rookPos120[WHITE.getCode()][i] = square;
                            break;
                        }
                    }
                    rooksNb[WHITE.getCode()] += 1;
                    break;
                case 'k':
                    mailbox120[square] = new Piece(KING, BLACK);
                    kingPos120[BLACK.getCode()] = square;
                    break;
                case 'r':
                    mailbox120[square] = new Piece(ROOK, BLACK);
                    for (int i = 0; i < rookPos120[BLACK.getCode()].length; i++) {
                        if (rookPos120[BLACK.getCode()][i] == 0) {
                            rookPos120[BLACK.getCode()][i] = square;
                            break;
                        }
                    }
                    rooksNb[BLACK.getCode()] += 1;
                    break;
                case '/':
                    square -= 1;
                    break;
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                    square -= c - '1';
                    break;
                default:
                    throw new IllegalStateException("Недопустимый символ - " + c);
            }
        }
        if (fenParts[1].charAt(0) == 'w') {
            sideToMove = WHITE;
        } else if (fenParts[1].charAt(0) == 'b') {
            sideToMove = BLACK;
        } else {
            throw new IllegalStateException("Недопустимый символ - " + fenParts[1].charAt(0));
        }
    }

    public Color getSideToMove() {
        return sideToMove;
    }

    public int getKingPos(Color color) {
        return kingPos120[color.getCode()];
    }

    public int getRookPos(Color color, int index) {
        return rookPos120[color.getCode()][index];
     }

    public int[] getOffsets(PieceType piece) {
         return offset[piece.getCode()];
     }

    public Piece getPiece(int newPos) {
        return mailbox120[newPos];
    }

    public int getRooksNb(Color color) {
        return rooksNb[color.getCode()];
    }

    /**
     * Выполнение хода. Изменяет положения фигур.
     *
     * @param move ход
     */
    public void doMove(Move move) {
        // удаление взятой фигуры, если была
        Piece pieceTo = mailbox120[move.getTo().getCode()];
        PieceType pieceType = pieceTo.getPieceType();
        if (pieceType != null) {
            switch (pieceType) {
                case ROOK:
                    for (int i = 0; i < rookPos120[pieceTo.getColor().getCode()].length; i++) {
                        if (rookPos120[pieceTo.getColor().getCode()][i] == move.getTo().getCode()) {
                            rookPos120[pieceTo.getColor().getCode()][i] = 0;
                            rooksNb[pieceTo.getColor().getCode()] -= 1;
                            // в случае, если удалили из середины - сдвигаем все значения, оставляя нули справа
                            for (int j = i + 1; j < rookPos120[pieceTo.getColor().getCode()].length; j++) {
                                int nextPos = rookPos120[pieceTo.getColor().getCode()][j];
                                rookPos120[pieceTo.getColor().getCode()][j-1] = nextPos;
                                if (nextPos == 0) {
                                    break;
                                }
                            }
                            break;
                        }
                    }
            }
        }

        mailbox120[move.getFrom().getCode()] = EMP;
        mailbox120[move.getTo().getCode()] = move.getPiece();

        // обновление массивов быстрого доступа
        switch (move.getPiece().getPieceType()) {
            case KING:
                kingPos120[sideToMove.getCode()] = move.getTo().getCode();
                break;
            case ROOK:
                for (int i = 0; i < rookPos120[sideToMove.getCode()].length; i++) {
                    if (rookPos120[sideToMove.getCode()][i] == move.getFrom().getCode()) {
                        rookPos120[sideToMove.getCode()][i] = move.getTo().getCode();
                        break;
                    }
                }
                break;
        }

        sideToMove = sideToMove.getOpposite();
    }

    /**
     * Отмена выполненного хода. Изменяет положения фигур.
     *
     * @param move выполненный прежде ход
     */
    public void undoMove(Move move) {
        mailbox120[move.getFrom().getCode()] = move.getPiece();
        mailbox120[move.getTo().getCode()] = move.getCapture().orElse(EMP);

        // обновление массивов быстрого доступа
        switch (move.getPiece().getPieceType()) {
            case KING:
                kingPos120[sideToMove.getOppositeCode()] = move.getTo().getCode();
                break;
            case ROOK:
                for (int i = 0; i < rookPos120[sideToMove.getOppositeCode()].length; i++) {
                    if (rookPos120[sideToMove.getOppositeCode()][i] == move.getTo().getCode()) {
                        rookPos120[sideToMove.getOppositeCode()][i] = move.getFrom().getCode();
                        break;
                    }
                }
                break;
        }

        // возвращение взятой фигуры, если была
        if (move.getCapture().isPresent()) {
            switch (move.getCapture().get().getPieceType()) {
                case ROOK:
                    for (int i = 0; i < rookPos120[sideToMove.getCode()].length; i++) {
                        if (rookPos120[sideToMove.getCode()][i] == 0) {
                            rookPos120[sideToMove.getCode()][i] = move.getTo().getCode();
                            rooksNb[sideToMove.getCode()] += 1;
                            break;
                        }
                    }
                    break;
                default:
                    throw new NotImplementedException();
            }
        }

        sideToMove = sideToMove.getOpposite();
    }

    public boolean isAttackedBy(Color sideToMove, Square square) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        for (int s = A8.getCode(); s >= H8.getCode(); s--) {
            sb.append(mailbox120[s]).append(" ");
        }
        sb.append("\n");
        for (int s = A7.getCode(); s >= H7.getCode(); s--) {
            sb.append(mailbox120[s]).append(" ");
        }
        sb.append("\n");
        for (int s = A6.getCode(); s >= H6.getCode(); s--) {
            sb.append(mailbox120[s]).append(" ");
        }
        sb.append("\n");
        for (int s = A5.getCode(); s >= H5.getCode(); s--) {
            sb.append(mailbox120[s]).append(" ");
        }
        sb.append("\n");
        for (int s = A4.getCode(); s >= H4.getCode(); s--) {
            sb.append(mailbox120[s]).append(" ");
        }
        sb.append("\n");
        for (int s = A3.getCode(); s >= H3.getCode(); s--) {
            sb.append(mailbox120[s]).append(" ");
        }
        sb.append("\n");
        for (int s = A2.getCode(); s >= H2.getCode(); s--) {
            sb.append(mailbox120[s]).append(" ");
        }
        sb.append("\n");
        for (int s = A1.getCode(); s >= H1.getCode(); s--) {
            sb.append(mailbox120[s]).append(" ");
        }

        return sb.toString();
    }
}
