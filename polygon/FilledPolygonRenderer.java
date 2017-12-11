package polygon;

import windowing.drawable.Drawable;
import geometry.Vertex3D;
import shading.PixelShader;
import windowing.graphics.Color;

public class FilledPolygonRenderer implements PolygonRenderer{

	private FilledPolygonRenderer(){}
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable drawable, PixelShader shader) {
		Chain left = polygon.leftChain();
		Chain right = polygon.rightChain();
		
		int y_top = left.get(0).getIntY();
		int y_bottom = left.get(left.length()-1).getIntY();
		
		int index_left = 0;
		int index_right = 0;
		
		double m_left=0;
		double x_left=0;
		double z_left =0;
		double z_left_inverse=0;
		double z_m_left_inverse=0;
		Color c_left = new Color(0,0,0);
		Color c_m_left = new Color(0,0,0);
		
		double m_right=0;
		double x_right=0;
		double z_right=0;
		double z_right_inverse=0;
		double z_m_right_inverse=0;
		Color c_right = new Color(0,0,0);
		Color c_m_right = new Color(0,0,0);
		
		for(int y = y_top; y > y_bottom; y--){
			if( y == left.get(index_left).getIntY()){
				while(y <= left.get(index_left).getIntY()){
					index_left++;
				}
				x_left = left.get(index_left-1).getX();
				z_left = left.get(index_left-1).getZ();
				z_left_inverse = 1/z_left;
				c_left = left.get(index_left-1).getColor().scale(z_left_inverse);
				
				m_left = (left.get(index_left).getX()-left.get(index_left-1).getX())/(left.get(index_left).getY()-left.get(index_left-1).getY());
				c_m_left = (left.get(index_left).getColor().scale(1/left.get(index_left).getZ()).subtract(left.get(index_left-1).getColor().scale(1/left.get(index_left-1).getZ()))).scale(1/(left.get(index_left).getY()-left.get(index_left-1).getY()));
				z_m_left_inverse = (1/left.get(index_left).getZ()-1/left.get(index_left-1).getZ())/(left.get(index_left).getY()-left.get(index_left-1).getY());
				
			}
			
			if( y == right.get(index_right).getIntY()){
				while(y <= right.get(index_right).getIntY()){
					index_right++;
				}
				x_right = right.get(index_right-1).getX();
				z_right = right.get(index_right-1).getZ();
				z_right_inverse = 1/z_right;
				c_right = right.get(index_right-1).getColor().scale(z_right_inverse);
				
				m_right = (right.get(index_right).getX()-right.get(index_right-1).getX())/(right.get(index_right).getY()-right.get(index_right-1).getY());
				c_m_right = (right.get(index_right).getColor().scale(1/right.get(index_right).getZ()).subtract(right.get(index_right-1).getColor().scale(1/right.get(index_right-1).getZ()))).scale(1/(right.get(index_right).getY()-right.get(index_right-1).getY()));
				z_m_right_inverse = (1/right.get(index_right).getZ()-1/right.get(index_right-1).getZ())/(right.get(index_right).getY()-right.get(index_right-1).getY());
			}
			
			for(int x = (int)Math.floor(x_left+1/2); x < (int) Math.floor(x_right+1/2); x++){
				double z_inverse = z_left_inverse + (z_right_inverse-z_left_inverse)*(x-x_left)/(x_right-x_left);
				Color c_divided_z = c_left.add(c_right.subtract(c_left).scale((x-x_left)/(x_right-x_left)));
				drawable.setPixel(x, y, 1/z_inverse, shader.shade(polygon, new Vertex3D(x, y, 1/z_inverse,c_divided_z.scale(1/z_inverse))).asARGB());
			}
			
			x_left = x_left-m_left;
			x_right = x_right-m_right;
			//z_left = z_left-z_m_left;
			//z_right = z_right-z_m_right;
			c_left = c_left.subtract(c_m_left);
			c_right = c_right.subtract(c_m_right);
			z_left_inverse = z_left_inverse - z_m_left_inverse;
			z_right_inverse = z_right_inverse - z_m_right_inverse;
		}
	}
	
	public static PolygonRenderer make(){
		return new FilledPolygonRenderer();
	}
}
