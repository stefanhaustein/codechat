package org.kobjects.codechat.android;

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
import android.util.DisplayMetrics;
import android.view.Gravity;
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
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends AppCompatActivity implements Environment.EnvironmentListener, PopupMenu.OnMenuItemClickListener {
    static final String SETTINGS_FILE_NAME = "fileName";
    static final String SETTINGS_FILE_NAME_DEFAULT = "CodeChat";

    static final String MENU_ITEM_CONTINUE = "Continue";
    static final String MENU_ITEM_PAUSE = "Pause";
    static final String MENU_ITEM_WINDOW_MODE = "Window mode";

    static final int SHOW_TOOLBAR_HEIGHT_DP = 620;

    EmojiEditText input;
    FrameLayout rootLayout;
    FrameLayout contentLayout;
    LinearLayout inputRow;
    ChatView chatView;
    Environment environment;
    Toolbar toolbar;
    ImageButton menuButton;

    MenuItem pauseItem;

    String pending = "";
    EmojiPopup emojiPopup;
    int balance;
    SharedPreferences settings;
    File codeDir;
    private float pixelPerDp;
    boolean windowMode;
    Point displaySize = new Point();

    protected void onCreate(Bundle whatever) {
        super.onCreate(whatever);
        pixelPerDp = getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT;

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

        environment = new AndroidEnvironment(this, contentLayout, codeDir);

        inputRow = new LinearLayout(this);

        final ImageButton emojiInputButton = new ImageButton(this);
        inputRow.addView(emojiInputButton);
        emojiInputButton.setImageResource(R.drawable.ic_tag_faces_black_24dp);
//        emojiInputButton.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
        emojiInputButton.setBackgroundColor(0);

        input = new EmojiEditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); // TYPE_TEXT_FLAG_NO_SUGGESTIONS);//|
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
//        input.setPrivateImeOptions("nm");
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

        chatView.setSelectionCallback(new ChatView.SelectionCallback() {
            @Override
            public void selected(boolean right, String text) {
                int lineCount = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '\n') {
                        lineCount++;
                        if (lineCount > 5) {
                            return;
                        }
                    }
                }
                if (lineCount == 0) {
                    text = input.getText().toString() + text;
                }
                input.setText(text);
            }
        });


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
                fillMenu(popup.getMenu(), false);
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

        getWindowManager().getDefaultDisplay().getSize(displaySize);

        // System.out.println("**** DisplayHeight in DP: " + displayHightDp);

        if (displaySize.x > displaySize.y) {
            LinearLayout columns = new LinearLayout(this);
            rootLayout.addView(columns);
            FrameLayout.LayoutParams columnsParams = (FrameLayout.LayoutParams) columns.getLayoutParams();
            columnsParams.height = MATCH_PARENT;
            columnsParams.width = MATCH_PARENT;

            LinearLayout rows = new LinearLayout(this);
            rows.setOrientation(LinearLayout.VERTICAL);

            rows.addView(toolbar);
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
        } else {
            FrameLayout overlay = new FrameLayout(this);
            overlay.addView(chatView);
            FrameLayout.LayoutParams chatParams = (FrameLayout.LayoutParams) chatView.getLayoutParams();
            chatParams.width = MATCH_PARENT;
            chatParams.height = MATCH_PARENT;

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

            overlay.addView(contentLayout);
            FrameLayout.LayoutParams contentParams = (FrameLayout.LayoutParams) contentLayout.getLayoutParams();

            if (windowMode) {
                contentLayout.setBackgroundColor(0x0ff888888);
                contentParams.gravity = Gravity.RIGHT | Gravity.TOP;
                contentParams.width = displaySize.x / 2;
                contentParams.height = displaySize.x * 3 / 8;
            } else {
                contentLayout.setBackgroundColor(0);
                contentParams.width = MATCH_PARENT;
                contentParams.height = MATCH_PARENT;
            }
        }

        float displayHightDp = displaySize.y / pixelPerDp;
        if (displayHightDp < SHOW_TOOLBAR_HEIGHT_DP || windowMode) {
            toolbar.setVisibility(View.GONE);
            menuButton.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.VISIBLE);
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
    public boolean onPrepareOptionsMenu(final Menu menu) {
       menu.clear();
       fillMenu(menu, true);
       return true;
    }

    public void fillMenu(final Menu menu, boolean isOptionsMenu) {
        MenuItem pause = menu.add(environment.paused ? MENU_ITEM_CONTINUE : MENU_ITEM_PAUSE);
        if (isOptionsMenu) {
            pauseItem = pause;
            pauseItem.setIcon(environment.paused ? R.drawable.ic_play_arrow_white_24dp : R.drawable.ic_pause_white_24dp);
            pauseItem.setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        }
        if (displaySize.x <= displaySize.y) {
            menu.add(MENU_ITEM_WINDOW_MODE).setCheckable(true).setChecked(windowMode);
        }
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
            case MENU_ITEM_WINDOW_MODE:
                windowMode = !windowMode;
                arrangeUi();
                break;
            default:
                processInput(title);
        }
        return true;
    }

    void printRight(CharSequence s, boolean update) {
        if (update) {
            chatView.setValue(chatView.getCount() - 3, s);
        } else {
            chatView.add(true, s);
        }
    }

    public void print(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatView.add(false, s);
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
        } else if (line.length() == 0) {
            printRight("", false);
            return;
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
                ParsingContext parsingContext = new ParsingContext(environment);
                Statement statement = environment.parse(pending, parsingContext);

                if (statement instanceof ExpressionStatement) {
                    Expression expression = ((ExpressionStatement) statement).expression;
                    String s = expression.toString();
                    printRight(s, update);
                    printed = true;
                    Object result = expression.eval(parsingContext.createEvaluationContext());
                    if (Type.VOID.equals(expression.getType())) {
                        print("ok");
                    } else {
                        print(Formatting.toLiteral(result));
                    }
                } else {
                    printRight(statement.toString(), update);
                    printed = true;
                    statement.eval(parsingContext.createEvaluationContext());
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