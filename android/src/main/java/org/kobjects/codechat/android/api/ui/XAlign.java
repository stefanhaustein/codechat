package org.kobjects.codechat.android.api.ui;

import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.Type;

public enum XAlign implements EnumLiteral {
    LEFT, CENTER, RIGHT;
    public static EnumType TYPE = new EnumType("XAlign", LEFT, CENTER, RIGHT);
    @Override
    public Type getType() {
        return TYPE;
    }
}
