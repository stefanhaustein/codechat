package org.kobjects.codechat.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.LinkedHashSet;
import org.kobjects.codechat.android.api.gpio.DigitalOutput;
import org.kobjects.codechat.android.api.gpio.Gpio;
import org.kobjects.codechat.android.api.sound.Sound;
import org.kobjects.codechat.android.api.sound.SampleManager;
import org.kobjects.codechat.android.api.ui.EdgeMode;
import org.kobjects.codechat.android.api.ui.Screen;
import org.kobjects.codechat.android.api.ui.Sprite;
import org.kobjects.codechat.android.api.ui.Text;
import org.kobjects.codechat.android.api.ui.XAlign;
import org.kobjects.codechat.android.api.ui.YAlign;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.LoadExampleLink;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.NativeFunction;
import org.kobjects.codechat.type.Type;

public class AndroidEnvironment extends Environment implements Runnable {

    private static final String[] SOUND_EXTENSIONS = {".mp3", ".wav"};
    public FrameLayout rootView;
    public LinkedHashSet<Ticking> ticking = new LinkedHashSet<>();
    public Screen screen;
    public double scale;
    Handler handler = new Handler();
    public final Context context;


    public AndroidEnvironment(EnvironmentListener environmentListener, FrameLayout rootView, File codeDir) {
        super(environmentListener, codeDir);
        this.rootView = rootView;
        this.context = rootView.getContext();
        this.screen = new Screen(this);
        handler.postDelayed(this, 100);
        final SampleManager soundManager = new SampleManager(context);

        addSystemConstant("Screen", Screen.TYPE, new AnnotatedStringBuilder()
                .append("The screen object contains information about the visible device screen such as the dimensions. The ")
                .append("bounce example", new LoadExampleLink("Bounce"))
                .append(" illustrates using several of the screen properties.").build());
        addSystemConstant("Sprite", Sprite.TYPE,
                "A sprite is an emoji displayed on a particular position on the screen. "
                + "It is able to move and rotate at a given speed by setting the corresponding properties.");
        addSystemConstant("Text", Text.TYPE, "A text object displayed on the screen.");
        addSystemConstant("XAlign", XAlign.TYPE, null);
        addSystemConstant("YAlign", YAlign.TYPE, null);
        addSystemConstant("EdgeMode", EdgeMode.TYPE, null);
        addSystemConstant("Sensors", Sensors.TYPE, null);

        addSystemConstant("screen", screen, null);
        addSystemConstant("sensors", new Sensors(this, rootView.getContext()), null);
        addSystemConstant("play", new NativeFunction( null, Type.STRING) {
                    @Override
                    protected Object eval(Object[] params) {
                        new Sound(soundManager, (String) params[0]).play();
                        return null;
                    }
                }, "Plays the given sound");


        boolean androidThings = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_EMBEDDED);
        if (androidThings) {
            addSystemConstant("DigitalOutput", DigitalOutput.TYPE, "A pin configured for digital output");
            addSystemConstant("Gpio", Gpio.TYPE, "The gpio object contains information about the periperal hardware io pins available.");
            addSystemConstant("gpio", new Gpio(this), "The gpio object contains information about the periperal hardware io pins available.");
        }
    }

    @Override
    public void run() {
        int width = rootView.getWidth();
        int height = rootView.getHeight();
        boolean force = screen.update(width, height);
        scale = Math.min(width, height) / 100f;
        if (!isSuspended()) {
           for (Object sprite : screen.sprites.get()) {
                try {
                    ((Sprite) sprite).tick(0.017, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // TODO: Use Choreographer instead?
        handler.postDelayed(this, 17);
    }

    @Override
    public void clearAll() {
        screen.clearAll();
        super.clearAll();
        if (rootView != null) {
            for (int i = rootView.getChildCount() - 1; i >= 0; i--) {
                View child = rootView.getChildAt(i);
                if (child instanceof ImageView) {
                    rootView.removeViewAt(i);
                }
            }
        }
    }
}
