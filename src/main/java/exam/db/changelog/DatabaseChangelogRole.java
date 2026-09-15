package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Role;
import exam.db.repository.role.RoleRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@ChangeLog(order = "001")
@Slf4j
public class DatabaseChangelogRole {

	@ChangeSet(id = "role_default", author = "system", order = "001")
	public void initializerRole(RoleRepository roleRepo) {
		List<Role> roles = new ArrayList<>();
		roles.add(new Role("ROLE_USER"));
		roles.add(new Role("ROLE_ADMIN"));
		roles.add(new Role("ROLE_TEACHING_ASSISTANT"));
		roleRepo.saveAll(roles);
		log.info("Seeded default roles");
	}
}
