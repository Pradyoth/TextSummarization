import org.jgrapht.*;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

public final class PageRank
{
    public static final int MAX_ITERATIONS_DEFAULT = 100;
    public static final double TOLERANCE_DEFAULT = 0.0001;
    public static final double DAMPING_FACTOR_DEFAULT = 0.85d;

    private final Graph<String, DefaultWeightedEdge> g;

    @SuppressWarnings("rawtypes")
	private Map scores = new HashMap<String, Double>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public PageRank(Graph g)
    {
        this.g = g;
        this.scores = new HashMap<String, Double>();

        run(DAMPING_FACTOR_DEFAULT, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    @SuppressWarnings("rawtypes")
	public Map getScores()
    {
        return scores;
    }

    public Double getVertexScore(String v)
    {
        if (!g.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        return (Double) scores.get(v);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void run(double dampingFactor, int maxIterations, double tolerance)
    {
        // initialization
        int totalVertices = g.vertexSet().size();

        double initScore = 1.0d / totalVertices;
        for (String v : g.vertexSet()) {
        	//scores.put(v, g.outgoingEdgesOf(v).size() + g.incomingEdgesOf(v).size());
            scores.put(v, initScore);
        }

        // run PageRank
        Map nextScores = new HashMap<>();
        double maxChange = tolerance;

        while (maxIterations > 0 && maxChange >= tolerance) {
            // compute next iteration scores
            double r = 0d;
            for (String v : g.vertexSet()) {
                if (g.outgoingEdgesOf(v).size() + g.incomingEdgesOf(v).size() > 0) {
                    r += (1d - dampingFactor) * (Double)scores.get(v);
                } else {
                    r += (Double)scores.get(v);
                }
            }
            r /= totalVertices;

            maxChange = 0d;
            for (String v : g.vertexSet()) {
                double contribution = 0d;

                for (DefaultWeightedEdge e : g.incomingEdgesOf(v)) {
                    String w = Graphs.getOppositeVertex(g, e, v);
                    contribution += dampingFactor * (Double)scores.get(w) / (g.outgoingEdgesOf(w).size()+g.incomingEdgesOf(w).size());
                }
                
                for (DefaultWeightedEdge e : g.outgoingEdgesOf(v)) {
                    String w = Graphs.getOppositeVertex(g, e, v);
                    contribution += dampingFactor * (Double)scores.get(w) / (g.outgoingEdgesOf(w).size()+g.incomingEdgesOf(w).size());
                }

                double vOldValue = (Double)scores.get(v);
                double vNewValue = r + contribution;
                maxChange = Math.max(maxChange, Math.abs(vNewValue - vOldValue));
                nextScores.put(v, vNewValue);
            }

            // swap scores
            Map tmp = scores;
            scores = nextScores;
            nextScores = tmp;

            // progress
            maxIterations--;
        }

    }

}