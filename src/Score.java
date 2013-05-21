
public class Score {
	public static final Score ZERO = new Score(0, 0);
	
	private Double boring = null;
	private Double fantasyland = null;

	public Score(double boring, double fantasyland) {
		this.boring = boring;
		this.fantasyland = fantasyland;
	}
	
	public Double getBoring() {
		return boring;
	}
	
	public Double getFantasyland() {
		return fantasyland;
	}
}
