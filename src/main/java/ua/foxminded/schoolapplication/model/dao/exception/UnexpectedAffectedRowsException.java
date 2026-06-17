package ua.foxminded.schoolapplication.model.dao.exception;

import org.springframework.dao.DataAccessException;

public class UnexpectedAffectedRowsException extends DataAccessException {
	public UnexpectedAffectedRowsException(String msg) {
		super(msg);
	}

	public UnexpectedAffectedRowsException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
