package ua.foxminded.schoolapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.PostgreSQLErrorCodeTranslator;
import ua.foxminded.schoolapplication.model.domain.Course;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.domain.Student;

import java.util.Scanner;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = { "ua.foxminded.schoolapplication" })
public class ApplicationConfig {

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setExceptionTranslator(new PostgreSQLErrorCodeTranslator());
		return jdbcTemplate;
	}

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
		return new NamedParameterJdbcTemplate(jdbcTemplate);
	}

	@Bean
	public DataSourceTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public RowMapper<Course> courseRowMapper() {
		return (rs, rowNum) -> new Course(rs.getLong(DBSchemaConstants.COURSE_ID),
				rs.getString(DBSchemaConstants.COURSE_NAME), rs.getString(DBSchemaConstants.COURSE_DESCRIPTION));
	}

	@Bean
	public RowMapper<Student> studentRowMapper() {
		return (rs, rowNum) -> new Student(rs.getLong(DBSchemaConstants.STUDENT_ID),
				rs.getLong(DBSchemaConstants.STUDENT_GROUP_ID), rs.getString(DBSchemaConstants.STUDENT_FIRST_NAME),
				rs.getString(DBSchemaConstants.STUDENT_LAST_NAME));
	}

	@Bean
	public RowMapper<Group> groupRowMapper() {
		return (rs, rowNum) -> new Group(rs.getLong(DBSchemaConstants.GROUP_ID),
				rs.getString(DBSchemaConstants.GROUP_NAME));
	}

	@Bean
	public Scanner scanner() {
		return new Scanner(System.in);
	}
}
