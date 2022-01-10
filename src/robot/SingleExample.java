package robot;

// Klasa reprezentuj¹ca pojedyñczy przyk³ad
public class SingleExample
{
	private double x;
	private double y;
	private double alfa;
	private double beta;

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getAlfa() {
		return alfa;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setAlfa(double alfa) {
		this.alfa = alfa;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

	public double getBeta() {
		return beta;
	}

	public SingleExample(double x, double y, double alfa, double beta)
	{
		this.x = x;
		this.y = y;
		this.alfa = alfa;
		this.beta = beta;
	}

	public SingleExample() { }
	
	
}
