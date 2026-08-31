package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import lombok.extern.slf4j.Slf4j;

@ChangeLog(order = "000")
@Slf4j
public class DatabaseChangelogInit {

	@ChangeSet(id = "init_placeholder", author = "system", order = "000")
	public void placeholder() {
		log.info("Mongock ready. Role/Skill/Topic seed comes in Module 3.");
	}
}
