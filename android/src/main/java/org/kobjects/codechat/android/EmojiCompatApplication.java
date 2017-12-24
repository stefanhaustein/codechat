package org.kobjects.codechat.android;

import android.app.Application;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiUtils;
// import com.vanniktech.emoji.googlecompat.GoogleCompatEmojiProvider;
import com.vanniktech.emoji.one.EmojiOneProvider;
//import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

public class EmojiCompatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final EmojiCompat.Config config = new BundledEmojiCompatConfig(getApplicationContext());
        config.setReplaceAll(true);
        //EmojiManager.install(new GoogleCompatEmojiProvider(EmojiCompat.init(config)));



        EmojiManager.install(new EmojiOneProvider());

    }

}
