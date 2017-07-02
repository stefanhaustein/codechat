package org.kobjects.codechat.android;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.one.EmojiOneProvider;
import java.io.File;
import java.util.List;
import java.util.SortedMap;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Annotation;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends AppCompatActivity implements EnvironmentListener, PopupMenu.OnMenuItemClickListener {
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
    LinearLayout inputButtons;
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
    private boolean fullScreenEditMode;

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

        input = new EmojiEditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); // TYPE_TEXT_FLAG_NO_SUGGESTIONS);//|
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
//        input.setPrivateImeOptions("nm");

        /*
        input.setOnEditorActionListener( new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

s                System.out.println("onEditorAction id: " + actionId + "KeyEvent: " + event);
                if (v.getText().length() == 0) {
                    return false;
                }
                if (event != null && event.isShiftPressed()) {
                    return false;
                }
                String line = v.getText().toString();
                // print(line);
                v.setText("");
                processInput(line);
                return true;
            }
        });
*/

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    inputButtons.setOrientation(LinearLayout.HORIZONTAL);
                    if (fullScreenEditMode) {
                        fullScreenEditMode = false;
                        arrangeUi();
                    }
                }
            }
        });
        inputRow.addView(input);
        ((LinearLayout.LayoutParams) input.getLayoutParams()).weight = 1;
        ((LinearLayout.LayoutParams) input.getLayoutParams()).gravity = Gravity.BOTTOM;

        input.setVerticalScrollBarEnabled(true);

        inputRow.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int newLeft, int newTop, int newRight, int newBottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // System.out.println("Input layout old: " + oldLeft + "/" + oldTop + " - " + oldRight + "/" + oldBottom);
                // System.out.println("Input layout new: " + newLeft + "/" + newTop + " - " + newRight + "/" + newBottom);

                final int oldHeight = oldBottom - oldTop;
                final int newHeight = newBottom - newTop;

                if (newHeight > oldHeight && oldHeight != 0) {
                    inputRow.post(new Runnable() {
                        @Override
                        public void run() {
                            if (inputButtons.getOrientation() == LinearLayout.HORIZONTAL) {
                                inputButtons.setOrientation(LinearLayout.VERTICAL);
                                //              ((LinearLayout.LayoutParams) input.getLayoutParams()).weight = 0;
                            }
                            if (newHeight > chatView.getHeight() && !fullScreenEditMode) {
                                fullScreenEditMode = true;
                                arrangeUi();
                            }
                        }
                    });
                }
            }
        });

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


        inputButtons = new LinearLayout(this);
       // inputButtons.setOrientation(LinearLayout.VERTICAL);

        inputRow.addView(inputButtons);
        ((LinearLayout.LayoutParams) inputButtons.getLayoutParams()).gravity = Gravity.BOTTOM;

        final ImageButton emojiInputButton = new ImageButton(this);
        inputButtons.addView(emojiInputButton);

        ((LinearLayout.LayoutParams) emojiInputButton.getLayoutParams()).gravity = Gravity.BOTTOM;

        emojiInputButton.setImageResource(R.drawable.ic_tag_faces_black_24dp);
//        emojiInputButton.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
        emojiInputButton.setBackgroundColor(0);
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
                input.setText("");
                processInput(line);
            }
        });
        inputButtons.addView(enterButton);


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
        inputButtons.addView(menuButton);
        ((LinearLayout.LayoutParams) menuButton.getLayoutParams()).gravity = Gravity.BOTTOM;


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
        detach(chatView);
        detach(inputRow);
        detach(toolbar);
        detach(contentLayout);
        rootLayout.removeAllViews();

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
            if (!fullScreenEditMode) {
                rows.addView(chatView);
                ((LinearLayout.LayoutParams) chatView.getLayoutParams()).weight = 1;
            }
            rows.addView(inputRow);
            ((LinearLayout.LayoutParams) inputRow.getLayoutParams()).weight = fullScreenEditMode ? 1: 0;

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
            LinearLayout rows = new LinearLayout(this);
            rows.setOrientation(LinearLayout.VERTICAL);

            rows.addView(toolbar);

            if (!fullScreenEditMode) {
                FrameLayout overlay = new FrameLayout(this);
                overlay.addView(chatView);
                FrameLayout.LayoutParams chatParams = (FrameLayout.LayoutParams) chatView.getLayoutParams();
                chatParams.width = MATCH_PARENT;
                chatParams.height = MATCH_PARENT;

                rows.addView(overlay);
                ((LinearLayout.LayoutParams) overlay.getLayoutParams()).weight = 1;
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
            rows.addView(inputRow);
            ((LinearLayout.LayoutParams) inputRow.getLayoutParams()).weight = fullScreenEditMode ? 1: 0;

            rootLayout.addView(rows);
            FrameLayout.LayoutParams rowsParams = (FrameLayout.LayoutParams) rows.getLayoutParams();
            rowsParams.height = MATCH_PARENT;
            rowsParams.width = MATCH_PARENT;
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
            chatView.setValue(chatView.getCount() - 1, s);
        } else {
            chatView.add(true, s);
        }
    }

    public void print(final String s) {
        print(s, null);
    }

    public void print(final String s, final List<Annotation> annotations) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (annotations != null) {  // FIXME
                    SpannableString spannable = new SpannableString(s);
                    for (final Annotation annotation : annotations) {
                        if (annotation.getLink() != null) {
                            spannable.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View view) {
                                    Object link = annotation.getLink();
                                    if (link instanceof Instance) {
                                        StringBuilder sb = new StringBuilder();
                                        ((Instance) link).serialize(sb, Instance.Detail.FULL, null);
                                        input.setText(sb);
                                    }
                                }
                            }, annotation.getStart(), annotation.getEnd(), 0);
                        }
                    }
                    chatView.add(false, spannable);
                } else {
                    chatView.add(false, s);
                }
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
            } else if (pending.equals("edit") || pending.startsWith("edit ")) {
                String key = pending.substring(4).trim();
                SortedMap<String,RootVariable> matches = environment.rootVariables.subMap(key, key + "ZZZZ");

                printRight(pending, update);
                printed = true;
                if (matches.size() == 0) {
                    print("not found: " + key);
                } else if (matches.size() == 1) {
                    RootVariable var = matches.get(matches.firstKey());
                    StringBuilder sb = new StringBuilder();
                    var.dump(sb);
                    input.setText(sb.toString());
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String s: matches.keySet()) {
                        sb.append(s);
                        sb.append("\n");
                    }
                    print(sb.toString());
                }
                pending = "";
            } else {
                ParsingContext parsingContext = new ParsingContext(environment);
                Statement statement = environment.parse(parsingContext, pending);

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