package org.kobjects.codechat.api;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;

public class Sprite extends Instance implements Ticking {
    ImageView view;
    double dx;
    double dy;
    double size;
    double x;
    double y;
    double rotation;
    boolean touched;
    private Emoji face;

    public Sprite(Environment environment, int id) {
        super(environment, id);
        view = new ImageView(environment.rootView.getContext());
        view.setAdjustViewBounds(true);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    touched = true;
                    return true;
                }
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    touched = false;
                    return true;
                }
                return false;
            }
        });
        environment.rootView.addView(view);

        setFace(new Emoji(0x1f603));
        setSize(100);
        setX(500);
        setY(500);
    }

    public void move(double x, double y) {
        setX(x);
        setY(y);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    public double getSize() { return size; }
    public Emoji getFace() { return face; }

    public void setX(double x) {
        this.x = x;
        view.setX((float) (environment.scale * (x - size / 2)));
    }

    public void setY(double y) {
        this.y = y;
        view.setY(environment.rootView.getMeasuredHeight() - (float) (environment.scale * (y + size / 2)));
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void setRotation(double r) {
        this.rotation = r;
        view.setRotation((float) r);
    }

    public double getRotation() {
        return rotation;
    }

    public void setSize(double s) {
        size = s;

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = params.height = Math.round((float) (environment.scale * size));
        setX(x);
        setY(y);
    }

    public void setTouched(boolean value) {
        touched = value;
    }

    public boolean getTouched() {
        return touched;
    }


    public void setFace(Emoji emoji) {
        view.setImageDrawable(emoji.getDrawable(view.getContext()));
    }
/*
    public Emoji getFace() {
        return new Emoji(view.getText().toString());
    }
*/
    @Override
    public void tick(boolean force) {
        if (force || dx != 0 || dy != 0) {
            setX(getX() + dx);
            setY(getY() + dy);
        }
    }

    public void delete() {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
        environment.ticking.remove(this);
    }


}
