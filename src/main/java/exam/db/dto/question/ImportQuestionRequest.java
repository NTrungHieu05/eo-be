package exam.db.dto.question;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImportQuestionRequest {
	private String filePath;
}
