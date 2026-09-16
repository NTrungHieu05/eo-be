package exam.db.dto.card;

import exam.db.dto.BaseListRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListCardRequest extends BaseListRequest {
	private String keyword;
	private String examId;
}
