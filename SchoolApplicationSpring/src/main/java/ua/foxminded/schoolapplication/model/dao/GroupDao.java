package ua.foxminded.schoolapplication.model.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import java.util.stream.IntStream;

import ua.foxminded.schoolapplication.model.dao.constants.DBSchemaConstants;
import ua.foxminded.schoolapplication.model.dao.exception.UnexpectedAffectedRowsException;
import ua.foxminded.schoolapplication.model.domain.Group;
import ua.foxminded.schoolapplication.model.validation.EntityValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Transactional
public class GroupDao implements Dao<Group> {
	private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

	private static final String FIND_BY_ID = String.format("SELECT * FROM %s WHERE %s = :%s",
			DBSchemaConstants.GROUPS_TABLE,
			DBSchemaConstants.GROUP_ID,
			DBSchemaConstants.PARAM_GROUP_ID);

	private static final String FIND_GROUPS_WITH_STUDENT_COUNT_LESS_OR_EQUAL = String.format(
			"SELECT g.* FROM %s g LEFT JOIN %s s ON g.%s = s.%s GROUP BY g.%s, g.%s HAVING COUNT(s.%s) <= :%s",
			DBSchemaConstants.GROUPS_TABLE,
			DBSchemaConstants.STUDENTS_TABLE,
			DBSchemaConstants.GROUP_ID,
			DBSchemaConstants.STUDENT_GROUP_ID,
			DBSchemaConstants.GROUP_ID,
			DBSchemaConstants.GROUP_NAME,
			DBSchemaConstants.STUDENT_ID,
			DBSchemaConstants.PARAM_MAX_COUNT);

	private static final String INSERT_GROUP = String.format("INSERT INTO %s (%s) VALUES (:%s)",
			DBSchemaConstants.GROUPS_TABLE,
			DBSchemaConstants.GROUP_NAME,
			DBSchemaConstants.PARAM_GROUP_NAME);

	private static final String UPDATE_GROUP = String.format("UPDATE %s SET %s = :%s WHERE %s = :%s",
			DBSchemaConstants.GROUPS_TABLE,
			DBSchemaConstants.GROUP_NAME,
			DBSchemaConstants.PARAM_GROUP_NAME,
			DBSchemaConstants.GROUP_ID,
			DBSchemaConstants.PARAM_GROUP_ID);

	private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE %s = :%s",
			DBSchemaConstants.GROUPS_TABLE,
			DBSchemaConstants.GROUP_ID,
			DBSchemaConstants.PARAM_GROUP_ID);

	private RowMapper<Group> groupRowMapper;
	private NamedParameterJdbcTemplate namedJdbcTemplate;
	private EntityValidator<Group> groupValidator;

	public GroupDao(RowMapper<Group> groupRowMapper, NamedParameterJdbcTemplate namedJdbcTemplate,
			EntityValidator<Group> groupValidator) {

		this.groupRowMapper = groupRowMapper;
		this.namedJdbcTemplate = namedJdbcTemplate;
		this.groupValidator = groupValidator;
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<Group> findById(Long id) {
		logger.debug("Searching for group with ID: {}", id);
		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_GROUP_ID, id);
		try {
			Group group = namedJdbcTemplate.queryForObject(FIND_BY_ID, params, groupRowMapper);
			logger.info("Group found: {}", group);
			return Optional.ofNullable(group);
		} catch (Exception e) {
			logger.warn("Group not found with ID: {}", id, e);
			return Optional.empty();
		}
	}

	@Transactional(readOnly = true)
	public List<Group> findGroupsWithStudentCountLessOrEqual(int maxCount) {
		logger.debug("Finding groups with student count <= {}", maxCount);
		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_MAX_COUNT, maxCount);

		List<Group> groups = namedJdbcTemplate
				.query(FIND_GROUPS_WITH_STUDENT_COUNT_LESS_OR_EQUAL, params, groupRowMapper);

		logger.info("Found {} groups with student count <= {}", groups.size(), maxCount);
		return groups;
	}

	@Override
	public List<Group> save(List<Group> groups) {
		groupValidator.validateEntities(
				Optional.ofNullable(groups).orElseGet(Collections::emptyList).stream().toArray(Group[]::new));
		logger.debug("Saving groups: {}", groups);

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(groups);
		KeyHolder keyHolder = new GeneratedKeyHolder();

		int[] affectedRows = namedJdbcTemplate
				.batchUpdate(INSERT_GROUP, batch, keyHolder, new String[] { DBSchemaConstants.GROUP_ID });
		List<Map<String, Object>> keyList = keyHolder.getKeyList();

		boolean allSuccessful = Arrays.stream(affectedRows).allMatch(count -> count == 1);
		if (!allSuccessful || keyList.size() != groups.size()) {
			logger.warn("Mismatch during batch insert. Affected: {}, Keys: {}, Expected: {}",
					Arrays.toString(affectedRows),
					keyList.size(),
					groups.size());
			throw new UnexpectedAffectedRowsException(String.format(
					"Batch insert issue. AffectedRows match: %s, Keys match: %s [Affected: %s, Keys: %s, Groups: %s]",
					allSuccessful,
					keyList.size() == groups.size(),
					Arrays.toString(affectedRows),
					keyList.size(),
					groups.size()));
		}

		IntStream.range(0, groups.size())
				.forEach(i -> groups.get(i).setGroupId((Long) keyList.get(i).get(DBSchemaConstants.GROUP_ID)));

		logger.info("All groups saved successfully: {}", groups);
		return groups;
	}

	@Override
	public void update(Group group) {
		groupValidator.validateEntities(group);
		logger.debug("Updating group: {}", group);

		Map<String, Object> params = new HashMap<>();
		params.put(DBSchemaConstants.PARAM_GROUP_ID, group.getGroupId());
		params.put(DBSchemaConstants.PARAM_GROUP_NAME, group.getGroupName());

		int updated = namedJdbcTemplate.update(UPDATE_GROUP, params);
		if (updated != 1) {
			logger.warn("No group found to update with ID: {}", group.getGroupId());
			throw new UnexpectedAffectedRowsException("No group found to update with ID: " + group.getGroupId());
		}

		logger.info("Group updated successfully: {}", group);
	}

	@Override
	public void deleteById(Long id) {
		logger.debug("Attempting to delete group with ID: {}", id);
		Map<String, Object> params = Map.of(DBSchemaConstants.PARAM_GROUP_ID, id);

		int deleted = namedJdbcTemplate.update(DELETE_BY_ID, params);
		if (deleted != 1) {
			logger.warn("No group found to delete with ID: {}", id);
			throw new UnexpectedAffectedRowsException("No group found to delete with ID: " + id);
		}

		logger.info("Group deleted successfully with ID: {}", id);
	}
}
