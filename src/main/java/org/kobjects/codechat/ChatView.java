package org.kobjects.codechat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
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
        setBackgroundColor(0xffeeeedd);
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
            LinearLayout result;
            TextView textView;
            if (view instanceof LinearLayout) {
                result = (LinearLayout) view;
                textView = (TextView) result.getChildAt(0);
            } else {
                textView = new TextView(viewGroup.getContext());
                boolean r = right.get(i);
                textView.setBackground(new BubbleDrawable(16, 24, r));
                textView.setPadding(r ? 20 : 36, 6, r ? 36 : 20, 10);
                textView.setTextColor(0x0ff000000);
                textView.setGravity(r ? Gravity.RIGHT : Gravity.LEFT);
                result = new LinearLayout(viewGroup.getContext());
                result.setOrientation(LinearLayout.VERTICAL);
                result.addView(textView);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.gravity = r ? Gravity.RIGHT : Gravity.LEFT;
                params.topMargin = 10;
                params.bottomMargin = 10;
                params.leftMargin = r ? 160 : 48;
                params.rightMargin = r ? 48 : 160;
            }
            textView.setText(String.valueOf(getItem(i)));
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
        float cornerBox;
        float arrowSize;
        BubbleDrawable(float arrowSize, float cornerBox, boolean right) {
            this.arrowSize = arrowSize;
            this.cornerBox = cornerBox;
            this.right = right;
        }

        Paint paint = new Paint();

        @Override
        public void setBounds(Rect bounds) {
            super.setBounds(bounds);
        }

        @Override
        public void draw(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            RectF bounds = new RectF(getBounds());
            if (right) {
                paint.setColor(0xffC5CAE9);
            } else {
                paint.setColor(0xffffffff);
            }
          //  canvas.drawRoundRect(bounds, 16, 16 , paint);

            Path path = new Path();
            RectF arcBox = new RectF();

            if (right) {
                path.moveTo(bounds.right, bounds.top);
                arcBox.set(bounds.left, bounds.top, bounds.left + cornerBox, bounds.top + cornerBox);
                path.arcTo(arcBox, 270, -90, false);
                arcBox.set(bounds.left, bounds.bottom - cornerBox, bounds.left + cornerBox, bounds.bottom);
                path.arcTo(arcBox, 180, -90, false);
                arcBox.set(bounds.right - cornerBox - arrowSize, bounds.bottom - cornerBox, bounds.right - arrowSize, bounds.bottom);
                path.arcTo(arcBox, 90, -90, false);
                path.lineTo(bounds.right - arrowSize, bounds.top + arrowSize);
            } else {
                path.moveTo(bounds.left, bounds.top);
                arcBox.set(bounds.right - cornerBox, bounds.top, bounds.right, bounds.top + cornerBox);
                path.arcTo(arcBox, 270, 90, false);
                arcBox.set(bounds.right - cornerBox, bounds.bottom - cornerBox, bounds.right, bounds.bottom);
                path.arcTo(arcBox, 0, 90, false);
                arcBox.set(bounds.left + arrowSize, bounds.bottom - cornerBox, bounds.left + cornerBox + arrowSize, bounds.bottom);
                path.arcTo(arcBox, 90, 90, false);
                path.lineTo(bounds.left + arrowSize, bounds.top + arrowSize);
            }
            path.close();

        /*    Path path = new Path();
            path.moveTo(bounds.right, bounds.top + 20);
            path.lineTo(bounds.right + 20, bounds.top);
            path.lineTo(bounds.right - 20, bounds.top);*/
            canvas.drawPath(path, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xffcccccc);
         //   canvas.drawRoundRect(bounds, 16, 16 , paint);
            canvas.drawPath(path, paint);
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
