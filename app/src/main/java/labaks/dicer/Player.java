package labaks.dicer;

public class Player {

    private final int diceSides,
            numberOfDices;

    public Dice[] dices;

    boolean hasNoComb = false;
    boolean hasPair = false;
    boolean hasTwoPair = false;
    boolean hasThree = false;
    boolean hasLittleStrait = false;
    boolean hasBigStrait = false;
    boolean hasFullHouse = false;
    boolean hasFour = false;
    boolean hasPoker = false;

    int pokerValue = 0;
    int fourValue = 0;
    int threeValue = 0;
    int pairValue = 0;

    int[] fullHouseValue = {0, 0};
    int[] twoPairValue = {0, 0};

    double doubleResult = 0;

    public int[] droppedValuesCount;

    public Player(int diceSides, int numberOfDices) {
        this.diceSides = diceSides;
        this.numberOfDices = numberOfDices;
        droppedValuesCount = new int[diceSides];
        dices = new Dice[numberOfDices];
    }

    private static int generateDiceValue() {
        return (int) Math.round(Math.random() * 5 + 1);
    }

    public void dropAllowedDices() {
        for (int i = 0; i < numberOfDices; i++) {
            if (dices[i].isLeft) {
                continue;
            }
            dices[i].value = generateDiceValue();
        }
    }

    public void resultToNumber() {
        if (hasPair) {
            doubleResult = 2 + pairValue * 0.1;
        } else if (hasTwoPair) {
            doubleResult = 3 + (twoPairValue[0] + twoPairValue[1]) * 0.1;
        } else if (hasThree) {
            doubleResult = 4 + threeValue * 0.1;
        } else if (hasLittleStrait) {
            doubleResult = 5.0;
        } else if (hasBigStrait) {
            doubleResult = 5.5;
        } else if (hasFullHouse) {
            doubleResult = 6 + fullHouseValue[0] * 0.1 + fullHouseValue[1] * 0.01;
        } else if (hasFour) {
            doubleResult = 7 + fourValue * 0.1;
        } else if (hasPoker) {
            doubleResult = 8 + pokerValue * 0.1;
        }
    }

    public static int maxInMass(int mass[]) {
        int max = 0;
        for (int mas : mass) {
            max = Math.max(max, mas);
        }
        return max;
    }

    public void hasCombinations() {
        boolean isMaxOne = maxInMass(droppedValuesCount) == 1;
        if (isMaxOne) {
            if (droppedValuesCount[0] == 0) {
                hasBigStrait = true;
            } else if (droppedValuesCount[5] == 0) {
                hasLittleStrait = true;
            } else {
                hasNoComb = true;
            }
        } else {
            for (int i = 0; i < diceSides; i++) {
                if (droppedValuesCount[i] == 5) {
                    hasPoker = true;
                    pokerValue = i + 1;
                } else if (droppedValuesCount[i] == 4) {
                    hasFour = true;
                    hasPair = false;
                    fourValue = i + 1;
                } else if (droppedValuesCount[i] == 3) {
                    if (hasPair) {
                        hasFullHouse = true;
                        hasPair = false;
                        fullHouseValue[0] = i + 1;
                        fullHouseValue[1] = pairValue;
                        pairValue = 0;
                    } else {
                        hasThree = true;
                        threeValue = i + 1;
                    }
                } else if (droppedValuesCount[i] == 2) {
                    if (hasPair) {
                        hasTwoPair = true;
                        hasPair = false;
                        twoPairValue[0] = pairValue;
                        twoPairValue[1] = i + 1;
                        pairValue = 0;
                    } else if (hasThree) {
                        hasFullHouse = true;
                        hasThree = false;
                        fullHouseValue[0] = threeValue;
                        fullHouseValue[1] = i + 1;
                        threeValue = 0;
                    } else {
                        hasPair = true;
                        pairValue = i + 1;
                    }
                }
            }
        }
    }

    public void increaseValueCounter() {
        for (int i = 0; i < numberOfDices; i++) {
            droppedValuesCount[this.dices[i].value - 1]++;
        }

    }

    public void resetValues() {
        for (int i = 0; i < diceSides; i++) {
            droppedValuesCount[i] = 0;
        }
        hasNoComb = hasPair = hasTwoPair = hasThree = hasLittleStrait = hasBigStrait = hasFullHouse = hasFour = hasPoker = false;
    }

}
