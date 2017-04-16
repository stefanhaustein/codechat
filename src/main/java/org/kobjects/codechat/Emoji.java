package org.kobjects.codechat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import java.io.IOException;
import java.io.InputStream;

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


    // TODO: Let the drawable load the best image resolution
    public Drawable getDrawable(Context context) {
        try
        {
            InputStream is = context.getAssets().open("emoji/png_128/" + Integer.toHexString(codepoint) + ".png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(is, null);
            // set image to ImageView

            is.close();
            return d;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

    }
}
