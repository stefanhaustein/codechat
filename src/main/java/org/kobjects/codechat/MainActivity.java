package org.kobjects.codechat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.kobjects.codechat.expr.BuiltinInvocation;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.ui.ChatView;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;

public class MainActivity extends AppCompatActivity implements Environment.EnvironmentListener {
    EditText input;
    FrameLayout mainLayout;
    ChatView listView;
    Environment environment;
    Toolbar toolbar;
    MenuItem pauseItem;
    String pending = "";

    protected void onCreate(Bundle whatever) {
        super.onCreate(whatever);
        mainLayout = new FrameLayout(this);
        environment = new Environment(this, mainLayout);

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

//        option


        listView = new ChatView(this);
        linearLayout.addView(listView);
        ((LinearLayout.LayoutParams) listView.getLayoutParams()).weight = 1;

        LinearLayout inputLayout = new LinearLayout(this);

        input = new AppCompatEditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);//|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        pauseItem = menu.add("pause");
        pauseItem.setIcon(R.drawable.ic_pause_white_24dp).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == pauseItem) {
            environment.pause(!environment.paused);
        } else {
            processInput(item.getTitle().toString());
        }
        return true;
    }

    void printRight(String s, boolean update) {
        if (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        if (update) {
            listView.setValue(listView.getCount() - 1, s);
        } else {
            listView.addRight(s);
            listView.setSelection(listView.getCount() - 1);
        }
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.smoothScrollToPosition(listView.getCount());
            }
        });
    }

    public void print(String s) {
        listView.addLeft(s);
        listView.setSelection(listView.getCount() - 1);
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.smoothScrollToPosition(listView.getCount());
            }
        });
    }

    void processInput(String line) {
        boolean printed = false;
        boolean update = !pending.isEmpty();
        pending += line + "\n";


        int balance = environment.getBalance(pending);
        try {
            if (balance < 0) {
                throw new RuntimeException("Unmatched closing '}'");
            } else if (balance > 0) {
                printRight(pending + "(append " + (balance == 1 ? "" : (String.valueOf(balance) + ' ')) + "'}' to complete input)", update);
            } else {
                Statement statement = environment.parse(pending);

                if (statement instanceof ExpressionStatement) {
                    Expression expression = ((ExpressionStatement) statement).expression;
                    String s = expression.toString();
                    printRight(s, update);
                    printed = true;
                    Object result = expression.eval(environment.getRootContext());
                    if (Type.VOID.equals(expression.getType())) {
                        print("ok");
                    } else {
                        print(Formatting.toLiteral(result));
                    }
                } else {
                    printRight(statement.toString(), update);
                    printed = true;
                    statement.eval(environment.getRootContext());
                    print("ok");
                }
                pending = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!printed) {
                printRight(pending, update);
            }
            pending = "";
            print(e.getMessage());
        }
    }

    @Override
    public void paused(boolean paused) {
        pauseItem.setIcon(paused ? R.drawable.ic_play_arrow_white_24dp : R.drawable.ic_pause_white_24dp);
        pauseItem.setTitle(paused ? "continue" : "pause");
    }

    @Override
    public void setTitle(String name) {
        toolbar.setTitle(name);
    }
}