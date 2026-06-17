package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
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
@Import({ GroupDao.class, StudentDao.class, CourseDao.class, TestcontainersConfiguration.class,
		ApplicationConfig.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StudentDaoTest {

	static final Long DEFAULT_ID = 0L;
	static final Long NON_EXISTENT_ID = 999L;
	static final String DEFAULT_GROUP_NAME = "TestGroup-11";
	static final String EMPTY_GROUP_NAME = "EmptyGroup-22";
	static final String DEFAULT_FIRST_NAME = "John";
	static final String UPDATED_FIRST_NAME = "UpdatedJohn";
	static final String DEFAULT_LAST_NAME = "Doe";
	static final String UPDATED_LAST_NAME = "UpdatedDoe";

	static final int GENERATED_ENTITY = 0;

	@Autowired
	private GroupDao groupDao;

	@Autowired
	private StudentDao studentDao;

	@Autowired
	private CourseDao courseDao;

	private Long groupId;

	@BeforeEach
	void setupGroup() {
		Group savedGroup = groupDao.save(List.of(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME))).get(GENERATED_ENTITY);
		groupId = savedGroup.getGroupId();
	}

	@AfterEach
	void cleanupGroup() {
		groupDao.deleteById(groupId);
	}

	@Test
	@DisplayName("Find students by group name should return matching students")
	void findByGroupNameShouldReturnStudentsInGroup() {
		List<Student> saved = studentDao
				.save(List.of(new Student(DEFAULT_ID, groupId, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME),
						new Student(DEFAULT_ID, groupId, UPDATED_FIRST_NAME, UPDATED_LAST_NAME)));
		List<Student> found = studentDao.findByGroupName(DEFAULT_GROUP_NAME);

		assertEquals(saved.size(), found.size(), "Should find savedamount students in the group");
		assertTrue(found.stream().anyMatch(s -> s.getFirstName().equals(DEFAULT_FIRST_NAME)),
				"Should contain student 1");
		assertTrue(found.stream().anyMatch(s -> s.getFirstName().equals(UPDATED_FIRST_NAME)),
				"Should contain student 2");

		saved.forEach(s -> studentDao.deleteById(s.getStudentId()));
	}

	@Test
	@DisplayName("Save and retrieve a student")
	void saveShouldSaveAndFindStudent() {
		Student saved = studentDao
				.save(List.of(new Student(DEFAULT_ID, groupId, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)))
				.get(GENERATED_ENTITY);

		assertNotNull(saved.getStudentId(), "Student ID should not be null after saving");

		Optional<Student> fetched = studentDao.findById(saved.getStudentId());
		assertTrue(fetched.isPresent(), "Student should be found by ID");
		assertEquals(DEFAULT_FIRST_NAME, fetched.get().getFirstName(), "First name should match");
		assertEquals(DEFAULT_LAST_NAME, fetched.get().getLastName(), "Last name should match");

		studentDao.deleteById(saved.getStudentId());
	}

	@Test
	@DisplayName("Find students by course name should return matching students")
	void findByCourseNameShouldReturnStudentsForCourse() {
		Student student = studentDao
				.save(List.of(new Student(DEFAULT_ID, groupId, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)))
				.get(GENERATED_ENTITY);

		Course course = new Course(DEFAULT_ID, "TestCourse-123", "Test description");
		course = courseDao.save(List.of(course)).get(GENERATED_ENTITY);

		courseDao.saveStudentToCourse(student.getStudentId(), course.getCourseId());

		List<Student> found = studentDao.findByCourseName(course.getCourseName());

		assertEquals(1, found.size(), "Should find one student for the course");
		assertEquals(student.getStudentId(), found.get(GENERATED_ENTITY).getStudentId(), "Student IDs should match");
		assertEquals(student.getFirstName(), found.get(GENERATED_ENTITY).getFirstName(), "First names should match");
		assertEquals(student.getLastName(), found.get(GENERATED_ENTITY).getLastName(), "Last names should match");

		courseDao.deleteStudentFromCourse(student.getStudentId(), course.getCourseId());
		courseDao.deleteById(course.getCourseId());
		studentDao.deleteById(student.getStudentId());
	}

	@Test
	@DisplayName("Update an existing student")
	void updateShouldModifyExistingStudent() {
		Student saved = studentDao
				.save(List.of(new Student(DEFAULT_ID, groupId, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)))
				.get(GENERATED_ENTITY);

		saved.setFirstName(UPDATED_FIRST_NAME);
		saved.setLastName(UPDATED_LAST_NAME);
		studentDao.update(saved);

		Optional<Student> updated = studentDao.findById(saved.getStudentId());
		assertTrue(updated.isPresent(), "Student should still exist after update");
		assertEquals(UPDATED_FIRST_NAME, updated.get().getFirstName(), "First name should be updated");
		assertEquals(UPDATED_LAST_NAME, updated.get().getLastName(), "Last name should be updated");

		studentDao.deleteById(saved.getStudentId());
	}

	@Test
	@DisplayName("Delete an existing student")
	void deleteShouldRemoveStudent() {
		Student saved = studentDao
				.save(List.of(new Student(DEFAULT_ID, groupId, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)))
				.get(GENERATED_ENTITY);

		studentDao.deleteById(saved.getStudentId());

		Optional<Student> deleted = studentDao.findById(saved.getStudentId());
		assertFalse(deleted.isPresent(), "Student should no longer exist after deletion");
	}

	@Test
	@DisplayName("Find non-existent student should return empty Optional")
	void findByIdShouldReturnEmptyIfStudentNotFound() {
		Optional<Student> result = studentDao.findById(NON_EXISTENT_ID);

		assertFalse(result.isPresent(), "Expected empty Optional when student is not found");
	}

	@Test
	@DisplayName("Find students by group name should return empty list if no students exist")
	void findByGroupNameShouldReturnEmptyIfGroupHasNoStudents() {
		Group emptyGroup = groupDao.save(List.of(new Group(DEFAULT_ID, EMPTY_GROUP_NAME))).get(GENERATED_ENTITY);

		List<Student> result = studentDao.findByGroupName(EMPTY_GROUP_NAME);

		assertTrue(result.isEmpty(), "Expected empty result list for group with no students");

		groupDao.deleteById(emptyGroup.getGroupId());
	}

	@Test
	@DisplayName("Saving student with null name should throw ValidationException")
	void saveShouldThrowExceptionIfNameIsNull() {
		Student invalid = new Student(DEFAULT_ID, groupId, null, DEFAULT_LAST_NAME);

		assertThrows(ValidationException.class,
				() -> studentDao.save(List.of(invalid)),
				"Expected ValidationException when first name is null");
	}

	@Test
	@DisplayName("Saving student with non-existent groupId should throw DataIntegrityViolationException")
	void saveShouldThrowExceptionIfGroupIdInvalid() {
		Student invalid = new Student(DEFAULT_ID, NON_EXISTENT_ID, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME);

		assertThrows(DataIntegrityViolationException.class,
				() -> studentDao.save(List.of(invalid)),
				"Expected exception due to invalid groupId FK");
	}

	@Test
	@DisplayName("Update non-existent student should throw exception")
	void updateShouldThrowExceptionIfStudentNotFound() {
		Student nonExistent = new Student(NON_EXISTENT_ID, groupId, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME);

		assertThrows(UnexpectedAffectedRowsException.class,
				() -> studentDao.update(nonExistent),
				"Expected exception when updating non-existent student");
	}

	@Test
	@DisplayName("Delete non-existent student should throw exception")
	void deleteShouldThrowExceptionIfStudentNotFound() {
		assertThrows(UnexpectedAffectedRowsException.class,
				() -> studentDao.deleteById(NON_EXISTENT_ID),
				"Expected exception when deleting non-existent student");
	}
}
