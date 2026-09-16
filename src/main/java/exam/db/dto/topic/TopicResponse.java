package exam.db.dto.topic;

import exam.db.dto.exam.ExamDataResponse;
import exam.db.entity.Topic;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class TopicResponse {
	private String id;
	private String name;
	private List<String> examIds;
	private List<ExamDataResponse> exams;

	public TopicResponse(Topic topic) {
		BeanUtils.copyProperties(topic, this);
	}
}
