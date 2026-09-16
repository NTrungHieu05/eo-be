package exam.db.dto.exam;

import exam.db.dto.BaseListRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListExamRequest extends BaseListRequest {
	private String keyword;
}
