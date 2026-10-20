package exam.db.dto.topic;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AddExamToTopicRequest {
	private String id;
	private List<String> examIds;
}
