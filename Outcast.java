import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    private final WordNet wordnet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        checkNull(wordnet, "Can't calcualte outcast of a null net.");
        this.wordnet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        checkNull(nouns, "Input nouns are empty");
        // Sum of distance to every other noun.
        int[] distSum = new int[nouns.length];
        int maxDist = -1, ind = -1;
        for (int i = 0; i < distSum.length; i++) {
            checkNull(nouns[i], "Input nouns contain null");
            distSum[i] = calcualteDist(nouns[i], nouns);
            // record max distance and their index.
            if (maxDist < distSum[i]) {
                maxDist = distSum[i];
                ind = i;
            }
        }
        if (ind == -1) {
            throw new IllegalArgumentException("Error input.");
        }
        return nouns[ind];
    }

    private int calcualteDist(String noun, String[] nouns) {
        int dist = 0;
        for (String i : nouns) {
            if (noun.equals(i)) {
                continue;
            }
            dist += wordnet.distance(noun, i);
        }
        return dist;
    }

    private static void checkNull(Object any, String message) {
        if (any == null)
            throw new IllegalArgumentException(message);
    }

    // see test client below
    public static void main(String[] args) {
        WordNet wordnet = new WordNet("synsets.txt", "hypernyms.txt");
        Outcast outcast = new Outcast(wordnet);
        In outcastfile = new In("outcast11.txt");
        String[] nouns = outcastfile.readAllStrings();
        StdOut.println(outcast.outcast(nouns));
    }
}