package ua.foxminded.schoolapplication;

import org.springframework.boot.SpringApplication;

public class TestSchoolApplicationSpringApplication {

	public static void main(String[] args) {
		SpringApplication.from(SchoolApplicationConsole::main).with(TestcontainersConfiguration.class).run(args);
	}

}
