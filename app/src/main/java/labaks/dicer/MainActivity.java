package labaks.dicer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private static final int NUMBER_OF_DICES = 5;
    private final int DICE_SIDES = 6;
    private TextView[] diceInfo = new TextView[NUMBER_OF_DICES];
    private TextView massInfo, combinationInfo, doubleResultOutput;
    private Bitmap[] croppedDiceImage = new Bitmap[DICE_SIDES];
    private final static DisplayMetrics metrics = new DisplayMetrics();

    Player firstPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstPlayer = new Player(DICE_SIDES, NUMBER_OF_DICES);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        initDicesInfo();

        final int diceWidth = (metrics.widthPixels - 20) / NUMBER_OF_DICES;
        cropDiceSides();
        initDicesImage(croppedDiceImage, diceWidth, firstPlayer);
        massInfo = (TextView) findViewById(R.id.massInfo);
        combinationInfo = (TextView) findViewById(R.id.combination);
        doubleResultOutput = (TextView) findViewById(R.id.doubleResult);


        final Button dropTheDices = (Button) findViewById(R.id.dropDice);
        dropTheDices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstPlayer.dropAllowedDices();
                firstPlayer.hasCombinations();
                firstPlayer.resultToNumber();
                printInfo(firstPlayer);
                firstPlayer.resetValuesCounter();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initDicesInfo() {
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            diceInfo[i] = (TextView) findViewById(getBaseContext().getResources().getIdentifier("diceInfo" + (i + 1), "id", getBaseContext().getPackageName()));
        }
    }

    private void initDicesImage(Bitmap[] background, int diceWidth, Player player) {
        Dice[] dicesImage = new Dice[NUMBER_OF_DICES];
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            dicesImage[i] = new Dice();
            dicesImage[i].diceButton = (ImageButton) findViewById(getBaseContext().getResources().getIdentifier("diceImage" + (i + 1), "id", getBaseContext().getPackageName()));
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
        firstPlayer.dices = onDiceLeftImpl(view.getId());
    }

    private static Dice[] onDiceLeftImpl(int id) {
        Dice[] dicesImage = new Dice[NUMBER_OF_DICES];
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            if (dicesImage[i].diceButton.getId() == id) {
                if (!dicesImage[i].isLeft) {
                    dicesImage[i].diceButton.setBackgroundColor(Color.BLACK);
                    dicesImage[i].switchIsDiceLeft();
                } else {
                    dicesImage[i].diceButton.setBackgroundColor(Color.WHITE);
                    dicesImage[i].switchIsDiceLeft();
                }
            }
        }
        return dicesImage;
    }

    public void printInfo(Player player) {
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            diceInfo[i].setText(
                    getString(getBaseContext().getResources().getIdentifier("dice_" + (i + 1), "string", getBaseContext().getPackageName())) +
                            ": " + Integer.toString(player.dices[i].value));
            player.dices[i].diceButton.setImageBitmap(croppedDiceImage[player.dices[i].value - 1]);
        }
        printDroppedValuesCounts(player.droppedValuesCount);
        outputResult(player);
        doubleResultOutput.setText(getString(R.string.numericalResult) + Double.toString(player.doubleResult));
    }

    public void printDroppedValuesCounts(int droppedValuesCounts[]) {
        StringBuilder builder = new StringBuilder();
        String separator = " | ";
        for (int i = 0; i < droppedValuesCounts.length; i++) {
            builder.append(i + 1).append(": ").append(Integer.toString(droppedValuesCounts[i])).append(separator);
        }
        massInfo.setText(builder.toString());
    }

    public void outputResult(Player player) {
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

}
