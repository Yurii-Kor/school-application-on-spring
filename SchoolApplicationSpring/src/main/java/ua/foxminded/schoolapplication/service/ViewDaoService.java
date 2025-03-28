package ua.foxminded.schoolapplication.service;

import org.springframework.stereotype.Service;
import ua.foxminded.schoolapplication.model.dao.CourseDao;
import ua.foxminded.schoolapplication.model.dao.GroupDao;
import ua.foxminded.schoolapplication.model.dao.StudentDao;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.List;

@Service
public class ViewDaoService {

	private final GroupDao groupDao;
	private final StudentDao studentDao;
	private final CourseDao courseDao;

	public ViewDaoService(GroupDao groupDao, StudentDao studentDao, CourseDao courseDao) {
		this.groupDao = groupDao;
		this.studentDao = studentDao;
		this.courseDao = courseDao;
	}

	public List<Group> findGroupsWithStudentCountLessOrEqual(int maxStudents) {
		return groupDao.findGroupsWithStudentCountLessOrEqual(maxStudents);
	}

	public List<Student> findStudentsByCourseName(String courseName) {
		return studentDao.findByCourseName(courseName);
	}

	public void addStudent(Student student) {
		studentDao.save(List.of(student));
	}

	public void deleteStudentById(Long studentId) {
		studentDao.deleteById(studentId);
	}

	public void addStudentToCourse(Long studentId, Long courseId) {
		courseDao.saveStudentToCourse(studentId, courseId);
	}

	public void removeStudentFromCourse(Long studentId, Long courseId) {
		courseDao.deleteStudentFromCourse(studentId, courseId);
	}
}
