package client.interpreter;

import java.util.ArrayList;
import java.util.List;
import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

class ObjReader {
	private static final char COMMENT_CHAR = '#';
	private static final int NOT_SPECIFIED = -1;

	private class ObjVertex {
		// TODO: fill this class in.  Store indices for a vertex, a texture, and a normal.  Have getters for them.
		
		private int v_index;
		private int t_index;
		private int n_index;
		
		ObjVertex(int v, int t, int n){
			this.v_index = v;
			this.t_index = t;
			this.n_index = n;
		}
		
		public int get_v_index(){
			return v_index;
		}
		
		public int get_t_index(){
			return t_index;
		}
		
		public int get_n_index(){
			return n_index;
		}
	}
	private class ObjFace extends ArrayList<ObjVertex> {
		private static final long serialVersionUID = -4130668677651098160L;
	}	
	private LineBasedReader reader;
	
	private List<Vertex3D> objVertices;
	private List<Vertex3D> transformedVertices;
	private List<Point3DH> objNormals;
	private List<ObjFace> objFaces;

	private Color defaultColor;
	
	ObjReader(String filename, Color defaultColor) {
		// TODO: Initialize an instance of this class.
		reader = new LineBasedReader(filename);
		this.defaultColor = defaultColor;
		
		objVertices = new ArrayList<Vertex3D>();
		objNormals = new ArrayList<Point3DH>();
		objFaces = new ArrayList<ObjFace>();
	}

	public void render(PolygonDrawer drawer) {
		// TODO: Implement.  All of the vertices, normals, and faces have been defined.
		// First, transform all of the vertices.		
		// Then, go through each face, break into triangles if necessary, and send each triangle to the renderer.
		// You may need to add arguments to this function, and/or change the visibility of functions in SimpInterpreter.
		for(int in=0; in <objFaces.size(); in++){
			for(int on = 0; on < objFaces.get(in).size()-2; on++){
				int i = objFaces.get(in).get(0).get_v_index();
				int j = objFaces.get(in).get(on+1).get_v_index();
				int k = objFaces.get(in).get(on+2).get_v_index();
				int i_n = objFaces.get(in).get(0).get_n_index();
				int j_n = objFaces.get(in).get(on+1).get_n_index();
				int k_n = objFaces.get(in).get(on+2).get_n_index();
				if(i_n!= NOT_SPECIFIED && j_n != NOT_SPECIFIED  && k_n!=NOT_SPECIFIED){
					Vertex3D v1 = new Vertex3D(objVertices.get(i).getPoint3D(),objNormals.get(i_n),objVertices.get(i).getColor());
					Vertex3D v2 = new Vertex3D(objVertices.get(j).getPoint3D(),objNormals.get(j_n),objVertices.get(j).getColor());
					Vertex3D v3 = new Vertex3D(objVertices.get(k).getPoint3D(),objNormals.get(k_n),objVertices.get(k).getColor());
					drawer.polygon(v1, v2, v3);
				}
				else
					drawer.polygon(objVertices.get(i), objVertices.get(j), objVertices.get(k));
			}
		}
	}


	public void read() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretObjLine(line);
		}
	}
	private void interpretObjLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretObjCommand(tokens);
			}
		}
	}

	private void interpretObjCommand(String[] tokens) {
		switch(tokens[0]) {
		case "v" :
		case "V" :
			interpretObjVertex(tokens);
			break;
		case "vn":
		case "VN":
			interpretObjNormal(tokens);
			break;
		case "f":
		case "F":
			interpretObjFace(tokens);
			break;
		default:	// do nothing
			break;
		}
	}
	private void interpretObjFace(String[] tokens) {
		ObjFace face = new ObjFace();
		
		for(int i = 1; i<tokens.length; i++) {
			String token = tokens[i];
			String[] subtokens = token.split("/");
			
			int vertexIndex  = objIndex(subtokens, 0, objVertices.size());
			int textureIndex = objIndex(subtokens, 1, 0);
			int normalIndex  = objIndex(subtokens, 2, objNormals.size());

			// TODO: fill in action to take here.
			face.add(new ObjVertex(vertexIndex,textureIndex,normalIndex));
		}
		// TODO: fill in action to take here.
		objFaces.add(face);
	}

	private int objIndex(String[] subtokens, int tokenIndex, int baseForNegativeIndices) {
		// TODO: write this.  subtokens[tokenIndex], if it exists, holds a string for an index.
		// use Integer.parseInt() to get the integer value of the index.
		// Be sure to handle both positive and negative indices.
		if(subtokens.length > tokenIndex){
			if(subtokens[tokenIndex].length()>0){
				int index_input = Integer.parseInt(subtokens[tokenIndex]);
				if(index_input >0)
					return index_input-1;
				else
					return baseForNegativeIndices+ index_input;
			}
		}
		return NOT_SPECIFIED;
	}

	private void interpretObjNormal(String[] tokens) {
		int numArgs = tokens.length - 1;
		if(numArgs != 3) {
			throw new BadObjFileException("vertex normal with wrong number of arguments : " + numArgs + ": " + tokens);				
		}
		Point3DH normal = SimpInterpreter.interpretPoint(tokens, 1);
		// TODO: fill in action to take here.
		objNormals.add(normal);
	}
	private void interpretObjVertex(String[] tokens) {
		int numArgs = tokens.length - 1;
		Point3DH point = objVertexPoint(tokens, numArgs);
		Color color = objVertexColor(tokens, numArgs);
		
		// TODO: fill in action to take here.
		objVertices.add(new Vertex3D(point,color));
	}

	private Color objVertexColor(String[] tokens, int numArgs) {
		if(numArgs == 6) {
			return SimpInterpreter.interpretColor(tokens, 4);
		}
		if(numArgs == 7) {
			return SimpInterpreter.interpretColor(tokens, 5);
		}
		return defaultColor;
	}

	private Point3DH objVertexPoint(String[] tokens, int numArgs) {
		if(numArgs == 3 || numArgs == 6) {
			return SimpInterpreter.interpretPoint(tokens, 1);
		}
		else if(numArgs == 4 || numArgs == 7) {
			return SimpInterpreter.interpretPointWithW(tokens, 1);
		}
		throw new BadObjFileException("vertex with wrong number of arguments : " + numArgs + ": " + tokens);
	}
}
