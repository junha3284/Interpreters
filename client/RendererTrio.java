package client;

import line.LineRenderer;
import polygon.PolygonRenderer;
//import polygon.FilledPolygonRenderer;
//import polygon.WireframePolygonRenderer;

public class RendererTrio {
	private LineRenderer lineRenderer;
	private PolygonRenderer filledPolygonRenderer;
	private PolygonRenderer wirePolygonRenderer;
	
	RendererTrio(LineRenderer lineRenderer, PolygonRenderer filledPolygonRenderer, PolygonRenderer wirePolygonRenderer){
		this.lineRenderer = lineRenderer;
		this.filledPolygonRenderer = filledPolygonRenderer;
		this.wirePolygonRenderer = wirePolygonRenderer;
	}
	
	public LineRenderer getLineRenderer(){
		return lineRenderer;
	}
	
	public PolygonRenderer getFilledRenderer(){
		return filledPolygonRenderer;
	}
	
	public PolygonRenderer getWireframeRenderer(){
		return wirePolygonRenderer;
	}
	
}
