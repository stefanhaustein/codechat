package org.kobjects.codechat.android;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
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
import com.vanniktech.emoji.EmojiPopup;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedString;
import org.kobjects.codechat.annotation.Link;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.lang.Dependency;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.expressionparser.ExpressionParser.ParsingException;

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
    Object errorSpan;
    ImageButton emojiInputButton;

    EmojiPopup emojiPopup;
    SharedPreferences settings;
    File codeDir;
    private float pixelPerDp;
    boolean windowMode;
    Point displaySize = new Point();
    private boolean fullScreenEditMode;
    private long lastEdit;
    private int errorStart;
    private int errorEnd;
    private String errorText;
    private String errorPrinted;

    protected void onCreate(Bundle whatever) {
        super.onCreate(whatever);
        pixelPerDp = getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT;

        codeDir = getExternalFilesDir("code");

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

        input = new EmojiEditText(this) {
            @Override
            public boolean isSuggestionsEnabled() {
                return false;
            }

            @Override
            protected void onSelectionChanged(int selStart, int selEnd) {
                if (errorSpan != null && selStart == selEnd && selStart >= errorStart && selStart <= errorEnd && errorText != null && !errorText.equals(errorPrinted)) {
                    print(errorText);
                    errorPrinted = errorText;
                }
            }

        };
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
                lastEdit = System.currentTimeMillis();
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

                if (newHeight > oldHeight * 1.5 && oldHeight != 0) {
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

        emojiInputButton = new ImageButton(this);
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

        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b && emojiPopup != null && emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
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

        final Handler handler = new Handler();
        final Runnable syntaxChecker = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastEdit > 1000) {
                    lastEdit = Integer.MAX_VALUE;
                    Editable editable = input.getText();

                    if (errorSpan != null) {
                        editable.removeSpan(errorSpan);
                        errorSpan = null;

                    }
                    try {
                        ParsingContext parsingContext = new ParsingContext(environment);
                        environment.parse(parsingContext, input.getText().toString());

                    } catch (ParsingException e) {
                        errorText = e.getMessage();
                   //     if (e.start >= 0 && e.start < e.end && e.end <= editable.length()) {
                            // protection against expressionparser position bug.
                            errorSpan = new BackgroundColorSpan(Color.RED);


                            errorStart = Math.max(Math.min(e.start, editable.length() - 1), 0);
                            errorEnd = Math.min(Math.max(e.end, 1), editable.length());

                            input.getText().setSpan(errorSpan, errorStart, errorEnd, 0);

                     //   }

                    } catch (Exception e) {
                      e.printStackTrace();
                    }

                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(syntaxChecker, 1000);

    }

    void detach(View view) {
        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    void arrangeUi() {
        if (emojiPopup != null) {
            emojiPopup.dismiss();
            emojiInputButton.setImageResource(R.drawable.ic_tag_faces_black_24dp);
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
        menu.add("Help");
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
                processInput(title.toLowerCase());
        }
        return true;
    }

    void printRight(CharSequence s) {
        if (s instanceof String && ((String) s).indexOf('\n') == -1) {
            print(new AnnotatedString((String) s, Collections.singletonList(new AnnotationSpan(0, s.length(), s))), true);
        } else {
            chatView.add(true, s);
        }
    }

    public void print(final CharSequence s) {
        print(s, false);
    }

    @Override
    public void edit(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                input.setText(s);
            }
        });
    }

    public void print(final CharSequence s, final boolean right) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (s instanceof AnnotatedCharSequence) {  // FIXME
                    SpannableString spannable = new SpannableString(s);
                    for (final AnnotationSpan annotation : ((AnnotatedCharSequence) s).getAnnotations()) {
                        if (annotation.getLink() != null) {
                            spannable.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View view) {
                                    Object link = annotation.getLink();
                                    if (link instanceof Link) {
                                        ((Link) link).execute(environment);
                                    } else if (link instanceof Instance) {
                                        StringBuilder sb = new StringBuilder();
                                        ((Instance) link).serialize(new AnnotatedStringBuilder(sb, null), Instance.Detail.DETAIL, new HashMap<Dependency, Environment.SerializationState>());
                                        input.setText(sb.toString());
                                        // input.requestFocus();
                                    } else if (link instanceof Documented) {
                                        print(((Documented) link).getDocumentation());
                                    } else {
                                        input.setText(String.valueOf(link));
                                        // input.requestFocus();
                                    }
                                }
                            }, annotation.getStart(), annotation.getEnd(), 0);
                        }
                    }
                    chatView.add(right, spannable);
                } else {
                    chatView.add(right, s);
                }
            }
        });
    }

    void processInput(String line) {
        boolean printed = false;
        if (line.length() == 0) {
            printRight("");
            return;
        }
        try {
            if (line.equals("edit") || line.startsWith("edit ")) {
                String key = line.substring(4).trim();
                SortedMap<String,RootVariable> matches = environment.rootVariables.subMap(key, key + "ZZZZ");

                printRight(line);
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
            } else {
                ParsingContext parsingContext = new ParsingContext(environment);
                Statement statement = environment.parse(parsingContext, line);

                if (statement instanceof ExpressionStatement) {
                    Expression expression = ((ExpressionStatement) statement).expression;
                    String s = expression.toString();
                    printRight(s);
                    printed = true;
                    Object result = expression.eval(parsingContext.createEvaluationContext());
                    if (Type.VOID.equals(expression.getType())) {
                        print("ok");
                    } else if (result instanceof Instance) {
                        String literal = Formatting.toLiteral(result);
                        AnnotationSpan annotation = new AnnotationSpan(0, literal.length(), result);
                        print(new AnnotatedString(literal, Collections.singletonList(annotation)));
                    } else {
                        print(Formatting.toLiteral(result));
                    }
                } else {
                    printRight(statement.toString());
                    printed = true;
                    statement.eval(parsingContext.createEvaluationContext());
                    print("ok");
                }

                if (environment.autoSave) {
                    environment.save(toolbar.getTitle().toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!printed) {
                printRight(line);
            }
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