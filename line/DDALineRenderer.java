package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class DDALineRenderer implements LineRenderer {
	// 
	private DDALineRenderer() {}

	/*
	 * (non-Javadoc)
	 * @see client.LineRenderer#drawLine(client.Vertex2D, client.Vertex2D, windowing.Drawable)
	 * 
	 * @pre: p2.x >= p1.x && p2.y >= p1.y
	 */
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		double deltaX = p2.getIntX() - p1.getIntX();
		double deltaY = p2.getIntY() - p1.getIntY();
		double deltaZ = p2.getIntZ() - p1.getIntZ();
		Color deltaC = p2.getColor().subtract(p1.getColor());
		//Color deltaColor = p2.getColor().subtract(p1.getColor());
		
		double slope = deltaY / deltaX;
		double slope_z = deltaZ / deltaX;
		Color slope_C = deltaC.scale(1/deltaX);
		//Color slopeColor= deltaColor.scale(1/deltaX);
		
		double y = p1.getY();
		int x = p1.getIntX();
		double z = p1.getZ();
		int x1 = p2.getIntX();
		Color x_R = p1.getColor();
		//Color pointColor = p1.getColor();

		while(x <= x1){
			drawable.setPixel(x, (int)Math.floor(y+0.5),z, x_R.asARGB());
			x++;
			y+=slope;
			z+=slope_z;
			x_R = x_R.add(slope_C);
		}
		
	}

	public static LineRenderer make() {
		return new AnyOctantLineRenderer(new DDALineRenderer());
	}
}

