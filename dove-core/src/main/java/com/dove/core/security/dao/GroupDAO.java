package com.dove.core.security.dao;

import java.util.Collection;

import com.dove.core.PersistentObjectDAO;
import com.dove.core.security.Group;

public interface GroupDAO extends PersistentObjectDAO<Group> {
	/**
	 * This method persists a new group object. All permissions and extended
	 * attributes of the parent group will be replicated.
	 * 
	 * @param group Group which should be stored in a database.
	 * @param parentGroupId ID of the group this group inherits ACLs from
	 * @return True if successful stored in a database.
	 */
	public boolean insert(Group group, long parentGroupId);

	/**
	 * This method replicates all ACLs of the parent group to another group.
	 * <p>
	 * <b>Attention:</b> The group(groupId) ACLs will be discarded.
	 * 
	 * @param groupId The group to be altered.
	 * @param parentGroupId The group whose ACLs will be inherited.
	 */
	public void inheritACLs(long groupId, long parentGroupId);

	/**
	 * Finds a group by name.
	 * 
	 * @param name name of wanted group.
	 * @param tenantId ID of the owning tenant
	 * 
	 * @return Wanted group or null.
	 */
	public Group findByName(String name, long tenantId);

	/**
	 * This method selects all groupnames.
	 */
	public Collection<String> findAllGroupNames(long tenantId);

	/**
	 * This method finds a Group by name.
	 * 
	 * @param name The name of wanted Group.
	 * @param tenantId ID of the owning tenant
	 * 
	 * @return Collection of selected groups.
	 */
	public Collection<Group> findByLikeName(String name, long tenantId);

	/**
	 * Counts the total number of groups
	 */
	public int count();

	/**
	 * Initialize the group collections.
	 */
	public void initialize(Group group);
}