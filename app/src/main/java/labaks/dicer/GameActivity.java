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
    private Bitmap[] croppedDiceImage = new Bitmap[DICE_SIDES];
    private final static DisplayMetrics metrics = new DisplayMetrics();
    public String game_mode, pvc, pvp;
    public int diceWidth, bank, blind;
    public boolean isFirstPlayerTurn = true;
    public boolean betTime = false;
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

    public Player initPlayer(Player player, int combId, int moneyId, int dropDiceBtnId, int tableId, int raiseBtnId, int callBtnId, int foldBtnId) {
        player.combInfo = (TextView) findViewById(combId);
        player.moneyInfo = (TextView) findViewById(moneyId);
        player.dropDicesBtn = (Button) findViewById(dropDiceBtnId);
        player.dicesTable = (TableLayout) findViewById(tableId);
        player.raiseBtn = (Button) findViewById(raiseBtnId);
        player.callBtn = (Button) findViewById(callBtnId);
        player.foldBtn = (Button) findViewById(foldBtnId);
        return player;
    }

    public void initGame() {
        firstPlayer = new Player(DICE_SIDES, NUMBER_OF_DICES);
        initPlayer(firstPlayer,
                R.id.firstPlayerCombination,
                R.id.firstPlayerMoney,
                R.id.dropFirstPlayerDice,
                R.id.firstTable,
                R.id.firstPlayerRaise,
                R.id.firstPlayerCall,
                R.id.firstPlayerFold);
        initDicesImage(croppedDiceImage, diceWidth, firstPlayer, FIRST_PLAYER_SHIFT);

        secondPlayer = new Player(DICE_SIDES, NUMBER_OF_DICES);
        initPlayer(secondPlayer,
                R.id.secondPlayerCombination,
                R.id.secondPlayerMoney,
                R.id.dropSecondPlayerDice,
                R.id.secondTable,
                R.id.secondPlayerRaise,
                R.id.secondPlayerCall,
                R.id.secondPlayerFold);
        initDicesImage(croppedDiceImage, diceWidth, secondPlayer, SECOND_PLAYER_SHIFT);

        winner = (TextView) findViewById(R.id.winner);
        bankInfo = (TextView) findViewById(R.id.bank);

        printPlayerMoney(firstPlayer);
        printPlayerMoney(secondPlayer);
        printBankInfo();

        showNewGameDialog();

        switch (game_mode) {
            case MainActivity.PLAYER_VS_AI:
                secondPlayer.dropDicesBtn.setVisibility(View.GONE);
                firstPlayer.dropDicesBtn.setText(getString(R.string.drop_the_dice));
                break;
            case MainActivity.PLAYER_VS_PLAYER:
                secondPlayer.dropDicesBtn.setVisibility(View.VISIBLE);
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
            firstPlayer.isDrop = false;
            secondPlayer.isDrop = false;
        }
    }

    public void gamePVP() {
        if ((!firstPlayer.isDrop && !secondPlayer.isDrop) || (firstPlayer.isDrop && secondPlayer.isDrop && !betTime)) {
            playerDropDice(firstPlayer);
            firstPlayer.resetValues();
            firstPlayer.isDrop = !firstPlayer.isDrop;
            isFirstPlayerTurn = !isFirstPlayerTurn;
            firstPlayer.dropDicesBtn.setEnabled(false);
            secondPlayer.dropDicesBtn.setEnabled(true);
            disableDices();
        } else {
            playerDropDice(secondPlayer);
            secondPlayer.resetValues();
            secondPlayer.isDrop = !secondPlayer.isDrop;
            isFirstPlayerTurn = !isFirstPlayerTurn;
            firstPlayer.dropDicesBtn.setEnabled(true);
            secondPlayer.dropDicesBtn.setEnabled(false);
            disableDices();
        }
        if (!firstPlayer.isDrop && !secondPlayer.isDrop) {
            defineWinner();
        }
        if (firstPlayer.isDrop && secondPlayer.isDrop) {
            switchEnableOperationsBtns(firstPlayer, true);
            firstPlayer.dropDicesBtn.setEnabled(false);
            betTime = true;
            disableDices();
        }
    }

    public void switchEnableOperationsBtns(Player player, boolean state) {
        player.raiseBtn.setEnabled(state);
        player.callBtn.setEnabled(state);
        player.foldBtn.setEnabled(state);
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
        if (!betTime) {
            if (isFirstPlayerTurn) {
                firstPlayer.dices = onDiceLeftImpl(view.getId(), firstPlayer);
            }
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
        firstPlayer.bet = secondPlayer.bet = 0;
        betTime = false;
        onStartNewGame();
    }

    public void dropFirstPlayerDice(View view) {
        if (game_mode.equals(pvc)) {
            firstPlayer.dicesTable.setVisibility(View.VISIBLE);
            secondPlayer.dicesTable.setVisibility(View.VISIBLE);
            gamePVC();
        } else if (game_mode.equals(pvp)) {
            firstPlayer.dicesTable.setVisibility(View.VISIBLE);
            gamePVP();
        }
    }

    public void dropSecondPlayerDice(View view) {
        secondPlayer.dicesTable.setVisibility(View.VISIBLE);
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
            if (betTime) {
                firstPlayer.dices[i].diceButton.setBackgroundColor(Color.GRAY);
                secondPlayer.dices[i].diceButton.setBackgroundColor(Color.GRAY);
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
        StringBuilder stringBuilder = new StringBuilder();
        String space = " ";
        String newline = "\n";
        if (bank == 0) {
            stringBuilder.append(getString(R.string.new_game_q));
        } else {
            stringBuilder.append(getString(R.string.first_player_comb)).append(space).append(firstPlayer.combInfo.getText()).append(newline)
                    .append(getString(R.string.second_player_comb)).append(space).append(secondPlayer.combInfo.getText()).append(newline)
                    .append(winner.getText()).append(newline)
                    .append(getString(R.string.prize)).append(space).append(bank).append(newline)
                    .append(getString(R.string.new_game_q));
        }
        builder.setTitle(getString(R.string.new_game))
                .setMessage(stringBuilder.toString())
                .setCancelable(false)
                .setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                                dialogInterface.dismiss();
                            }
                        })
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                blindDialog();
                                dialogInterface.dismiss();
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
                blinds();
                blindDialog.dismiss();
            }
        });

        blind5Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blind = 5;
                blinds();
                blindDialog.dismiss();
            }
        });

        blind10Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        showNewGameDialog();
        firstPlayer.combInfo.setText("");
        secondPlayer.combInfo.setText("");
        winner.setText("");
        firstPlayer.dicesTable.setVisibility(View.GONE);
        secondPlayer.dicesTable.setVisibility(View.GONE);
        firstPlayer.dropDicesBtn.setVisibility(View.VISIBLE);
        firstPlayer.dropDicesBtn.setEnabled(true);
        switchEnableOperationsBtns(firstPlayer, false);
        switchEnableOperationsBtns(secondPlayer, false);
        firstPlayer.isDrop = false;
        secondPlayer.isDrop = false;
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
                        if (firstPlayer.bet > secondPlayer.bet) {
                            switchEnableOperationsBtns(firstPlayer, false);
                            switchEnableOperationsBtns(secondPlayer, true);
                            firstPlayer.dropDicesBtn.setEnabled(false);
                            secondPlayer.callBtn.setText(getString(R.string.call));
                        } else if (firstPlayer.bet < secondPlayer.bet) {
                            switchEnableOperationsBtns(firstPlayer, true);
                            switchEnableOperationsBtns(secondPlayer, false);
                            firstPlayer.callBtn.setText(getString(R.string.call));
                            secondPlayer.dropDicesBtn.setEnabled(false);
                        } else {
                            firstPlayer.callBtn.setText(getString(R.string.check));
                            secondPlayer.callBtn.setText(getString(R.string.check));
                            firstPlayer.dropDicesBtn.setEnabled(true);
                            switchEnableOperationsBtns(secondPlayer, false);
                            betTime = false;
                            disableDices();
                        }
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
        player.bet += bet;
        bank += bet;
        player.money -= bet;
        printPlayerMoney(player);
        printBankInfo();
    }

    public void fold(Player player) {
        player.doubleResult = -1;
        defineWinner();
    }

    public void onFirstPlayerRaise(View view) {
        raiseDialog(firstPlayer);
    }

    public void onSecondPlayerRaise(View view) {
        raiseDialog(secondPlayer);
    }

    public void onFirstPlayerFold(View view) {
        fold(firstPlayer);
    }

    public void onSecondPlayerFold(View view) {
        fold(secondPlayer);
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
