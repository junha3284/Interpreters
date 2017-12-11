package geometry;

import geometry.Vertex3D;

public class Transformation {
	private static final int num_col = 4;
	private static final int num_row = 4;
	
	private double[][] matrix;
	
	public Transformation(){
		matrix = new double[num_row][num_col];
		for(int i=0; i < num_col; i++)
			for(int j=0; j < num_col; j++)
				if(i==j)
					matrix[i][j]=1;
				else
					matrix[i][j]=0;
	}
	
	public Transformation(Transformation m){
		matrix = new double[num_row][num_col];
		for(int i=0; i < num_row; i++)
			for(int j=0; j< num_col; j++)
				this.matrix[i][j]=m.matrix[i][j];
	}
	
	public Transformation(double... entries){
		matrix = new double[num_row][num_col];
		if(entries.length!=16){
			System.out.println("length is not right");
			for(int i=0; i < num_col; i++)
				for(int j=0; j < num_col; j++)
					if(i==j)
						matrix[i][j]=1;
					else
						matrix[i][j]=0;
		}
		else
		{	
			for(int i=0; i < entries.length; i++){
				int row = i/4;
				int col = i-4*row;
				matrix[row][col]=entries[i];
			}
		}
	}
	
	public static Transformation identity(){
		return new Transformation();
	}
	
	public static Transformation perspective(double d){
		Transformation p = new Transformation(1,0,0,0,0,1,0,0,0,0,1,0,0,0,1/d,0);
		return p;
	}
	
	public Transformation inverse(){
		Transformation inverse = new Transformation();
		for(int i =0; i < 4; i++)
			for(int j =0; j< 4; j++)
				inverse.matrix[i][j]=this.matrix[i][j];

		inverse.matrix = invert(inverse.matrix);
		return inverse;
	}
	
	public Transformation multiply(Transformation m2){
		Transformation result = new Transformation();
		for(int i=0; i < num_row; i++)
			for(int j=0; j < num_col; j++){
				double temp=0;
				for(int k=0; k < 4; k++)
					temp = temp+(this.matrix[i][k])*(m2.matrix[k][j]);
				result.matrix[i][j]=temp;
			}
		return result;
	}
	
	public void Scale(double sx, double sy, double sz){
		Transformation scaleMatrix = new Transformation();
		scaleMatrix.matrix[0][0] = scaleMatrix.matrix[0][0]*sx;
		scaleMatrix.matrix[1][1] = scaleMatrix.matrix[1][1]*sy;
		scaleMatrix.matrix[2][2] = scaleMatrix.matrix[2][2]*sz;
		this.matrix = this.multiply(scaleMatrix).matrix;
	}
	
	public void Translate(double tx, double ty, double tz){
		Transformation transMatrix = new Transformation();
		transMatrix.matrix[0][3] = tx;
		transMatrix.matrix[1][3] = ty;
		transMatrix.matrix[2][3] = tz;
		this.matrix = this.multiply(transMatrix).matrix;
	}
	
	public void Copy(Transformation m){
		for(int i=0; i < num_row; i++)
			for(int j=0; j< num_col; j++)
				this.matrix[i][j]=m.matrix[i][j];
	}
	
	public void Rotate(String axis, double angle){
		double angle_ramda = angle*Math.PI/180;
		Transformation rotateMatrix;
		if(axis.equals("X")){
			rotateMatrix = new Transformation(1,0,0,0,0,Math.cos(angle_ramda),-1*Math.sin(angle_ramda),0,0,Math.sin(angle_ramda),Math.cos(angle_ramda),0,0,0,0,1);
			this.matrix = this.multiply(rotateMatrix).matrix;
		}
		if(axis.equals("Y")) {
			rotateMatrix = new Transformation(Math.cos(angle_ramda),0,Math.sin(angle_ramda),0,0,1,0,0,-1*Math.sin(angle_ramda),0,Math.cos(angle_ramda),0,0,0,0,1);
			this.matrix = this.multiply(rotateMatrix).matrix;
		}
		if(axis.equals("Z")){
			rotateMatrix = new Transformation(Math.cos(angle_ramda),-1*Math.sin(angle_ramda),0,0,Math.sin(angle_ramda),Math.cos(angle_ramda),0,0,0,0,1,0,0,0,0,1);
			this.matrix = this.multiply(rotateMatrix).matrix;
		}
	}
	
