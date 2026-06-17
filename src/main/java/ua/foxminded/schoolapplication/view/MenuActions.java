package ua.foxminded.schoolapplication.view;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import ua.foxminded.schoolapplication.model.dao.exception.UnexpectedAffectedRowsException;
import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;
import ua.foxminded.schoolapplication.service.ViewDaoService;

import java.util.List;
import java.util.Scanner;

@Component
public class MenuActions {
	private ViewDaoService viewDaoService;
	private Scanner scanner;

	public MenuActions(ViewDaoService viewDaoService, Scanner scanner) {
		this.viewDaoService = viewDaoService;
		this.scanner = scanner;
	}

	public void findGroupsByStudentCount() {
		System.out.print("Enter the max number of students: ");
		int maxStudents = scanner.nextInt();
		scanner.nextLine();

		try {
			List<Group> groups = viewDaoService.findGroupsWithStudentCountLessOrEqual(maxStudents);

			if (groups.isEmpty()) {
				System.out.println("No groups found with student count ≤ " + maxStudents);
			} else {
				System.out.println("Groups with student count ≤ " + maxStudents + ":");
				groups.forEach(System.out::println);
			}
		} catch (DataAccessException e) {
			System.err.println("An error occurred while retrieving groups: " + e.getMessage());
		}
	}

	public void findStudentsByCourseName() {
		System.out.print("Enter course name: ");
		String courseName = scanner.nextLine().trim();

		try {
			List<Student> students = viewDaoService.findStudentsByCourseName(courseName);

			if (students.isEmpty()) {
				System.out.println("No students found for course: " + courseName);
			} else {
				System.out.println("Students enrolled in course '" + courseName + "':");
				students.forEach(System.out::println);
			}
		} catch (DataAccessException e) {
			System.err.println("An error occurred while retrieving students: " + e.getMessage());
		}
	}

	public void addNewStudent() {
		System.out.print("Enter group ID: ");
		Long groupId = scanner.nextLong();
		scanner.nextLine();

		System.out.print("Enter first name: ");
		String firstName = scanner.nextLine().trim();

		System.out.print("Enter last name: ");
		String lastName = scanner.nextLine().trim();

		Student student = new Student(0L, groupId, firstName, lastName);

		try {
			viewDaoService.addStudent(student);
			System.out.printf("Student added successfully: %s %s (Group ID: %d)%n", firstName, lastName, groupId);
		} catch (DataIntegrityViolationException e) {
			System.err.println("Error: Invalid group ID. This group does not exist.");
		} catch (ValidationException e) {
			System.err.println("Error: A required field is missing or wrong. Please ensure all fields are filled.");
		} catch (DataAccessException e) {
			System.err.println("An unexpected error occurred while adding the student: " + e.getMessage());
		}
	}

	public void deleteStudentById() {
		System.out.print("Enter STUDENT_ID to delete: ");
		Long studentId = scanner.nextLong();
		scanner.nextLine();

		try {
			viewDaoService.deleteStudentById(studentId);
			System.out.println("Student with ID " + studentId + " was deleted successfully.");
		} catch (UnexpectedAffectedRowsException e) {
			System.err.println("Error: No student found with ID " + studentId + ".");
		} catch (DataAccessException e) {
			System.err.println("An unexpected error occurred while deleting the student: " + e.getMessage());
		}
	}

	public void addStudentToCourse() {
		System.out.print("Enter STUDENT_ID: ");
		Long studentId = scanner.nextLong();
		scanner.nextLine();

		System.out.print("Enter COURSE_ID: ");
		Long courseId = scanner.nextLong();
		scanner.nextLine();

		try {
			viewDaoService.addStudentToCourse(studentId, courseId);
			System.out.println("Student with ID " + studentId + " successfully added to course ID " + courseId + ".");
		} catch (DuplicateKeyException e) {
			System.err.println("Error: The student is already enrolled in this course.");
		} catch (DataAccessException e) {
			System.err.println("An error occurred while adding the student to the course: " + e.getMessage());
		}
	}

	public void removeStudentFromCourse() {
		System.out.print("Enter STUDENT_ID: ");
		Long studentId = scanner.nextLong();
		scanner.nextLine();

		System.out.print("Enter COURSE_ID to remove: ");
		Long courseId = scanner.nextLong();
		scanner.nextLine();

		try {
			viewDaoService.removeStudentFromCourse(studentId, courseId);
			System.out.println("Student with ID " + studentId + " removed from course ID " + courseId + ".");
		} catch (UnexpectedAffectedRowsException e) {
			System.err.println("Error: No such enrollment found. The student is not registered for this course.");
		} catch (DataAccessException e) {
			System.err.println("An error occurred while removing the student from the course: " + e.getMessage());
		}
	}
}
