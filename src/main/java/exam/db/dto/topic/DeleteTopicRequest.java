package exam.db.dto.topic;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DeleteTopicRequest {
	private List<String> ids;
}
