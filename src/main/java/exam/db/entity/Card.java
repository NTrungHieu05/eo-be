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

@Document(collection = CollectionConst.CARD)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Card extends AbstractVersion {
	@Id
	private String id;

	@Field(FieldConst.QUESTION)
	private Question question;

	@Field(FieldConst.ANSWER)
	private Answer answer;

	@Field(FieldConst.TOPIC_ID)
	private String topicId;

	@Field(FieldConst.CHILD_CARDS)
	private List<Card> childCards;

	@Field(FieldConst.EXAM_ID)
	private String examId;

	@Field(FieldConst.IS_QUESTION_GROUP)
	private Boolean isQuestionGroup;
}
