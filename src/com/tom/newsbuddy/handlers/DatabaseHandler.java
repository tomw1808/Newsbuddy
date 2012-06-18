package com.tom.newsbuddy.handlers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.tom.newsbuddy.classes.Articles;
import com.tom.newsbuddy.classes.Groups;
import com.tom.newsbuddy.classes.Server;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "newsbuddy";

	// Contacts Table Columns names
	private static final String SERVER_TABLE = "server";
	private static final String SERVER_ID = "id";
	private static final String SERVER_ADDRESS = "address";
	private static final String SERVER_PORT = "port";
	private static final String SERVER_USERNAME = "username";
	private static final String SERVER_PASSWORD = "password";
	private static final String SERVER_SSL = "ssl";

	// Groups Table Columns
	private static final String GROUPS_TABLE = "groups";
	private static final String GROUPS_ID = "id";
	private static final String GROUPS_NAME = "name";
	private static final String GROUPS_HIGH = "high";
	private static final String GROUPS_LOW = "low";
	private static final String GROUPS_PERMISSION = "permission";
	private static final String GROUPS_SERVER_FKID = "server_fkid";

	// Articles Table
	private static final String ART_TABLE = "articles";
	private static final String ART_ID = "id";
	private static final String ART_NUMBER = "article_number";
	private static final String ART_MESSID = "message_id";
	private static final String ART_FROM = "isfrom";
	private static final String ART_SUBJECT = "subject";
	private static final String ART_BODY = "body";
	private static final String ART_REF = "referenceMap";
	private static final String ART_LEVEL = "level";
	private static final String ART_DATE = "date";
	private static final String ART_GROUPS_FKID = "groups_fkid";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SERVERS_TABLE = "CREATE TABLE " + SERVER_TABLE + "("
				+ SERVER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ SERVER_ADDRESS + " TEXT NOT NULL," + SERVER_PORT
				+ " TEXT NOT NULL," + SERVER_USERNAME + " TEXT,"
				+ SERVER_PASSWORD + " TEXT," + SERVER_SSL + " INTEGER" + ")";

		String CREATE_GROUPS_TABLE = "CREATE TABLE " + GROUPS_TABLE + "("
				+ GROUPS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ GROUPS_NAME + " TEXT NOT NULL," + GROUPS_HIGH
				+ " TEXT NOT NULL," + GROUPS_LOW + " TEXT NOT NULL,"
				+ GROUPS_PERMISSION + " TEXT NOT NULL," + GROUPS_SERVER_FKID
				+ " INTEGER NOT NULL," + " FOREIGN KEY (" + GROUPS_SERVER_FKID
				+ ") REFERENCES " + SERVER_TABLE + " (" + SERVER_ID
				+ ") ON DELETE CASCADE" + ")";
		String CREATE_ARTICLES_TABLE = "CREATE TABLE " + ART_TABLE + "("
				+ ART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + ART_NUMBER
				+ " TEXT NOT NULL," + ART_MESSID + " TEXT NOT NULL," + ART_FROM
				+ " TEXT NOT NULL," + ART_SUBJECT + " TEXT NOT NULL,"
				+ ART_BODY + " TEXT," + ART_REF + " TEXT,"
				+ ART_LEVEL + " TEXT," + ART_DATE + " TEXT,"
				+ ART_GROUPS_FKID + " INTEGER NOT NULL, " + " FOREIGN KEY ("
				+ ART_GROUPS_FKID + ") REFERENCES " + GROUPS_TABLE + " ("
				+ GROUPS_ID + ") ON DELETE CASCADE" + ")";

		db.execSQL("PRAGMA foreign_keys = ON");
		db.execSQL(CREATE_SERVERS_TABLE);
		db.execSQL(CREATE_GROUPS_TABLE);
		db.execSQL(CREATE_ARTICLES_TABLE);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		db.execSQL("PRAGMA foreign_keys = ON");
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + SERVER_TABLE);

		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + GROUPS_TABLE);
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + ART_TABLE);

		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new contact
	void addServer(Server server) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(SERVER_ADDRESS, server.getAddress()); // Contact Name
		values.put(SERVER_PORT, server.getPort()); // Contact Phone
		values.put(SERVER_USERNAME, server.getUsername()); // Contact Phone
		values.put(SERVER_PASSWORD, server.getPassword()); // Contact Phone
		values.put(SERVER_SSL, server.getSsl()); // Contact Phone

		// Inserting Row
		db.insert(SERVER_TABLE, null, values);
		db.close(); // Closing database connection
	}

	public void addServerIfItsNotExistent(Server server) {
		try {
			this.getServerByAddress(server.getAddress());
		} catch (Exception e) {
			this.addServer(server);
		}
	}

	Server getServerByAddress(String name) throws Exception {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(SERVER_TABLE, new String[] { SERVER_ID,
				SERVER_ADDRESS, SERVER_PORT, SERVER_USERNAME, SERVER_PASSWORD,
				SERVER_SSL }, SERVER_ADDRESS + "=?",
				new String[] { String.valueOf(name) }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		} else {
			throw new Exception("Server does not exist");
		}

		Server server = new Server(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2), cursor.getString(3),
				cursor.getString(4), cursor.getString(5));
		// return contact
		return server;
	}

	// Getting single contact
	public Server getServer(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(SERVER_TABLE, new String[] { SERVER_ID,
				SERVER_ADDRESS, SERVER_PORT, SERVER_USERNAME, SERVER_PASSWORD,
				SERVER_SSL }, SERVER_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		Server server = new Server(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2), cursor.getString(3),
				cursor.getString(4), cursor.getString(5));
		// return contact
		return server;
	}

	// Getting All Contacts
	public ArrayList<Server> getAllServers() {
		ArrayList<Server> serverList = new ArrayList<Server>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + SERVER_TABLE;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Server server = new Server();
				server.setId(Integer.parseInt(cursor.getString(0)));
				server.setAddress(cursor.getString(1));
				server.setPort(cursor.getString(2));
				// Adding contact to list
				serverList.add(server);
			} while (cursor.moveToNext());
		}

		// return contact list
		return serverList;
	}

	// Updating single contact
	public int updateServer(Server server) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(SERVER_ADDRESS, server.getAddress());
		values.put(SERVER_PORT, server.getPort());
		values.put(SERVER_USERNAME, server.getUsername());
		values.put(SERVER_PASSWORD, server.getPassword());
		values.put(SERVER_SSL,server.getSsl());

		// updating row
		return db.update(SERVER_TABLE, values, SERVER_ID + " = ?",
				new String[] { String.valueOf(server.getId()) });
	}

	// Deleting single contact
	public void deleteServer(Server server) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(SERVER_TABLE, SERVER_ID + " = ?",
				new String[] { String.valueOf(server.getId()) });
		db.close();
	}

	// Getting contacts Count
	public int getServersCount() {
		String countQuery = "SELECT  * FROM " + SERVER_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);

		// return count
		int count = cursor.getCount();
		db.close();
		return count;

	}

	public void addGroup(Groups group) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(GROUPS_NAME, group.getName());
		values.put(GROUPS_HIGH, group.getHigh());
		values.put(GROUPS_LOW, group.getLow());
		values.put(GROUPS_PERMISSION, group.getPermission());
		values.put(GROUPS_SERVER_FKID, group.getServer_fkid());
		// Inserting Row
		db.insert(GROUPS_TABLE, null, values);
		db.close(); // Closing database connection
	}

	public void addGroupIfItsNotExistent(Groups group) {
		try {
			this.getGroupByName(group.getName(), group.getServer_fkid());
		} catch (Exception e) {
			this.addGroup(group);
		}
	}

	public Groups getGroupByName(String name, int server_fkid) throws Exception {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(GROUPS_TABLE, new String[] { GROUPS_ID,
				GROUPS_NAME, GROUPS_HIGH, GROUPS_LOW, GROUPS_PERMISSION,
				GROUPS_SERVER_FKID }, GROUPS_NAME + "=?"+ " AND "+GROUPS_SERVER_FKID+"=?",
				new String[] { String.valueOf(name), String.valueOf(server_fkid) }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		} else {
			throw new Exception("Group does not exist");
		}

		Groups group = new Groups(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2), cursor.getString(3),
				cursor.getString(4), Integer.parseInt(cursor.getString(5)));
		// return contact
		return group;
	}

	// Deleting single contact
	public void deleteGroup(Groups group) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(GROUPS_TABLE, GROUPS_ID + " = ?",
				new String[] { String.valueOf(group.getId()) });
		db.close();
	}

	// Getting All Contacts
	public ArrayList<Groups> getAllGroups(int server_fkid) throws Exception {
		ArrayList<Groups> list = new ArrayList<Groups>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(GROUPS_TABLE, new String[] { GROUPS_ID,
				GROUPS_NAME, GROUPS_HIGH, GROUPS_LOW, GROUPS_PERMISSION,
				GROUPS_SERVER_FKID }, GROUPS_SERVER_FKID + "=?",
				new String[] { String.valueOf(server_fkid) }, null, null, null, null);
		if (cursor != null) {
			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					Groups group = new Groups(Integer.parseInt(cursor.getString(0)),
							cursor.getString(1), cursor.getString(2), cursor.getString(3),
							cursor.getString(4), Integer.parseInt(cursor.getString(5)));
					// Adding contact to list
					list.add(group);
				} while (cursor.moveToNext());
			}
		} else {
			throw new Exception("Group does not exist");
		}

		// return contact list
		return list;
	}

	// Getting contacts Count
	public int getGroupsCount(int server_idfk) {
		String countQuery = "SELECT  * FROM " + GROUPS_TABLE+ " WHERE "+GROUPS_SERVER_FKID+ " = "+String.valueOf(server_idfk);
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);

		// return count
		int count = cursor.getCount();
		db.close();
		return count;

	}

	// Getting single contact
	public Groups getGroup(int id) throws Exception {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(GROUPS_TABLE, new String[] { GROUPS_ID,
				GROUPS_NAME, GROUPS_HIGH, GROUPS_LOW, GROUPS_PERMISSION,
				GROUPS_SERVER_FKID }, GROUPS_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		} else {
			throw new Exception("Group does not exist");
		}

		Groups group = new Groups(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2), cursor.getString(3),
				cursor.getString(4), Integer.parseInt(cursor.getString(5)));
		// return contact
		return group;
	}

	public void addArticle(Articles article) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ART_NUMBER, article.getArticleNumber());
		values.put(ART_BODY, article.getContent());
		if(article.getDate() != null) {
			values.put(ART_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(article.getDate()));
		}
		values.put(ART_FROM, article.getFrom());
		values.put(ART_GROUPS_FKID, article.getGroups_fkid());

		values.put(ART_LEVEL, article.getLevel());
		values.put(ART_MESSID, article.getMessageId());
		values.put(ART_REF, article.getReferenceMap());
		values.put(ART_SUBJECT, article.getSubject());

		// Inserting Row
		db.insert(ART_TABLE, null, values);
		db.close(); // Closing database connection
	}

	Articles getArticleByMessageID(String message_id) throws Exception {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(ART_TABLE, new String[] { ART_ID, ART_BODY,
				ART_DATE, ART_FROM, ART_GROUPS_FKID, ART_LEVEL, ART_MESSID,
				ART_REF, ART_SUBJECT, ART_NUMBER}, ART_MESSID + "=?",
				new String[] { String.valueOf(message_id) }, null, null, null,
				null);
		if (cursor != null) {
			cursor.moveToFirst();
		} else {
			throw new Exception("Article does not exist");
		}

		Articles article = new Articles();
		article.setId(Integer.parseInt(cursor.getString(0)));
		article.setContent(cursor.getString(1));
		article.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(2)));
		article.setFrom(cursor.getString(3));
		article.setGroups_fkid(Integer.parseInt(cursor.getString(4)));
		article.setLevel(Integer.parseInt(cursor.getString(5)));
		article.setMessageId(cursor.getString(6));
		article.setReferenceMap(cursor.getString(7));
		article.setSubject(cursor.getString(8));

		article.setArticleNumber(cursor.getInt(9));
		article.setSortOrder(0);
		// return contact
		return article;
	}
	
	public Articles getArticle(int id) throws Exception {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(ART_TABLE, new String[] { ART_ID, ART_BODY,
				ART_DATE, ART_FROM, ART_GROUPS_FKID, ART_LEVEL, ART_MESSID,
				ART_REF, ART_SUBJECT, ART_NUMBER}, ART_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null,
				null);
		if (cursor != null) {
			cursor.moveToFirst();
		} else {
			throw new Exception("Article does not exist");
		}

		Articles article = new Articles();
		article.setId(Integer.parseInt(cursor.getString(0)));
		article.setContent(cursor.getString(1));
		article.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(2)));
		article.setFrom(cursor.getString(3));
		article.setGroups_fkid(Integer.parseInt(cursor.getString(4)));
		article.setLevel(Integer.parseInt(cursor.getString(5)));
		article.setMessageId(cursor.getString(6));
		article.setReferenceMap(cursor.getString(7));
		article.setSubject(cursor.getString(8));

		article.setArticleNumber(cursor.getInt(9));
		article.setSortOrder(0);
		// return contact
		return article;
	}

	Articles getArticleByMessageID(String message_id, String order_field)
			throws Exception {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(ART_TABLE, new String[] { ART_ID, ART_BODY,
				ART_DATE, ART_FROM, ART_GROUPS_FKID, ART_LEVEL, ART_MESSID,
				ART_REF, ART_SUBJECT, ART_NUMBER}, ART_MESSID + "=?",
				new String[] { String.valueOf(message_id) }, null, null, order_field,null);
		if (cursor != null) {
			cursor.moveToFirst();
		} else {
			throw new Exception("Article does not exist");
		}

		Articles article = new Articles();
		article.setId(Integer.parseInt(cursor.getString(0)));
		article.setContent(cursor.getString(1));
		article.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(2)));
		article.setFrom(cursor.getString(3));
		article.setGroups_fkid(Integer.parseInt(cursor.getString(4)));
		article.setLevel(Integer.parseInt(cursor.getString(5)));
		article.setMessageId(cursor.getString(6));
		article.setReferenceMap(cursor.getString(7));
		article.setSubject(cursor.getString(8));
		article.setArticleNumber(cursor.getInt(8));
		// return contact
		return article;
	}

	// Getting All Contacts
	public ArrayList<Articles> getAllArticles(int group_idfk, String order) throws Exception {
		SQLiteDatabase db = this.getReadableDatabase();

		ArrayList<Articles> list = new ArrayList<Articles>();
		Cursor cursor = db.query(ART_TABLE, new String[] { ART_ID, ART_BODY,
				ART_DATE, ART_FROM, ART_GROUPS_FKID, ART_LEVEL, ART_MESSID,
				ART_REF, ART_SUBJECT, ART_NUMBER}, ART_GROUPS_FKID + "=?",
				new String[] { String.valueOf(group_idfk) }, null, null, order+" DESC", null);
		int counter = 0;
		if (cursor != null) {
			cursor.moveToFirst();
			do {
				Articles article = new Articles();
				article.setId(Integer.parseInt(cursor.getString(0)));
				article.setContent(cursor.getString(1));
				String date = cursor.getString(2);
				article.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
				article.setFrom(cursor.getString(3));
				article.setGroups_fkid(Integer.parseInt(cursor.getString(4)));
				article.setLevel(Integer.parseInt(cursor.getString(5)));
				article.setMessageId(cursor.getString(6));
				article.setReferenceMap(cursor.getString(7));
				article.setSubject(cursor.getString(8));
				article.setSortOrder(counter);
				counter++;
				article.setArticleNumber(cursor.getInt(9));
				// Adding contact to list
				list.add(article);
			} while (cursor.moveToNext());
		} else {
			throw new Exception("No Articles in Table");
		}

		// return contact list
		return list;
	}// Getting All Contacts

	public ArrayList<Articles> getAllArticles(int group_idfk) throws Exception {
		SQLiteDatabase db = this.getReadableDatabase();

		ArrayList<Articles> list = new ArrayList<Articles>();
		Cursor cursor = db.query(ART_TABLE, new String[] { ART_ID, ART_BODY,
				ART_DATE, ART_FROM, ART_GROUPS_FKID, ART_LEVEL, ART_MESSID,
				ART_REF, ART_SUBJECT, ART_NUMBER}, ART_GROUPS_FKID + "=?",
				new String[] { String.valueOf(group_idfk) }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			do {
				Articles article = new Articles();
				article.setId(Integer.parseInt(cursor.getString(0)));
				article.setContent(cursor.getString(1));
				article.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(2)));
				article.setFrom(cursor.getString(3));
				article.setGroups_fkid(Integer.parseInt(cursor.getString(4)));
				article.setLevel(Integer.parseInt(cursor.getString(5)));
				article.setMessageId(cursor.getString(6));
				article.setReferenceMap(cursor.getString(7));
				article.setSubject(cursor.getString(8));
				article.setSortOrder(0);
				article.setArticleNumber(cursor.getInt(9));
				// Adding contact to list
				list.add(article);
			} while (cursor.moveToNext());
		} else {
			throw new Exception("No Articles in Table");
		}

		// return contact list
		return list;
	}
	
	// Getting contacts Count
		public int getArticlesCount(int groups_idfk) {
			String countQuery = "SELECT  * FROM " + ART_TABLE + " WHERE "+ART_GROUPS_FKID+ " = "+groups_idfk;
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(countQuery, null);

			// return count
			int count = cursor.getCount();
			db.close();
			return count;

		}
		// Getting contacts Count
		public int getLastArticleId(int groups_idfk) {
			String countQuery = "SELECT "+ART_NUMBER+" FROM " + ART_TABLE + " WHERE "+ART_GROUPS_FKID+ " = "+groups_idfk+" ORDER BY "+ART_NUMBER+" DESC";
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(countQuery, null);

			int count = 0;
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				count = cursor.getInt(0);
			}
			// return count
			
			db.close();
			return count;

		}
		public void addArticleIfItsNotExistent(Articles article) {
			try {
				this.getArticleByMessageID(article.getMessageId());
			} catch (Exception e) {
				this.addArticle(article);
			}
		}

		// Updating single contact
		public int updateArticle(Articles article) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(ART_NUMBER, article.getArticleNumber());
			values.put(ART_BODY, article.getContent());
			if(article.getDate() != null) {
				values.put(ART_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(article.getDate()));
			}
			values.put(ART_FROM, article.getFrom());
			values.put(ART_GROUPS_FKID, article.getGroups_fkid());

			values.put(ART_LEVEL, article.getLevel());
			values.put(ART_MESSID, article.getMessageId());
			values.put(ART_REF, article.getReferenceMap());
			values.put(ART_SUBJECT, article.getSubject());

			// updating row
			return db.update(ART_TABLE, values, ART_ID + " = ?",
					new String[] { String.valueOf(article.getId()) });
		}
}