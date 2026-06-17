package ua.foxminded.schoolapplication.model.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {
	Optional<T> findById(Long id);

	List<T> save(List<T> entities);

	void update(T entity);

	void deleteById(Long id);
}
