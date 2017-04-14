package org.kobjects.codechat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.BitSet;

import static android.graphics.PixelFormat.TRANSLUCENT;

public class ChatView extends ListView {
    ArrayList<String> text = new ArrayList<>();
    BitSet right = new BitSet();
    ChatAdapter chatAdapter = new ChatAdapter();

    public ChatView(Context context) {
        super(context);
        setDivider(null);
        setAdapter(chatAdapter);
    }

    public void addRight(String s) {
        right.set(text.size(), true);
        text.add(s);
        chatAdapter.notifyDataSetChanged();
    }

    public void addLeft(String s) {
        text.add(s);
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView result;
            if (view instanceof TextView) {
                result = (TextView) view;
            } else {
                result = new TextView(viewGroup.getContext());
                // result.setTextColor(Color.BLACK);
                // result.setTextSize(20);

                boolean r = right.get(i);
                result.setBackground(new BubbleDrawable(r));
                result.setPadding(20 + (r ? 40 : 0), 20, 20 + (r ? 0 : 40), 20);
            }
            result.setText(String.valueOf(getItem(i)));
            return result;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        public int getItemViewType(int position) {
            return right.get(position) ? 0 : 1;
        }
    }

    static class BubbleDrawable extends Drawable {

        boolean right;
        BubbleDrawable(boolean right) {
            this.right = right;
        }

        Paint paint = new Paint();

        @Override
        public void draw(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            RectF bounds = new RectF(getBounds());
            if (right) {
                paint.setColor(0xff88ff88);
                bounds.left += 40;
            } else {
                bounds.right -= 40;
                paint.setColor(0xff888888);
            }
            canvas.drawRoundRect(bounds, 20, 20 , paint);
        }

        @Override
        public void setAlpha(int i) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return TRANSLUCENT;
        }

    }

}
