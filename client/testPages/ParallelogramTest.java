package client.testPages;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.InvertedYDrawable;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class ParallelogramTest {
	private final LineRenderer renderer;
	private final Drawable panel;
	
	public ParallelogramTest(Drawable panel, LineRenderer renderer){
		this.panel = new InvertedYDrawable(panel);
		this.renderer = renderer;
		render();
	}
	
	public void render() {
		Vertex3D p1 = new Vertex3D(20,80,0,Color.WHITE);
		Vertex3D p2 = new Vertex3D(150,150,0,Color.WHITE);
		
		Vertex3D p3 = new Vertex3D(160,270,0,Color.WHITE);
		Vertex3D p4 = new Vertex3D(240,40,0,Color.WHITE);
		
		Vertex3D adding_x = new Vertex3D (1,0,0,Color.BLACK);
		Vertex3D adding_y = new Vertex3D(0,1,0,Color.BLACK);
		for(int i=0; i <= 50; i++){
			renderer.drawLine(p1, p2, panel);
			renderer.drawLine(p3, p4, panel);
			p1 = p1.add(adding_y);
			p2 = p2.add(adding_y);
			p3 = p3.add(adding_x);
			p4 = p4.add(adding_x);
		}
	}
}
