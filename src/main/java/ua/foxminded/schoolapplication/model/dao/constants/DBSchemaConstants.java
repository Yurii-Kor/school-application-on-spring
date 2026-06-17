package ua.foxminded.schoolapplication.model.dao.constants;

public class DBSchemaConstants {
	public static final String GROUPS_TABLE = "groups";
	public static final String GROUP_ID = "group_id";
	public static final String GROUP_NAME = "group_name";

	public static final String PARAM_GROUP_ID = "groupId";
	public static final String PARAM_GROUP_NAME = "groupName";
	public static final String PARAM_MAX_COUNT = "maxCount";

	public static final String STUDENTS_TABLE = "students";
	public static final String STUDENT_ID = "student_id";
	public static final String STUDENT_GROUP_ID = "group_id";
	public static final String STUDENT_FIRST_NAME = "first_name";
	public static final String STUDENT_LAST_NAME = "last_name";

	public static final String PARAM_STUDENT_ID = "studentId";
	public static final String PARAM_FIRST_NAME = "firstName";
	public static final String PARAM_LAST_NAME = "lastName";

	public static final String COURSES_TABLE = "courses";
	public static final String COURSE_ID = "course_id";
	public static final String COURSE_NAME = "course_name";
	public static final String COURSE_DESCRIPTION = "course_description";

	public static final String PARAM_COURSE_ID = "courseId";
	public static final String PARAM_COURSE_NAME = "courseName";
	public static final String PARAM_COURSE_DESCRIPTION = "courseDescription";

	public static final String STUDENTS_COURSES_TABLE = "students_courses";
	public static final String STUDENTS_COURSES_STUDENT_ID = "student_id";
	public static final String STUDENTS_COURSES_COURSE_ID = "course_id";

	private DBSchemaConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
