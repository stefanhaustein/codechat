package org.kobjects.codechat.android.chatview;

public abstract class BubbleAction {
    final int resId;
    final String label;

    protected BubbleAction(int resId, String label) {
        this.resId = resId;
        this.label = label;
    }

    public abstract void invoke(CharSequence text);

}
