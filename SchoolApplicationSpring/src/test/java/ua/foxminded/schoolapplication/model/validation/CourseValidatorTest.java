package ua.foxminded.schoolapplication.model.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Course;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

@SpringBootTest
class CourseValidatorTest {
	static final Long DEFAULT_ID = 1L;

	static final String VALID_COURSE_NAME = "Mathematics 101";
	static final String VALID_COURSE_DESCRIPTION = "An introductory course to mathematics.";

	static final String NULL = "null";
	static final String EMPTY = "";
	static final String TOO_SHORT = "A";
	static final String INVALID_CHARS = "Math@101";
	static final String TOO_LONG_NAME = "This is a very long course name that is intended to exceed the maximum allowed length of one hundred characters for courses";

	static final String COURSE_PATTERN = "courseName: \"{0}\", courseDescription: \"{1}\" | Expected: {2}";

	@Autowired
	private EntityValidator<Course> validator;

	@ParameterizedTest(name = COURSE_PATTERN)
	@MethodSource("provideCoursesForValidation")
	void validateEntities_ShouldBehaveAsExpected(String courseName, String courseDescription, boolean shouldPass) {
		String validatedCourseName = NULL.equals(courseName) ? null : courseName;
		String validatedCourseDescription = NULL.equals(courseDescription) ? null : courseDescription;

		Course course = new Course(DEFAULT_ID, validatedCourseName, validatedCourseDescription);

		if (shouldPass) {
			assertDoesNotThrow(() -> validator.validateEntities(course),
					"Validation should pass for course: " + course);
		} else {
			assertThrows(ValidationException.class,
					() -> validator.validateEntities(course),
					"Validation should fail for course: " + course);
		}
	}

	static Stream<Arguments> provideCoursesForValidation() {
		return Stream.of(Arguments.of(VALID_COURSE_NAME, VALID_COURSE_DESCRIPTION, true),
				Arguments.of(VALID_COURSE_NAME, EMPTY, true),
				Arguments.of(NULL, VALID_COURSE_DESCRIPTION, false),
				Arguments.of(EMPTY, VALID_COURSE_DESCRIPTION, false),
				Arguments.of(TOO_SHORT, VALID_COURSE_DESCRIPTION, false),
				Arguments.of(INVALID_CHARS, VALID_COURSE_DESCRIPTION, false),
				Arguments.of(TOO_LONG_NAME, VALID_COURSE_DESCRIPTION, false));
	}
}
