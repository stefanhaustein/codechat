package org.kobjects.codechat.android;


public class AndroidBuiltins {
    AndroidEnvironment environment;
    AndroidBuiltins(AndroidEnvironment environment) {
        this.environment = environment;
    }

    public void move(Sprite sprite, double speed, double angle) {
        sprite.move(speed, angle);
    }

}
