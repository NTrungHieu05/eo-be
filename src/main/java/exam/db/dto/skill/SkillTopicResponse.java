package exam.db.dto.skill;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SkillTopicResponse {
	private String topicId;
	private String topicName;
	private String slug;
}
