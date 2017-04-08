package org.kobjects.codechat;

public class Emoji {
    final int codepoint;

    public Emoji(int codepoint) {
        this.codepoint = codepoint;
    }

    public Emoji(String s) {
        this.codepoint = s.codePointAt(0);
    }

    public String toString() {
        return new String(Character.toChars(codepoint));
    }
}
