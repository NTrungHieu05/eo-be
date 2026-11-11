package exam.db.dto.question;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateQuestionRequest extends CreateQuestionRequest {
	private String id;
}
