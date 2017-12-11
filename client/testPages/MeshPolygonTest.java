package client.testPages;

import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import polygon.Polygon;
import java.util.Random;
import geometry.Vertex3D;

public class MeshPolygonTest { 
	
	public static final boolean NO_PERTURBATION = false;
	public static final boolean USE_PERTURBATION = true;
	private static final int SHIFT_MINIMUM = -12;
	private static final int SHIFT_MAXIMUM = 12;

	private static final int WIDTH = 65;
	private static final int HEIGHT = 65;
	
	private final long SEED = 301291870L;
	private final Random random = new Random(SEED);
	
	private final PolygonRenderer renderer;
	private final Drawable panel;
	
	private final Vertex3D[][] vertex3Ds = new Vertex3D[10][10]; 
	
	public MeshPolygonTest(Drawable panel, PolygonRenderer renderer, boolean isPermutation){
		this.panel = panel;
		this.renderer = renderer;
		
		createVertex3Ds(isPermutation);
		render();
	}
	
	private void createVertex3Ds  (boolean isPermutation){
		int x_init = 32;
		int y_init = 32;
		if(isPermutation){
			for(int row=0; row < 10; row++)
				for(int column=0; column < 10; column++)
					vertex3Ds[row][column] = new Vertex3D(x_init+column*WIDTH + permuteValue(), y_init+row*HEIGHT + permuteValue(),0.0,Color.random());
		}
		else {
			for(int row=0; row < 10; row++)
				for(int column=0; column < 10; column++)
					vertex3Ds[row][column] = new Vertex3D(x_init+column*WIDTH, y_init+row*HEIGHT, 0.0, Color.random());
		}
	}
	
	private void render() {
		for(int index_row = 0; index_row < 9; index_row++)
			for(int index_column = 0; index_column < 9; index_column++){
				drawUpTrianlge(index_row,index_column);
				drawBelowTriangle(index_row,index_column);
			}
	}
	
	private void drawBelowTriangle(int index_row, int index_column) {
		renderer.drawPolygon(Polygon.makeEnsuringClockwise(vertex3Ds[index_row][index_column],vertex3Ds[index_row][index_column+1], vertex3Ds[index_row+1][index_column+1]), panel);

	}

	private void drawUpTrianlge(int index_row, int index_column) {
		renderer.drawPolygon(Polygon.makeEnsuringClockwise(vertex3Ds[index_row][index_column],vertex3Ds[index_row+1][index_column], vertex3Ds[index_row+1][index_column+1]), panel);
	}
	
	private double permuteValue(){
		return SHIFT_MINIMUM+(SHIFT_MAXIMUM-SHIFT_MINIMUM)*random.nextDouble();
	}
	
}