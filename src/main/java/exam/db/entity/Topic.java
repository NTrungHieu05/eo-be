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

@Document(collection = CollectionConst.TOPIC)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Topic extends AbstractVersion {
	@Id
	private String id;

	@Field(FieldConst.NAME)
	private String name;

	@Field(FieldConst.EXAM_IDS)
	private List<String> examIds;

	public Topic(String name) {
		this.name = name;
	}
}
