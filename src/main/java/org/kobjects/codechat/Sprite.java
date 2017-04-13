package org.kobjects.codechat;

import android.app.ActionBar;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class Sprite extends Instance implements Ticking {
    ImageView view;
    double dx;
    double dy;
    double size;
    double x;
    double y;

    public Sprite(Environment environment, int id) {
        super(environment, id);
        view = new ImageView(environment.rootView.getContext());
        view.setAdjustViewBounds(true);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
        view.setY(environment.rootView.getMeasuredHeight() - (float) (environment.scale * (y + size / 2)));
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void setSize(double s) {
        size = s;

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = params.height = Math.round((float) (environment.scale * size));
        setX(x);
        setY(y);
    }

    public void setFace(Emoji emoji) {
        try
        {
            InputStream is = view.getContext().getAssets().open("emoji/png_128/" + Integer.toHexString(emoji.codepoint) + ".png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(is, null);
            // set image to ImageView
            view.setImageDrawable(d);
            is.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
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

}
