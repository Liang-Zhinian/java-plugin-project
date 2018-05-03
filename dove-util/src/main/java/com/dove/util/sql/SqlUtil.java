package com.dove.util.sql;

/**
 * This class contains methods about SQL handling
 * 
 * @author Liang Zhinian - 
 * @version 4.5
 */
public class SqlUtil {

	public static String doubleQuotes(String input){
		return input.replaceAll("'", "''");
	}
	
	public void initialize(int[] vector, int index){
		vector[index]=0;
		if(index<vector.length)
			initialize(vector, index++);
	}
}
