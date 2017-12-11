package windowing.drawable;
import java.util.*;

// This class just sends every message to its delegate.
// It is intended for subclassing; subclasses simply override
// whatever behaviour they wish to modify.

public class DrawableDecorator implements Drawable {
	protected final Drawable delegate;
	protected ArrayList<ArrayList<Double>> zBuffer;

	public DrawableDecorator(Drawable delegate) {
		this.delegate = delegate;
		zBuffer = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> temp;
		for(int i=0; i < delegate.getHeight(); i++){
			temp = new ArrayList<Double>();
			for(int j=0; j <delegate.getWidth(); j++)
				temp.add((double)-201);
			zBuffer.add(i,temp);
		}
	}
	
	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		delegate.setPixel(x,  y,  z, argbColor);
	}
	@Override
	public int getPixel(int x, int y) {
		return delegate.getPixel(x,  y);
	}
	@Override
	public double getZValue(int x, int y) {
		return delegate.getZValue(x, y);
	};
	
	@Override
	public int getWidth() {
		return delegate.getWidth();
	}
	@Override
	public int getHeight() {
		return delegate.getHeight();
	}

}