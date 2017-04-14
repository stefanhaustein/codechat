package org.kobjects.codechat;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.io.StringWriter;
import org.kobjects.codechat.expr.Node;
import org.kobjects.codechat.statement.On;
import org.kobjects.expressionparser.ExpressionParser;

import java.io.StringReader;
import java.util.Scanner;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;

public class MainActivity extends Activity {
    EditText input;
    FrameLayout mainLayout;
    ChatView listView;
    Environment environment;

    protected void onCreate(Bundle whatever) {
        super.onCreate(whatever);
        mainLayout = new FrameLayout(this);
        environment = new Environment(mainLayout);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(linearLayout);

        listView = new ChatView(this);
        linearLayout.addView(listView);
        ((LinearLayout.LayoutParams) listView.getLayoutParams()).weight = 1;

        input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(IME_ACTION_SEND);
        input.setImeActionLabel("Send", IME_ACTION_DONE);

        input.setOnEditorActionListener( new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (v.getText().length() == 0) {
                    return false;
                }
                if (event != null && event.isShiftPressed()) {
                    return false;
                }
                String line = v.getText().toString();
                // print(line);
                processInput(line);
                v.setText("");
                return true;
            }
        });

        linearLayout.addView(input);

        setContentView(mainLayout);
    }

    void printRight(String s) {
        listView.addRight(s);
        listView.smoothScrollToPosition(listView.getCount());
    }

    void printLeft(String s) {
        listView.addLeft(s);
        listView.smoothScrollToPosition(listView.getCount());
    }

    void processInput(String line) {
        boolean printed = false;
        try {
            Evaluable evaluable = environment.parse(line);
            printRight(evaluable.toString());
            printed = true;

            Object result = evaluable.eval(environment);
            printLeft(result == null ? "ok" : String.valueOf(result));
        } catch (Exception e) {
            if (!printed) {
                printRight(line);
            }
            printLeft(e.getMessage());
        }
    }
}