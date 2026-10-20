package exam.db.dto.skill;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AddSkillRequest {
	private String id;
	private List<String> topicIds;
}
