package exam.service.exam;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToeicScoreTest {

	@Test
	void blankTestIsTenAndPerfectTestIsNineNinety() {
		assertThat(ToeicScore.listening(0)).isEqualTo(5);
		assertThat(ToeicScore.reading(0)).isEqualTo(5);
		assertThat(ToeicScore.total(0, 0)).isEqualTo(10);

		assertThat(ToeicScore.listening(100)).isEqualTo(495);
		assertThat(ToeicScore.reading(100)).isEqualTo(495);
		assertThat(ToeicScore.total(100, 100)).isEqualTo(990);
	}

	@Test
	void anchorCountsMatchThePracticeChart() {
		assertThat(ToeicScore.listening(50)).isEqualTo(245);
		assertThat(ToeicScore.reading(50)).isEqualTo(220);
		assertThat(ToeicScore.listening(95)).isEqualTo(485);
		assertThat(ToeicScore.reading(96)).isEqualTo(480);
		assertThat(ToeicScore.total(70, 60)).isEqualTo(355 + 275);
	}

	@Test
	void everyRawCountStaysOnTheScaledLadder() {
		int previousListening = 0;
		int previousReading = 0;
		for (int correct = 0; correct <= 100; correct++) {
			int listening = ToeicScore.listening(correct);
			int reading = ToeicScore.reading(correct);
			assertThat(listening % 5).isZero();
			assertThat(reading % 5).isZero();
			assertThat(listening).isBetween(5, 495);
			assertThat(reading).isBetween(5, 495);
			assertThat(listening).isGreaterThanOrEqualTo(previousListening);
			assertThat(reading).isGreaterThanOrEqualTo(previousReading);
			previousListening = listening;
			previousReading = reading;
		}
	}

	@Test
	void outOfRangeCountsAreClamped() {
		assertThat(ToeicScore.total(-3, 150)).isEqualTo(5 + 495);
	}
}
