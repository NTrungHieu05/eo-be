package exam.db.repository.topic;

import exam.db.entity.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
	List<Topic> findByIdInAndDeletedIsFalse(Collection<String> ids);

	Topic findFirstByIdAndDeletedIsFalse(String id);

	List<Topic> findAllByIdInAndDeletedIsFalse(List<String> ids);
}
