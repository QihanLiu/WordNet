import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Merge;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

public class SAP {
    private final Digraph graph;
    private final CacheSAP cache;
    private final BnfBFS bnfbfs;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        graph = new Digraph(G);
        cache = new CacheSAP(100);
        bnfbfs = new BnfBFS(G);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        Bag<Integer> vBag = new Bag<Integer>();
        vBag.add(v);
        Bag<Integer> wBag = new Bag<Integer>();
        wBag.add(w);
        // standardize input. Make it all iterables.
        return length(vBag, wBag);
    }

    // a common ancestor of v and w that participates in a shortest ancestral path;
    // -1 if no such path
    public int ancestor(int v, int w) {
        Bag<Integer> vBag = new Bag<Integer>();
        vBag.add(v);
        Bag<Integer> wBag = new Bag<Integer>();
        wBag.add(w);
        return ancestor(vBag, wBag);
    }

    // length of shortest ancestral path between any vertex in v and any vertex in
    // w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (checkNull(v, "Can't find length of null items") || checkNull(w, "Can't find length of null items")) {
            return -1;
        }
        return cache.process(v, w).length;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such
    // path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (checkNull(v, "Can't find ancestor of null items") || checkNull(w, "Can't find ancestor of null items")) {
            return -1;
        }
        return cache.process(v, w).ancestor;
    }

    // contains result of length and ancestor
    private class SAPResults {
        private int length;
        private int ancestor;

        // store results
        private SAPResults(int length, int ancestor) {
            this.length = length;
            this.ancestor = ancestor;
        }
    }

    // record results from previous calculation and delete data using LRU (least
    // recently used).
    // Or calcualte a new input.
    private class CacheSAP {
        // hashmap use v and w as string key, and its result.
        private final HashMap<String, SAPResults> cacheResults;
        // deque is a list that recored the key as refer order
        // the top is the most recent reference, and the last is the least.
        private final Deque<String> usageDeque;
        private final int maxcap;

        private CacheSAP(int maxsize) {
            cacheResults = new HashMap<String, SAPResults>();
            usageDeque = new LinkedList<>();
            maxcap = maxsize;
        }

        // Retrieve (old) or calculate (new) results using bfs.
        // delete using LRU (usageDeque)
        private SAPResults process(Iterable<Integer> v, Iterable<Integer> w) {
            // key is unique to every v and w
            String key = generateKey(v, w);
            // if the result is stored, return it.
            if (cacheResults.containsKey(key)) {
                SAPResults result = cacheResults.get(key);
                // no need to update if it is at top
                if (usageDeque.getFirst().equals(key)) {
                    return result;
                }
                // update its usage: push it to top
                usageDeque.remove(key);
                usageDeque.push(key);
                return result;
            }
            // remove the least used result.
            if (usageDeque.size() == maxcap) {
                String oldkey = usageDeque.removeLast();
                cacheResults.remove(oldkey);
            }
            // if it is a new request, calculate it
            SAPResults result = bnfbfs.bfs(v, w);
            // store results
            usageDeque.push(key);
            cacheResults.put(key, result);
            return result;
        }

        // Generate unique keys to the input.
        // To avoid multiple calculation, sort v and w first.
        private String generateKey(Iterable<Integer> v, Iterable<Integer> w) {
            Integer[] vList, wList;
            // get v sorted
            int ind = 0;
            for (int i : v) {
                ind++;
            }
            vList = new Integer[ind];
            ind = 0;
            for (int i : v) {
                vList[ind] = i;
                ind++;
            }
            Merge.sort(vList);

            // get w sorted
            ind = 0;
            for (int i : w) {
                ind++;
            }
            wList = new Integer[ind];
            ind = 0;
            for (int i : w) {
                wList[ind] = i;
                ind++;
            }
            Merge.sort(wList);

            // Generate key for the results.
            StringBuilder vkey = new StringBuilder();
            for (int i = 0; i < vList.length; i++) {
                vkey.append(vList[i]);
                vkey.append(".");
            }
            StringBuilder wkey = new StringBuilder();
            for (int i = 0; i < wList.length; i++) {
                wkey.append(wList[i]);
                wkey.append(".");
            }
            StringBuilder key = new StringBuilder();
            // v and w can be switched.
            if (vList[0].compareTo(wList[0]) < 0) {
                key.append(vkey.toString());
                key.append("_");
                key.append(wkey.toString());
            } else {
                key.append(wkey.toString());
                key.append("_");
                key.append(vkey.toString());
            }
            return key.toString();
        }
    }

    // Use back and forth bfs to find common ancestor
    // 2 bfs search, 1st point visted by both bfs is common ancestor.
    // Use a class to store arrays, to avoid reinitialization after each search
    private class BnfBFS {
        private final boolean[] vMarked; // marked[v] = is there an s->v path?
        private final boolean[] wMarked;
        private final int[] vDistTo; // distTo[v] = length of shortest s->v path
        private final int[] wDistTo;
        private final Queue<Integer> vVisited; // use this to save time in reinitial
        private final Queue<Integer> wVisited;
        private SAPResults result;

        private BnfBFS(Digraph G) {
            vVisited = new Queue<Integer>();
            wVisited = new Queue<Integer>();
            vMarked = new boolean[G.V()];
            wMarked = new boolean[G.V()];
            vDistTo = new int[G.V()];
            wDistTo = new int[G.V()];
            result = new SAPResults(-1, -1);
            for (int v = 0; v < G.V(); v++) {
                vDistTo[v] = Integer.MAX_VALUE;
                wDistTo[v] = Integer.MAX_VALUE;
            }
        }

        private SAPResults bfs(Iterable<Integer> v, Iterable<Integer> w) {
            // reinit selected poits
            reInit();
            Queue<Integer> vq = new Queue<Integer>();
            Queue<Integer> wq = new Queue<Integer>();
            // add sources of two bfs.
            for (int vp : v) {
                vMarked[vp] = true;
                vVisited.enqueue(vp);
                vDistTo[vp] = 0;
                vq.enqueue(vp);
            }
            for (int wp : w) {
                wMarked[wp] = true;
                wVisited.enqueue(wp);
                wDistTo[wp] = 0;
                wq.enqueue(wp);
            }
            // bfs with v queue and w queue
            while (!vq.isEmpty() || !wq.isEmpty()) {
                // do bfs back and forth with v and w
                if (!vq.isEmpty()) {
                    search(vq, vMarked, vVisited, vDistTo, wMarked);
                }
                if (!wq.isEmpty()) {
                    search(wq, wMarked, wVisited, wDistTo, vMarked);
                }
            }
            return result;
        }

        // return true when finds a ancestor
        private void search(Queue<Integer> q, boolean[] marked, Queue<Integer> visited, int[] distTo,
                boolean[] otherMarked) {
            if (!q.isEmpty()) {
                int v = q.dequeue();
                // if there is a vertex visted by both search,
                // it is an ancestor
                if (otherMarked[v]) {
                    if (vDistTo[v] + wDistTo[v] < result.length || result.length == -1) {
                        result.ancestor = v;
                        result.length = vDistTo[v] + wDistTo[v];
                    }
                }
                // stop adding new vertex to queue if the distance exceeds the min length
                if (distTo[v] < result.length || result.length == -1) {
                    for (int w : graph.adj(v)) {
                        if (!marked[w]) {
                            distTo[w] = distTo[v] + 1;
                            marked[w] = true;
                            visited.enqueue(w);
                            q.enqueue(w);
                        }
                    }
                }

            }
        }

        // only reinitial the visited vertex from previous run
        private void reInit() {
            while (!vVisited.isEmpty()) {
                int v = vVisited.dequeue();
                vMarked[v] = false;
                vDistTo[v] = Integer.MAX_VALUE;
            }
            while (!wVisited.isEmpty()) {
                int w = wVisited.dequeue();
                wMarked[w] = false;
                wDistTo[w] = Integer.MAX_VALUE;
            }
            result = new SAPResults(-1, -1);
        }
    }

    // validate inputs.
    // if input is illegal, throw exception.
    // if input is empty, return true.
    private boolean checkNull(Iterable<Integer> v, String message) {
        if (v == null) {
            throw new IllegalArgumentException(message);
        }
        int size = 0;
        for (Integer i : v) {
            size ++;
            if (i == null) {
                throw new IllegalArgumentException(message);
            }
            if (i < 0 || i >= graph.V()) {
                throw new IllegalArgumentException(message);
            }
        }
        if (size == 0) {
            return true;
        }
        return false;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In testfile = new In("digraph1.txt");
        Digraph graph = new Digraph(testfile);
        SAP sap = new SAP(graph);

        Bag<Integer> vBag = new Bag<Integer>();
        Bag<Integer> wBag = new Bag<Integer>();
        wBag.add(6);
        StdOut.println("Ancestor is: " + sap.ancestor(vBag, wBag) + ", Length is: " + sap.length(6, 1));
    }
}
