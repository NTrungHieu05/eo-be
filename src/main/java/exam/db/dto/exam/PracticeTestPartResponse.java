package exam.db.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeTestPartResponse {
	private Integer partNumber;
	private String name;
	private String topicId;
	private Integer questionCount;
	private Integer cardCount;
}
