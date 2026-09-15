package exam.db.repository.history;

import exam.db.entity.History;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends MongoRepository<History, String> {
	List<History> findAllByUserIdAndDeletedIsFalse(String userId);

	History findByUserIdAndDeletedIsFalse(String userId);
}
