package ua.foxminded.schoolapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ua.foxminded.schoolapplication.view.MainMenu;

@SpringBootApplication
public class SchoolApplicationConsole {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SchoolApplicationConsole.class, args);
		MainMenu mainMenu = context.getBean(MainMenu.class);
		mainMenu.start();
	}
}
