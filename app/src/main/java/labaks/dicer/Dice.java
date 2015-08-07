package labaks.dicer;

import android.widget.ImageButton;

public class Dice {
    boolean isLeft;
    ImageButton diceButton;

    boolean switchDiceLefted() {
        return isLeft = !isLeft;
    }
}
