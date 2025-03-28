package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.UnexpectedAffectedRowsException;
import ua.foxminded.schoolapplication.model.domain.Student;
import ua.foxminded.schoolapplication.model.validation.EntityValidator;

import java.util.*;
import java.util.stream.IntStream;

@Repository
@Transactional
public class StudentDao implements Dao<Student> {

	private static final Logger logger = LoggerFactory.getLogger(StudentDao.class);

	private static final String FIND_BY_ID = String.format("SELECT * FROM %s WHERE %s = :%s",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENT_ID,
			DBSchemaConstants.PARAM_STUDENT_ID);

	private static final String FIND_BY_GROUP_NAME = String.format(
			"SELECT s.* FROM %s s " + "JOIN %s g ON s.%s = g.%s " + "WHERE g.%s = :%s",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.GROUPS_TABLE,
			DBSchemaConstants.STUDENT_GROUP_ID,
			DBSchemaConstants.GROUP_ID,
			DBSchemaConstants.GROUP_NAME,
			DBSchemaConstants.PARAM_GROUP_NAME);

	private static final String FIND_BY_COURSE_NAME = String.format(
			"SELECT s.* FROM %s s JOIN %s sc ON s.%s = sc.%s JOIN %s c ON sc.%s = c.%s WHERE c.%s = :%s",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_TABLE,
			DBSchemaConstants.STUDENT_ID,
			DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID,
			DBSchemaConstants.COURSES_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_COURSE_ID,
			DBSchemaConstants.COURSE_ID,
			DBSchemaConstants.COURSE_NAME,
			DBSchemaConstants.PARAM_COURSE_NAME);

	private static final String INSERT_STUDENT = String.format("INSERT INTO %s (%s, %s, %s) VALUES (:%s, :%s, :%s)",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENT_GROUP_ID,
			DBSchemaConstants.STUDENT_FIRST_NAME,
			DBSchemaConstants.STUDENT_LAST_NAME,
			DBSchemaConstants.PARAM_GROUP_ID,
			DBSchemaConstants.PARAM_FIRST_NAME,
			DBSchemaConstants.PARAM_LAST_NAME);

	private static final String UPDATE_STUDENT = String.format(
			"UPDATE %s SET %s = :%s, %s = :%s, %s = :%s WHERE %s = :%s",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENT_GROUP_ID,
			DBSchemaConstants.PARAM_GROUP_ID,
			DBSchemaConstants.STUDENT_FIRST_NAME,
			DBSchemaConstants.PARAM_FIRST_NAME,
			DBSchemaConstants.STUDENT_LAST_NAME,
			DBSchemaConstants.PARAM_LAST_NAME,
			DBSchemaConstants.STUDENT_ID,
			DBSchemaConstants.PARAM_STUDENT_ID);

	private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE %s = :%s",
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.STUDENT_ID,
			DBSchemaConstants.PARAM_STUDENT_ID);

	private RowMapper<Student> studentRowMapper;
	private NamedParameterJdbcTemplate namedJdbcTemplate;
	private EntityValidator<Student> studentValidator;

	public StudentDao(RowMapper<Student> studentRowMapper, NamedParameterJdbcTemplate namedJdbcTemplate,
			EntityValidator<Student> studentValidator) {

		this.studentRowMapper = studentRowMapper;
		this.namedJdbcTemplate = namedJdbcTemplate;
		this.studentValidator = studentValidator;
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<Student> findById(Long id) {
		logger.debug("Searching for student with ID: {}", id);
		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_STUDENT_ID, id);
		try {
			Student student = namedJdbcTemplate.queryForObject(FIND_BY_ID, params, studentRowMapper);
			logger.info("Student found: {}", student);
			return Optional.ofNullable(student);
		} catch (Exception e) {
			logger.warn("Student not found with ID: {}", id, e);
			return Optional.empty();
		}
	}

	@Transactional(readOnly = true)
	public List<Student> findByGroupName(String groupName) {
		logger.debug("Finding students by group name: {}", groupName);

		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_GROUP_NAME, groupName);

		List<Student> students = namedJdbcTemplate.query(FIND_BY_GROUP_NAME, params, studentRowMapper);
		logger.info("Found {} students for group '{}'", students.size(), groupName);
		return students;
	}

	@Transactional(readOnly = true)
	public List<Student> findByCourseName(String courseName) {
		logger.debug("Finding students by course name: {}", courseName);

		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_COURSE_NAME, courseName);

		List<Student> students = namedJdbcTemplate.query(FIND_BY_COURSE_NAME, params, studentRowMapper);
		logger.info("Found {} students for course '{}'", students.size(), courseName);
		return students;
	}

	@Override
	public List<Student> save(List<Student> students) {
		studentValidator.validateEntities(
				Optional.ofNullable(students).orElseGet(Collections::emptyList).stream().toArray(Student[]::new));
		logger.debug("Saving students: {}", students);

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(students);
		KeyHolder keyHolder = new GeneratedKeyHolder();

		int[] affectedRows = namedJdbcTemplate
				.batchUpdate(INSERT_STUDENT, batch, keyHolder, new String[] { DBSchemaConstants.STUDENT_ID });
		List<Map<String, Object>> keyList = keyHolder.getKeyList();

		boolean allSuccessful = Arrays.stream(affectedRows).allMatch(count -> count == 1);
		if (!allSuccessful || keyList.size() != students.size()) {
			logger.warn("Mismatch during batch insert. Affected: {}, Keys: {}, Expected: {}",
					Arrays.toString(affectedRows),
					keyList.size(),
					students.size());
			throw new UnexpectedAffectedRowsException(String.format(
					"Batch insert issue. AffectedRows match: %s, Keys match: %s [Affected: %s, Keys: %s, Students: %s]",
					allSuccessful,
					keyList.size() == students.size(),
					Arrays.toString(affectedRows),
					keyList.size(),
					students.size()));
		}

		IntStream.range(0, students.size())
				.forEach(i -> students.get(i).setStudentId((Long) keyList.get(i).get(DBSchemaConstants.STUDENT_ID)));

		logger.info("All students saved successfully: {}", students);
		return students;
	}

	@Override
	public void update(Student student) {
		studentValidator.validateEntities(student);
		logger.debug("Updating student: {}", student);

		Map<String, Object> params = new HashMap<>();
		params.put(DBSchemaConstants.PARAM_STUDENT_ID, student.getStudentId());
		params.put(DBSchemaConstants.PARAM_GROUP_ID, student.getGroupId());
		params.put(DBSchemaConstants.PARAM_FIRST_NAME, student.getFirstName());
		params.put(DBSchemaConstants.PARAM_LAST_NAME, student.getLastName());

		int updated = namedJdbcTemplate.update(UPDATE_STUDENT, params);
		if (updated != 1) {
			logger.warn("No student found to update with ID: {}", student.getStudentId());
			throw new UnexpectedAffectedRowsException("No student found to update with ID: " + student.getStudentId());
		}

		logger.info("Student updated successfully: {}", student);
	}

	@Override
	public void deleteById(Long id) {
		logger.debug("Attempting to delete student with ID: {}", id);
		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_STUDENT_ID, id);

		int deleted = namedJdbcTemplate.update(DELETE_BY_ID, params);
		if (deleted != 1) {
			logger.warn("No student found to delete with ID: {}", id);
			throw new UnexpectedAffectedRowsException("No student found to delete with ID: " + id);
		}

		logger.info("Student deleted successfully with ID: {}", id);
	}
}
