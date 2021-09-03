package bweb;

public enum Piece {
	Small,
	Medium,
	Large,
	Addon,
	Row;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static Piece forValue(int value) {
		return values()[value];
	}
}