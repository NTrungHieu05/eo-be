package exam.db.repository.exam;

import exam.db.entity.Exam;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ExamRepository extends MongoRepository<Exam, String> {
	Exam findFirstByIdAndDeletedIsFalse(String id);

	List<Exam> findByIdInAndDeletedIsFalse(Collection<String> ids);
}
