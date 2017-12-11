package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;

public class AntialiasingLineRenderer implements LineRenderer {
	// use the static factory make() instead of constructor.
	private AntialiasingLineRenderer(){}
	
	/*
	 * (non-Javadoc)
	 * @see client.LineRenderer#drawLine(client.Vertex2D, client.Vertex2D, windowing.Drawable)
	 * 
	 * @pre: p2.x >= p1.x && p2.y >= p1.y
	 */
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		int x0 = p1.getIntX();
		int x1 = p2.getIntX();
		int m_num = 2*(p2.getIntY() - p1.getIntY());
		int m_den = 2*(x1-x0);
		int y_int = p1.getIntY();
		int y_den = m_den;
		int k = m_num - y_den;
		int y_num = x1 - x0 + k;
		int x = x0;
		
		int argbColor = p1.getColor().asARGB();
		
		while(x <= x1){
			
			// Setting pixels
			// slope is [0,0.5] => just need to check one pixel from below and above (x, y_int) respectively
			//drawable.setPixelWithCoverage(x, y_int+2, 0.0, argbColor, coverageByLine(this.distanceFromLine(p1,p2,x,y_int+2))); 
			drawable.setPixelWithCoverage(x, y_int+1, 0.0, argbColor, coverageByLine(this.distanceFromLine(p1,p2,x,y_int+1)));
			drawable.setPixelWithCoverage(x, y_int, 0.0, argbColor, coverageByLine(this.distanceFromLine(p1,p2,x,y_int)));
			drawable.setPixelWithCoverage(x, y_int-1, 0.0, argbColor, coverageByLine(this.distanceFromLine(p1,p2,x,y_int-1)));
			
			
			// Updating x and y values
			x++;
			if(y_num < 0){
				y_num += m_num;
			}
			else{
				y_num +=k;
				y_int++;
			}
		}
		
		
	}
	
	public static LineRenderer make(){
		return new AnyOctantLineRenderer(new AntialiasingLineRenderer());
	}
	
	private double distanceFromLine(Vertex3D p1, Vertex3D p2, int x, int y){
		double x0 = p1.getX();
		double y0 = p1.getY();
		
		double x1 = p2.getX();
		double y1 = p2.getY();
		return Math.abs(((y1-y0)*x-(x1-x0)*y+x1*y0-y1*x0))/Math.pow((Math.pow((y1-y0),2) + Math.pow((x1-x0),2)), 0.5);
	}
	
	private double coverageByLine(double d){
		if(d > 1)
			return 0;
		else if( d > 0.5){
			double distance = d-0.5;
			double angle = Math.acos(distance/0.5);
			double coveredArea = angle*Math.pow(0.5, 2)-(0.5)*Math.sin(angle)*distance;
			return coveredArea/(Math.PI*Math.pow(0.5, 2));
		}
		
		else if( d >= 0){
			double distance = 0.5-d;
			double angle = Math.acos(distance/0.5);
			double coveredArea = (Math.PI-angle)*Math.pow(0.5, 2)+(0.5)*Math.sin(angle)*distance;
			return coveredArea / (Math.PI*Math.pow(0.5, 2));
		}
		return -1;
	}
}
