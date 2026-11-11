package exam.db.dto.question;

import exam.db.dto.BaseListRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListQuestionRequest extends BaseListRequest {
	private String keyword;
	private String examId;
}
