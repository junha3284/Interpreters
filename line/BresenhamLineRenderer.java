package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;

public class BresenhamLineRenderer implements LineRenderer{
	// use the static factory make() instead of constructor.
	private BresenhamLineRenderer(){}
	
	/*
	 * (non-Javadoc)
	 * @see client.LineRenderer#drawLine(client.Vertex2D, client.Vertex2D, windowing.Drawable)
	 * 
	 * @pre: p2.x >= p1.x && p2.y >= p1.y
	 */
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		int x = p1.getIntX();
		int m_num = 2*(p2.getIntY()-p1.getIntY());
		//int m_den = 2*(p2.getIntX()-p1.getIntX());
		int y_int = p1.getIntY();
		int y_den = 2*(p2.getIntX()-p1.getIntX());
		int x1 = p2.getIntX();
		int argbColor = p1.getColor().asARGB();
		int k = m_num - y_den;
		int y_num = p2.getIntX()-p1.getIntX()+k;
		
		while(x<=x1){
			drawable.setPixel(x, y_int, 0.0, argbColor);
			x++;
			if((y_num) < 0){
				y_num += m_num;
			}
			else {
				y_num += k;
				y_int++;
			}
		}
		
		
	}
	
	public static LineRenderer make(){
		return new AnyOctantLineRenderer(new BresenhamLineRenderer());
	}
	
	
}
