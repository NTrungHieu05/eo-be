package exam.db.dto.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ToeicScoreResponse {
	private int listeningCorrect;
	private int readingCorrect;
	private int listeningScore;
	private int readingScore;
	private int totalScore;
}
