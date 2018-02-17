package org.kobjects.codechat.android.api.ui;

import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.Type;

public enum EdgeMode implements EnumLiteral {
    NONE, BOUNCE, WRAP;
    public static EnumType TYPE = new EnumType("EdgeMode", NONE, BOUNCE, WRAP);
    @Override
    public Type getType() {
        return TYPE;
    }
}
