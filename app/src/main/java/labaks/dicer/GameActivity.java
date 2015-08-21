package labaks.dicer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class GameActivity extends Activity {

    private static final int NUMBER_OF_DICES = 5;
    private final int DICE_SIDES = 6;
    public final int FIRST_PLAYER_SHIFT = 0;
    public final int SECOND_PLAYER_SHIFT = 5;
    private TextView[] diceInfo = new TextView[NUMBER_OF_DICES]; // log
    private TextView massInfo, combinationInfo1, combinationInfo2, doubleResultOutput1, doubleResultOutput2, log, winner;
    private Bitmap[] croppedDiceImage = new Bitmap[DICE_SIDES];
    private final static DisplayMetrics metrics = new DisplayMetrics();

    Player firstPlayer;
    Player AIPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        firstPlayer = new Player(DICE_SIDES, NUMBER_OF_DICES);
        AIPlayer = new Player(DICE_SIDES, NUMBER_OF_DICES);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

//        initDicesInfo(); // log

        final int diceWidth = (metrics.widthPixels - 20) / NUMBER_OF_DICES;
        cropDiceSides();
        initDicesImage(croppedDiceImage, diceWidth, firstPlayer, FIRST_PLAYER_SHIFT);
        initDicesImage(croppedDiceImage, diceWidth, AIPlayer, SECOND_PLAYER_SHIFT);

        massInfo = (TextView) findViewById(R.id.massInfo);
        combinationInfo1 = (TextView) findViewById(R.id.combination);
        doubleResultOutput1 = (TextView) findViewById(R.id.doubleResult);
        combinationInfo2 = (TextView) findViewById(R.id.combination2);
        doubleResultOutput2 = (TextView) findViewById(R.id.doubleResult2);
        log = (TextView) findViewById(R.id.log);
        winner = (TextView) findViewById(R.id.winner);


        final Button dropTheDices = (Button) findViewById(R.id.dropDice);
        dropTheDices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstPlayer.dropAllowedDices();
                firstPlayer.increaseValueCounter();
                firstPlayer.hasCombinations();
                firstPlayer.resultToNumber();
                printInfo(firstPlayer, combinationInfo1, doubleResultOutput1);

                AIPlayer.dropAllowedDices();
                AIPlayer.increaseValueCounter();
                AIPlayer.hasCombinations();
                AIPlayer.resultToNumber();
                printInfo(AIPlayer, combinationInfo2, doubleResultOutput2);
//                log(); //Метод для проверки значений
                defineWinner(firstPlayer, AIPlayer);
                AIPlayer.resetValues();
                firstPlayer.resetValues();
            }
        });
    }


    private void initDicesInfo() { // log
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            diceInfo[i] = (TextView) findViewById(getBaseContext().getResources().getIdentifier("diceInfo" + (i + 1), "id", getBaseContext().getPackageName()));
        }
    }

    private void initDicesImage(Bitmap[] background, int diceWidth, Player player, int shift) {
        Dice[] dicesImage = new Dice[NUMBER_OF_DICES];
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            dicesImage[i] = new Dice();
            dicesImage[i].diceButton = (ImageButton) findViewById(getBaseContext().getResources().getIdentifier("diceImage" + (i + 1 + shift), "id", getBaseContext().getPackageName()));
            dicesImage[i].diceButton.setMaxWidth(diceWidth);
            dicesImage[i].diceButton.setImageBitmap(background[i]);
        }
        player.dices = dicesImage;
    }

    private void cropDiceSides() {
        Bitmap bitmapDice = BitmapFactory.decodeResource(this.getResources(), R.drawable.dices_sides);
        for (int i = 0; i < DICE_SIDES; i++) {
            croppedDiceImage[i] = Bitmap.createBitmap(bitmapDice, i * 1114, 0, 1114, 1114);
        }
    }

    public void onDiceLeft(View view) {
        firstPlayer.dices = onDiceLeftImpl(view.getId(), firstPlayer);
    }
    public void onDiceLeft2(View view) {
        AIPlayer.dices = onDiceLeftImpl(view.getId(), AIPlayer);
    }


    private static Dice[] onDiceLeftImpl(int id, Player player) {
        Dice[] dices = player.dices;
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            if (dices[i].diceButton.getId() == id) {
                if (!dices[i].isLeft) {
                    dices[i].diceButton.setBackgroundColor(Color.BLACK);
                    dices[i].switchIsDiceLeft();
                } else {
                    dices[i].diceButton.setBackgroundColor(Color.WHITE);
                    dices[i].switchIsDiceLeft();
                }
            }
        }
        return dices;
    }

    public void defineWinner(Player player1, Player player2) {
        if (player1.doubleResult > player2.doubleResult) {
            winner.setText(getString(R.string.first_player_win));
        } else if (player1.doubleResult < player2.doubleResult){
            winner.setText(getString(R.string.second_player_win));
        } else {
            winner.setText(getString(R.string.dead_heat));
        }
    }

    public void printInfo(Player player, TextView combinationInfo, TextView doubleResultOutput) {
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
//            diceInfo[i].setText(                  // log
//                    getString(getBaseContext().getResources().getIdentifier("dice_" + (i + 1), "string", getBaseContext().getPackageName())) +
//                            ": " + Integer.toString(player.dices[i].value));
            player.dices[i].diceButton.setImageBitmap(croppedDiceImage[player.dices[i].value - 1]);
        }
        outputResult(player, combinationInfo);
