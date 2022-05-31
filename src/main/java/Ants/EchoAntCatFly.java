package Ants;

/**
 * This interaction model is based on Holland's original Echo interactions.  However, it uses
 * the Hamming Distance calculation proposed in Smith and Bedau's paper on Echo.
 */
public class EchoAntCatFly extends InteractionModel{

    /**
     * Returns the probability, 1 - HammingDistance/Tag length, that an agent will win a fight.
     * @param offense
     * @param defense
     * @return
     */
    public double likelyWinner(String offense, String defense) {
        double ret = 0;
        // First, we need to find the smallest string, as they may not be the same size
        String s1 = offense;
        String s2 = defense;
        int minLen = offense.length();
        if(defense.length() < minLen) {
            s1 = defense;
            s2 = offense;
            minLen = defense.length();
        }
        int countDiff = 0;
        for(int i = 0; i < minLen; i++) {
            if(s1.charAt(i) != s2.charAt(i)) {
                countDiff++;
            }
        }
        ret = 1 - countDiff/minLen; // todo:  introduce a bonus for longer tags.
        return ret;
    }
}
