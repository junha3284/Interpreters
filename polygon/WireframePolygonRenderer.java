package polygon;

import windowing.drawable.Drawable;
import line.DDALineRenderer;
import line.LineRenderer;
import shading.PixelShader;

public class WireframePolygonRenderer implements PolygonRenderer{
	
	private WireframePolygonRenderer(){}
	LineRenderer renderer = DDALineRenderer.make();
	
	public static PolygonRenderer make(){
		return new WireframePolygonRenderer();
	}
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable drawable, PixelShader vertexShader) {
		for(int i=0; i < polygon.length(); i++)
			renderer.drawLine(polygon.get(i),polygon.get(i+1),drawable);
	}
}
