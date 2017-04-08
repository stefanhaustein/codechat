package org.kobjects.codechat;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import org.kobjects.codechat.tree.Node;
import org.kobjects.expressionparser.ExpressionParser;

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
        list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
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
            Node node = parser.parse(line);
            print(node.toString());
            print(String.valueOf(node.eval(environment)));
        } catch (Exception e) {
            e.printStackTrace();
            print(e.getMessage());
        }
    }
}