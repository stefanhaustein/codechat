package org.kobjects.codechat;

import android.widget.TextView;

public class Sprite implements Ticking {
    Environment environment;
    TextView view;
    double dx;
    double dy;
    double size;
    double x;
    double y;

    public Sprite(Environment environment) {
        this.environment = environment;
        view = new TextView(environment.rootView.getContext());
        view.setText(new String(Character.toChars(0x1F603)));

        view.setTextColor(0x0ff000000);

        setSize(50);
        setX(500);
        setY(500);

        environment.rootView.addView(view);
    }

    public void move(double x, double y) {
        setX(x);
        setY(y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDx() {
        return dx;
    }
    public double getDy() {
        return dy;
    }

    public void setX(double x) {
        this.x = x;
        view.setX((float) (environment.scale * (x - size / 2)));
    }

    public void setY(double y) {
        this.y = y;
        view.setY((float) (environment.scale * (y - size / 2)));
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void setSize(double s) {
        size = s;
        view.setTextSize((float) (s * environment.scale));
        setX(x);
        setY(y);
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

    @Override
    public String toString() {
        return "sprite x:" + getX() + " y:" + getY() + " dx:" + dx + " dy:" + dy + " viewX:" + view.getX() + " viewY:" + view.getY();
    }
}
