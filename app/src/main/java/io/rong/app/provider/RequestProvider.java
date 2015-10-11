package io.rong.app.provider;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class RequestProvider extends ContentProvider {
	public static final String AUTHORITY = "io.rong.app.provider";
	public static final String TABLE_NAME = "request";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);

	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int REQUEST = 1;
	private static final int REQUEST_ID = 2;

	static {
		URI_MATCHER.addURI(AUTHORITY, "request", REQUEST);
		URI_MATCHER.addURI(AUTHORITY, "request/#", REQUEST_ID);
	}

	private static final String TAG = "RequestProvider";

	private SQLiteOpenHelper mOpenHelper;
	
	public RequestProvider() {
		
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (URI_MATCHER.match(uri)) {

		case REQUEST:
			count = db.delete(TABLE_NAME, where, whereArgs);
			break;
		case REQUEST_ID:
			String segment = uri.getPathSegments().get(1);

			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}

			count = db.delete(TABLE_NAME, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Cannot delete from URL: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		int match = URI_MATCHER.match(uri);
		switch (match) {
		case REQUEST:
			return RequestConstants.CONTENT_TYPE;
		case REQUEST_ID:
			return RequestConstants.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// TODO Auto-generated method stub
		if (URI_MATCHER.match(uri) != REQUEST) {
			throw new IllegalArgumentException("Cannot insert into URL: " + uri);
		}

		ContentValues values = (initialValues != null) ? new ContentValues(
				initialValues) : new ContentValues();

		for (String colName : RequestConstants.getRequiredColumns()) {
			if (values.containsKey(colName) == false) {
				throw new IllegalArgumentException("Missing column: " + colName);
			}
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = db.insert(TABLE_NAME, RequestConstants.USERID, values);

		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + uri);
		}

		Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
		getContext().getContentResolver().notifyChange(noteUri, null);
		return noteUri;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mOpenHelper = new RequestDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs,
			String sortOrder) {
		// TODO Auto-generated method stub


		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = URI_MATCHER.match(uri);

		switch (match) {
		case REQUEST:
			qBuilder.setTables(TABLE_NAME);
			break;
		case REQUEST_ID:
			qBuilder.setTables(TABLE_NAME);
			qBuilder.appendWhere("_id=");
			qBuilder.appendWhere(uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = RequestConstants.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qBuilder.query(db, projectionIn, selection, selectionArgs,
				null, null, orderBy);

		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		int count;
		int match = URI_MATCHER.match(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (match) {
		case REQUEST:
			count = db.update(TABLE_NAME, values, where, whereArgs);
			break;
		case REQUEST_ID:
			String segment = uri.getPathSegments().get(1);
			count = db.update(TABLE_NAME, values, "_id=" + segment, null);
			break;
		default:
			throw new UnsupportedOperationException("Cannot update URL: " + uri);
		}

		Log.d(TAG, "*** notifyChange() rowId: " + " url " + uri);

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	private static class RequestDatabaseHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "request.db";
		private static final int DATABASE_VERSION = 2;

		public RequestDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "creating new chat table");

			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + RequestConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ RequestConstants.USERID + " TEXT,"
					+ RequestConstants.USERNAME + " TEXT," 
					+ RequestConstants.PORTRAIT + " TEXT,"
					+ RequestConstants.CLASSID + " TEXT,"
					+ RequestConstants.CLASSNAME + " TEXT,"
					+ RequestConstants.ISCLASS + " INTEGER,"
					+ RequestConstants.STATUS + " INTEGER);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
				onCreate(db);
			}
		}


	public static final class RequestConstants implements BaseColumns {

		private RequestConstants() {
		}

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.rong.request";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rong.request";
		public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by
																	// auto-id

		public static final String USERID = "user_id";
		public static final String USERNAME = "username";
		public static final String PORTRAIT = "portrait";
		public static final String STATUS = "status";
		public static final String CLASSID = "class_id";
		public static final String CLASSNAME = "class_name";
		public static final String ISCLASS = "is_class";
		
		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();
			tmpList.add(USERID);
			tmpList.add(USERNAME);
			tmpList.add(PORTRAIT);
			tmpList.add(STATUS);
			tmpList.add(ISCLASS);
			return tmpList;
		}

	}

}

