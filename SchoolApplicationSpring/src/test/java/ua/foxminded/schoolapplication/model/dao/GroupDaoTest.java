package ua.foxminded.schoolapplication.model.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.TestcontainersConfiguration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ua.foxminded.schoolapplication.config.ApplicationConfig;
import ua.foxminded.schoolapplication.model.dao.exception.UnexpectedAffectedRowsException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@JdbcTest
@Import({ GroupDao.class, StudentDao.class, TestcontainersConfiguration.class, ApplicationConfig.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class GroupDaoTest {
	static final Long DEFAULT_ID = 0L;
	static final Long NON_EXISTENT_GROUP_ID = 999L;
	static final String DEFAULT_GROUP_NAME = "TestGroup-11";
	static final String UPDATED_GROUP_NAME = "UpdatedGroup-22";
	static final String STUDENT_FIRST_NAME = "John";
	static final String STUDENT_LAST_NAME = "Doe";
	static final String UNSAVED_NAME = null;

	static final int GENERATED_INDEX = 0;
	static final int STUDENTS_IN_GROUP = 1;

	@Autowired
	private GroupDao groupDao;

	@Autowired
	private StudentDao studentDao;

	@Test
	@DisplayName("Save and retrieve a group")
	void saveShouldSaveAndFindGroup() {
		Group saved = groupDao.save(Collections.singletonList(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME)))
				.get(GENERATED_INDEX);

		assertNotNull(saved.getGroupId(), "Group ID should not be null after saving");
		Optional<Group> fetched = groupDao.findById(saved.getGroupId());
		assertTrue(fetched.isPresent(), "Group should be found by ID");
		assertEquals(DEFAULT_GROUP_NAME,
				fetched.get().getGroupName(),
				"Group name should match after saving and fetching");

		if (saved != null && saved.getGroupId() != null) {
			groupDao.deleteById(saved.getGroupId());
		}

	}

	@Test
	@DisplayName("Update an existing group")
	void updateShouldModifyExistingGroup() {
		Group saved = groupDao.save(Collections.singletonList(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME)))
				.get(GENERATED_INDEX);

		saved.setGroupName(UPDATED_GROUP_NAME);
		groupDao.update(saved);

		Optional<Group> updated = groupDao.findById(saved.getGroupId());
		assertTrue(updated.isPresent(), "Group should still exist after update");
		assertEquals(UPDATED_GROUP_NAME, updated.get().getGroupName(), "Group name should be updated");

		if (saved != null && saved.getGroupId() != null) {
			groupDao.deleteById(saved.getGroupId());
		}
	}

	@Test
	@DisplayName("Delete an existing group")
	void deleteShouldRemoveGroupById() {
		Group saved = groupDao.save(Collections.singletonList(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME)))
				.get(GENERATED_INDEX);

		groupDao.deleteById(saved.getGroupId());

		Optional<Group> deleted = groupDao.findById(saved.getGroupId());
		assertFalse(deleted.isPresent(), "Group should no longer exist after deletion");
	}

	@Test
	@DisplayName("Saving duplicate group names should throw exception")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void saveShouldThrowExceptionIfGroupNamesAreDuplicated() {
		Group group1 = new Group(DEFAULT_ID, DEFAULT_GROUP_NAME);
		Group group2 = new Group(DEFAULT_ID, DEFAULT_GROUP_NAME);

		assertThrows(DuplicateKeyException.class,
				() -> groupDao.save(List.of(group1, group2)),
				"Expected DuplicateKeyException when saving duplicate group names in one batch");

		Group saved = groupDao.save(Collections.singletonList(group1)).get(GENERATED_INDEX);

		Group duplicate = new Group(DEFAULT_ID, DEFAULT_GROUP_NAME);
		assertThrows(DuplicateKeyException.class,
				() -> groupDao.save(Collections.singletonList(duplicate)),
				"Expected DuplicateKeyException when saving a duplicate group name");

		if (saved != null && saved.getGroupId() != null) {
			groupDao.deleteById(saved.getGroupId());
		}

	}

	@Test
	@DisplayName("Saving group with null name should throw exception")
	void saveShouldThrowExceptionIfGroupNameIsNull() {
		assertThrows(ValidationException.class,
				() -> groupDao.save(Collections.singletonList(new Group(DEFAULT_ID, UNSAVED_NAME))),
				"Expected ValidationException when group name is null");
	}

	@Test
	@DisplayName("Find non-existent group should return empty Optional")
	void findByIdShouldReturnEmptyIfNotFound() {
		Optional<Group> result = groupDao.findById(NON_EXISTENT_GROUP_ID);

		assertFalse(result.isPresent(), "Expected empty Optional when group is not found");
	}

	@Test
	@DisplayName("Find groups with student count less than or equal to N should return matching groups")
	void findGroupsWithStudentCountLessOrEqualShouldReturnMatchingGroups() {
		Group saved = groupDao.save(List.of(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME))).get(GENERATED_INDEX);
		Long groupId = saved.getGroupId();

		List<Group> result = groupDao.findGroupsWithStudentCountLessOrEqual(STUDENTS_IN_GROUP);

		assertFalse(result.isEmpty());
		assertTrue(result.stream().anyMatch(g -> g.getGroupId().equals(groupId)));

		studentDao.findByGroupName(DEFAULT_GROUP_NAME).forEach(s -> studentDao.deleteById(s.getStudentId()));
		groupDao.deleteById(groupId);
	}

	@Test
	@DisplayName("Find groups with student count less than or equal to N should return empty list if none match")
	void findGroupsWithStudentCountLessOrEqualShouldReturnEmptyListIfNoneMatch() {
		Group saved = groupDao.save(List.of(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME))).get(GENERATED_INDEX);
		Long groupId = saved.getGroupId();

		studentDao.save(List.of(new Student(DEFAULT_ID, groupId, STUDENT_FIRST_NAME, STUDENT_LAST_NAME),
				new Student(DEFAULT_ID, groupId, STUDENT_FIRST_NAME, STUDENT_LAST_NAME)));

		List<Group> result = groupDao.findGroupsWithStudentCountLessOrEqual(STUDENTS_IN_GROUP);

		assertTrue(result.isEmpty());

		studentDao.findByGroupName(DEFAULT_GROUP_NAME).forEach(s -> studentDao.deleteById(s.getStudentId()));
		groupDao.deleteById(groupId);
	}

	@Test
	@DisplayName("Update non-existent group should throw exception")
	void updateShouldThrowExceptionIfGroupNotFound() {
		Group nonExistent = new Group(NON_EXISTENT_GROUP_ID, UPDATED_GROUP_NAME);

		assertThrows(UnexpectedAffectedRowsException.class,
				() -> groupDao.update(nonExistent),
				"Expected exception when updating non-existent group");
	}

	@Test
	@DisplayName("Delete non-existent group should throw exception")
	void deleteShouldThrowExceptionIfGroupNotFound() {
		assertThrows(UnexpectedAffectedRowsException.class,
				() -> groupDao.deleteById(NON_EXISTENT_GROUP_ID),
				"Expected exception when deleting non-existent group");
	}

	@Test
	@DisplayName("Delete group with existing students should throw exception")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void deleteGroupWithStudentsShouldThrowException() {
		Group saved = groupDao.save(List.of(new Group(DEFAULT_ID, DEFAULT_GROUP_NAME))).get(GENERATED_INDEX);
		Long groupId = saved.getGroupId();

		studentDao.save(List.of(new Student(DEFAULT_ID, groupId, STUDENT_FIRST_NAME, STUDENT_LAST_NAME)));

		assertThrows(DataIntegrityViolationException.class, () -> groupDao.deleteById(groupId));

		studentDao.findByGroupName(DEFAULT_GROUP_NAME).forEach(s -> studentDao.deleteById(s.getStudentId()));
		groupDao.deleteById(groupId);
	}
}
