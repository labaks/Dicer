package labaks.dicer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {

    private static final int NUMBER_OF_DICES = 5;
    private final int DICE_SIDES = 6;
    public final int FIRST_PLAYER_SHIFT = 0;
    public final int SECOND_PLAYER_SHIFT = 5;
    private TextView winner, bankInfo;
    private Button dropSecondPlayerDice, dropFirstPlayerDice;
    private TableLayout firstPlayerTable, secondPlayerTable;
    private Bitmap[] croppedDiceImage = new Bitmap[DICE_SIDES];
    private final static DisplayMetrics metrics = new DisplayMetrics();
    public String game_mode, pvc, pvp;
    public int diceWidth, bank, blind;
    public boolean isFirstPlayerTurn = true;
    RelativeLayout blindDialogView, raiseDialogView;

    Player firstPlayer;
    Player secondPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        game_mode = getIntent().getStringExtra(MainActivity.GAME_MODE);
        pvc = MainActivity.PLAYER_VS_AI;
        pvp = MainActivity.PLAYER_VS_PLAYER;
        diceWidth = (metrics.widthPixels - 20) / NUMBER_OF_DICES;
        cropDiceSides();
        initGame();
    }

    public void initGame() {
        firstPlayer = new Player(DICE_SIDES, NUMBER_OF_DICES);
        initDicesImage(croppedDiceImage, diceWidth, firstPlayer, FIRST_PLAYER_SHIFT);
        secondPlayer = new Player(DICE_SIDES, NUMBER_OF_DICES);
        initDicesImage(croppedDiceImage, diceWidth, secondPlayer, SECOND_PLAYER_SHIFT);
        firstPlayer.combInfo = (TextView) findViewById(R.id.firstPlayerCombination);
        secondPlayer.combInfo = (TextView) findViewById(R.id.secondPlayerCombination);
        firstPlayer.moneyInfo = (TextView)findViewById(R.id.firstPlayerMoney);
        secondPlayer.moneyInfo = (TextView) findViewById(R.id.secondPlayerMoney);
        winner = (TextView) findViewById(R.id.winner);
        bankInfo = (TextView) findViewById(R.id.bank);
        dropFirstPlayerDice = (Button) findViewById(R.id.dropFirstPlayerDice);
        dropSecondPlayerDice = (Button) findViewById(R.id.dropSecondPlayerDice);
        firstPlayerTable = (TableLayout) findViewById(R.id.firstTable);
        secondPlayerTable = (TableLayout) findViewById(R.id.secondTable);

        printPlayerMoney(firstPlayer);
        printPlayerMoney(secondPlayer);
        printBankInfo();

        showNewGameDialog();

        switch (game_mode) {
            case MainActivity.PLAYER_VS_AI:
                dropSecondPlayerDice.setVisibility(View.GONE);
                dropFirstPlayerDice.setText(getString(R.string.drop_the_dice));
                break;
            case MainActivity.PLAYER_VS_PLAYER:
                dropSecondPlayerDice.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void gamePVC() {
        if (!firstPlayer.isDrop && !secondPlayer.isDrop) {
            playerDropDice(firstPlayer);
            playerDropDice(secondPlayer);
            secondPlayer.resetValues();
            firstPlayer.resetValues();
            firstPlayer.isDrop = true;
            secondPlayer.isDrop = true;
        } else if (firstPlayer.isDrop && secondPlayer.isDrop) {
            playerDropDice(firstPlayer);
            playerDropDice(secondPlayer);
            defineWinner();
            secondPlayer.resetValues();
            firstPlayer.resetValues();
            showNewGameDialog();
            firstPlayer.isDrop = false;
            secondPlayer.isDrop = false;
        }
    }

    public void gamePVP() {
        if ((!firstPlayer.isDrop && !secondPlayer.isDrop) || (firstPlayer.isDrop && secondPlayer.isDrop)) {
            playerDropDice(firstPlayer);
            firstPlayer.resetValues();
            firstPlayer.isDrop = !firstPlayer.isDrop;
            isFirstPlayerTurn = !isFirstPlayerTurn;
            dropFirstPlayerDice.setEnabled(false);
            dropSecondPlayerDice.setEnabled(true);
            disableDices();
        } else {
            playerDropDice(secondPlayer);
            secondPlayer.resetValues();
            secondPlayer.isDrop = !secondPlayer.isDrop;
            isFirstPlayerTurn = !isFirstPlayerTurn;
            dropFirstPlayerDice.setEnabled(true);
            dropSecondPlayerDice.setEnabled(false);
            disableDices();
        }
        if (!firstPlayer.isDrop && !secondPlayer.isDrop) {
            defineWinner();
            showNewGameDialog();
        }
    }

    public void playerDropDice(Player player) {
        player.dropAllowedDices();
        player.increaseValueCounter();
        player.hasCombinations();
        player.resultToNumber();
        printInfo(player);
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
        if (isFirstPlayerTurn) {
            firstPlayer.dices = onDiceLeftImpl(view.getId(), firstPlayer);
        }
    }

    public void onDiceLeft2(View view) {
        if (game_mode.equals(pvp)) {
            if (!isFirstPlayerTurn) {
                secondPlayer.dices = onDiceLeftImpl(view.getId(), secondPlayer);
            }
        }
    }

    private static Dice[] onDiceLeftImpl(int id, Player player) {
        Dice[] dices = player.dices;
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            if (dices[i].diceButton.getId() == id) {
                if (!dices[i].isLeft) {
                    dices[i].diceButton.setBackgroundColor(Color.BLUE);
                    dices[i].switchIsDiceLeft();
                } else {
                    dices[i].diceButton.setBackgroundColor(Color.WHITE);
                    dices[i].switchIsDiceLeft();
                }
            }
        }
        return dices;
    }

    public void defineWinner() {
        if (firstPlayer.doubleResult > secondPlayer.doubleResult) {
            winner.setText(getString(R.string.first_player_win));
            firstPlayer.money += bank;
        } else if (firstPlayer.doubleResult < secondPlayer.doubleResult) {
            winner.setText(getString(R.string.second_player_win));
            secondPlayer.money += bank;
        } else {
            winner.setText(getString(R.string.dead_heat));
        }
    }

    public void dropFirstPlayerDice(View view) {
        if (game_mode.equals(pvc)) {
            firstPlayerTable.setVisibility(View.VISIBLE);
            secondPlayerTable.setVisibility(View.VISIBLE);
            gamePVC();
        } else if (game_mode.equals(pvp)) {
            firstPlayerTable.setVisibility(View.VISIBLE);
            gamePVP();
        }
    }

    public void dropSecondPlayerDice(View view) {
        secondPlayerTable.setVisibility(View.VISIBLE);
        gamePVP();
    }

    public void disableDices() {
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            if (isFirstPlayerTurn) {
                firstPlayer.dices[i].diceButton.setBackgroundColor(Color.WHITE);
                secondPlayer.dices[i].diceButton.setBackgroundColor(Color.GRAY);
            } else {
                firstPlayer.dices[i].diceButton.setBackgroundColor(Color.GRAY);
                secondPlayer.dices[i].diceButton.setBackgroundColor(Color.WHITE);
            }
        }
    }

    public void blinds() {
        firstPlayer.money -= blind;
        secondPlayer.money -= blind;
        bank = 2 * blind;
        printPlayerMoney(firstPlayer);
        printPlayerMoney(secondPlayer);
        printBankInfo();
    }

    public void showNewGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getString(R.string.new_game))
                .setMessage(winner.getText() + "\n" + getString(R.string.new_game_q))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                                dialogInterface.cancel();
                            }
                        })
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                blindDialog();
                                dialogInterface.cancel();
                            }
                        });
        AlertDialog newGameDialog = builder.create();
        newGameDialog.show();
    }

    public void blindDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        blindDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.blind_dialog, null);
        Button blindOkBtn = (Button) blindDialogView.findViewById(R.id.blindOkBtn);
        Button blindPreviousBtn = (Button) blindDialogView.findViewById(R.id.blindPreviousBtn);
        Button blind5Btn = (Button) blindDialogView.findViewById(R.id.blind5Btn);
        Button blind10Btn = (Button) blindDialogView.findViewById(R.id.blind10Btn);
        final Toast enterBlindQ = Toast.makeText(getApplicationContext(), getString(R.string.please_enter_blind), Toast.LENGTH_SHORT);

        builder.setTitle(getString(R.string.blind))
                .setCancelable(false)
                .setMessage(getString(R.string.set_blind))
                .setView(blindDialogView);

        final AlertDialog blindDialog = builder.create();

        blindOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartNewGame();
                EditText setBlindEdit = (EditText) blindDialogView.findViewById(R.id.setBlindEdit);
                String currentBlindString = setBlindEdit.getText().toString();
                if (!currentBlindString.equals("")) {
                    int currentBlind = Integer.parseInt(currentBlindString);
                    if (currentBlind <= 0) {
                        enterBlindQ.show();
                    } else {
                        blind = currentBlind;
                        blinds();
                        blindDialog.dismiss();
                    }
                } else {
                    enterBlindQ.show();
                }
            }
        });

        blindPreviousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartNewGame();
                blinds();
                blindDialog.dismiss();
            }
        });

        blind5Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartNewGame();
                blind = 5;
                blinds();
                blindDialog.dismiss();
            }
        });

        blind10Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartNewGame();
                blind = 10;
                blinds();
                blindDialog.dismiss();
            }
        });
        setPreviousBlind();
        blindDialog.show();
    }

    public void printInfo(Player player) {
        for (int i = 0; i < NUMBER_OF_DICES; i++) {
            player.dices[i].diceButton.setImageBitmap(croppedDiceImage[player.dices[i].value - 1]);
        }
        outputResult(player);
        printBankInfo();
    }

    public void printBankInfo() {
        bankInfo.setText(getString(R.string.bank) + " " + bank);
    }

    public void printPlayerMoney(Player player) {
        player.moneyInfo.setText(getString(R.string.money_count) + " " + player.money);
    }

    private void onStartNewGame() {
        firstPlayer.combInfo.setText("");
        secondPlayer.combInfo.setText("");
        winner.setText("");
        firstPlayerTable.setVisibility(View.GONE);
        secondPlayerTable.setVisibility(View.GONE);
    }

    private void setPreviousBlind() {
        Button blindPreviousBtn = (Button) blindDialogView.findViewById(R.id.blindPreviousBtn);
        if (blind == 5 || blind == 10) {
            blindPreviousBtn.setVisibility(View.GONE);
        } else if (blind != 0) {
            blindPreviousBtn.setVisibility(View.VISIBLE);
            blindPreviousBtn.setText(Integer.toString(blind));
        } else {
            blindPreviousBtn.setVisibility(View.GONE);
        }
    }

    public void raiseDialog(final Player player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        raiseDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.raise_dialog, null);
        Button raiseBtnOk = (Button) raiseDialogView.findViewById(R.id.raiseBtnOk);
        Button raiseBtnCancel = (Button) raiseDialogView.findViewById(R.id.raiseBtnCancel);
        Button raiseBtnAllIn = (Button) raiseDialogView.findViewById(R.id.raiseBtnAllIn);
        final Toast enterBetQ = Toast.makeText(getApplicationContext(), getString(R.string.please_enter_bet), Toast.LENGTH_SHORT);

        builder.setTitle(getString(R.string.raise))
                .setCancelable(false)
                .setView(raiseDialogView);

        final AlertDialog raiseDialog = builder.create();

        raiseBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText setBet = (EditText) raiseDialog.findViewById(R.id.raiseBetEdit);
                String betString = setBet.getText().toString();
                if (!betString.equals("")) {
                    int bet = Integer.parseInt(betString);
                    if (bet <= 0) {
                        enterBetQ.show();
                    } else {
                        raise(player, bet);
                        raiseDialog.dismiss();
                    }
                } else {
                    enterBetQ.show();
                }

            }
        });

        raiseBtnAllIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allIn(player);
                raiseDialog.dismiss();
            }
        });

        raiseBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseDialog.dismiss();
            }
        });

        raiseDialog.show();

    }

    public void allIn(Player player) {
        bank += player.money;
        player.money = 0;
        printPlayerMoney(player);
        printBankInfo();
    }

    public void raise(Player player, int bet) {
        bank += bet;
        player.money -= bet;
        printPlayerMoney(player);
        printBankInfo();
    }

    public void onFirstPlayerRaise(View view) {
        raiseDialog(firstPlayer);
    }
    public void onSecondPlayerRaise(View view) {
        raiseDialog(secondPlayer);
    }


    public void outputResult(Player player) {
        StringBuilder builder = new StringBuilder();
        String space = " ";
        if (player.hasPoker) {
            builder.append(getString(R.string.you_have_poker)).append(space).append(getString(R.string.of)).append(space).append(player.pokerValue);
            player.combInfo.setText(builder.toString());
        } else if (player.hasFour) {
            builder.append(getString(R.string.you_have_four_of_a_kind)).append(space).append(getString(R.string.of)).append(space).append(player.fourValue);
            player.combInfo.setText(builder.toString());
        } else if (player.hasFullHouse) {
            builder.append(getString(R.string.you_have_full_house)).append(space).append(getString(R.string.of)).append(space).append(player.fullHouseValue[0]).append(space).append(getString(R.string.and)).append(space).append(player.fullHouseValue[1]);
            player.combInfo.setText(builder.toString());
        } else if (player.hasBigStrait) {
            builder.append(getString(R.string.you_have_big_strait));
            player.combInfo.setText(builder.toString());
        } else if (player.hasLittleStrait) {
            builder.append(getString(R.string.you_have_little_strait));
            player.combInfo.setText(builder.toString());
        } else if (player.hasThree) {
            builder.append(getString(R.string.you_have_three_of_a_kind)).append(space).append(getString(R.string.of)).append(space).append(player.threeValue);
            player.combInfo.setText(builder.toString());
        } else if (player.hasTwoPair) {
            builder.append(getString(R.string.you_have_two_pair)).append(space).append(getString(R.string.of)).append(space).append(player.twoPairValue[0]).append(space).append(getString(R.string.and)).append(space).append(player.twoPairValue[1]);
            player.combInfo.setText(builder.toString());
        } else if (player.hasPair) {
            builder.append(getString(R.string.you_have_one_pair)).append(space).append(getString(R.string.of)).append(space).append(player.pairValue);
            player.combInfo.setText(builder.toString());
        } else if (player.hasNoComb) {
            builder.append(getString(R.string.you_have_no_combinations));
            player.combInfo.setText(builder.toString());
        }
    }
}
