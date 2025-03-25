package ua.foxminded.schoolapplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.foxminded.schoolapplication.model.dao.CourseDao;
import ua.foxminded.schoolapplication.model.dao.Dao;
import ua.foxminded.schoolapplication.model.dao.GroupDao;
import ua.foxminded.schoolapplication.model.dao.StudentDao;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ViewDaoService {

	@Autowired
	private Map<Class<?>, Dao<?>> daoRegistry;

	public List<Group> findGroupsWithStudentCountLessOrEqual(int maxStudents) {
		GroupDao groupDao = (GroupDao) daoRegistry.get(GroupDao.class);
		return groupDao.findGroupsWithStudentCountLessOrEqual(maxStudents);
	}

	public List<Student> findStudentsByCourseName(String courseName) {
		StudentDao studentDao = (StudentDao) daoRegistry.get(StudentDao.class);
		return studentDao.findByCourseName(courseName);
	}

	public void addStudent(Student student) {
		StudentDao studentDao = (StudentDao) daoRegistry.get(StudentDao.class);
		studentDao.save(Collections.singletonList(student));
	}

	public void deleteStudentById(Long studentId) {
		StudentDao studentDao = (StudentDao) daoRegistry.get(StudentDao.class);
		studentDao.deleteById(studentId);
	}

	public void addStudentToCourse(Long studentId, Long courseId) {
		CourseDao courseDao = (CourseDao) daoRegistry.get(CourseDao.class);
		courseDao.saveStudentToCourse(studentId, courseId);
	}

	public void removeStudentFromCourse(Long studentId, Long courseId) {
		CourseDao courseDao = (CourseDao) daoRegistry.get(CourseDao.class);
		courseDao.deleteStudentFromCourse(studentId, courseId);
	}
}
