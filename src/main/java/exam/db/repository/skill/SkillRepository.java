package exam.db.repository.skill;

import exam.db.entity.Skill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SkillRepository extends MongoRepository<Skill, String> {
	Skill findFirstByIdAndDeletedIsFalse(String id);

	List<Skill> findByIdInAndDeletedIsFalse(Collection<String> ids);

	List<Skill> findAllByDeletedIsFalse();
}
