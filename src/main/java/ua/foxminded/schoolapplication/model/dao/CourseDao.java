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
import ua.foxminded.schoolapplication.model.domain.Course;
import ua.foxminded.schoolapplication.model.validation.EntityValidator;

import java.util.*;
import java.util.stream.IntStream;

@Repository
@Transactional
public class CourseDao implements Dao<Course> {
	private static final Logger logger = LoggerFactory.getLogger(CourseDao.class);

	private static final String FIND_BY_ID = String.format("SELECT * FROM %s WHERE %s = :%s",
			DBSchemaConstants.COURSES_TABLE,
			DBSchemaConstants.COURSE_ID,
			DBSchemaConstants.PARAM_COURSE_ID);

	private static final String INSERT_COURSE = String.format("INSERT INTO %s (%s, %s) VALUES (:%s, :%s)",
			DBSchemaConstants.COURSES_TABLE,
			DBSchemaConstants.COURSE_NAME,
			DBSchemaConstants.COURSE_DESCRIPTION,
			DBSchemaConstants.PARAM_COURSE_NAME,
			DBSchemaConstants.PARAM_COURSE_DESCRIPTION);

	private static final String INSERT_STUDENT_COURSE = String.format("INSERT INTO %s (%s, %s) VALUES (:%s, :%s)",
			DBSchemaConstants.STUDENTS_COURSES_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID,
			DBSchemaConstants.STUDENTS_COURSES_COURSE_ID,
			DBSchemaConstants.PARAM_STUDENT_ID,
			DBSchemaConstants.PARAM_COURSE_ID);

	private static final String UPDATE_COURSE = String.format("UPDATE %s SET %s = :%s, %s = :%s WHERE %s = :%s",
			DBSchemaConstants.COURSES_TABLE,
			DBSchemaConstants.COURSE_NAME,
			DBSchemaConstants.PARAM_COURSE_NAME,
			DBSchemaConstants.COURSE_DESCRIPTION,
			DBSchemaConstants.PARAM_COURSE_DESCRIPTION,
			DBSchemaConstants.COURSE_ID,
			DBSchemaConstants.PARAM_COURSE_ID);

