package client;

import client.testPages.StarburstLineTest;
import client.testPages.ParallelogramTest;
import geometry.Point2D;
import geometry.Vertex3D;
import line.AlternatingLineRenderer;
import line.LineRenderer;
import windowing.graphics.Color;
import client.ColoredDrawable;
import client.testPages.RandomLineTest;
import client.testPages.MeshPolygonTest;
import client.testPages.RandomPolygonTest;
import client.testPages.StarburstPolygonTest;
import line.AntialiasingLineRenderer;
import line.BresenhamLineRenderer;
import line.DDALineRenderer;
import polygon.FilledPolygonRenderer;
import polygon.Polygon;
import polygon.WireframePolygonRenderer;
import polygon.PolygonRenderer;
import windowing.PageTurner;
import windowing.drawable.Drawable;
import windowing.drawable.GhostWritingDrawable;
import windowing.drawable.InvertedYDrawable;
import windowing.drawable.TranslatingDrawable;
import windowing.graphics.Dimensions;
import client.RendererTrio;
import client.interpreter.SimpInterpreter;

import java.util.Random;

public class Client implements PageTurner {
	private final long SEED = 301291870L;
	private final Random random = new Random(SEED);
	
	private static final int ARGB_WHITE = 0xff_ff_ff_ff;
	private static final int ARGB_GREEN = 0xff_00_ff_40;
	
	private static final int NUM_PAGES = 16;
	protected static final double GHOST_COVERAGE = 0.14;

	private static final int NUM_PANELS = 4;
	private static final Dimensions PANEL_SIZE = new Dimensions(300, 300);
	private static final Point2D[] lowCornersOfPanels = {
			new Point2D( 50, 400),
			new Point2D(400, 400),
			new Point2D( 50,  50),
			new Point2D(400,  50),
	};
	
	private final Drawable drawable;
	private int pageNumber = 0;
	
	private Drawable image;
	private Drawable[] panels;
	private Drawable[] ghostPanels;		// use transparency and write only white
	private Drawable fullPanel;
	private Drawable largePanel;
	
	
	private LineRenderer lineRenderers[];
	private PolygonRenderer polygonRenderer;
	private PolygonRenderer wireframeRenderer;
	private RendererTrio renderers;
	
	private SimpInterpreter interpreter;
	
	
	public Client(Drawable drawable) {
		this.drawable = drawable;	
		createDrawables();
		createRenderers();
	}

	public void createDrawables() {
		image = new InvertedYDrawable(drawable);
		image = new TranslatingDrawable(image, point(0, 0), dimensions(750, 750));
		image = new ColoredDrawable(image, ARGB_WHITE);
		
		largePanel = new TranslatingDrawable(image, point(  50, 50),  dimensions(650, 650));
		fullPanel = largePanel;
		createPanels();
		createGhostPanels();
	}

	public void createPanels() {
		panels = new Drawable[NUM_PANELS];
		
		for(int index = 0; index < NUM_PANELS; index++) {
			panels[index] = new TranslatingDrawable(image, lowCornersOfPanels[index], PANEL_SIZE);
		}
	}

	private void createGhostPanels() {
		ghostPanels = new Drawable[NUM_PANELS];
		
		for(int index = 0; index < NUM_PANELS; index++) {
			Drawable drawable = panels[index];
			ghostPanels[index] = new GhostWritingDrawable(drawable, GHOST_COVERAGE);
		}
	}
	private Point2D point(int x, int y) {
		return new Point2D(x, y);
	}	
	private Dimensions dimensions(int x, int y) {
		return new Dimensions(x, y);
	}
	private void createRenderers() {
		lineRenderers = new LineRenderer[4];
		lineRenderers[0] = DDALineRenderer.make();
		lineRenderers[1] = BresenhamLineRenderer.make();
		lineRenderers[2] = AlternatingLineRenderer.make();
		lineRenderers[3] = AntialiasingLineRenderer.make();
		
		polygonRenderer = FilledPolygonRenderer.make();
		wireframeRenderer = WireframePolygonRenderer.make();
		renderers = new RendererTrio(lineRenderers[0], polygonRenderer, wireframeRenderer); 
	}
	@Override
	public void nextPage() {
		System.out.println("PageNumber " + (pageNumber + 1));
		pageNumber = (pageNumber + 1) % NUM_PAGES;
		
		image.clear();
		fullPanel.clear();
		String filename;

		switch(pageNumber) {
		case 1:  filename = "page-a1";	 break;
		case 2:  filename = "page-a2";	 break;
		case 3:	 filename = "page-a3";	 break;
		case 4:  filename = "page-b1";	 break;
		case 5:  filename = "page-b2";	 break;
		case 6:  filename = "page-b3";	 break;
		case 7:  filename = "page-c1";	 break;
		case 8:  filename = "page-c2";	 break;
		case 9:  filename = "page-c3";	 break;
		case 10:  filename = "page-d";	 break;
		case 11:  filename = "page-e";	 break;
		case 12:  filename = "page-f1";	 break;
		case 13:  filename = "page-f2";	 break;
		case 14:  filename = "page-g";	 break;
		case 15:  filename = "page-h";	 break;
		case 0:  filename = "page-i";	 break;

		default: defaultPage();
				 return;
		}				 
		interpreter = new SimpInterpreter(filename + ".simp", fullPanel, renderers);
		interpreter.interpret();
	}
	
