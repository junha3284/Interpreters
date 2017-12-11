package client.testPages;

import java.util.Random;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class RandomLineTest {
	private static final int Num_Lines =30;
	private final int Width;
	private final int Height;
	
	private final LineRenderer renderer;
	private final Drawable panel;
	
	private final long SEED = 301291870L;
	private final Random random_Color = new Random(SEED);
	private final Random random_Point = new Random(SEED);
	
	private Vertex3D[] P1;
	private Vertex3D[] P2;
	
	public RandomLineTest(Drawable panel, LineRenderer renderer){
		this.panel = panel;
		this.renderer = renderer;
		Width = panel.getWidth();
		Height = panel.getHeight();
		
		createVertex3Ds();
		render();
	}
	
	public void createColors(){
		
	}
	
	public void createVertex3Ds(){
		P1 = new Vertex3D[Num_Lines];
		P2 = new Vertex3D[Num_Lines];
		
		for(int i=0; i < Num_Lines; i++){
			Color color_Point = Color.random(random_Color);
			P1[i] = new Vertex3D(Math.floor(random_Point.nextDouble()*Width), Math.floor(random_Point.nextDouble()*Height), 0, color_Point);
			P2[i] = new Vertex3D(Math.floor(random_Point.nextDouble()*Width), Math.floor(random_Point.nextDouble()*Height), 0, color_Point);
		}
	}
	
	public void render(){
		for(int i =0 ; i < Num_Lines; i++){
			renderer.drawLine(P1[i], P2[i], panel);
		}
	}
}
