package org.kobjects.codechat.lang;

public class ParsingException extends RuntimeException {
    public final int start;
    public final int end;

    public ParsingException(int start, int end, String message) {
        super(message);
        this.start = start;
        this.end = end;
    }
}
