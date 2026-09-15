package exam.db.repository.card;

import exam.db.entity.Card;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CardRepository extends MongoRepository<Card, String> {
	Card findFirstByIdAndDeletedIsFalse(String id);

	List<Card> findByIdInAndDeletedIsFalse(Collection<String> ids);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 2 } }"
	})
	List<Card> findRandomTwoCardsByTopicId(String topicId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 4 } }"
	})
	List<Card> findRandomFourCardsByTopicId(String topicId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 6 } }"
	})
	List<Card> findRandomSixCardsByTopicId(String topicId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 12 } }"
	})
	List<Card> findRandomTwelveCardsByTopicId(String topicId);

	@Aggregation(pipeline = {
			"{ $match: { topicId: ?0 } }",
			"{ $sample: { size: 16 } }"
	})
	List<Card> findRandomSixteenCardsByTopicId(String topicId);
}
