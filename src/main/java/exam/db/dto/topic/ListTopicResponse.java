package exam.db.dto.topic;

import exam.db.dto.TotalResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ListTopicResponse extends TotalResponse {
	private List<TopicResponse> topics = new ArrayList<>();

	public ListTopicResponse(List<TopicResponse> topics, long total) {
		this.topics = topics;
		super.setTotal(total);
	}
}
