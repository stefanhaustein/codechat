package org.kobjects.codechat.android.chatview;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import com.vanniktech.emoji.EmojiTextView;
import java.util.ArrayList;
import java.util.BitSet;
import org.kobjects.codechat.android.R;

public class ChatView extends ListView {

    public enum BubbleType {
        EMPTY, LEFT, RIGHT,
    }

    ArrayList<Entry> entries = new ArrayList<>();
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


    public void add(BubbleType type, CharSequence text, BubbleAction... actions) {
        entries.add(new Entry(type, text, actions));
        chatAdapter.notifyDataSetChanged();
    }

    public void clear() {
        entries.clear();
        chatAdapter.notifyDataSetChanged();
    }

    class ChatAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return entries.size();
        }

        @Override
        public Object getItem(int i) {
            return entries.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, final ViewGroup viewGroup) {
            final Entry entry = (Entry) getItem(i);
            if (entry.bubbleType == BubbleType.EMPTY) {
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
            final ImageView imageView;
            if (view instanceof LinearLayout) {
                result = (LinearLayout) view;
                textView = (EmojiTextView) result.getChildAt(entry.bubbleType == BubbleType.LEFT ? 0 : result.getChildCount() - 1);
                imageView = (ImageView) result.getChildAt(1);
            } else {
                textView = new EmojiTextView(viewGroup.getContext());
                final boolean right = entry.bubbleType == BubbleType.RIGHT;
                textView.setBackground(new BubbleDrawable(arrowSize, cornerBox, right));
                textView.setPadding(right ? sidePadding : sidePadding + arrowSize, topPadding, right ? sidePadding + arrowSize : sidePadding, bottomPadding);
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
                params.gravity = right ? Gravity.RIGHT : Gravity.LEFT;
                params.topMargin = verticalMargin;
                params.bottomMargin = verticalMargin;
                params.rightMargin = narrowHorizontalMarign;
                params.leftMargin = narrowHorizontalMarign;
              //  params.leftMargin = r ? wideHorizontalMarign : narrowHorizontalMarign;
              //  params.rightMargin = r ? narrowHorizontalMarign : wideHorizontalMarign;
                textView.setTextIsSelectable(true);
                textView.setMovementMethod(LinkMovementMethod.getInstance());

                imageView = new ImageView(viewGroup.getContext());
                // editButton.setImageResource(bubbleActionResId);
                imageView.setAlpha(0.5f);
                result.addView(imageView, right ? 0 : 1);

                View emptyView = new View(viewGroup.getContext());
                result.addView(emptyView, right ?  0 : result.getChildCount());
                ((LinearLayout.LayoutParams) emptyView.getLayoutParams()).weight = 100;

            }
            CharSequence cs = ((Entry) getItem(i)).text;
            if (cs == null) {
                System.err.println("text is null for entry " + i + ": " + getItem(i));
                cs = "";
            }
            int cut = cs.length();
            while (cut > 0 && cs.charAt(cut-1) == '\n') {
                cut--;
            }
            textView.setText(cs.subSequence(0, cut));

            if (entry.actions == null || entry.actions.length == 0) {
                imageView.setImageBitmap(null);
                imageView.setOnClickListener(null);
            } else if (entry.actions.length == 1) {
                imageView.setImageResource(entry.actions[0].resId);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        entry.actions[0].invoke(entry.text);
                    }
                });
            } else {
                imageView.setImageResource(R.drawable.ic_more_vert_black_24dp);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(getContext(), imageView);
                        for (final BubbleAction action : entry.actions) {
                            popupMenu.getMenu().add(action.label).setIcon(action.resId).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                   action.invoke(entry.text);
                                   return true;
                                }
                            });
                        }
                    }
                });

            }
            return result;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        public int getItemViewType(int position) {
            return entries.get(position).bubbleType.ordinal();
        }
    }

    static class Entry {
        final BubbleType bubbleType;
        final CharSequence text;
        final BubbleAction[] actions;

        Entry(BubbleType bubbleType, CharSequence text, BubbleAction... actions) {
            this.bubbleType = bubbleType;
            this.text = text;
            this.actions = actions;
        }
    }


}
