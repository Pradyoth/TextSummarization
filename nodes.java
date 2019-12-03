import java.util.ArrayList;

public class nodes
{
	public ArrayList<String> connections;
	public ArrayList<String> weights;
	
	public nodes()
	{
		connections = new ArrayList<>();
		weights = new ArrayList<>();
	}
	
	public void insert(String node, String weight)
	{
		connections.add(node);
		weights.add(weight);
	}
}