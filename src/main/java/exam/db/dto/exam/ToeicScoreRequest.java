package exam.db.dto.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ToeicScoreRequest {
	private Integer listeningCorrect;
	private Integer readingCorrect;
}
