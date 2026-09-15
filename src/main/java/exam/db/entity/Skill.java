package exam.db.entity;

import exam.db.enums.CollectionConst;
import exam.db.enums.FieldConst;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = CollectionConst.SKILL)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Skill extends AbstractVersion {
	@Id
	private String id;

	@Field(FieldConst.NAME)
	private String name;

	@Field(FieldConst.TOPIC_IDS)
	private List<String> topicIds;

	public Skill(String name) {
		this.name = name;
	}
}
