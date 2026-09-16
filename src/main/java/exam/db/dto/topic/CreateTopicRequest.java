package exam.db.dto.topic;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateTopicRequest {
	private String name;
	private List<String> examIds;
}
