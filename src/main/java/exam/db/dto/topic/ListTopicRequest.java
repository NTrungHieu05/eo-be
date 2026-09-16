package exam.db.dto.topic;

import exam.db.dto.BaseListRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListTopicRequest extends BaseListRequest {
	private String keyword;
}
