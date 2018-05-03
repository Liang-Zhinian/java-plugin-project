package com.dove.core.security.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import com.dove.core.HibernatePersistentObjectDAO;
import com.dove.core.PersistentObject;
import com.dove.core.generic.Generic;
import com.dove.core.generic.GenericDAO;
import com.dove.core.security.Group;
import com.dove.core.security.Tenant;
import com.dove.core.security.User;
import com.dove.core.security.UserGroup;
import com.dove.core.security.UserHistory;
import com.dove.core.security.UserListener;
import com.dove.core.security.UserListenerManager;
import com.dove.util.Context;
import com.dove.util.StringUtil;
import com.dove.util.config.ContextProperties;
import com.dove.util.crypt.CryptUtil;

/**
 * Hibernate implementation of <code>UserDAO</code>
 * 
 * @author Marco Meschieri - Logical Objects
 * @since 3.0
 */
@SuppressWarnings("unchecked")
public class HibernateUserDAO extends HibernatePersistentObjectDAO<User> implements UserDAO {

	private GenericDAO genericDAO;

	private UserHistoryDAO userHistoryDAO;

	private UserListenerManager userListenerManager;

	private ContextProperties config;

	private HibernateUserDAO() {
		super(User.class);
		super.log = LoggerFactory.getLogger(HibernateUserDAO.class);
	}

	public static boolean ignoreCaseLogin() {
		return "true".equals(Context.get().getProperties().getProperty("login.ignorecase"));
	}

	/**
	 * @see com.dove.core.security.dao.UserDAO#delete(long)
	 */
	public boolean delete(long userId, int code) {
		return delete(userId, null);
	}

	/**
	 * @see com.dove.core.security.dao.UserDAO#findByName(java.lang.String)
	 */
	public List<User> findByName(String name) {
		return findByWhere("lower(_entity.name) like ?1", new Object[] { name.toLowerCase() }, null, null);
	}

	@Override
	public User findByUsername(String username) {
		User user = null;
		List<User> coll = findByWhere("_entity.username = ?1", new Object[] { username }, null, null);
		if (coll.size() > 0)
			user = coll.iterator().next();
		initialize(user);
		return user;
	}

	@Override
	public User findByUsernameIgnoreCase(String username) {
		User user = null;
		List<User> coll = findByWhere("lower(_entity.username) = ?1", new Object[] { username.toLowerCase() }, null,
				null);
		if (coll.size() > 0)
			user = coll.iterator().next();
		initialize(user);
		return user;
	}

	/**
	 * @see com.dove.core.security.dao.UserDAO#findByUsername(java.lang.String)
	 */
	public List<User> findByLikeUsername(String username) {
		return findByWhere("_entity.username like ?1", new Object[] { username }, null, null);
	}

	/**
	 * @see com.dove.core.security.dao.UserDAO#findByUsernameAndName(java.lang.String,
	 *      java.lang.String)
	 */
	public List<User> findByUsernameAndName(String username, String name) {
		return findByWhere("lower(_entity.name) like ?1 and _entity.username like ?2",
				new Object[] { name.toLowerCase(), username }, null, null);
	}

	/**
	 * @see com.dove.core.security.dao.UserDAO#store(com.dove.core.security.User)
	 */
	public boolean store(User user) {
		return store(user, null);
	}

