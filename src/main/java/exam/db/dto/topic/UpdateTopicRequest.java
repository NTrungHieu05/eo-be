package exam.db.dto.topic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateTopicRequest extends CreateTopicRequest {
	private String id;
}
