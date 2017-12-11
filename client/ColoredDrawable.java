package client;

import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;

public class ColoredDrawable extends DrawableDecorator {
	private final int argb_color;
	
	public ColoredDrawable(Drawable delegate, int argb_color ){
		super(delegate);
		this.argb_color=argb_color;
	}
	
	@Override
	public void clear() {
		this.fill(argb_color, Double.MAX_VALUE);
	}
}
