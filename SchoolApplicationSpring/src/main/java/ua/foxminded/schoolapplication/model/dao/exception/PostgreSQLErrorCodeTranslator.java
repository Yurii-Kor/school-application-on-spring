package ua.foxminded.schoolapplication.model.dao.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import ua.foxminded.schoolapplication.model.dao.constants.PostgreSQLErrorCode;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PostgreSQLErrorCodeTranslator extends SQLErrorCodeSQLExceptionTranslator {
	private static final Map<String, Function<SQLException, DataAccessException>> handlers = new HashMap<>();

	static {
		handlers.put(PostgreSQLErrorCode.UNIQUE_VIOLATION,
				ex -> new DuplicateKeyException("Unique constraint violated", ex));
		handlers.put(PostgreSQLErrorCode.FOREIGN_KEY_VIOLATION,
				ex -> new DataIntegrityViolationException("Foreign key constraint violated", ex));
		handlers.put(PostgreSQLErrorCode.NULL_CONSTRAINT_VIOLATION,
				ex -> new NullValueViolationException("Null value not allowed", ex));
	}

	@Override
	protected DataAccessException customTranslate(String task, String sql, SQLException sqlException) {
		String sqlState = extractSqlState(sqlException);
		Function<SQLException, DataAccessException> handler = handlers.get(sqlState);
		return (handler != null) ? handler.apply(sqlException) : null;
	}

	private String extractSqlState(SQLException exception) {
		if (exception == null) {
			return PostgreSQLErrorCode.UNKNOWN_SQL_STATE;
		}
		String sqlState = exception.getSQLState();
		return (sqlState != null) ? sqlState : extractSqlState(exception.getNextException());
	}
}