	public void ArgumentNextPage(String file) {

		image.clear();
		fullPanel.clear();
		String filename = file;			 
		interpreter = new SimpInterpreter(filename + ".simp", fullPanel, renderers);
		interpreter.interpret();
	}

	@FunctionalInterface
	private interface TestPerformer {
		public void perform(Drawable drawable, LineRenderer renderer);
	}
	
	private void lineDrawerPage(TestPerformer test) {
		image.clear();

		for(int panelNumber = 0; panelNumber < panels.length; panelNumber++) {
			panels[panelNumber].clear();
			test.perform(panels[panelNumber], lineRenderers[panelNumber]);
		}
	}
	
	public void polygonDrawerPage(Drawable[] panelArray) {
		image.clear();
		for(Drawable panel: panels) {		// 'panels' necessary here.  Not panelArray, because clear() uses setPixel.
			panel.clear();
		}
		largePanel.clear();
		new StarburstPolygonTest(panelArray[0], polygonRenderer);
		new MeshPolygonTest(panelArray[1], polygonRenderer, MeshPolygonTest.NO_PERTURBATION);
		new MeshPolygonTest(panelArray[2], polygonRenderer, MeshPolygonTest.USE_PERTURBATION);
		new RandomPolygonTest(panelArray[3], polygonRenderer);
	}

	private void defaultPage() {
		image.clear();
		largePanel.fill(ARGB_GREEN, Double.MAX_VALUE);
	}
	
	public void centeredTriangleTest(Drawable panel, PolygonRenderer renderer){
		panel = new DepthCueingDrawable(panel, 0, -200);
		// panel = new GhostWritingDrawable(panel,GHOST_COVERAGE);
		// for checking Z-clipping is working
		Color[] array_color = new Color[6];
		array_color[0] = new Color(1,1,1);
		array_color[1] = new Color(0.85,0.85,0.85);
		array_color[2] = new Color(0.7,0.7,0.7);
		array_color[3] = new Color(0.55,0.55,0.55);
		array_color[4] = new Color(0.4,0.4,0.4);
		array_color[5] = new Color(0.25,0.25,0.25);
		
		int width = panel.getWidth();
		int height = panel.getHeight();
		
		double centerX = width/2;
		double centerY = height/2;
		Vertex3D center = new Vertex3D(centerX, centerY,0.0, Color.WHITE);
		
		double radius = 275;
		double z;
		for(int i=0; i< array_color.length; i++){
			z= (random.nextDouble()*198+1)*-1;
			double angel_first = random.nextDouble()*Math.PI;
			Vertex3D first = radialPoint(center, radius, z, array_color[i], angel_first);
			Vertex3D second = radialPoint(center, radius, z, array_color[i], angel_first + Math.PI*2/3);
			Vertex3D third = radialPoint(center, radius, z, array_color[i], angel_first + Math.PI*4/3);
			renderer.drawPolygon(Polygon.makeEnsuringClockwise(first,second,third), panel);
		}
		
	}
	private Vertex3D radialPoint(Vertex3D center, double radius, double z, Color c, double angle) {
		double x = center.getX() + radius * Math.cos(angle);
		double y = center.getY() + radius * Math.sin(angle);
		return new Vertex3D(x, y, z, c);
	}
}
