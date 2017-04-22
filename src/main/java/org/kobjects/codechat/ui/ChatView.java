package org.kobjects.codechat.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
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

    public ChatView(Context context) {
        super(context);
        setDivider(null);
        setAdapter(chatAdapter);
        setBackgroundColor(0xffeeeedd);
        float dpToPx = context.getResources().getDisplayMetrics().density;

        arrowSize = Math.round(6 * dpToPx);
        cornerBox = Math.round(10 * dpToPx);
        sidePadding = Math.round(5 * dpToPx);
        topPadding = Math.round(2 * dpToPx);
        bottomPadding = Math.round(3 * dpToPx);
        verticalMargin = Math.round(2 * dpToPx);
        narrowHorizontalMarign = Math.round(4 * dpToPx);
        wideHorizontalMarign = Math.round(32 * dpToPx);
    }

    public void add(boolean right, CharSequence s) {
        this.right.set(text.size(), right);
        text.add(s);
        chatAdapter.notifyDataSetChanged();
    }

    public void setValue(int i, CharSequence s) {
        text.set(i, s);
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
                textView.setBackground(new BubbleDrawable(arrowSize, cornerBox, r));
                textView.setPadding(r ? sidePadding : sidePadding + arrowSize, topPadding, r ? sidePadding + arrowSize : sidePadding, bottomPadding);
                textView.setTextColor(0x0ff000000);
              //  textView.setGravity(r ? Gravity.RIGHT : Gravity.LEFT);
                textView.setTextIsSelectable(true);
                result = new LinearLayout(viewGroup.getContext());
                result.setOrientation(LinearLayout.VERTICAL);
                result.addView(textView);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.gravity = r ? Gravity.RIGHT : Gravity.LEFT;
                params.topMargin = verticalMargin;
                params.bottomMargin = verticalMargin;
                params.leftMargin = r ? wideHorizontalMarign : narrowHorizontalMarign;
                params.rightMargin = r ? narrowHorizontalMarign : wideHorizontalMarign;
            }
            textView.setText((CharSequence) getItem(i));
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
