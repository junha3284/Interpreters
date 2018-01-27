package client.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import client.interpreter.LineBasedReader;
import geometry.Point3DH;
//import geometry.Rectangle;
import geometry.Vertex3D;
import line.LineRenderer;
import client.Clipper;
import client.DepthCueingDrawable;
import client.RendererTrio;
import geometry.Transformation;
import polygon.Polygon;
import polygon.PolygonRenderer;
//import polygon.Shader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;
import shading.FaceShader;
import shading.VertexShader;
import shading.PixelShader;

public class SimpInterpreter implements PixelShader{
	
	private class Light{
		private Vertex3D light;
		private double a_attenuation;
		private double b_attenuation;
		
		Light(Vertex3D l, double a, double b){
			this.light = l;
			this.a_attenuation = a;
			this.b_attenuation = b;
		}
		
		Vertex3D get_light(){
			return this.light;
		}
		double get_a_attenuation(){
			return a_attenuation;
		}
		double get_b_attenuation(){
			return b_attenuation;
		}
	}
	
	private static final int NUM_TOKENS_FOR_POINT = 3;
	private static final int NUM_TOKENS_FOR_COMMAND = 1;
	private static final int NUM_TOKENS_FOR_COLORED_VERTEX = 6;
	private static final int NUM_TOKENS_FOR_UNCOLORED_VERTEX = 3;
	private static final char COMMENT_CHAR = '#';
	private RenderStyle renderStyle;
	
	private Transformation CTM;
	private Stack<Transformation> matrixStack;
	private Transformation worldToScreen;
	
	private static int WORLD_LOW_X = -100;
	private static int WORLD_HIGH_X = 100;
	private static int WORLD_LOW_Y = -100;
	private static int WORLD_HIGH_Y = 100;
	
	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;
	
	private Color defaultColor = Color.WHITE;
	private Color ambientLight = Color.BLACK;
	
	private Drawable drawable;
	private Drawable depthCueingDrawable;
	
	private LineRenderer lineRenderer;
	private PolygonRenderer filledRenderer;
	private PolygonRenderer wireframeRenderer;
	private Transformation cameraToScreen;
	private Transformation perspective;
	private Clipper clipper;
	
	private List<Light> lights;
	private ShaderStyle shaderStyle;
	private double k_s;
	private double p;
	
	public enum RenderStyle {
		FILLED,
		WIREFRAME;
	}
	
	public enum ShaderStyle{
		PHONG,
		GOURAUD,
		FLAT;
	}
	
	public SimpInterpreter(String filename, Drawable drawable, RendererTrio renderers) {
		this.drawable = drawable;
		this.depthCueingDrawable = new DepthCueingDrawable(drawable, -201, -201, Color.BLACK);
		this.lineRenderer = renderers.getLineRenderer();
		this.filledRenderer = renderers.getFilledRenderer();
		this.wireframeRenderer = renderers.getWireframeRenderer();
		this.defaultColor = Color.WHITE;
		
		reader = new LineBasedReader(filename);
		readerStack = new Stack<>();
		renderStyle = RenderStyle.FILLED;
		
		CTM = Transformation.identity();
		perspective = Transformation.perspective(-1);
		matrixStack = new Stack<>();
		
		shaderStyle = ShaderStyle.PHONG;
		k_s = 0.3;
		p = 8;
		lights = new ArrayList<Light>();
	}

	private void makeWorldToScreenTransform(Dimensions dimensions) {
		
		worldToScreen = Transformation.identity();
		
		double ratio;
		double length_width = WORLD_HIGH_X-WORLD_LOW_X;
		double length_height = WORLD_HIGH_Y-WORLD_LOW_Y;
		
		if( length_width > length_height){
			ratio = dimensions.getWidth()/length_width;
		}
		else{
			ratio = dimensions.getHeight()/length_height;
		}
		
		double center_x = ((double)(WORLD_HIGH_X + WORLD_LOW_X))/2;
		double center_y = ((double)(WORLD_HIGH_Y + WORLD_LOW_Y))/2;
		double origin_x = dimensions.getWidth()/2 - center_x*ratio;
		double origin_y = dimensions.getHeight()/2 - center_y*ratio;
		worldToScreen.Translate(origin_x, origin_y, 0);
		worldToScreen.Scale(ratio, ratio, 1);
	}
	
