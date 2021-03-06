package com.dove.core.communication;

import java.util.List;

import com.dove.core.PersistentObjectDAO;

/**
 * This is a DAO service for SystemMessages.
 * 
 * @author Michael Scholz
 * @author Marco Meschieri
 * @version 1.0
 */
public interface SystemMessageDAO extends PersistentObjectDAO<SystemMessage> {

	/**
	 * This method selects all the messages for the specified recipient and type
	 * 
	 * @param recipient The recipient name
	 * @param type The message type
	 * @param read Optional flag
	 * @return The messages list
	 */
	public List<SystemMessage> findByRecipient(String recipient, int type, Integer read);

	/**
	 * This methods gets the number of messages for the specified recipient and
	 * type, and optionally a specified read flag
	 * 
	 * @param recipient The recipient name
	 * @param type The message type
	 * @param read Optional flag
	 * @return The number of messages
	 */
	public int getCount(String recipient, int type, Integer read);

	/**
	 * Removes all system expired messages for the specified recipient
	 * 
	 * @param recipient The recipient
	 */
	public void deleteExpiredMessages(String recipient);

	/**
	 * Removes all expired messages for the specified type
	 * 
	 * @param type The message type
	 */
	public void deleteExpiredMessages(int type);

	/**
	 * This method selects all the messages for the specified type
	 * 
	 * @param recipient The recipient name
	 * @return The list of messages with the given type
	 */
	public List<SystemMessage> findByType(int type);

	/**
	 * This method selects all the messages for the specified mode
	 * 
	 * @param mode The message mode
	 * @return The list of messages with the given mode
	 */
	public List<SystemMessage> findByMode(String mode);

	/**
	 * This method selects all the messages for the specified type that are not
	 * been already sent and for which the number of sending trials is less than
	 * the maximum number (parameter 'notifier.maxtrials')
	 * 
	 * @param type The message type
	 * @param maxTrials The maximum number of sending trials
	 */
	public List<SystemMessage> findMessagesToBeSent(int type, int maxTrials);
}