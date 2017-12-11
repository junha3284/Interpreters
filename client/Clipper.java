package client;

import geometry.Point3DH;
import polygon.Polygon;
import geometry.Vertex3D;

public class Clipper {
	private Point3DH firstQuad;
	private Point3DH secondQuad;
	private Point3DH thirdQuad;
	private Point3DH fourthQuad;
	private Point3DH hither;
	private Point3DH yon;
	
	public Clipper(double low_x, double high_x, double low_y, double high_y, double hither, double yon){
		
		this.firstQuad = new Point3DH(high_x,high_y,-1,1);
		this.fourthQuad = new Point3DH(high_x,low_y,-1,1);
		this.thirdQuad = new Point3DH(low_x,low_y,-1,1);
		this.secondQuad = new Point3DH(low_x,high_y,-1,1);
		this.hither = new Point3DH(0,0,-1,hither);
		this.yon = new Point3DH(0,0,1, -1*yon);
	}
	
	public boolean check_any_z_in (Vertex3D ... vertexs){
		boolean result = false;
		for(int i = 0; i < vertexs.length; i++)
			if(this.hither.multiply(vertexs[i].getPoint3D()) >= 0){
				result = true;
			}
		if(result){
			result = false;
			for(int i = 0; i < vertexs.length; i++){
				if(this.yon.multiply(vertexs[i].getPoint3D()) >= 0)
					return true;
			}
		}
		return false;
	}
	
	public Polygon clip_Z (Polygon raw){
		Polygon cliped = clip_by_plane(hither,raw);
		cliped = clip_by_plane(yon, cliped);
		return cliped;
	}
	
	
	public Polygon clip_XY(Polygon raw){
		Point3DH high_y_plane = planeOrigin(crossProduct(this.secondQuad,this.firstQuad));
		Point3DH high_x_plane = planeOrigin(crossProduct(this.firstQuad,this.fourthQuad));
		Point3DH low_y_plane = planeOrigin(crossProduct(this.fourthQuad,this.thirdQuad));
		Point3DH low_x_plane = planeOrigin(crossProduct(this.thirdQuad,this.secondQuad));
		
		Polygon cliped = clip_by_plane(high_y_plane,raw);
		cliped = clip_by_plane(high_x_plane,cliped);
		cliped = clip_by_plane(low_y_plane,cliped);
		cliped = clip_by_plane(low_x_plane,cliped);
		return cliped;
	}

	public Polygon clip_by_plane(Point3DH plane, Polygon raw){
		if(raw.length()==0)
			return raw;
		Polygon cliped = Polygon.makeEmpty();
		int k = -1;
		for(int i = 0; i < raw.length(); i++){
			if(raw.get(i).getPoint3D().multiply(plane) < 0 && raw.get(i+1).getPoint3D().multiply(plane) >= 0)
				k=i;
		}
		if( k == -1){
			if(raw.get(0).getPoint3D().multiply(plane) < 0)
				return cliped;
			else
				return raw;
		}
		else {
			cliped.add(intersectionPoint(plane,raw.get(k),raw.get(k+1)));
			for(int i = k+1; i < raw.length()+1+k; i++){
				if(plane.multiply(raw.get(i).getPoint3D())>=0)
					cliped.add(raw.get(i));
				else{
					cliped.add(intersectionPoint(plane,raw.get(i-1),raw.get(i)));
					return cliped;
				}
			}
		}
		return cliped;
	}
	
	private Vertex3D intersectionPoint(Point3DH plane, Vertex3D v1, Vertex3D v2){
		double t = (plane.multiply(v1.getPoint3D()))/(plane.multiply(v1.getPoint3D())-plane.multiply(v2.getPoint3D()));
		return v1.add(v2.subtract(v1).scale(t));
	}
	
	
	private Point3DH crossProduct(Point3DH a, Point3DH b){
		a = a.euclidean();
		b = b.euclidean();
		
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
	
	private Point3DH planeOrigin (Point3DH normalV){
		return new Point3DH(normalV.getX(), normalV.getY(), normalV.getZ(),0);
	}
}
