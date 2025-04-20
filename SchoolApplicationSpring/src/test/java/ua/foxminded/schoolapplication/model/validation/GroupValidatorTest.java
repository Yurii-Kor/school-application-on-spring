package ua.foxminded.schoolapplication.model.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ua.foxminded.schoolapplication.model.dao.exception.ValidationException;
import ua.foxminded.schoolapplication.model.domain.Group;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

@SpringBootTest
class GroupValidatorTest {
	static final Long DEFAULT_ID = 1L;

	static final String VALID_GROUP_NAME_SIMPLE = "AB-12";
	static final String VALID_GROUP_NAME_LONG = "Mathematics-101";

	static final String INVALID_GROUP_NAME_NO_HYPHEN = "AB12";
	static final String INVALID_GROUP_NAME_NON_DIGIT_AFTER_HYPHEN = "AB-1A";
	static final String INVALID_GROUP_NAME_EMPTY = "";
	static final String NULL = "null";
	static final String INVALID_GROUP_NAME_HYPHEN_WITHOUT_DIGITS = "AB-";
	static final String INVALID_GROUP_NAME_HYPHEN_WITHOUT_LETTERS = "-12";
	static final String INVALID_GROUP_NAME_ADDITIONAL_SYMBOLS = "AB-12-34";

	static final String GROUP_PATTERN = "GroupName: {0} | Expected: {1}";

	@Autowired
	private EntityValidator<Group> validator;

	@ParameterizedTest(name = GROUP_PATTERN)
	@MethodSource("provideGroupsForValidation")
	void validate_GroupName_ShouldBehaveAsExpected(String groupName, boolean shouldPass) {
		Group testedGroup = NULL.equals(groupName) ? new Group(DEFAULT_ID, null) : new Group(DEFAULT_ID, groupName);

		if (shouldPass) {
			assertDoesNotThrow(() -> validator.validateEntities(testedGroup),
					"Validation should pass for groupName: " + groupName);
		} else {
			assertThrows(ValidationException.class,
					() -> validator.validateEntities(testedGroup),
					"Validation should fail for groupName: " + groupName);
		}
	}

	static Stream<Arguments> provideGroupsForValidation() {
		return Stream.of(Arguments.of(VALID_GROUP_NAME_SIMPLE, true),
				Arguments.of(VALID_GROUP_NAME_LONG, true),
				Arguments.of(INVALID_GROUP_NAME_NO_HYPHEN, false),
				Arguments.of(INVALID_GROUP_NAME_NON_DIGIT_AFTER_HYPHEN, false),
				Arguments.of(INVALID_GROUP_NAME_EMPTY, false),
				Arguments.of(NULL, false),
				Arguments.of(INVALID_GROUP_NAME_HYPHEN_WITHOUT_DIGITS, false),
				Arguments.of(INVALID_GROUP_NAME_HYPHEN_WITHOUT_LETTERS, false),
				Arguments.of(INVALID_GROUP_NAME_ADDITIONAL_SYMBOLS, false));
	}
}
