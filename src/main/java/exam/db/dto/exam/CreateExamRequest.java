package exam.db.dto.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateExamRequest {
	private String name;
	private String topicId;
	private List<String> cardIds;
}
