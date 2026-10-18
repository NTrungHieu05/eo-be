package exam.db.dto.history;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryRequestTest {

	@Test
	void progressAcceptsNumberStringAndNull() {
		HistoryRequest request = new HistoryRequest();

		request.setProgress(12);
		assertThat(request.getProgress()).isEqualTo(12);

		request.setProgress(12.9d);
		assertThat(request.getProgress()).isEqualTo(12);

		request.setProgress("7");
		assertThat(request.getProgress()).isEqualTo(7);

		request.setProgress(null);
		assertThat(request.getProgress()).isNull();
	}
}
