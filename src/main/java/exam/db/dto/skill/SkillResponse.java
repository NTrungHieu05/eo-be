package exam.db.dto.skill;

import exam.db.entity.Skill;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class SkillResponse {
	private String id;
	private String name;
	private List<String> topicIds;
	private List<SkillTopicResponse> topics;

	public SkillResponse(Skill skill) {
		BeanUtils.copyProperties(skill, this);
	}
}
