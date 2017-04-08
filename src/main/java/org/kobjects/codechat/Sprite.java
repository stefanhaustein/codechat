package org.kobjects.codechat;

import android.widget.TextView;

public class Sprite implements Ticking {

    TextView view;
    double dx;
    double dy;

    public Sprite(Environment environment) {
        view = new TextView(environment.rootView.getContext());
        view.setText(new String(Character.toChars(0x1F603)));

        view.setTextColor(0x0ff000000);

        view.setX(environment.rootView.getMeasuredWidth() / 2);
        view.setY(environment.rootView.getMeasuredHeight() / 3);

        environment.rootView.addView(view);
    }



    public void move(double x, double y) {
        view.setX((float) x);
        view.setY((float) y);
    }

    public double getX() {
        return view.getX();
    }

    public double getY() {
        return view.getY();
    }

    public double getDx() {
        return dx;
    }
    public double getDy() {
        return dy;
    }

    public void setX(double x) {
        view.setX((float) x);
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void setY(double y) {
        view.setY((float) y);
    }

    public void setSize(double s) {
        view.setTextSize((float) s);
    }

    public void setFace(Emoji emoji) {
        view.setText(emoji.toString());
    }

    public Emoji getFace() {
        return new Emoji(view.getText().toString());
    }

    @Override
    public void tick() {
        if (dx != 0 || dy != 0) {
            setX(getX() + dx);
            setY(getY() + dy);
        }
    }
}