//        doubleResultOutput.setText(getString(R.string.numericalResult) + Double.toString(player.doubleResult));
    }

    public void printDroppedValuesCounts(int droppedValuesCounts[]) {
        StringBuilder builder = new StringBuilder();
        String separator = " | ";
        for (int i = 0; i < droppedValuesCounts.length; i++) {
            builder.append(i + 1).append(": ").append(Integer.toString(droppedValuesCounts[i])).append(separator);
        }
        massInfo.setText(builder.toString());
    }

    public void outputResult(Player player, TextView combinationInfo) {
        StringBuilder builder = new StringBuilder();
        String space = " ";
        if (player.hasPoker) {
            builder.append(getString(R.string.you_have_poker)).append(space).append(getString(R.string.of)).append(space).append(player.pokerValue);
            combinationInfo.setText(builder.toString());
        } else if (player.hasFour) {
            builder.append(getString(R.string.you_have_four_of_a_kind)).append(space).append(getString(R.string.of)).append(space).append(player.fourValue);
            combinationInfo.setText(builder.toString());
        } else if (player.hasFullHouse) {
            builder.append(getString(R.string.you_have_full_house)).append(space).append(getString(R.string.of)).append(space).append(player.fullHouseValue[0]).append(space).append(getString(R.string.and)).append(space).append(player.fullHouseValue[1]);
            combinationInfo.setText(builder.toString());
        } else if (player.hasBigStrait) {
            builder.append(getString(R.string.you_have_big_strait));
            combinationInfo.setText(builder.toString());
        } else if (player.hasLittleStrait) {
            builder.append(getString(R.string.you_have_little_strait));
            combinationInfo.setText(builder.toString());
        } else if (player.hasThree) {
            builder.append(getString(R.string.you_have_three_of_a_kind)).append(space).append(getString(R.string.of)).append(space).append(player.threeValue);
            combinationInfo.setText(builder.toString());
        } else if (player.hasTwoPair) {
            builder.append(getString(R.string.you_have_two_pair)).append(space).append(getString(R.string.of)).append(space).append(player.twoPairValue[0]).append(space).append(getString(R.string.and)).append(space).append(player.twoPairValue[1]);
            combinationInfo.setText(builder.toString());
        } else if (player.hasPair) {
            builder.append(getString(R.string.you_have_one_pair)).append(space).append(getString(R.string.of)).append(space).append(player.pairValue);
            combinationInfo.setText(builder.toString());
        } else if (player.hasNoComb) {
            builder.append(getString(R.string.you_have_no_combinations));
            combinationInfo.setText(builder.toString());
        }
    }

    public void log() { //Метод для проверки значений
        printDroppedValuesCounts(firstPlayer.droppedValuesCount);
        StringBuilder builder = new StringBuilder();
        boolean isLeftMass[] = new boolean[NUMBER_OF_DICES];
        StringBuilder builder1 = new StringBuilder();
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            isLeftMass[i] = firstPlayer.dices[i].isLeft;
        }
        for (boolean m : isLeftMass) {
            builder1.append(" | " + m);
        }

        builder.append("hasNoComb: " + firstPlayer.hasNoComb + "\n" +
                "hasPair: " + firstPlayer.hasPair + "\n" +
                "hasTwoPair: " + firstPlayer.hasTwoPair + "\n" +
                "hasThree: " + firstPlayer.hasThree + "\n" +
                "hasLittleStrait: " + firstPlayer.hasLittleStrait + "\n" +
                "hasBigStrait: " + firstPlayer.hasBigStrait + "\n" +
                "hasFullHouse: " + firstPlayer.hasFullHouse + "\n" +
                "hasFour: " + firstPlayer.hasFour + "\n" +
                "hasPoker: " + firstPlayer.hasPoker + "\n" +
                builder1);
        log.setText(builder.toString());
    }


}