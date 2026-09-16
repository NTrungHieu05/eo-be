package exam.db.dto.skill;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateSkillRequest {
	private String name;
	private List<String> topicIds;
}
