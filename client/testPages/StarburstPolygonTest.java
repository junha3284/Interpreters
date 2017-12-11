package client.testPages;

import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import polygon.Polygon;
import geometry.Vertex3D;

public class StarburstPolygonTest {
	private static final int NUM_TRIANGLES =90;
	private static final double FRACTION_OF_PANEL_FOR_DRAWING = 0.9;
	
	private final PolygonRenderer renderer;
	private final Drawable panel;
	Vertex3D center;
	
	public StarburstPolygonTest(Drawable panel, PolygonRenderer renderer){
		this.panel = panel;
		this.renderer = renderer;
		
		makeCenter();
		render();
	}
	
	private void render() {
		double radius = computeRadius();
		double angleDifference = (2.0 * Math.PI)/NUM_TRIANGLES;
		double angle = 0.0;
		
		for( int tri = 0; tri < NUM_TRIANGLES; tri++){
			Vertex3D first = radialPoint(radius,angle);
			Vertex3D second =  radialPoint(radius,angle + angleDifference);
			renderer.drawPolygon(Polygon.makeEnsuringClockwise(first,second,center), panel, (a,b)->Color.random());
			angle = angle + angleDifference;
		}
	}
	
	private void makeCenter() {
		int centerX = panel.getWidth() / 2;
		int centerY = panel.getHeight() / 2;
		center = new Vertex3D(centerX, centerY, 0, Color.WHITE);
	}
	
	private Vertex3D radialPoint(double radius, double angle) {
		double x = center.getX() + radius * Math.cos(angle);
		double y = center.getY() + radius * Math.sin(angle);
		return new Vertex3D(x, y, 0, Color.WHITE);
	}
	
	private double computeRadius() {
		int width = panel.getWidth();
		int height = panel.getHeight();
		
		int minDimension = width < height ? width : height;
		
		return (minDimension / 2.0) * FRACTION_OF_PANEL_FOR_DRAWING;
	}
}
