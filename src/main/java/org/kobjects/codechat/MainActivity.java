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
        environment.variables.put("sprite", Sprite.class);

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
        try {
            ExpressionParser<Node> parser = Processor.createParser();
            ExpressionParser.Tokenizer tokenizer = new ExpressionParser.Tokenizer(
                    new Scanner(new StringReader(line)),
                    parser.getSymbols(), ":");

            tokenizer.nextToken();
            if (tokenizer.tryConsume("on")) {
                final Node condition = parser.parse(tokenizer);
                tokenizer.consume(":");
                final Node exec = parser.parse(tokenizer);
                printRight ("on " + condition + ": " + exec);
                environment.ticking.add(new Ticking() {
                    @Override
                    public void tick(boolean force) {
                        if (Boolean.TRUE.equals(condition.eval(environment))) {
                            exec.eval(environment);
                        }
                    }
                });
            } else if (tokenizer.tryConsume("dump")) {
                StringWriter sw = new StringWriter();
                environment.dump(sw);
                printRight("dump");
                printLeft(sw.toString());
            } else {
                Node node = parser.parse(line);
                printRight(node.toString());
                printLeft(String.valueOf(node.eval(environment)));
            }
        } catch (Exception e) {
            printRight(line);
            e.printStackTrace();
            printLeft(e.getMessage());
        }
    }
}