	public void interpret() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretLine(line);
			while(!reader.hasNext()) {
				if(readerStack.isEmpty()) {
					return;
				}
				else {
					reader = readerStack.pop();
				}
			}
		}
	}
	public void interpretLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretCommand(tokens);
			}
		}
	}
	private void interpretCommand(String[] tokens) {
		switch(tokens[0]) {
		case "{" :      push();   break;
		case "}" :      pop();    break;
		case "wire" :   wire();   break;
		case "filled" : filled(); break;
		
		case "file" :		interpretFile(tokens);		break;
		case "scale" :		interpretScale(tokens);		break;
		case "translate" :	interpretTranslate(tokens);	break;
		case "rotate" :		interpretRotate(tokens);	break;
		case "line" :		interpretLine(tokens);		break;
		case "polygon" :	interpretPolygon(tokens);	break;
		case "camera" :		interpretCamera(tokens);	break;
		case "surface" :	interpretSurface(tokens);	break;
		case "ambient" :	interpretAmbient(tokens);	break;
		case "depth" :		interpretDepth(tokens);		break;
		case "obj" :		interpretObj(tokens);		break;
		case "light" : 		interpretLight(tokens);		break;
		case "flat" :		interpretShader(tokens);	break;
		case "gouraud" :	interpretShader(tokens);	break;
		case "phong" :		interpretShader(tokens);	break;
		
		default :
			System.err.println("bad input line: " + tokens);
			break;
		}
	}
	
	private void interpretShader(String[] tokens) {
		if(tokens[0].equals("flat"))
			shaderStyle = ShaderStyle.FLAT;
		else if(tokens[0].equals("phong"))
			shaderStyle = ShaderStyle.PHONG;
		else if(tokens[0].equals("gouraud")){
			shaderStyle = ShaderStyle.GOURAUD;
		}
	}

	private void interpretLight(String[] tokens){
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		double a_atten = cleanNumber(tokens[4]);
		double b_atten = cleanNumber(tokens[5]);
		
		Color light_color = new Color(r,g,b);
		Vertex3D light_vertex3D = CTM.transformVertex3D(new Vertex3D(0,0,0,light_color));
		lights.add(new Light(light_vertex3D,a_atten,b_atten));
	}

	private void interpretCamera(String[] tokens) {
		double x_low = cleanNumber(tokens[1]);
		double y_low = cleanNumber(tokens[2]);
		double x_high = cleanNumber(tokens[3]);
		double y_high = cleanNumber(tokens[4]);
		double hither = cleanNumber(tokens[5]);
		double yon = cleanNumber(tokens[6]);
		
		WORLD_LOW_X = (int) x_low;
		WORLD_LOW_Y = (int) y_low ;
		WORLD_HIGH_X = (int) x_high;
		WORLD_HIGH_Y = (int) y_high;
		
		clipper = new Clipper(WORLD_LOW_X, WORLD_HIGH_X, WORLD_LOW_Y, WORLD_HIGH_Y, hither, yon);
		cameraToScreen = (CTM).inverse();
		makeWorldToScreenTransform(drawable.getDimensions());
	}

	private void interpretDepth(String[] tokens) {
		double near = cleanNumber(tokens[1]);
		double far = cleanNumber(tokens[2]);
		double r = cleanNumber(tokens[3]);
		double g = cleanNumber(tokens[4]);
		double b = cleanNumber(tokens[5]);
		Color c = new Color(r,g,b);
		this.depthCueingDrawable = new DepthCueingDrawable(drawable, near, far, c);
	}

	private void interpretSurface(String[] tokens) {
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		k_s = cleanNumber(tokens[4]);
		p = cleanNumber(tokens[5]);
		
		defaultColor = new Color(r,g,b);
	}

	private void push() {
		Transformation push = new Transformation(CTM);
		matrixStack.push(push);
	}
	private void pop() {
		CTM = matrixStack.pop();
	}
	private void wire() {
		renderStyle = RenderStyle.WIREFRAME;
	}
	private void filled() {
		renderStyle = RenderStyle.FILLED;
	}
	
	private void interpretFile(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"'; 
		String filename = quotedFilename.substring(1, length-1);
		file(filename + ".simp");
	}
	private void file(String filename) {
		readerStack.push(reader);
		reader = new LineBasedReader(filename);
	}
	
	private void interpretAmbient(String[] tokens) {
		double a1 = cleanNumber(tokens[1]);
		double a2 = cleanNumber(tokens[2]);
		double a3 = cleanNumber(tokens[3]);
		ambientLight = new Color(a1,a2,a3);
	}
	
	private void interpretScale(String[] tokens) {
		double sx = cleanNumber(tokens[1]);
		double sy = cleanNumber(tokens[2]);
		double sz = cleanNumber(tokens[3]);
		CTM.Scale(sx, sy, sz);
	}
	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);
		CTM.Translate(tx, ty, tz);
	}
	private void interpretRotate(String[] tokens) {
		String axisString = tokens[1];
		double angleInDegrees = cleanNumber(tokens[2]);
		CTM.Rotate(axisString, angleInDegrees);
	}
	
	private void interpretObj(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"'; 
		String filename = quotedFilename.substring(1, length-1);
		objFile(filename + ".obj");
	}
	
	private void objFile(String filename) {
		ObjReader objReader = new ObjReader(filename, defaultColor);
		objReader.read();

		switch(shaderStyle) {
		case PHONG :	objReader.render((v1,v2,v3)->polygon(v1,v2,v3));  break;
		case FLAT :		objReader.render((v1,v2,v3) -> polygon(v1,v2,v3,(k)->faceshade(k))); break;
		case GOURAUD :	objReader.render((v1,v2,v3)->polygon(v1,v2,v3,(a,b)->vertexShade(a,b))); break;
		
		default :	System.err.println("not possible case of shader Style "); break;
		}	
	}
	
	private static double cleanNumber(String string) {
		return Double.parseDouble(string);
	}
	
	private enum VertexColors {
		COLORED(NUM_TOKENS_FOR_COLORED_VERTEX),
		UNCOLORED(NUM_TOKENS_FOR_UNCOLORED_VERTEX);
		
		private int numTokensPerVertex;
		
		private VertexColors(int numTokensPerVertex) {
			this.numTokensPerVertex = numTokensPerVertex;
		}
		public int numTokensPerVertex() {
			return numTokensPerVertex;
		}
	}
	private void interpretLine(String[] tokens) {			
		Vertex3D[] vertices = interpretVertices(tokens, 2, 1);
		line(vertices[0],vertices[1]);
	}	
	private void interpretPolygon(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);
		if(shaderStyle == ShaderStyle.PHONG){
			polygon(vertices[0],vertices[1],vertices[2]);
		}
		if(shaderStyle == ShaderStyle.FLAT){
			polygon(vertices[0],vertices[1],vertices[2],(k)->faceshade(k));
		}
		else if(shaderStyle == ShaderStyle.GOURAUD){
			polygon(vertices[0],vertices[1],vertices[2],(a,b)->vertexShade(a,b));
		}
	}
	public Vertex3D[] interpretVertices(String[] tokens, int numVertices, int startingIndex) {
		VertexColors vertexColors = verticesAreColored(tokens, numVertices);	
		Vertex3D vertices[] = new Vertex3D[numVertices];
		
		for(int index = 0; index < numVertices; index++) {
			vertices[index] = interpretVertex(tokens, startingIndex + index * vertexColors.numTokensPerVertex(), vertexColors);
		}
		return vertices;
	}
	public VertexColors verticesAreColored(String[] tokens, int numVertices) {
		return hasColoredVertices(tokens, numVertices) ? VertexColors.COLORED :
														 VertexColors.UNCOLORED;
	}
	public boolean hasColoredVertices(String[] tokens, int numVertices) {
		return tokens.length == numTokensForCommandWithNVertices(numVertices);
	}
	public int numTokensForCommandWithNVertices(int numVertices) {
		return NUM_TOKENS_FOR_COMMAND + numVertices*(NUM_TOKENS_FOR_COLORED_VERTEX);
	}

	
	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		Point3DH point = interpretPoint(tokens, startingIndex);
		
		Color color = defaultColor;
		if(colored == VertexColors.COLORED) {
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT).multiply(ambientLight);
		}

		return new Vertex3D(point,color);
	}
	public static Point3DH interpretPoint(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);

		return new Point3DH(x,y,z);
	}
	public static Color interpretColor(String[] tokens, int startingIndex) {
		double r = cleanNumber(tokens[startingIndex]);
		double g = cleanNumber(tokens[startingIndex + 1]);
		double b = cleanNumber(tokens[startingIndex + 2]);

		return new Color(r,g,b);
	}

	private void line(Vertex3D p1, Vertex3D p2) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		
		if(!clipper.check_any_z_in(screenP1, screenP2)){
			screenP1 = worldToScreen.transformVertex3D(screenP1);
			screenP2 = worldToScreen.transformVertex3D(screenP2);
			lineRenderer.drawLine(screenP1, screenP2, depthCueingDrawable);
		}
	}
	
	//draw polygon using phong shading	
	private void polygon(Vertex3D p1, Vertex3D p2, Vertex3D p3) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		Vertex3D screenP3 = transformToCamera(p3);
		
		if(clipper.check_any_z_in(screenP1,screenP2, screenP3)){
			Polygon polygon = Polygon.make(screenP1, screenP2, screenP3);
			polygon = clipper.clip_Z(polygon);
			polygon = clipper.clip_XY(polygon).transform_only_point(perspective);
			polygon = polygon.euclideanXY();
			polygon = polygon.transform_only_point(worldToScreen);

			if(polygon.length()!=0)
				if(renderStyle == RenderStyle.FILLED)
					filledRenderer.drawPolygon(polygon, depthCueingDrawable,(a,b)->this.shade(a, b));
				else if(renderStyle == RenderStyle.WIREFRAME)
					wireframeRenderer.drawPolygon(polygon, depthCueingDrawable);
		}
	}

	//draw polygon using flat shading	
	private void polygon(Vertex3D p1, Vertex3D p2, Vertex3D p3, FaceShader shader){
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		Vertex3D screenP3 = transformToCamera(p3);
		if(clipper.check_any_z_in(screenP1,screenP2,screenP3)){
			Polygon polygon = Polygon.make(screenP1, screenP2, screenP3);
			polygon = shader.shade(polygon);
			polygon = clipper.clip_Z(polygon);
			polygon = clipper.clip_XY(polygon).transform_only_point(perspective);
			polygon = polygon.euclideanXY();
			polygon = polygon.transform_only_point(worldToScreen);

			if(polygon.length()!=0)
				if(renderStyle == RenderStyle.FILLED)
					filledRenderer.drawPolygon(polygon, depthCueingDrawable);
				else if(renderStyle == RenderStyle.WIREFRAME)
					wireframeRenderer.drawPolygon(polygon, depthCueingDrawable);
		}
	}
	
	// draw polygon using gouraud shading	
	private void polygon(Vertex3D p1, Vertex3D p2, Vertex3D p3, VertexShader shader){
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		Vertex3D screenP3 = transformToCamera(p3);
		if(clipper.check_any_z_in(screenP1,screenP2,screenP3)){
			Polygon temp = Polygon.make(screenP1, screenP2, screenP3);
			screenP1 = shader.shade(temp, screenP1);
			screenP2 = shader.shade(temp, screenP2);
			screenP3 = shader.shade(temp, screenP3);
			Polygon polygon = Polygon.make(screenP1,screenP2,screenP3);
			//System.out.print(polygon.get(0).toString());
			//System.out.print("*");
			polygon = clipper.clip_Z(polygon);
			polygon = clipper.clip_XY(polygon).transform_only_point(perspective);
			polygon = polygon.euclideanXY();
			polygon = polygon.transform_only_point(worldToScreen);
			//System.out.println(perspective_inverse(worldToScreen.inverse().transformVertex3D(polygon.get(0)),-1).toString());

			if(polygon.length()!=0)
				if(renderStyle == RenderStyle.FILLED)
					filledRenderer.drawPolygon(polygon, depthCueingDrawable);
				else if(renderStyle == RenderStyle.WIREFRAME)
					wireframeRenderer.drawPolygon(polygon, depthCueingDrawable);
		}
	}
	
	// If current vertex has a normal vector, calculate color with using it and create new vertex with that calculated color
	// Otherwise, calculate face normal and use it instead of the current vertex's normal vector	
	private Vertex3D vertexShade(Polygon p, Vertex3D current){
		Point3DH n = new Point3DH(0,0,0);
		boolean check = false;
		if(current.check_normal()==true){
			n=current.getNormal();
			check = true;
		}
		for(int i=0; i < p.length() && !check; i++){
			if(p.get(i).check_normal() == false){
				Point3DH v1 = p.get(0).getPoint3D();
				Point3DH v2 = p.get(1).getPoint3D();
				Point3DH v3 = p.get(2).getPoint3D();
				n = crossProduct(v2.subtract(v1),v3.subtract(v1)).normalize();
				check = true;
			}
		}
		
		Color lighted_color = current.getColor().multiply(ambientLight);
		for(int i=0; i < lights.size(); i++){
			Light light = lights.get(i);
			Point3DH light_position = cameraToScreen.transformVertex3D(lights.get(i).get_light()).getPoint3D();
			double d = light_position.subtract(current.getPoint3D()).length();
			Point3DH L =  light_position.subtract(current.getPoint3D()).normalize();
			Point3DH R = n.scale(2*L.dot_product(n)).subtract(L);
			double f = 1.0/(light.get_a_attenuation()+light.get_b_attenuation()*d);
			
			if(n.dot_product(L)>=0){
				lighted_color = lighted_color.add(current.getColor().scale(f).multiply(light.get_light().getColor().scale(n.dot_product(L))));
				lighted_color = lighted_color.add(light.get_light().getColor().scale(k_s*Math.pow(R.dot_product(current.getPoint3D().scale(-1).normalize()), this.p)*f));
			}
		}
		return new Vertex3D(current.getPoint3D(),n,lighted_color);
	}
	
	// using face vector as normal for each vertexs and calcualte and assign new colors to each vertex in polygon and return it	
	private Polygon faceshade(Polygon po){
		Point3DH n = new Point3DH(0,0,0);
		boolean check = false;
		for(int i=0; i < po.length() && !check; i++)
			if(po.get(i).check_normal() == false){
				Point3DH v1 = po.get(0).getPoint3D();
				Point3DH v2 = po.get(1).getPoint3D();
				Point3DH v3 = po.get(2).getPoint3D();
				n = crossProduct(v2.subtract(v1),v3.subtract(v1)).normalize();
				check = true;
			}
		if(check == false){
			for(int i=0; i < po.length(); i++)
				n = n.add(po.get(i).getNormal());
			n = n.normalize();
		}
		Point3DH center_point = new Point3DH(0,0,0);
		Color center_color = new Color(0,0,0);
		
		for(int i=0; i < po.length(); i++){
			center_point = center_point.add(po.get(i).getPoint3D());
			center_color = center_color.add(po.get(i).getColor());
		}
		
		center_point = center_point.scale(1.0/po.length());
		center_color = center_color.scale(1.0/po.length());
		
		Color lighted_color = center_color.multiply(ambientLight);
		double[] colors = {lighted_color.getR(),lighted_color.getG(),lighted_color.getB()};
		
		/*
		for(int i=0; i < lights.size(); i++){
			Light light = lights.get(i);
			Point3DH light_position = lights.get(i).get_light().getPoint3D();
			double d = light_position.subtract(center_point).length();
			Point3DH L =  light_position.subtract(center_point).normalize();
			Point3DH R = n.scale(2*L.dot_product(n)).subtract(L);
			double f = 1.0/(light.get_a_attenuation()+light.get_b_attenuation()*d);
			
			if(n.dot_product(L)>=0){
				lighted_color = lighted_color.add(center_color.scale(f).multiply(light.get_light().getColor().scale(n.dot_product(L))));
				lighted_color = lighted_color.add(light.get_light().getColor().scale(k_s*Math.pow(R.dot_product(center_point.scale(-1).normalize()), p)*f));
			}
		}*/

		for(int i=0; i < lights.size(); i++){
			Light light = lights.get(i);
			Point3DH light_position = cameraToScreen.transformVertex3D(light.get_light()).getPoint3D();
			//Point3DH light_position = lights.get(i).get_light().getPoint3D();
			double d = light_position.subtract(center_point).length();
			Point3DH L = light_position.subtract(center_point).normalize();
			Point3DH R = n.scale(2*L.dot_product(n)).subtract(L).normalize();
			Point3DH V = center_point.normalize().scale(-1);
			double f = 1/(light.get_a_attenuation()+light.get_b_attenuation()*d);
			if(n.dot_product(L)>0){
				colors[0] += light.get_light().getColor().getR()*f*(center_color.getR()*n.dot_product(L)+k_s*Math.pow(V.dot_product(R), p));
				colors[1] += light.get_light().getColor().getG()*f*(center_color.getG()*n.dot_product(L)+k_s*Math.pow(V.dot_product(R), p));
				colors[2] += light.get_light().getColor().getB()*f*(center_color.getB()*n.dot_product(L)+k_s*Math.pow(V.dot_product(R), p));
			}
		}
		
		
		Polygon temp = Polygon.makeEmpty();
		for(int i=0; i < po.length(); i++)
			temp.add(new Vertex3D(po.get(i).getPoint3D(),new Color(colors[0],colors[1],colors[2])));
		return temp;
	}
	
	// calculate normal vector for phong shading
	@Override
	public Color shade(Polygon polygon, Vertex3D current) {
		boolean check = true;
		Point3DH n = new Point3DH(0,0,0);
		for(int i=0; i < polygon.length()&&check; i++){
			if(polygon.get(i).check_normal()==false){
				Vertex3D v1 = perspective_inverse(worldToScreen.inverse().transformVertex3D(polygon.get(0)),-1);
				Vertex3D v2 = perspective_inverse(worldToScreen.inverse().transformVertex3D(polygon.get(1)),-1);
				Vertex3D v3 = perspective_inverse(worldToScreen.inverse().transformVertex3D(polygon.get(2)),-1);
				
				n = crossProduct(v2.subtract(v1).getPoint3D(),v3.subtract(v1).getPoint3D());
				current = perspective_inverse(worldToScreen.inverse().transformVertex3D(current),-1);
				current = new Vertex3D(current.getPoint3D(),n,current.getColor());
				return vertexShade(polygon,current).getColor();
			}
		}
		
		current = perspective_inverse(worldToScreen.inverse().transformVertex3D(current),-1);
		
		Vertex3D v1 = perspective_inverse(worldToScreen.inverse().transformVertex3D_only_point(polygon.get(0)),-1);
		Vertex3D v2 = perspective_inverse(worldToScreen.inverse().transformVertex3D_only_point(polygon.get(1)),-1);
		Vertex3D v3 = perspective_inverse(worldToScreen.inverse().transformVertex3D_only_point(polygon.get(2)),-1);
		
		double s_total = crossProduct(v2.subtract(v1).getPoint3D(), v3.subtract(v1).getPoint3D()).length();
		double bary_1 = crossProduct(v2.subtract(current).getPoint3D(), v3.subtract(current).getPoint3D()).length()/s_total;
		double bary_2 = crossProduct(v3.subtract(current).getPoint3D(), v1.subtract(current).getPoint3D()).length()/s_total;
		double bary_3 = crossProduct(v1.subtract(current).getPoint3D(), v2.subtract(current).getPoint3D()).length()/s_total;
		
		Point3DH n1 = polygon.get(0).getNormal().scale(bary_1);
		Point3DH n2 = polygon.get(1).getNormal().scale(bary_2);
		Point3DH n3 = polygon.get(2).getNormal().scale(bary_3);
		
		n = n1.add(n2).add(n3).normalize();
		
		current = new Vertex3D(current.getPoint3D(),n,current.getColor());
		
		return vertexShade(polygon,current).getColor();
	}
	
	private Vertex3D perspective_inverse(Vertex3D v, double d){
		if(v.check_normal()==true)
			return new Vertex3D(new Point3DH(v.getX()*v.getZ()/d,v.getY()*v.getZ()/d,v.getZ()),v.getNormal(), v.getColor());
		else
			return new Vertex3D(v.getX()*v.getZ()/d,v.getY()*v.getZ()/d,v.getZ(), v.getColor());
	}

	private Vertex3D transformToCamera(Vertex3D vertex) {
		return cameraToScreen.multiply(CTM).transformVertex3D(vertex);
	}
	
	public static Point3DH interpretPointWithW(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);
		double w = cleanNumber(tokens[startingIndex + 3]);
		Point3DH point = new Point3DH(x, y, z, w);
		return point;
	}
	
	private Point3DH crossProduct(Point3DH a, Point3DH b){		
		double a_x = a.getX();
		double a_y = a.getY();
		double a_z = a.getZ();
		
		double b_x = b.getX();
		double b_y = b.getY();
		double b_z = b.getZ();
		
		double x = a_y*b_z-a_z*b_y;
		double y = -1*(a_x*b_z-a_z*b_x);
		double z = a_x*b_y-a_y*b_x;
		
		return new Point3DH(x,y,z,1);
	}
}
