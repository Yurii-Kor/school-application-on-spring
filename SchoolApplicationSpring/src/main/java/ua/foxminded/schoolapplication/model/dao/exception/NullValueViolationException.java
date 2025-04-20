package ua.foxminded.schoolapplication.model.dao.exception;

import org.springframework.dao.DataAccessException;

public class NullValueViolationException extends DataAccessException {
	public NullValueViolationException(String msg) {
		super(msg);
	}

	public NullValueViolationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
