package exam.service.skill;

import exam.db.dto.skill.CreateSkillRequest;
import exam.db.dto.skill.DeleteSkillRequest;
import exam.db.dto.skill.ListSkillRequest;
import exam.db.dto.skill.ListSkillResponse;
import exam.db.dto.skill.SkillResponse;
import exam.db.dto.skill.UpdateSkillRequest;
import exam.ultis.ExamBaseException;

import java.util.List;

public interface SkillService {
	SkillResponse create(CreateSkillRequest request) throws ExamBaseException;

	SkillResponse update(UpdateSkillRequest request) throws ExamBaseException;

	Boolean deleteMultiple(DeleteSkillRequest request) throws ExamBaseException;

	SkillResponse getDetail(String id) throws ExamBaseException;

	ListSkillResponse getListSkillForAdmin(ListSkillRequest request) throws ExamBaseException;

	List<SkillResponse> getAllSkillForHeader();
}
