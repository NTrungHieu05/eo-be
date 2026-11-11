package exam.db.repository.question;

import exam.db.entity.Question;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
	Question findFirstByIdAndDeletedIsFalse(String id);

	List<Question> findByIdInAndDeletedIsFalse(Collection<String> ids);

	List<Question> findAllByDeletedIsFalse();

	List<Question> findAllByTopicIdAndDeletedIsFalse(String topicId);

	List<Question> findByExamIdAndDeletedIsFalse(String examId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 2 } }"
	})
	List<Question> findRandomTwoQuestionsByTopicId(String topicId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 4 } }"
	})
	List<Question> findRandomFourQuestionsByTopicId(String topicId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 6 } }"
	})
	List<Question> findRandomSixQuestionsByTopicId(String topicId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 12 } }"
	})
	List<Question> findRandomTwelveQuestionsByTopicId(String topicId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 16 } }"
	})
	List<Question> findRandomSixteenQuestionsByTopicId(String topicId);
}
