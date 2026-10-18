package exam.service.exam;

/**
 * Practice conversion from raw correct answers (0–100) to a scaled section
 * score (5–495, step 5). Listening and Reading use different curves, so a
 * perfect test is 990 and a blank test is 10.
 *
 * ETS publishes a different table for each test form. This chart follows that
 * public shape for practice scoring; it is not an official form key.
 */
public final class ToeicScore {

	private static final int[][] LISTENING = {
			{0, 5},
			{10, 45},
			{20, 90},
			{30, 140},
			{40, 190},
			{50, 245},
			{60, 300},
			{70, 355},
			{80, 410},
			{90, 460},
			{95, 485},
			{100, 495}
	};

	private static final int[][] READING = {
			{0, 5},
			{10, 35},
			{20, 75},
			{30, 120},
			{40, 170},
			{50, 220},
			{60, 275},
			{70, 330},
			{80, 390},
			{90, 445},
			{96, 480},
			{100, 495}
	};

	private ToeicScore() {
	}

	public static int listening(int correct) {
		return scale(correct, LISTENING);
	}

	public static int reading(int correct) {
		return scale(correct, READING);
	}

	public static int total(int listeningCorrect, int readingCorrect) {
		return listening(listeningCorrect) + reading(readingCorrect);
	}

	private static int scale(int correct, int[][] anchors) {
		int raw = Math.max(0, Math.min(100, correct));
		if (raw <= anchors[0][0]) {
			return anchors[0][1];
		}
		for (int i = 1; i < anchors.length; i++) {
			int highRaw = anchors[i][0];
			int highScore = anchors[i][1];
			int lowRaw = anchors[i - 1][0];
			int lowScore = anchors[i - 1][1];
			if (raw <= highRaw) {
				double ratio = (raw - lowRaw) / (double) (highRaw - lowRaw);
				int rounded = (int) (Math.round((lowScore + ratio * (highScore - lowScore)) / 5.0) * 5);
				return Math.max(5, Math.min(495, rounded));
			}
		}
		return 495;
	}
}
