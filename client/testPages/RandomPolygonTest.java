package client.testPages;

import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import java.util.Random;
import geometry.Vertex3D;
import polygon.Polygon;

public class RandomPolygonTest {
	private final long SEED = 301291870L;
	private final Random random = new Random(SEED);
	
	private static final int NUM_TRIANGLES = 20;
	
	private final PolygonRenderer renderer;
	private final Drawable panel;
	
	private final int WIDTH;
	private final int HEIGHT;
	
	public RandomPolygonTest(Drawable panel, PolygonRenderer renderer){
		this.panel = panel;
		this.renderer = renderer;
		
		WIDTH = panel.getWidth();
		HEIGHT = panel.getHeight();
		
		render();
	}
	
	private void render(){
		for(int tri = 0; tri < NUM_TRIANGLES; tri++){
			renderer.drawPolygon(Polygon.makeEnsuringClockwise(createRandomVertex3Ds()), panel, (a,b)->Color.random());
		}
	}
	
	private Vertex3D[] createRandomVertex3Ds(){
		Vertex3D one = new Vertex3D(random.nextDouble()*WIDTH, random.nextDouble()*HEIGHT,0,Color.WHITE);
		Vertex3D two = new Vertex3D(random.nextDouble()*WIDTH, random.nextDouble()*HEIGHT,0,Color.WHITE);
		Vertex3D three = new Vertex3D(random.nextDouble()*WIDTH, random.nextDouble()*HEIGHT,0,Color.WHITE);
		Vertex3D[] list = {one, two, three};
		return list;
	}
	
}
