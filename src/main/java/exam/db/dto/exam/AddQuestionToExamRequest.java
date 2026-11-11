package exam.db.dto.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AddQuestionToExamRequest {
	private String id;
	private List<String> questionIds;
}
