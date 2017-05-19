package org.kobjects.codechat.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import java.io.IOException;
import java.io.InputStream;

public class Emoji {
    String code;
    public Emoji(String s) {
        code = s;
    }

    public String toString() {
        return code;
    }


    // TODO: Let the drawable load the best image resolution
    public Drawable getDrawable(Context context) {
        try
        {
            StringBuilder sb = new StringBuilder("emoji/png_128/");

            int codepoint = code.codePointAt(0);
            sb.append(Long.toHexString(codepoint));
            int offset = Character.charCount(codepoint);
            while (offset < code.length()) {
                sb.append('-');
                codepoint = code.codePointAt(offset);
                sb.append(Long.toHexString(codepoint));
                offset += Character.charCount(codepoint);
            }
            sb.append(".png");

            InputStream is = context.getAssets().open(sb.toString());
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