	@Override
	public boolean store(User user, UserHistory transaction) {
		boolean result = true;
		boolean newUser = user.getId() == 0;

		try {
			if (user.getId() == 0L)
				if (findByUsernameIgnoreCase(user.getUsername()) != null)
					throw new Exception("Another user exists with the same username " + user.getUsername()
							+ " (perhaps with different case)");

			Map<String, Object> dictionary = new HashMap<String, Object>();

			log.debug("Invoke listeners before store");
			for (UserListener listener : userListenerManager.getListeners()) {
				listener.beforeStore(user, transaction, dictionary);
			}

			saveOrUpdate(user);
			initialize(user);

			GroupDAO groupDAO = (GroupDAO) Context.get().getBean(GroupDAO.class);
			String userGroupName = user.getUserGroupName();
			Group grp = groupDAO.findByName(userGroupName, user.getTenantId());
			if (grp == null) {
				grp = new Group();
				grp.setName(userGroupName);
				grp.setType(Group.TYPE_USER);
				grp.setTenantId(user.getTenantId());
				groupDAO.store(grp);

				saveUserHistory(user, transaction);
			}
			if (!user.getGroups().contains(grp)) {
				user.getGroups().add(grp);
				user.getUserGroups().add(new UserGroup(grp.getId()));
				saveOrUpdate(user);
			}

			if (newUser) {
				// Save default dashlets
				Generic dash = new Generic("usersetting", "dashlet-1", user.getId());
				dash.setInteger1(1L);
				dash.setInteger2(0L);
				dash.setInteger3(0L);
				dash.setString1("0");
				dash.setTenantId(user.getTenantId());
				genericDAO.store(dash);
				dash = new Generic("usersetting", "dashlet-3", user.getId());
				dash.setInteger1(3L);
				dash.setInteger2(0L);
				dash.setInteger3(1L);
				dash.setString1("0");
				dash.setTenantId(user.getTenantId());
				genericDAO.store(dash);
				dash = new Generic("usersetting", "dashlet-6", user.getId());
				dash.setInteger1(6L);
				dash.setInteger2(1L);
				dash.setInteger3(0L);
				dash.setString1("0");
				dash.setTenantId(user.getTenantId());
				genericDAO.store(dash);
			}

			// If the admin password was changed the 'adminpasswd' also has to
			// be updated
			if ("admin".equals(user.getUsername()) && user.getTenantId() == Tenant.DEFAULT_ID) {
				log.info("Updated adminpasswd");
				config.setProperty("adminpasswd", user.getPassword());
				config.write();
			}
		} catch (Throwable e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			result = false;
		}

		return result;
	}

