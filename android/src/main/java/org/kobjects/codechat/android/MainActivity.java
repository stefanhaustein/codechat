package org.kobjects.codechat.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import android.widget.ScrollView;
import android.widget.TextView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.SortedMap;
import org.kobjects.codechat.android.chatview.BubbleAction;
import org.kobjects.codechat.android.chatview.ChatView;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.InstanceLink;
import org.kobjects.codechat.annotation.ErrorLink;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.HelpStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.expressionparser.ExpressionParser.ParsingException;


public class MainActivity extends AppCompatActivity implements EnvironmentListener, PopupMenu.OnMenuItemClickListener, MenuItem.OnMenuItemClickListener {
    static final String SETTINGS_FILE_NAME = "fileName";
    static final String SETTINGS_FILE_NAME_DEFAULT = "CodeChat";

    static final String MENU_ITEM_CLEAR_ALL = "Clear all";
    static final String MENU_ITEM_EXAMPLES = "Examples";
    static final String MENU_ITEM_CLEAR_MENU = "Clear";
    static final String MENU_ITEM_CLEAR_INPUT = "Clear input";
    static final String MENU_ITEM_CLEAR_OUTPUT = "Clear output";
    static final String MENU_ITEM_RESUME = "Resume";
    static final String MENU_ITEM_SUSPEND = "Suspend";
    static final String MENU_ITEM_DISPLAY_MENU = "Display";
    static final String MENU_ITEM_OVERLAY_MODE = "Overlay mode";
    static final String MENU_ITEM_WINDOW_MODE = "Window mode";
    static final String MENU_ITEM_FULLSCREEN = "Fullscreen";

    static final int SHOW_TOOLBAR_HEIGHT_DP = 620;

    /** Matches the accent color, easier than jumping through andorid hoops to obtain it. */
    static final int ERROR_COLOR = 0x0aaff5722;

    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                        .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                        "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    EmojiEditText input;
    FrameLayout rootLayout;
    FrameLayout contentLayout;
    LinearLayout inputRow;
    LinearLayout inputButtons;
    ChatView chatView;
    Environment environment;
    Toolbar toolbar;
    ImageButton menuButton;

    MenuItem suspendItem;
    Object errorSpan;
    ImageButton emojiInputButton;

    EmojiPopup emojiPopup;
    SharedPreferences settings;
    File codeDir;
    private float pixelPerDp;
    private boolean windowMode;
    private boolean fullScreenMode;
    private boolean fullScreenEditor;
    Point displaySize = new Point();
    private long lastEdit;
    private int errorStart;
    private int errorEnd;
    private String errorText;
    private BubbleAction copyAction;
    private BubbleAction editAction;
    private EmojiTextView errorView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

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
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_more_vert_white_24dp));
        toolbar.setTitle("CodeChat");
        setSupportActionBar(toolbar);

        // Construct first, add last...
        errorView = new EmojiTextView(this);
        errorView.setTextColor(0x0ffffffff);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorView.setVisibility(View.INVISIBLE);
            }
        });
        final GradientDrawable errorShape =  new GradientDrawable();
        errorShape.setCornerRadius(pixelPerDp * 8);
        errorShape.setColor(ERROR_COLOR);
        errorView.setBackground(errorShape);
        int pad = Math.round(pixelPerDp * 8);
        errorView.setPadding(pad, pad, pad, pad);


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
                if (errorSpan != null && selStart == selEnd && selStart >= errorStart && selStart <= errorEnd) {
                    showError(errorText);
                } else {
                    errorView.setVisibility(INVISIBLE);
                }
            }

        };
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); // TYPE_TEXT_FLAG_NO_SUGGESTIONS);//|
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
//        input.setPrivateImeOptions("nm");


        copyAction = new BubbleAction(R.drawable.ic_content_copy_black_24dp, "Copy") {
            @Override
            public void invoke(CharSequence text) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Code", text);
                clipboard.setPrimaryClip(clip);
            }
        };
        editAction = new BubbleAction(R.drawable.ic_edit_black_24dp, "Edit") {
            @Override
            public void invoke(CharSequence text) {
                input.setText(String.valueOf(text));
            }
        };

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
                    if (fullScreenEditor) {
                        fullScreenEditor = false;
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

                if ((newHeight > oldHeight * 1.2 && oldHeight != 0) || newHeight > chatView.getHeight()) {
                    inputRow.post(new Runnable() {
                        @Override
                        public void run() {
                            if (inputButtons.getOrientation() == LinearLayout.HORIZONTAL) {
                                inputButtons.setOrientation(LinearLayout.VERTICAL);
                                //              ((LinearLayout.LayoutParams) input.getLayoutParams()).weight = 0;
                            }
                            if (newHeight > chatView.getHeight() && !fullScreenEditor) {
                                fullScreenEditor = true;
                                arrangeUi();
                            }
                        }
                    });
                }
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
                showError(null);
            }
        });


        final ImageButton enterButton = new ImageButton(this);
        enterButton.setImageResource(R.drawable.ic_send_black_24dp);
        enterButton.setBackgroundColor(0);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String line = input.getText().toString();
                errorSpan = null;
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
              // setForceShowIcon(popup);
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

        rootLayout.addView(errorView);

        errorView.setVisibility(View.INVISIBLE);

        drawerLayout = new DrawerLayout(this);
        drawerLayout.addView(rootLayout);

        NavigationView navigationView = new NavigationView(this);
         drawerLayout.addView(navigationView);
        ((DrawerLayout.LayoutParams) navigationView.getLayoutParams()).gravity = Gravity.LEFT;

        navigationView.getMenu().add("About").setOnMenuItemClickListener(this);
        Menu examples = navigationView.getMenu().addSubMenu("Examples");
        fillExamplesMenu(examples);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);

        setContentView(drawerLayout);
