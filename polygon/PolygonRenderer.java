package polygon;

import polygon.Shader;
import shading.PixelShader;
import windowing.drawable.Drawable;

public interface PolygonRenderer {
	// assumes polygon is ccw.
	public void drawPolygon(Polygon polygon, Drawable drawable, PixelShader pixelShader);

	default public void drawPolygon(Polygon polygon, Drawable panel) {
		drawPolygon(polygon, panel,  (a,b) -> b.getColor());
	};
}
