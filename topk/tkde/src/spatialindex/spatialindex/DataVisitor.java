package spatialindex.spatialindex;

import java.util.HashMap;
import java.util.Map;

public class DataVisitor implements IVisitor {

	public Map<Integer, String> answers;
	public int nodeAccesses;
	
	public DataVisitor() {
		answers =  new HashMap<Integer, String>();
		nodeAccesses = 0;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void visitNode(INode n) {
		// TODO Auto-generated method stub
		nodeAccesses++;
	}
	@Override
	public void visitData(IData d) {
		// TODO Auto-generated method stub
		//if(d != null){
			//System.out.println(d.getIdentifier() + " " + d.getShape().toString());
			answers.put(d.getIdentifier(), new String(d.getData()));
			//System.out.println("Data size: "+ d.getData().length);
		//}
		
	}}