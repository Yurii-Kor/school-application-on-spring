package ua.foxminded.schoolapplication.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class MainMenu {

	@Autowired
	private MenuActions menuActions;

	@Autowired
	private Scanner scanner;

	public void start() {
		while (true) {
			printMenu();

			switch (scanner.nextLine().trim().toLowerCase()) {

			case "a":
				menuActions.findGroupsByStudentCount();
				break;

			case "b":
				menuActions.findStudentsByCourseName();
				break;

			case "c":
				menuActions.addNewStudent();
				break;

			case "d":
				menuActions.deleteStudentById();
				break;

			case "e":
				menuActions.addStudentToCourse();
				break;

			case "f":
				menuActions.removeStudentFromCourse();
				break;

			case "q":
				System.out.println("Exiting application...");
				return;

			default:
				System.out.println("Invalid choice. Please select a valid option.");
			}
		}
	}

	private void printMenu() {
		String lineSeparator = System.lineSeparator();
		StringBuilder menu = new StringBuilder();
		menu.append(lineSeparator)
				.append("=== Main Menu ===")
				.append(lineSeparator)
				.append(" a. Find all groups with less or equal students' number")
				.append(lineSeparator)
				.append(" b. Find all students related to the course with the given name")
				.append(lineSeparator)
				.append(" c. Add a new student")
				.append(lineSeparator)
				.append(" d. Delete a student by STUDENT_ID")
				.append(lineSeparator)
				.append(" e. Add a student to the course")
				.append(lineSeparator)
				.append(" f. Remove the student from one of their courses")
				.append(lineSeparator)
				.append(" q. Exit")
				.append(lineSeparator)
				.append("Choose an option: ");

		System.out.print(menu);
	}
}
