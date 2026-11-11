package exam.db.dto.question;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DeleteQuestionRequest {
	private List<String> ids;
}
