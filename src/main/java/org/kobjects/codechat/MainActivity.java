package org.kobjects.codechat;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.one.EmojiOneProvider;
import java.io.File;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.ui.ChatView;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends AppCompatActivity implements Environment.EnvironmentListener, PopupMenu.OnMenuItemClickListener {
    static final String SETTINGS_FILE_NAME = "fileName";
    static final String SETTINGS_FILE_NAME_DEFAULT = "CodeChat";

    static final String MENU_ITEM_CONTINUE = "Continue";
    static final String MENU_ITEM_PAUSE = "Pause";

    EmojiEditText input;
    FrameLayout rootLayout;
    FrameLayout contentLayout;
    LinearLayout inputRow;
    ChatView chatView;
    Environment environment;
    Toolbar toolbar;
    ImageButton menuButton;

    MenuItem pauseItem;
    MenuItem windowItem;

    String pending = "";
    EmojiPopup emojiPopup;
    int balance;
    SharedPreferences settings;
    File codeDir;

    protected void onCreate(Bundle whatever) {
        super.onCreate(whatever);

        codeDir = getExternalFilesDir("code");

        EmojiManager.install(new EmojiOneProvider());
        rootLayout = new FrameLayout(this);
        contentLayout = new FrameLayout(this);
        contentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(0xff3f51b5);
        toolbar.setTitleTextColor(0x0ffffffff);
        toolbar.setTitle("CodeChat");
        setSupportActionBar(toolbar);

        chatView = new ChatView(this);

        environment = new Environment(this, contentLayout, codeDir);

        inputRow = new LinearLayout(this);

        final ImageButton emojiInputButton = new ImageButton(this);
        inputRow.addView(emojiInputButton);
        emojiInputButton.setImageResource(R.drawable.ic_tag_faces_black_24dp);
//        emojiInputButton.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
        emojiInputButton.setBackgroundColor(0);

        input = new EmojiEditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_NO_SUGGESTIONS);//|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setPrivateImeOptions("nm");
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
        inputRow.addView(input);
        ((LinearLayout.LayoutParams) input.getLayoutParams()).weight = 1;

        emojiInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emojiPopup == null) {
                    emojiPopup = EmojiPopup.Builder.fromRootView(rootLayout).build(input);
                }
                emojiPopup.toggle();
                if (emojiPopup.isShowing()) {
                    emojiInputButton.setImageResource(R.drawable.ic_keyboard_black_24dp);
                } else {
                    emojiInputButton.setImageResource(R.drawable.ic_tag_faces_black_24dp);
                }
            }
        });

        final ImageButton enterButton = new ImageButton(this);
        enterButton.setImageResource(R.drawable.ic_send_black_24dp);
        enterButton.setBackgroundColor(0);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String line = input.getText().toString();
                processInput(line);
                input.setText("");
            }
        });
        inputRow.addView(enterButton);

        menuButton = new ImageButton(this);
        menuButton.setImageResource(R.drawable.ic_more_vert_black_24dp);
        menuButton.setBackgroundColor(0);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                Menu menu = popup.getMenu();
                popup.getMenu().add(environment.paused ? MENU_ITEM_CONTINUE : MENU_ITEM_PAUSE);

                popup.setOnMenuItemClickListener(MainActivity.this);
                popup.show();
            }
        });
        inputRow.addView(menuButton);


        chatView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String content = chatView.getAdapter().getItem(position).toString();
                System.out.println("Clicked: '" + content + "'");
                input.setText(content);
            }
        });

        setContentView(rootLayout);

        arrangeUi();

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        String fileName = settings.getString(SETTINGS_FILE_NAME, SETTINGS_FILE_NAME_DEFAULT);

        if (new File(codeDir, fileName).exists()) {
            try {
                environment.load(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void detach(View view) {
        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    void arrangeUi() {
        if (emojiPopup != null) {
            emojiPopup.dismiss();
            emojiPopup = null;
        }
        rootLayout.removeAllViews();

        detach(chatView);
        detach(inputRow);
        detach(toolbar);
        detach(contentLayout);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if (size.x > size.y) {
            LinearLayout columns = new LinearLayout(this);
            rootLayout.addView(columns);
            FrameLayout.LayoutParams columnsParams = (FrameLayout.LayoutParams) columns.getLayoutParams();
            columnsParams.height = MATCH_PARENT;
            columnsParams.width = MATCH_PARENT;

            LinearLayout rows = new LinearLayout(this);
            rows.setOrientation(LinearLayout.VERTICAL);

           // rows.addView(toolbar);
            rows.addView(chatView);
            ((LinearLayout.LayoutParams) chatView.getLayoutParams()).weight = 1;

            rows.addView(inputRow);

            columns.addView(rows);
            LinearLayout.LayoutParams rowsParams = (LinearLayout.LayoutParams) rows.getLayoutParams();
            rowsParams.width = 0;
            rowsParams.weight = 0.5f;
            rowsParams.height = MATCH_PARENT;

            columns.addView(contentLayout);
            LinearLayout.LayoutParams contentParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
            contentParams.width = 0;
            contentParams.weight = 0.5f;
            contentParams.height = MATCH_PARENT;
            contentLayout.setBackgroundColor(0xff888888);

            menuButton.setVisibility(View.VISIBLE);
        } else {
            FrameLayout overlay = new FrameLayout(this);
            overlay.addView(chatView);
            FrameLayout.LayoutParams chatParams = (FrameLayout.LayoutParams) chatView.getLayoutParams();
            chatParams.width = MATCH_PARENT;
            chatParams.height = MATCH_PARENT;
            overlay.addView(contentLayout);
            FrameLayout.LayoutParams contentParams = (FrameLayout.LayoutParams) contentLayout.getLayoutParams();
            contentParams.width = MATCH_PARENT;
            contentParams.height = MATCH_PARENT;

            LinearLayout rows = new LinearLayout(this);
            rows.setOrientation(LinearLayout.VERTICAL);

            rows.addView(toolbar);
            rows.addView(overlay);
            ((LinearLayout.LayoutParams) overlay.getLayoutParams()).weight = 1;
            rows.addView(inputRow);

            rootLayout.addView(rows);
            FrameLayout.LayoutParams rowsParams = (FrameLayout.LayoutParams) rows.getLayoutParams();
            rowsParams.height = MATCH_PARENT;
            rowsParams.width = MATCH_PARENT;
            contentLayout.setBackgroundColor(0);

            menuButton.setVisibility(View.GONE);
        }

        input.requestFocus();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        arrangeUi();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        pauseItem = menu.add(MENU_ITEM_PAUSE);
        pauseItem.setIcon(R.drawable.ic_pause_white_24dp).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onMenuItemClick(item);
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        String title = item.getTitle().toString();
        switch (title) {
            case MENU_ITEM_PAUSE:
                environment.pause(true);
                break;
            case MENU_ITEM_CONTINUE:
                environment.pause(false);
                break;
            default:
                processInput(title);
        }
        return true;
    }



    void printRight(CharSequence s, boolean update) {
        if (update) {
            chatView.setValue(chatView.getCount() - 1, s);
        } else {
            chatView.add(true, s);
            chatView.setSelection(chatView.getCount() - 1);
        }
        chatView.post(new Runnable() {
            @Override
            public void run() {
                chatView.smoothScrollToPosition(chatView.getCount());
            }
        });
    }

    public void print(String s) {
        chatView.add(false, s);
        chatView.setSelection(chatView.getCount() - 1);
        chatView.post(new Runnable() {
            @Override
            public void run() {
                chatView.smoothScrollToPosition(chatView.getCount());
            }
        });
    }

    void processInput(String line) {
        boolean printed = false;
        boolean update = !pending.isEmpty();
        if (update) {
            StringBuilder sb = new StringBuilder(pending);
            sb.append('\n');
            for (int i = 0; i < balance; i++) {
                sb.append("  ");
            }
            sb.append(line);
            pending = sb.toString();
        } else {
            pending = line;
        }

        balance = environment.getBalance(pending);
        try {
            if (balance < 0) {
                throw new RuntimeException("Unmatched closing '}'");
            } else if (balance > 0) {
                Spannable spannable = new SpannableString(pending + "\nappend " + (balance == 1 ? "" : (String.valueOf(balance) + ' ')) + "'}' to complete input");
                spannable.setSpan(new ForegroundColorSpan(0x088000000), pending.length(), spannable.length(), 0);
                spannable.setSpan(new RelativeSizeSpan(0.8f), pending.length(), spannable.length(), 0);
                printRight(spannable, update);
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

                if (environment.autoSave) {
                    environment.save(toolbar.getTitle().toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!printed) {
                printRight(pending, update);
            }
            pending = "";
            balance = 0;
            print(e.getMessage());
        }
    }

    @Override
    public void paused(boolean paused) {
        pauseItem.setIcon(paused ? R.drawable.ic_play_arrow_white_24dp : R.drawable.ic_pause_white_24dp);
        pauseItem.setTitle(paused ? MENU_ITEM_CONTINUE : MENU_ITEM_PAUSE);
    }

    @Override
    public void setName(String name) {
        if (!name.equals(toolbar.getTitle().toString())) {
            toolbar.setTitle(name);
            settings.edit().putString(SETTINGS_FILE_NAME, name).commit();
        }
    }

}