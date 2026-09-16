package exam.db.dto.skill;

import exam.db.dto.TotalResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ListSkillResponse extends TotalResponse {
	private List<SkillResponse> skills = new ArrayList<>();

	public ListSkillResponse(List<SkillResponse> skills, long total) {
		this.skills = skills;
		super.setTotal(total);
	}
}
