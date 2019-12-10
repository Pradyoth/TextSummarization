import org.jgrapht.*;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

public final class PageRank
{
    private final Graph<String, DefaultWeightedEdge> g;

    @SuppressWarnings("rawtypes")
	private Map vertexScores = new HashMap<String, Double>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public PageRank(Graph g)
    {
        this.g = g;
        this.vertexScores = new HashMap<String, Double>();

        run(0.85d,100,0.0001);
    }

    @SuppressWarnings("rawtypes")
	public Map getScores()
    {
        return vertexScores;
    }

    public Double getVertexScore(String v)
    {
        if (!g.containsVertex(v)) {
            System.out.println("no such vertex");
            return null;
        }
        return (Double) vertexScores.get(v);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void run(double dampingFactor, int iterations, double tolerance)
    {
        // initialization
        int totalVertices = g.vertexSet().size();

        double startScore = 1.0d / totalVertices;
        for (String v : g.vertexSet()) {
        	vertexScores.put(v, startScore);
        }

        Map updatedScores = new HashMap<>();
        double maxChange = tolerance;

        while (iterations > 0 && maxChange >= tolerance) {
            double r = 0d;
            for (String v : g.vertexSet()) {
                if (g.outgoingEdgesOf(v).size() + g.incomingEdgesOf(v).size() > 0) {
                    r += (1d - dampingFactor) * (Double)vertexScores.get(v);
                } else {
                    r += (Double)vertexScores.get(v);
                }
            }
            r /= totalVertices;

            maxChange = 0d;
            for (String v : g.vertexSet()) {
                double contribution = 0d;

                for (DefaultWeightedEdge e : g.incomingEdgesOf(v)) {
                    String w = Graphs.getOppositeVertex(g, e, v);
                    contribution += dampingFactor * (Double)vertexScores.get(w) / (g.outgoingEdgesOf(w).size()+g.incomingEdgesOf(w).size());
                }
                
                for (DefaultWeightedEdge e : g.outgoingEdgesOf(v)) {
                    String w = Graphs.getOppositeVertex(g, e, v);
                    contribution += dampingFactor * (Double)vertexScores.get(w) / (g.outgoingEdgesOf(w).size()+g.incomingEdgesOf(w).size());
                }

                double vOldValue = (Double)vertexScores.get(v);
                double vNewValue = r + contribution;
                maxChange = Math.max(maxChange, Math.abs(vNewValue - vOldValue));
                updatedScores.put(v, vNewValue);
            }

            // swap scores
            Map temp = vertexScores;
            vertexScores = updatedScores;
            updatedScores = temp;

            // progress
            iterations--;
        }

    }

}