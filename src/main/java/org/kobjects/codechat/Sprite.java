package org.kobjects.codechat;

import android.widget.TextView;

public class Sprite implements Ticking {
    Environment environment;
    TextView view;
    double dx;
    double dy;

    public Sprite(Environment environment) {
        this.environment = environment;
        view = new TextView(environment.rootView.getContext());
        view.setText(new String(Character.toChars(0x1F603)));

        view.setTextColor(0x0ff000000);

        setX(500);
        setY(500);
        setSize(50);

        environment.rootView.addView(view);
    }

    public void move(double x, double y) {
        setX(x);
        setY(y);
    }

    public double getX() {
        return (view.getX() + view.getMeasuredWidth() / 2) / environment.scale;
    }

    public double getY() {
        return (view.getY() + view.getMeasuredHeight() / 2) / environment.scale;
    }

    public double getDx() {
        return dx;
    }
    public double getDy() {
        return dy;
    }

    public void setX(double x) {
        view.setX((float) (environment.scale * x - view.getMeasuredWidth() / 2));
    }

    public void setY(double y) {
        view.setY((float) (environment.scale * y - view.getMeasuredHeight() / 2));
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void setSize(double s) {
        view.setTextSize((float) (s * environment.scale));
    }

    public void setFace(Emoji emoji) {
        view.setText(emoji.toString());
    }

    public Emoji getFace() {
        return new Emoji(view.getText().toString());
    }

    @Override
    public void tick(boolean force) {
        if (force || dx != 0 || dy != 0) {
            setX(getX() + dx);
            setY(getY() + dy);
        }
    }
}
