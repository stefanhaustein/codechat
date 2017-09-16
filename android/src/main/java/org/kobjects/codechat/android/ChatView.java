package org.kobjects.codechat.android;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.vanniktech.emoji.EmojiTextView;
import java.util.ArrayList;
import java.util.BitSet;

public class ChatView extends ListView {

    static final int VIEW_ITEM_TYPE_EMPTY = 0;
    static final int VIEW_ITEM_TYPE_LEFT = 1;
    static final int VIEW_ITEM_TYPE_RIGHT = 2;

    ArrayList<CharSequence> text = new ArrayList<>();
    BitSet right = new BitSet();
    ChatAdapter chatAdapter = new ChatAdapter();
    private final int topPadding;
    private final int bottomPadding;
    private final int arrowSize;
    private final int cornerBox;
    private final int sidePadding;
    private final int verticalMargin;
    private final int narrowHorizontalMarign;
    private final int wideHorizontalMarign;
    private final float dpToPx;
    private int bubbleActionResId;
    private BubbleAction bubbleAction;

    public ChatView(Context context) {
        super(context);
        setDivider(null);
        setAdapter(chatAdapter);
        setBackgroundColor(0xffeeeedd);
        dpToPx = context.getResources().getDisplayMetrics().density;

        arrowSize = Math.round(6 * dpToPx);
        cornerBox = Math.round(10 * dpToPx);
        sidePadding = Math.round(5 * dpToPx);
        topPadding = Math.round(2 * dpToPx);
        bottomPadding = Math.round(3 * dpToPx);
        verticalMargin = Math.round(2 * dpToPx);
        narrowHorizontalMarign = Math.round(4 * dpToPx);
        wideHorizontalMarign = Math.round(32 * dpToPx);

        setStackFromBottom(true);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
        setTranscriptMode(TRANSCRIPT_MODE_ALWAYS_SCROLL);

        //setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
    }

    public void setBubbleAction(int bubbleActionResId, BubbleAction bubbleAction) {
        this.bubbleActionResId = bubbleActionResId;
        this.bubbleAction = bubbleAction;
    }

    public void add(boolean right, CharSequence s) {
        this.right.set(text.size(), right);
        text.add(s == null ? "" : s);
        chatAdapter.notifyDataSetChanged();
    }

    class ChatAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return text.size();
        }

        @Override
        public Object getItem(int i) {
            return text.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, final ViewGroup viewGroup) {
            final int type = getItemViewType(i);
            if (type == 0) {
                if (view != null) {
                    return view;
                }
                View result = new View(viewGroup.getContext()) {
                    @Override
                    protected void onMeasure(int wms, int hms) {
                        setMeasuredDimension(1, Math.round(36 * dpToPx));
                    }
                };
                return result;
            }

            final LinearLayout result;
            final EmojiTextView textView;
            if (view instanceof LinearLayout) {
                result = (LinearLayout) view;
                textView = (EmojiTextView) result.getChildAt(type == VIEW_ITEM_TYPE_LEFT ? 0 : result.getChildCount() - 1);
            } else {
                textView = new EmojiTextView(viewGroup.getContext());
                final boolean r = type == 2;
                textView.setBackground(new BubbleDrawable(arrowSize, cornerBox, r));
                textView.setPadding(r ? sidePadding : sidePadding + arrowSize, topPadding, r ? sidePadding + arrowSize : sidePadding, bottomPadding);
                textView.setTextColor(0x0ff000000);
//                textView.setTextIsSelectable(true);
              //  textView.setGravity(r ? Gravity.RIGHT : Gravity.LEFT);
                result = new LinearLayout(viewGroup.getContext());
             /*   textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectionCallback.selected(r, textView.getText().toString());
                    }
                }); */
            //    textView.setFocusableInTouchMode(true);
               textView.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                       result.setBackgroundColor(b ? 0x011000000 : 0);
                    }
                });

                //  textView.setFocusableInTouchMode(false);
           //     result.setOrientation(LinearLayout.VERTICAL);
                result.addView(textView);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.gravity = r ? Gravity.RIGHT : Gravity.LEFT;
                params.topMargin = verticalMargin;
                params.bottomMargin = verticalMargin;
                params.rightMargin = narrowHorizontalMarign;
                params.leftMargin = narrowHorizontalMarign;
              //  params.leftMargin = r ? wideHorizontalMarign : narrowHorizontalMarign;
              //  params.rightMargin = r ? narrowHorizontalMarign : wideHorizontalMarign;
                textView.setTextIsSelectable(true);
                textView.setMovementMethod(LinkMovementMethod.getInstance());

                if (bubbleAction != null) {
                    ImageView editButton = new ImageView(viewGroup.getContext());
                    editButton.setImageResource(bubbleActionResId);
                    editButton.setAlpha(0.5f);
                    editButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bubbleAction.clicked(textView.getText());
                        }
                    });
                    result.addView(editButton, type == VIEW_ITEM_TYPE_LEFT ? 1 : 0);
                }

                View emptyView = new View(viewGroup.getContext());
                result.addView(emptyView, type == VIEW_ITEM_TYPE_LEFT ? result.getChildCount() : 0);
                ((LinearLayout.LayoutParams) emptyView.getLayoutParams()).weight = 100;

            }
            CharSequence cs = (CharSequence) getItem(i);
            int cut = cs.length();
            while (cut > 0 && cs.charAt(cut-1) == '\n') {
                cut--;
            }
            textView.setText(cs.subSequence(0, cut));

            return result;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        public int getItemViewType(int position) {
            return text.get(position).length() == 0 ? VIEW_ITEM_TYPE_EMPTY : right.get(position) ? VIEW_ITEM_TYPE_RIGHT : VIEW_ITEM_TYPE_LEFT;
        }
    }

    interface BubbleAction {
        void clicked(CharSequence text);
    }

}