	public Vertex3D transformVertex3D(Vertex3D v){
		double x=v.getX();
		double y=v.getY();
		double z=v.getZ();
		double w=1;
		double[] entries_trans= new double[4];
		for(int i=0; i < 4; i++){
			entries_trans[i] = this.matrix[i][0]*x+this.matrix[i][1]*y+this.matrix[i][2]*z+this.matrix[i][3]*w;
		}
		Point3DH transformed_point = new Point3DH(entries_trans);
		if(v.check_normal()==true){
			Point3DH normal = v.getNormal();
			double x_n = normal.getX();
			double y_n = normal.getY();
			double z_n = normal.getZ();
			double w_n = normal.getW();
			double[] entries_trans_n = new double[4];
			for(int i=0; i < 4; i++){
				Transformation inverse = this.inverse();
				entries_trans_n[i] = inverse.matrix[0][i]*x_n+inverse.matrix[1][i]*y_n+inverse.matrix[2][i]*z_n+inverse.matrix[3][i]*w_n;
			}
			Point3DH transformed_normal = new Point3DH(entries_trans_n).euclidean().normalize();
			return new Vertex3D(transformed_point, transformed_normal,v.getColor());
		}
		return new Vertex3D(transformed_point, v.getColor());
	}
	
	public Vertex3D transformVertex3D_only_point(Vertex3D v){
		double x=v.getX();
		double y=v.getY();
		double z=v.getZ();
		double w=1;
		double[] entries_trans= new double[4];
		for(int i=0; i < 4; i++){
			entries_trans[i] = this.matrix[i][0]*x+this.matrix[i][1]*y+this.matrix[i][2]*z+this.matrix[i][3]*w;
		}
		Point3DH transformed_point = new Point3DH(entries_trans);
		if(v.check_normal()==true){
			return new Vertex3D(transformed_point,v.getNormal(),v.getColor());
		}
		return new Vertex3D(transformed_point, v.getColor());
	}
	
	public void print(){
		for(int i=0; i < num_row; i++){
			for(int j=0; j < num_col; j++){
				System.out.print(matrix[i][j]);
				System.out.print('\t');
			}
			System.out.print('\n');
		}
	}

	// I refer codes in  http://www.sanfoundry.com/java-program-find-inverse-matrix/ for gaussian elimination
    private double[][] invert(double a[][]) 
    {
        int n = a.length;
        double x[][] = new double[n][n];
        double b[][] = new double[n][n];
        int index[] = new int[n];
        for (int i=0; i<n; ++i) 
            b[i][i] = 1;
 
        gaussian(a, index);
 
        for (int i=0; i<n-1; ++i)
            for (int j=i+1; j<n; ++j)
                for (int k=0; k<n; ++k)
                    b[index[j]][k]
                    	    -= a[index[j]][i]*b[index[i]][k];
 
        for (int i=0; i<n; ++i) 
        {
            x[n-1][i] = b[index[n-1]][i]/a[index[n-1]][n-1];
            for (int j=n-2; j>=0; --j) 
            {
                x[j][i] = b[index[j]][i];
                for (int k=j+1; k<n; ++k) 
                {
                    x[j][i] -= a[index[j]][k]*x[k][i];
                }
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }
 
    private void gaussian(double a[][], int index[]) 
    {
        int n = index.length;
        double c[] = new double[n];
 
        for (int i=0; i<n; ++i) 
            index[i] = i;
 
        for (int i=0; i<n; ++i) 
        {
            double c1 = 0;
            for (int j=0; j<n; ++j) 
            {
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1) c1 = c0;
            }
            c[i] = c1;
        }
 
        int k = 0;
        for (int j=0; j<n-1; ++j) 
        {
            double pi1 = 0;
            for (int i=j; i<n; ++i) 
            {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) 
                {
                    pi1 = pi0;
                    k = i;
                }
            }
 
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i=j+1; i<n; ++i) 	
            {
                double pj = a[index[i]][j]/a[index[j]][j];
 
                a[index[i]][j] = pj;
 
                for (int l=j+1; l<n; ++l)
                    a[index[i]][l] -= pj*a[index[j]][l];
            }
        }
    }

}
