package exam.db.repository.exam;

import exam.db.entity.ExamAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAttemptRepository extends MongoRepository<ExamAttempt, String> {
	List<ExamAttempt> findAllByUserIdAndDeletedIsFalseOrderBySubmittedAtDesc(String userId);

	ExamAttempt findFirstByIdAndUserIdAndDeletedIsFalse(String id, String userId);
}