//        getActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        arrangeUi();

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        String fileName = settings.getString(SETTINGS_FILE_NAME, SETTINGS_FILE_NAME_DEFAULT);

        if (new File(codeDir, fileName).exists()) {
            try {
                environment.load(fileName);
            } catch (Exception e) {
                print(e.getMessage());
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
                        ParsingContext parsingContext = new ParsingContext(environment, ParsingContext.Mode.SYNTAX_CHECK);
                        environment.parse(parsingContext, input.getText().toString());

                    } catch (ParsingException e) {
                        errorSpan = addErrorSpan(input.getText(), e);
                        errorText = e.getMessage();
                        errorStart = e.start;
                        errorEnd = e.end;
                     //   }

                    } catch (Exception e) {
                      e.printStackTrace();
                    }

                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(syntaxChecker, 1000);

        rootLayout.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {

                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
                    fullScreenMode = true;
                    arrangeUi();
                } else if (fullScreenMode) {
                    // Avoid arrangeUi call when this is triggerd by a context menu click without an actual change.
                    fullScreenMode = false;
                    arrangeUi();
                }
            }
        });

    }

    private BackgroundColorSpan addErrorSpan(Spannable text, ParsingException e) {
        BackgroundColorSpan errorSpan = new BackgroundColorSpan(ERROR_COLOR);

        int errorStart = Math.max(Math.min(e.start, text.length() - 1), 0);
        int errorEnd = Math.min(Math.max(e.end, 1), text.length());

        text.setSpan(errorSpan, errorStart, errorEnd, 0);
        return errorSpan;
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
        detach(errorView);
        rootLayout.removeAllViews();

        getWindowManager().getDefaultDisplay().getSize(displaySize);

        // System.out.println("**** DisplayHeight in DP: " + displayHightDp);

        if (fullScreenMode) {
            rootLayout.addView(contentLayout);
         //   rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);

            FrameLayout.LayoutParams contentParams = (FrameLayout.LayoutParams) contentLayout.getLayoutParams();
            contentParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            contentParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

        } else if (displaySize.x > displaySize.y) {
            LinearLayout columns = new LinearLayout(this);
            rootLayout.addView(columns);
            FrameLayout.LayoutParams columnsParams = (FrameLayout.LayoutParams) columns.getLayoutParams();
            columnsParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            columnsParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

            LinearLayout rows = new LinearLayout(this);
            rows.setOrientation(LinearLayout.VERTICAL);

            rows.addView(toolbar);
            if (!fullScreenEditor) {
                rows.addView(chatView);
                ((LinearLayout.LayoutParams) chatView.getLayoutParams()).weight = 1;
            }
            rows.addView(inputRow);
            ((LinearLayout.LayoutParams) inputRow.getLayoutParams()).weight = fullScreenEditor ? 1: 0;

            columns.addView(rows);
            LinearLayout.LayoutParams rowsParams = (LinearLayout.LayoutParams) rows.getLayoutParams();
            rowsParams.width = 0;
            rowsParams.weight = 0.5f;
            rowsParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

            columns.addView(contentLayout);
            LinearLayout.LayoutParams contentParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
            contentParams.width = 0;
            contentParams.weight = 0.5f;
            contentParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            contentLayout.setBackgroundColor(0xff888888);
        } else {
            LinearLayout rows = new LinearLayout(this);
            rows.setOrientation(LinearLayout.VERTICAL);

            rows.addView(toolbar);

            if (!fullScreenEditor) {
                FrameLayout overlay = new FrameLayout(this);
                overlay.addView(chatView);
                FrameLayout.LayoutParams chatParams = (FrameLayout.LayoutParams) chatView.getLayoutParams();
                chatParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                chatParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

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
                    contentParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    contentParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
            }
            rows.addView(inputRow);
            ((LinearLayout.LayoutParams) inputRow.getLayoutParams()).weight = fullScreenEditor ? 1: 0;

            rootLayout.addView(rows);
            FrameLayout.LayoutParams rowsParams = (FrameLayout.LayoutParams) rows.getLayoutParams();
            rowsParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            rowsParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        float displayHightDp = displaySize.y / pixelPerDp;
        if (displayHightDp < SHOW_TOOLBAR_HEIGHT_DP || windowMode) {
            toolbar.setVisibility(View.GONE);
            menuButton.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.VISIBLE);
            menuButton.setVisibility(View.GONE);
        }
        rootLayout.addView(errorView);

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
        menu.add("Help");

        if (displaySize.x <= displaySize.y) {
            Menu displayMenu = menu.addSubMenu(MENU_ITEM_DISPLAY_MENU);
            displayMenu.add(1, 0,0,MENU_ITEM_OVERLAY_MODE).setCheckable(true).setChecked(!windowMode);
            displayMenu.add(1, 0, 0, MENU_ITEM_WINDOW_MODE).setCheckable(true).setChecked(windowMode);
            displayMenu.add(1, 0, 0, MENU_ITEM_FULLSCREEN);
            displayMenu.setGroupCheckable(1, true, true);
        } else{
            menu.add(MENU_ITEM_FULLSCREEN);
        }

        Menu clearMenu = menu.addSubMenu(MENU_ITEM_CLEAR_MENU);
        clearMenu.add(MENU_ITEM_CLEAR_INPUT);
        clearMenu.add(MENU_ITEM_CLEAR_OUTPUT);
        clearMenu.add(MENU_ITEM_CLEAR_ALL);


        MenuItem suspend = menu.add(environment.isSuspended()? MENU_ITEM_RESUME : MENU_ITEM_SUSPEND);
        if (isOptionsMenu) {
            suspendItem = suspend;
            suspendItem.setIcon(environment.isSuspended() ? R.drawable.ic_play_arrow_white_24dp : R.drawable.ic_pause_white_24dp);
            suspendItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return onMenuItemClick(item);
    }

    private AlertDialog.Builder createDialog(CharSequence text, final AlertDialog[] handle) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        TextView textView = new TextView(MainActivity.this);
        textView.setText(convertAnnotatedCharSequence(text, new Runnable() {
            @Override
            public void run() {
                if (handle != null && handle.length > 0 && handle[0] != null) {
                    handle[0].dismiss();
                }
            }
        }));

        textView.setMovementMethod(LinkMovementMethod.getInstance());

        ScrollView scrollView = new ScrollView(MainActivity.this);
        scrollView.addView(textView);

        scrollView.setPadding(48, 48, 48, 0);
        alert.setView(scrollView);
        return alert;
    }


    private void fillExamplesMenu(Menu exampleMenu) {
        try {
            for (final String fileName : getAssets().list("examples")) {
                final String name = fileName.substring(0, fileName.lastIndexOf('.'));
                exampleMenu.add(name).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        StringBuilder sb = new StringBuilder();
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("examples/" + fileName)));
                            while (true) {
                                String s = reader.readLine();
                                if (s == null) {
                                    break;
                                }
                                sb.append(s);
                                sb.append('\n');
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final String code = sb.toString();
                        final AlertDialog[] alertHandle = new AlertDialog[1];
                        AlertDialog.Builder alert = createDialog(code, alertHandle);
                        alert.setTitle(name);
                        alert.setNegativeButton("Cancel", null);
                        alert.setPositiveButton("Load", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                environment.setProgram(code);
                                environment.environmentListener.setName(name);
                            }
                        });
                        alertHandle[0] = alert.show();
                        return true;
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        String title = item.getTitle().toString();
        switch (title) {
            case "About":
                print(Environment.ABOUT_TEXT, EnvironmentListener.Channel.HELP);
                break;
            case "Help":
                HelpStatement.printGeneralHelp(environment);
                break;
            case MENU_ITEM_SUSPEND:
                environment.suspend();
                break;
            case MENU_ITEM_RESUME:
                environment.resume();
                break;
            case MENU_ITEM_OVERLAY_MODE:
                windowMode = false;
                arrangeUi();
                break;
            case MENU_ITEM_WINDOW_MODE:
                windowMode = true;
                arrangeUi();
                break;
            case MENU_ITEM_FULLSCREEN:
                rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
                arrangeUi();
                break;
            case MENU_ITEM_CLEAR_ALL: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Clear All");
                alert.setMessage("Fully delete the current program?");
                alert.setNegativeButton("Cancel", null);
                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        processInput("clearAll");
                    }
                });
                alert.show();
                break;
            }
            case MENU_ITEM_CLEAR_INPUT:
                input.setText("");
                break;
            case MENU_ITEM_CLEAR_OUTPUT:
                chatView.clear();
                break;
            case MENU_ITEM_DISPLAY_MENU:
            case MENU_ITEM_CLEAR_MENU:
            case MENU_ITEM_EXAMPLES:
                break;
            default:
                processInput(title.toLowerCase());
        }
        return true;
    }

    void printInput(CharSequence s) {
        print(ChatView.BubbleType.RIGHT, s, copyAction, editAction);
    }

    @Override
    public void print(final CharSequence s, Channel channel) {
        if (channel == Channel.HELP) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog[] alertHandle = new AlertDialog[1];
                    AlertDialog.Builder alert = createDialog(s, alertHandle);
                    alert.setPositiveButton("Close", null);
                    alertHandle[0] = alert.show();
                }
            });
        } else {
            print(ChatView.BubbleType.LEFT, s);
        }
    }

    void printException(Exception e) {
        e.printStackTrace();
        if (e instanceof Printable) {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
            ((Printable) e).print(asb, Printable.Flavor.DEFAULT);
            print(asb.build());
        } else if (e instanceof ParsingException) {
            print(e.getMessage());
        } else {
            print(e.toString());
        }
    }

    @Override
    public void showError(CharSequence s) {
        if (s == null) {
            errorView.setVisibility(View.INVISIBLE);
            return;
        }
        errorView.setText(s);
        errorView.setVisibility(View.VISIBLE);
        errorView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        errorView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        ((FrameLayout.LayoutParams) errorView.getLayoutParams()).gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        errorView.requestLayout();
    }

    void print(final CharSequence s, BubbleAction... actions) {
        print(ChatView.BubbleType.LEFT, s);
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

    SpannableString convertAnnotatedCharSequence(final CharSequence s, final Runnable preLinkCallback) {
        SpannableString spannable = new SpannableString(s);
        if (s instanceof AnnotatedCharSequence) {
            for (final AnnotationSpan annotation : ((AnnotatedCharSequence) s).getAnnotations()) {
                if (annotation.getLink() != null) {
                    spannable.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            if (preLinkCallback != null) {
                                preLinkCallback.run();
                            }
                            annotation.getLink().execute(environment);
                        }
                    }, annotation.getStart(), annotation.getEnd(), 0);
                }
            }
        }
        return spannable;
    }


    public void print(final ChatView.BubbleType bubbleType, final CharSequence s, final BubbleAction... actions) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ArrayList<BubbleAction> actionList = new ArrayList<>();
                for (BubbleAction ba : actions) {
                    actionList.add(ba);
                }

                SpannableString spannable = convertAnnotatedCharSequence(s, null);
                AnnotationSpan annotationSpan = null;
                int linkCount = 0;
                if (s instanceof AnnotatedCharSequence) {
                    for (final AnnotationSpan annotation : ((AnnotatedCharSequence) s).getAnnotations()) {
                        if (annotation.getStart() == 0 && annotation.getEnd() == s.length()) {
                            annotationSpan = annotation;
                            linkCount++;
                        }
                    }
                }
                if (actions.length == 0) {
                    actionList.add(copyAction);
                    if (linkCount == 1 && annotationSpan.getLink() instanceof InstanceLink) {
                        final InstanceLink instanceLink = (InstanceLink) annotationSpan.getLink();
                            BubbleAction deleteAction = new BubbleAction(R.drawable.ic_delete_black_24dp, "Delete") {
                                @Override
                                public void invoke(CharSequence text) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Delete '" + s + "'");
                                    final Instance instance = instanceLink.instance.get();
                                    if (instance == null) {
                                        builder.setMessage("This entity has been deleted already.");
                                        builder.setPositiveButton("Ok", null);

                                    } else {
                                        builder.setMessage("Are you sure?");
                                        builder.setNegativeButton("Cancel", null);
                                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                instance.delete();
                                            }
                                        });
                                    }
                                    builder.show();
                                }
                            };
                        BubbleAction editAction = new BubbleAction(R.drawable.ic_edit_black_24dp, "Edit") {
                            @Override
                            public void invoke(CharSequence text) {
                                instanceLink.execute(environment);
                            }
                        };
                        actionList.add(editAction);
                        actionList.add(deleteAction);
                    }
                }
                chatView.add(bubbleType, spannable, actionList.toArray(new BubbleAction[actionList.size()]));
            }
        });
    }

    void processInput(String line) {
        boolean printed = false;
        if (line.length() == 0) {
            print(ChatView.BubbleType.EMPTY, "");
            return;
        }
        try {
            if (line.equals("edit") || line.startsWith("edit ")) {
                String key = line.substring(4).trim();
                SortedMap<String,RootVariable> matches = environment.rootVariables.subMap(key, key + "ZZZZ");

                printInput(line);
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
                ParsingContext parsingContext = new ParsingContext(environment, ParsingContext.Mode.INTERACTIVE);
                Statement statement = environment.parse(parsingContext, line);

                if (statement instanceof ExpressionStatement) {
                    Expression expression = ((ExpressionStatement) statement).expression;
                    String s = expression.toString();
                    printInput(s);
                    printed = true;
                    int count = chatView.getCount();
                    Object result = expression.eval(parsingContext.createEvaluationContext(environment));
                    if (expression.getType() == null) {
                        if (chatView.getCount() == count) {
                            print("ok");
                        }
                    } else {
                        print(Formatting.toLiteral(result), editAction);
                    }
                } else {
                    printInput(statement.toString());
                    printed = true;
                    int count = chatView.getCount();
                    statement.eval(parsingContext.createEvaluationContext(environment));
                    if (chatView.getCount() == count) {
                        print("ok");
                    }
                }

                if (environment.autoSave) {
                    environment.save(toolbar.getTitle().toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!printed) {
                if (e instanceof ParsingException) {
                    ParsingException pe = (ParsingException) e;
                    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
                    asb.append(line);
                    asb.addAnnotation(pe.start, pe.end, new ErrorLink(pe));
                    printInput(asb.build());
                } else {
                    printInput(line);
                }
            }
            printException(e);
        }
    }


    @Override
    public void clearAll() {
        chatView.clear();
    }

    @Override
    public void suspended(boolean suspended) {
        if (suspendItem != null) {
            suspendItem.setIcon(suspended ? R.drawable.ic_play_arrow_white_24dp : R.drawable.ic_pause_white_24dp);
            suspendItem.setTitle(suspended ? MENU_ITEM_RESUME : MENU_ITEM_SUSPEND);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (environment.isSuspended()) {
            environment.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        environment.suspend();
    }

    @Override
    public void setName(String name) {
        toolbar.setTitle(name + (environment.autoSave ? "" : "*"));
        settings.edit().putString(SETTINGS_FILE_NAME, name).commit();
    }

}