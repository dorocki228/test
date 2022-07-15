package l2s.commons.geometry;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 27.04.2019
 * Developed for L2-Scripts.com
 **/
public interface CoordsConverter {
	public static final CoordsConverter DEFAULT_CONVERTER = new CoordsConverter() {
		@Override
		public int convertX(int x) {
			return x;
		}

		@Override
		public int convertY(int y) {
			return y;
		}

		@Override
		public int convertDistance(int distance) {
			return distance;
		}
	};

	int convertX(int x);

	int convertY(int y);

	int convertDistance(int distance);
}