	private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE %s = :%s",
			DBSchemaConstants.COURSES_TABLE,
			DBSchemaConstants.COURSE_ID,
			DBSchemaConstants.PARAM_COURSE_ID);

	private static final String DELETE_STUDENT_COURSE = String.format("DELETE FROM %s WHERE %s = :%s AND %s = :%s",
			DBSchemaConstants.STUDENTS_COURSES_TABLE,
			DBSchemaConstants.STUDENTS_COURSES_STUDENT_ID,
			DBSchemaConstants.PARAM_STUDENT_ID,
			DBSchemaConstants.STUDENTS_COURSES_COURSE_ID,
			DBSchemaConstants.PARAM_COURSE_ID);

	private RowMapper<Course> courseRowMapper;
	private NamedParameterJdbcTemplate namedJdbcTemplate;
	private EntityValidator<Course> courseValidator;

	public CourseDao(RowMapper<Course> courseRowMapper, NamedParameterJdbcTemplate namedJdbcTemplate,
			EntityValidator<Course> courseValidator) {

		this.courseRowMapper = courseRowMapper;
		this.namedJdbcTemplate = namedJdbcTemplate;
		this.courseValidator = courseValidator;
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<Course> findById(Long id) {
		logger.debug("Searching for course with ID: {}", id);
		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_COURSE_ID, id);
		try {
			Course course = namedJdbcTemplate.queryForObject(FIND_BY_ID, params, courseRowMapper);
			logger.info("Course found: {}", course);
			return Optional.ofNullable(course);
		} catch (Exception e) {
			logger.warn("Course not found with ID: {}", id, e);
			return Optional.empty();
		}
	}

	@Override
	public List<Course> save(List<Course> courses) {
		courseValidator.validateEntities(
				Optional.ofNullable(courses).orElseGet(Collections::emptyList).toArray(new Course[0]));
		logger.debug("Saving courses: {}", courses);

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(courses);
		KeyHolder keyHolder = new GeneratedKeyHolder();

		int[] affectedRows = namedJdbcTemplate
				.batchUpdate(INSERT_COURSE, batch, keyHolder, new String[] { DBSchemaConstants.COURSE_ID });
		List<Map<String, Object>> keyList = keyHolder.getKeyList();

		boolean allSuccessful = Arrays.stream(affectedRows).allMatch(count -> count == 1);
		if (!allSuccessful || keyList.size() != courses.size()) {
			logger.warn("Mismatch during batch insert. Affected: {}, Keys: {}, Expected: {}",
					Arrays.toString(affectedRows),
					keyList.size(),
					courses.size());
			throw new UnexpectedAffectedRowsException(
					String.format("Batch insert issue. AffectedRows match: %s, Keys match: %s",
							allSuccessful,
							keyList.size() == courses.size()));
		}

		IntStream.range(0, courses.size())
				.forEach(i -> courses.get(i).setCourseId((Long) keyList.get(i).get(DBSchemaConstants.COURSE_ID)));

		logger.info("All courses saved successfully: {}", courses);
		return courses;
	}

	public void saveStudentToCourse(Long studentId, Long courseId) {
		logger.debug("Adding student-course relation: studentId={}, courseId={}", studentId, courseId);
		Map<String, Object> params = new HashMap<>();
		params.put(DBSchemaConstants.PARAM_STUDENT_ID, studentId);
		params.put(DBSchemaConstants.PARAM_COURSE_ID, courseId);

		try {
			int rowsAffected = namedJdbcTemplate.update(INSERT_STUDENT_COURSE, params);
			if (rowsAffected != 1) {
				logger.warn("Failed to insert student-course relation: {}", params);
				throw new UnexpectedAffectedRowsException("Student-course relation insert failed.");
			}
			logger.info("Student-course relation added: {}", params);
		} catch (Exception e) {
			logger.error("Error while adding student-course relation", e);
			throw e;
		}
	}

	@Override
	public void update(Course course) {
		courseValidator.validateEntities(course);
		logger.debug("Updating course: {}", course);

		Map<String, Object> params = new HashMap<>();
		params.put(DBSchemaConstants.PARAM_COURSE_ID, course.getCourseId());
		params.put(DBSchemaConstants.PARAM_COURSE_NAME, course.getCourseName());
		params.put(DBSchemaConstants.PARAM_COURSE_DESCRIPTION, course.getCourseDescription());

		int updated = namedJdbcTemplate.update(UPDATE_COURSE, params);
		if (updated != 1) {
			logger.warn("No course found to update with ID: {}", course.getCourseId());
			throw new UnexpectedAffectedRowsException("No course found to update with ID: " + course.getCourseId());
		}

		logger.info("Course updated successfully: {}", course);
	}

	@Override
	public void deleteById(Long id) {
		logger.debug("Attempting to delete course with ID: {}", id);
		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_COURSE_ID, id);

		int deleted = namedJdbcTemplate.update(DELETE_BY_ID, params);
		if (deleted != 1) {
			logger.warn("No course found to delete with ID: {}", id);
			throw new UnexpectedAffectedRowsException("No course found to delete with ID: " + id);
		}

		logger.info("Course deleted successfully with ID: {}", id);
	}

	public void deleteStudentFromCourse(Long studentId, Long courseId) {
		logger.debug("Removing student-course relation: studentId={}, courseId={}", studentId, courseId);
		Map<String, Object> params = new HashMap<>();
		params.put(DBSchemaConstants.PARAM_STUDENT_ID, studentId);
		params.put(DBSchemaConstants.PARAM_COURSE_ID, courseId);

		int rowsAffected = namedJdbcTemplate.update(DELETE_STUDENT_COURSE, params);
		if (rowsAffected != 1) {
			logger.warn("Failed to delete student-course relation: {}", params);
			throw new UnexpectedAffectedRowsException("Student-course relation delete failed.");
		}

		logger.info("Student-course relation removed: {}", params);
	}
}
