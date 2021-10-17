import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import java.util.HashMap;

public class WordNet {
    private final SAP sap;
    // id to a string of words (seperate by space)
    private final HashMap<Integer, String> id2Syn;
    // word to ids.
    private final HashMap<String, Bag<Integer>> syn2Id;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        checkNull(synsets, "Null synsets in initialization!");
        checkNull(hypernyms, "Null hypernyms in initialization!");

        id2Syn = new HashMap<Integer, String>();
        syn2Id = new HashMap<String, Bag<Integer>>();

        initSyn(synsets);
        Digraph graph = initHypernyms(hypernyms);
        DirectedCycle graphCycle = new DirectedCycle(graph);
        if (graphCycle.hasCycle()) {
            checkNull(null, "The input has a cycle");
        }
        if (!rootedDAG(graph)) {
            checkNull(null, "The input is not single rooted.");
        }
        sap = new SAP(graph);
    }

    // // initial id map and word map
    private void initSyn(String synsets) {
        In synFile = new In(synsets);
        while (synFile.hasNextLine()) {
            String[] line = synFile.readLine().split(",");
            // read id
            int id = Integer.parseInt(line[0]);
            // read synsets
            String n = line[1];
            id2Syn.put(id, n);

            String[] nouns = n.split(" ");
            for (String noun : nouns) {
                // if it is a new word
                if (syn2Id.get(noun) == null) {
                    Bag<Integer> bag = new Bag<Integer>();
                    bag.add(id);
                    syn2Id.put(noun, bag);
                }
                // if it is an old word, add id to bag
                else {
                    syn2Id.get(noun).add(id);
                }
            }
        }
    }

    // Contrust graph from hypernyms
    private Digraph initHypernyms(String hypernyms) {
        Digraph graph = new Digraph(id2Syn.size());

        In hyperFile = new In(hypernyms);
        while (hyperFile.hasNextLine()) {
            String[] line = hyperFile.readLine().split(",");
            int v = Integer.parseInt(line[0]);
            for (int i = 1; i < line.length; i++) {
                int w = Integer.parseInt(line[i]);
                graph.addEdge(v, w);
            }
        }
        return graph;
    }

    // Check if graph has one and only one root
    private boolean rootedDAG(Digraph graph) {
        int roots = 0;
        // check if the vertex is indegree = 0
        for (int i = 0; i < graph.V(); i++) {
            if (graph.outdegree(i) == 0) {
                roots++;
                if (roots > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return syn2Id.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        checkNull(word, "Can't find null word in a WordNet!");
        return syn2Id.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        checkNull(nounA, "Can't calulate dist of null words in a WordNet!");
        checkNull(nounB, "Can't calulate dist of null words in a WordNet!");
        Bag<Integer> idA = syn2Id.get(nounA);
        Bag<Integer> idB = syn2Id.get(nounB);
        return sap.length(idA, idB);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA
    // and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        checkNull(nounA, "Can't shortest ancestral path of null words in a WordNet!");
        checkNull(nounB, "Can't shortest ancestral path of null words in a WordNet!");
        Bag<Integer> idA = syn2Id.get(nounA);
        Bag<Integer> idB = syn2Id.get(nounB);
        int id = sap.ancestor(idA, idB);
        return id2Syn.get(id);
    }

    private static void checkNull(Object any, String message) {
        if (any == null)
            throw new IllegalArgumentException(message);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet words = new WordNet("synsets.txt", "hypernyms.txt");
        StdOut.println(words.nouns());
    }
}