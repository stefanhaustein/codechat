package org.kobjects.codechat;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    EditText input;
    FrameLayout mainLayout;
    ChatView listView;
    Environment environment;
    Toolbar toolbar;

    protected void onCreate(Bundle whatever) {
        super.onCreate(whatever);
        mainLayout = new FrameLayout(this);
        environment = new Environment(mainLayout);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(linearLayout);

        toolbar = new Toolbar(this);
        toolbar.setTitle("CodeChat");
        toolbar.setBackgroundColor(0xff3f51b5);
        toolbar.setTitleTextColor(0x0ffffffff);

        linearLayout.addView(toolbar);
//        toolbar.getLayoutParams().height = 100;
        setSupportActionBar(toolbar);

        listView = new ChatView(this);
        linearLayout.addView(listView);
        ((LinearLayout.LayoutParams) listView.getLayoutParams()).weight = 1;

        LinearLayout inputLayout = new LinearLayout(this);

        input = new AppCompatEditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
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
        inputLayout.addView(input);
        ((LinearLayout.LayoutParams) input.getLayoutParams()).weight = 1;

        ImageButton enterButton = new ImageButton(this);
        enterButton.setImageResource(R.drawable.ic_subdirectory_arrow_left_black_24dp);
      //  enterButton.setImageDrawable(new Emoji(0x2705).getDrawable(this));
        //enterButton.setText("\u23ce");
        inputLayout.addView(enterButton);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String line = input.getText().toString();
                processInput(line);
                input.setText("");
            }
        });
        enterButton.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;

        linearLayout.addView(inputLayout);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                input.setText(String.valueOf(listView.getAdapter().getItem(position)));
            }
        });

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