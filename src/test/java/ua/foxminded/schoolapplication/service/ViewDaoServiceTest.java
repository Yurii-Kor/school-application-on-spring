package ua.foxminded.schoolapplication.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.foxminded.schoolapplication.model.dao.CourseDao;
import ua.foxminded.schoolapplication.model.dao.GroupDao;
import ua.foxminded.schoolapplication.model.dao.StudentDao;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewDaoServiceTest {
	static final Integer STUDENTS_AMOUNT = 5;
	static final String COURSE_NAME = "Math";
	static final Long STUDENT_ID = 12L;
	static final Long COURSE_ID = 21L;

	@Mock
	private GroupDao groupDao;

	@Mock
	private StudentDao studentDao;

	@Mock
	private CourseDao courseDao;

	@InjectMocks
	private ViewDaoService viewDaoService;

	@Test
	void findGroupsWithStudentCountLessOrEqualShouldDelegateToGroupDao() {
		viewDaoService.findGroupsWithStudentCountLessOrEqual(STUDENTS_AMOUNT);
		verify(groupDao).findGroupsWithStudentCountLessOrEqual(STUDENTS_AMOUNT);
	}

	@Test
	void findStudentsByCourseNameShouldDelegateToStudentDao() {
		viewDaoService.findStudentsByCourseName(COURSE_NAME);
		verify(studentDao).findByCourseName(COURSE_NAME);
	}

	@Test
	void addStudentShouldCallSaveInStudentDao() {
		Student student = new Student();
		viewDaoService.addStudent(student);
		verify(studentDao).save(List.of(student));
	}

	@Test
	void deleteStudentByIdShouldCallDeleteInStudentDao() {
		viewDaoService.deleteStudentById(STUDENT_ID);
		verify(studentDao).deleteById(STUDENT_ID);
	}

	@Test
	void addStudentToCourseShouldCallSaveInCourseDao() {
		viewDaoService.addStudentToCourse(STUDENT_ID, COURSE_ID);
		verify(courseDao).saveStudentToCourse(STUDENT_ID, COURSE_ID);
	}

	@Test
	void removeStudentFromCourseShouldCallDeleteInCourseDao() {
		viewDaoService.removeStudentFromCourse(STUDENT_ID, COURSE_ID);
		verify(courseDao).deleteStudentFromCourse(STUDENT_ID, COURSE_ID);
	}
}
