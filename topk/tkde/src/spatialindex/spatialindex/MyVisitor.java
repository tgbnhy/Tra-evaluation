package spatialindex.spatialindex;

import java.util.HashMap;
import java.util.Map;

public class MyVisitor implements IVisitor {

	public Map<Integer, IShape> answers;
	public int nodeAccesses;
	
	public MyVisitor() {
		answers =  new HashMap<Integer, IShape>();
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
			answers.put(d.getIdentifier(), d.getShape());
			//System.out.println("Size: "+ d.getShape().toString().getBytes().length);
		//}
		
	}}