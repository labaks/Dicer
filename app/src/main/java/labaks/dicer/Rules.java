package labaks.dicer;

public class Rules {
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
}
