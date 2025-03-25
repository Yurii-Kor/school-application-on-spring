package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.TestcontainersConfiguration;

import ua.foxminded.schoolapplication.config.ApplicationConfig;
import ua.foxminded.schoolapplication.model.dao.exception.UnexpectedAffectedRowsException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Course;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@JdbcTest
@Import({ CourseDao.class, GroupDao.class, StudentDao.class, TestcontainersConfiguration.class,
		ApplicationConfig.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CourseDaoTest {

	static final Long DEFAULT_ID = 0L;
	static final Long NON_EXISTENT_ID = 999L;

	static final String DEFAULT_COURSE_NAME = "Cyber Security";
	static final String UPDATED_COURSE_NAME = "Advanced Security";
	static final String DEFAULT_DESCRIPTION = "Intro to cyber threats";
	static final String UPDATED_DESCRIPTION = "Detailed exploration of network attacks";
	static final String NULL_NAME = null;
	static final String DUPLICATE_COURSE_NAME = "Duplicate Course";

	static final String DEFAULT_GROUP_NAME = "TestGroup-11";

	static final String STUDENT_FIRST_NAME = "John";
	static final String STUDENT_LAST_NAME = "Doe";

	static final int GENERATED_INDEX = 0;

	@Autowired
	private CourseDao courseDao;

	@Autowired
	private GroupDao groupDao;

	@Autowired
	private StudentDao studentDao;

	@Test
	@DisplayName("Save and retrieve course")
	void saveShouldSaveAndFindCourse() {
		Course saved = courseDao.save(List.of(new Course(DEFAULT_ID, DEFAULT_COURSE_NAME, DEFAULT_DESCRIPTION)))
				.get(GENERATED_INDEX);

		assertNotNull(saved.getCourseId());
		Optional<Course> found = courseDao.findById(saved.getCourseId());

		assertTrue(found.isPresent());
		assertEquals(DEFAULT_COURSE_NAME, found.get().getCourseName());
		assertEquals(DEFAULT_DESCRIPTION, found.get().getCourseDescription());

		courseDao.deleteById(saved.getCourseId());
	}

	@Test
	@DisplayName("Successfully add student to course")
	void saveStudentToCourseShouldAddRelationSuccessfully() {
		Group savedGroup = groupDao.save(List.of(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME))).get(GENERATED_INDEX);

		Student savedStudent = studentDao
				.save(List.of(new Student(DEFAULT_ID, savedGroup.getGroupId(), STUDENT_FIRST_NAME, STUDENT_LAST_NAME)))
				.get(GENERATED_INDEX);

		Course savedCourse = courseDao.save(List.of(new Course(DEFAULT_ID, DEFAULT_COURSE_NAME, DEFAULT_DESCRIPTION)))
				.get(GENERATED_INDEX);

		Long studentId = savedStudent.getStudentId();
		Long courseId = savedCourse.getCourseId();

		assertDoesNotThrow(() -> courseDao.saveStudentToCourse(studentId, courseId));

		studentDao.deleteById(studentId);
		courseDao.deleteById(courseId);
		groupDao.deleteById(savedGroup.getGroupId());
	}

	@Test
	@DisplayName("Update existing course")
	void updateShouldModifyExistingCourse() {
		Course saved = courseDao.save(List.of(new Course(DEFAULT_ID, DEFAULT_COURSE_NAME, DEFAULT_DESCRIPTION)))
				.get(GENERATED_INDEX);

		saved.setCourseName(UPDATED_COURSE_NAME);
		saved.setCourseDescription(UPDATED_DESCRIPTION);
		courseDao.update(saved);

		Optional<Course> updated = courseDao.findById(saved.getCourseId());
		assertTrue(updated.isPresent());
		assertEquals(UPDATED_COURSE_NAME, updated.get().getCourseName());
		assertEquals(UPDATED_DESCRIPTION, updated.get().getCourseDescription());

		courseDao.deleteById(saved.getCourseId());
	}

	@Test
	@DisplayName("Delete existing course")
	void deleteShouldRemoveCourse() {
		Course saved = courseDao.save(List.of(new Course(DEFAULT_ID, DEFAULT_COURSE_NAME, DEFAULT_DESCRIPTION)))
				.get(GENERATED_INDEX);

		courseDao.deleteById(saved.getCourseId());
		Optional<Course> deleted = courseDao.findById(saved.getCourseId());

		assertFalse(deleted.isPresent());
	}

	@Test
	@DisplayName("Successfully delete student from course")
	void deleteStudentFromCourseShouldRemoveRelationSuccessfully() {
		Group group = groupDao.save(List.of(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME))).get(GENERATED_INDEX);

		Student student = studentDao
				.save(List.of(new Student(DEFAULT_ID, group.getGroupId(), STUDENT_FIRST_NAME, STUDENT_LAST_NAME)))
				.get(GENERATED_INDEX);

		Course course = courseDao.save(List.of(new Course(DEFAULT_ID, DEFAULT_COURSE_NAME, DEFAULT_DESCRIPTION)))
				.get(GENERATED_INDEX);

		Long studentId = student.getStudentId();
		Long courseId = course.getCourseId();

		courseDao.saveStudentToCourse(studentId, courseId);

		assertDoesNotThrow(() -> courseDao.deleteStudentFromCourse(studentId, courseId));

		studentDao.deleteById(studentId);
		courseDao.deleteById(courseId);
		groupDao.deleteById(group.getGroupId());
	}

	@Test
	@DisplayName("Saving course with null name should throw ValidationException")
	void saveShouldThrowIfNameIsNull() {
		Course invalid = new Course(DEFAULT_ID, NULL_NAME, DEFAULT_DESCRIPTION);
		assertThrows(ValidationException.class, () -> courseDao.save(List.of(invalid)));
	}

	@Test
	@DisplayName("Saving duplicate course name should throw DuplicateKeyException")
	void saveShouldThrowIfNameIsDuplicate() {
		Course original = new Course(DEFAULT_ID, DUPLICATE_COURSE_NAME, DEFAULT_DESCRIPTION);
		Course duplicate = new Course(DEFAULT_ID, DUPLICATE_COURSE_NAME, UPDATED_DESCRIPTION);

		Course saved = courseDao.save(List.of(original)).get(GENERATED_INDEX);

		assertThrows(DuplicateKeyException.class, () -> courseDao.save(List.of(duplicate)));

		courseDao.deleteById(saved.getCourseId());
	}

	@Test
	@DisplayName("Adding same student-course relation twice should throw DuplicateKeyException")
	void saveStudentToCourseShouldThrowIfRelationAlreadyExists() {
		Group savedGroup = groupDao.save(List.of(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME))).get(GENERATED_INDEX);

		Student savedStudent = studentDao
				.save(List.of(new Student(DEFAULT_ID, savedGroup.getGroupId(), STUDENT_FIRST_NAME, STUDENT_LAST_NAME)))
				.get(GENERATED_INDEX);

		Course savedCourse = courseDao.save(List.of(new Course(DEFAULT_ID, DUPLICATE_COURSE_NAME, DEFAULT_DESCRIPTION)))
				.get(GENERATED_INDEX);

		Long studentId = savedStudent.getStudentId();
		Long courseId = savedCourse.getCourseId();

		courseDao.saveStudentToCourse(studentId, courseId);

		assertThrows(DuplicateKeyException.class, () -> courseDao.saveStudentToCourse(studentId, courseId));

		studentDao.deleteById(studentId);
		courseDao.deleteById(courseId);
		groupDao.deleteById(savedGroup.getGroupId());
	}

	@Test
	@DisplayName("Find non-existent course should return empty Optional")
	void findByIdShouldReturnEmptyIfNotFound() {
		Optional<Course> result = courseDao.findById(NON_EXISTENT_ID);
		assertFalse(result.isPresent());
	}

	@Test
	@DisplayName("Update non-existent course should throw exception")
	void updateShouldThrowIfCourseNotFound() {
		Course nonExistent = new Course(NON_EXISTENT_ID, UPDATED_COURSE_NAME, UPDATED_DESCRIPTION);
		assertThrows(UnexpectedAffectedRowsException.class, () -> courseDao.update(nonExistent));
	}

	@Test
	@DisplayName("Delete non-existent course should throw exception")
	void deleteShouldThrowIfCourseNotFound() {
		assertThrows(UnexpectedAffectedRowsException.class, () -> courseDao.deleteById(NON_EXISTENT_ID));
	}

	@Test
	@DisplayName("Deleting non-existent student-course relation should throw exception")
	void deleteStudentFromCourseShouldThrowIfRelationNotFound() {
		Long nonRelatedStudentId = NON_EXISTENT_ID;
		Long nonRelatedCourseId = NON_EXISTENT_ID;

		assertThrows(UnexpectedAffectedRowsException.class,
				() -> courseDao.deleteStudentFromCourse(nonRelatedStudentId, nonRelatedCourseId));
	}

}
