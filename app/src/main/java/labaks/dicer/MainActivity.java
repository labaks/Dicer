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

    private final int numberOfDices = 5;
    private final int diceSides = 6;
    private TextView[] diceInfo = new TextView[numberOfDices];
    private Dice[] dicesImage = new Dice[numberOfDices];
    private Bitmap[] croppedDiceImage = new Bitmap[diceSides];
    private final static DisplayMetrics metrics = new DisplayMetrics();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        initDicesInfo();


        final int diceWidth = (metrics.widthPixels - 100) / numberOfDices;
        cropDiceSides();
        initDicesImage(croppedDiceImage, diceWidth);

        final Button dropTheDices = (Button) findViewById(R.id.dropDice);
        dropTheDices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < numberOfDices; i++) {
                    if (!dicesImage[i].isLeft) {
                        int currentResult = dropTheDice();
                        diceInfo[i].setText(
                                getString(getBaseContext().getResources().getIdentifier("dice_" + (i + 1), "string", getBaseContext().getPackageName())) +
                                        ": " + Integer.toString(currentResult));
                        dicesImage[i].diceButton.setImageBitmap(croppedDiceImage[currentResult - 1]);
                    }
                }
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

    private static int dropTheDice() {
        return (int) Math.round(Math.random() * 5 + 1);
    }

    private void initDicesInfo() {
        for (int i = 0; i < numberOfDices; i++) {
            diceInfo[i] = (TextView) findViewById(getBaseContext().getResources().getIdentifier("diceInfo" + (i + 1), "id", getBaseContext().getPackageName()));
        }
    }

    private void initDicesImage(Bitmap[] background, int diceWidth) {
        for (int i = 0; i < numberOfDices; i++) {
            dicesImage[i] = new Dice();
            dicesImage[i].diceButton = (ImageButton) findViewById(getBaseContext().getResources().getIdentifier("diceImage" + (i + 1), "id", getBaseContext().getPackageName()));
            dicesImage[i].diceButton.setMaxWidth(diceWidth);
            dicesImage[i].diceButton.setImageBitmap(background[i]);
        }
    }

    private void cropDiceSides() {
        Bitmap bitmapDice = BitmapFactory.decodeResource(this.getResources(), R.drawable.dices_sides);
        for (int i = 0; i < diceSides; i++) {
            croppedDiceImage[i] = Bitmap.createBitmap(bitmapDice, i * 1114, 0, 1114, 1114);
        }
    }

    public void onDiceLeft(View view) {
        for (int i = 0; i < numberOfDices; i++) {
            if (dicesImage[i].diceButton.getId() == view.getId()) {
                if (!dicesImage[i].isLeft) {
                    dicesImage[i].diceButton.setBackgroundColor(Color.BLACK);
                    dicesImage[i].switchIsDiceLeft();
                } else {
                    dicesImage[i].diceButton.setBackgroundColor(Color.WHITE);
                    dicesImage[i].switchIsDiceLeft();
                }
            }
        }
    }
}
