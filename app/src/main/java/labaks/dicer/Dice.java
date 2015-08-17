package labaks.dicer;

import android.widget.ImageButton;

public class Dice {
    boolean isLeft;
    ImageButton diceButton;
    int value;

    boolean switchIsDiceLeft() {
        return isLeft = !isLeft;
    }
}