	/**
	 * @see com.dove.core.security.dao.UserDAO#validateUser(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean validateUser(String username, String password) {
		boolean result = true;

		try {
			User user = null;
			if (ignoreCaseLogin())
				user = findByUsernameIgnoreCase(username);
			else
				user = findByUsername(username);

			if (!validateUser(user))
				return false;

			// Check the password match
			if (!user.getPassword().equals(CryptUtil.cryptString(password)))
				result = false;
		} catch (Throwable e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			result = false;
		}
		return result;
	}

	@Override
	public boolean validateUser(String username) {
		boolean result = true;
		try {
			if (ignoreCaseLogin())
				result = validateUser(findByUsername(username));
			else
				result = validateUser(findByUsernameIgnoreCase(username));
		} catch (Throwable e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			result = false;
		}
		return result;
	}

	private boolean validateUser(User user) {
		// Check the password match
		if ((user == null) || user.getType() != User.TYPE_DEFAULT)
			return false;

		// Check if the user is enabled
		if (user != null && user.getEnabled() == 0)
			return false;

		if (isPasswordExpired(user.getUsername()))
			return false;

		return true;
	}

	public int getPasswordTtl() {
		int value = 90;
		if (config.getProperty("password.ttl") != null)
			value = config.getInt("password.ttl");
		return value;
	}

	@Override
	public boolean isPasswordExpired(String username) {
		try {
			User user = findByUsernameIgnoreCase(username);
			if (user == null)
				return false;

			if (user.getPasswordExpired() == 1)
				return true;

			if (getPasswordTtl() <= 0)
				return false;

			// Check if the password is expired
			if (user.getPasswordExpires() == 1) {
				Date lastChange = user.getPasswordChanged();
				if (lastChange == null)
					return false;
				Calendar calendar = new GregorianCalendar();
				calendar.setTime(lastChange);
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR, 0);
				lastChange = calendar.getTime();

				calendar.setTime(new Date());
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR, 0);

				calendar.add(Calendar.DAY_OF_MONTH, -getPasswordTtl());
				Date date = calendar.getTime();

				return (lastChange.before(date));
			}
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			return true;
		}
		return false;
	}

	@Override
	public int count(Long tenantId) {
		String query = "select count(*) from ld_user where ld_type=0 and not(ld_deleted=1) "
				+ (tenantId != null ? " and ld_tenantid=" + tenantId : "");
		return queryForInt(query);
	}

	@Override
	public boolean delete(long userId, UserHistory transaction) {
		boolean result = true;

		try {
			User user = (User) findById(userId);
			Group userGroup = user.getUserGroup();

			if (user != null) {
				user.setDeleted(PersistentObject.DELETED_CODE_DEFAULT);
				user.setUsername(user.getUsername() + "." + user.getId());
				saveOrUpdate(user);
			}

			// Delete the user's group
			if (userGroup != null) {
				GroupDAO groupDAO = (GroupDAO) Context.get().getBean(GroupDAO.class);
				groupDAO.delete(userGroup.getId());
			}

			saveUserHistory(user, transaction);
		} catch (Throwable e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			result = false;
		}

		return result;
	}

	private void saveUserHistory(User user, UserHistory transaction) {
		if (transaction == null)
			return;

//		transaction.setUser(user);
//		transaction.setNotified(0);

		userHistoryDAO.store(transaction);
	}

	public UserHistoryDAO getUserHistoryDAO() {
		return userHistoryDAO;
	}

	public void setUserHistoryDAO(UserHistoryDAO userHistoryDAO) {
		this.userHistoryDAO = userHistoryDAO;
	}

	public void setUserListenerManager(UserListenerManager userListenerManager) {
		this.userListenerManager = userListenerManager;
	}

	public UserListenerManager getUserListenerManager() {
		return userListenerManager;
	}

	@Override
	public void initialize(User user) {
		if (user == null)
			return;

		refresh(user);

		for (UserGroup ug : user.getUserGroups()) {
			ug.getGroupId();
		}

		user.getGroups().clear();
		if (!user.getUserGroups().isEmpty()) {
			List<Long> groupIds = user.getUserGroups().stream().map(u -> u.getGroupId()).collect(Collectors.toList());
			GroupDAO gDao = (GroupDAO) Context.get().getBean(GroupDAO.class);
			List<Group> groups = gDao.findByWhere(
					"_entity.id in (" + StringUtil.arrayToString(groupIds.toArray(new Long[0]), ",") + ")", null, null);
			for (Group group : groups)
				user.getGroups().add(group);
		}
	}

	@Override
	public Map<String, Generic> findUserSettings(long userId, String namePrefix) {
		List<Generic> generics = genericDAO.findByTypeAndSubtype("usersetting", namePrefix + "%", userId, null);
		Map<String, Generic> map = new HashMap<String, Generic>();
		for (Generic generic : generics) {
			map.put(generic.getSubtype(), generic);
		}
		return map;
	}

	public void setGenericDAO(GenericDAO genericDAO) {
		this.genericDAO = genericDAO;
	}

	@Override
	public User findAdminUser(String tenantName) {
		if ("default".equals(tenantName))
			return findByUsername("admin");
		else
			return findByUsername("admin" + StringUtils.capitalize(tenantName));
	}

	public void setConfig(ContextProperties config) {
		this.config = config;
	}

	@Override
	public Set<User> findByGroup(long groupId) {
		List<Long> docIds = queryForList("select ld_userid from ld_usergroup where ld_groupid=" + groupId, Long.class);
		Set<User> set = new HashSet<User>();
		if (!docIds.isEmpty()) {
			String query = "_entity.id in (" + StringUtil.arrayToString(docIds.toArray(new Long[0]), ",") + ")";
			List<User> users = findByWhere(query, null, null, null);
			for (User user : users) {
				if (user.getDeleted() == 0 && !set.contains(user))
					set.add(user);
			}
		}
		return set;
	}

	@Override
	public User findById(long id) {
		User user = super.findById(id);
		if (user != null)
			initialize(user);
		return user;
	}

	@Override
	public User getUser(String username) {
		User user = null;
		if (HibernateUserDAO.ignoreCaseLogin())
			user = findByUsernameIgnoreCase(username);
		else
			user = findByUsername(username);
		initialize(user);
		return user;
	}
}