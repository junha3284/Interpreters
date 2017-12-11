package client;

import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;
import windowing.graphics.Color;

public class DepthCueingDrawable  extends DrawableDecorator{
	protected double MAX_Z;
	protected double MIN_Z;
	protected Color c;
	
	public DepthCueingDrawable(Drawable delegate,double MAX_Z, double MIN_Z){
		super(delegate);
		this.MAX_Z = MAX_Z;
		this.MIN_Z = MIN_Z;
		this.c = Color.BLACK;
	}
	
	public DepthCueingDrawable(Drawable delegate,double MAX_Z, double MIN_Z, Color c){
		super(delegate);
		this.MAX_Z = MAX_Z;
		this.MIN_Z = MIN_Z;
		this.c = c;
	}
	
	public void setDepth(double near, double far, Color background){
		this.MAX_Z=near;
		this.MIN_Z=far;
		c = background;
	}
	
	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		if(0<=x && x< this.getWidth() && 0<=y&& y<this.getHeight()){
			if(this.zBuffer.get(y).get(x) < z){
				if(z > MAX_Z){
					delegate.setPixel(x, y, z, argbColor);
					this.zBuffer.get(y).set(x, z);
				}
				
				else if(MIN_Z <= z ){
					Color surface = Color.fromARGB(argbColor);
					delegate.setPixel(x, y, z, surface.add(c.subtract(surface).scale((z-(MAX_Z))/(MIN_Z-MAX_Z))).asARGB());
					this.zBuffer.get(y).set(x, z);
				}
				else{
					delegate.setPixel(x, y, z, c.asARGB());
					this.zBuffer.get(y).set(x, z);
				}
			}
		}
	}
	
	@Override
	public void clear() {
		//this.fill(Color.BLACK.asARGB(), Double.MAX_VALUE);
		for(int i=0; i < this.delegate.getHeight(); i++)
			for(int j =0; j < this.delegate.getWidth(); j++)
				this.zBuffer.get(i).set(j, (double)-201);
	}
}
