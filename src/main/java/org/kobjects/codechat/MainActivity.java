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

    ArrayAdapter<String> list;
    EditText input;
    FrameLayout mainLayout;
    ListView listView;
    Environment environment;

    protected void onCreate(Bundle whatever) {
        super.onCreate(whatever);
        mainLayout = new FrameLayout(this);
        environment = new Environment(mainLayout);
        environment.variables.put("sprite", Sprite.class);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(linearLayout);

        listView = new ListView(this);
        list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView result;
                if (convertView instanceof TextView) {
                    result = (TextView) convertView;
                } else {
                    result = new TextView(parent.getContext());
//                    result.setTextColor(Color.BLACK);
  //                  result.setTextSize(20);

                result.setBackground(new Drawable() {
                    Paint paint = new Paint();
                    @Override
                    public void draw(Canvas canvas) {
                        paint.setColor(0xff88ff88);
                        paint.setStyle(Paint.Style.FILL);
                            canvas.drawRoundRect(new RectF(getBounds()), 20, 20, paint);
                    }

                    @Override
                    public void setAlpha(int i) {

                    }

                    @Override
                    public void setColorFilter(ColorFilter colorFilter) {

                    }

                    @Override
                    public int getOpacity() {
                        return TRANSLUCENT;
                    }
                });
                }
                result.setPadding(20, 20, 20, 20);
                result.setText(getItem(position));
                return result;
            }
        };
        listView.setAdapter(list);
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
                print(line);
                processInput(line);
                v.setText("");
                return true;
            }
        });

        linearLayout.addView(input);

        setContentView(mainLayout);
    }

    void print(String s) {
        list.add(s);
        listView.smoothScrollToPosition(list.getCount());
    }

    void processInput(String line) {
        try {
            ExpressionParser<Node> parser = Processor.createParser();
            ExpressionParser.Tokenizer tokenizer = new ExpressionParser.Tokenizer(
                    new Scanner(new StringReader(line)),
                    parser.getSymbols(), ":");

            tokenizer.nextToken();
            print(""+tokenizer.currentType);
            if (tokenizer.tryConsume("on")) {
                final Node condition = parser.parse(tokenizer);
                tokenizer.consume(":");
                final Node exec = parser.parse(tokenizer);
                print ("on " + condition + ": " + exec);
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
                print(sw.toString());
            } else {
                Node node = parser.parse(line);
                print(node.toString());
                print(String.valueOf(node.eval(environment)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            print(e.getMessage());
        }
    }
}