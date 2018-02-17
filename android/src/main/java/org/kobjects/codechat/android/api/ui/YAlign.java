package org.kobjects.codechat.android.api.ui;

import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.Type;

public enum YAlign implements EnumLiteral {
    TOP, CENTER, BOTTOM;
    public static EnumType TYPE = new EnumType("YAlign", TOP, CENTER, BOTTOM);
    @Override
    public Type getType() {
        return TYPE;
    }
}
