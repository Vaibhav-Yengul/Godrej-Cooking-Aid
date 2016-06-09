package godrejapp.vaibhav.com.godrejcookingaid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 5/26/2016.
 */
public class DBAdapter {

    static class DBHelper  extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "GodrejApp";
        private static final String TABLE_NAME = "RECIPElIST";
        private static final int VERSION_NUMBER = 4;

        private static final String UID = "_id";
        private static final String TITLE = "Title";
        private static final String STEPS = "Steps";
        private static final String TEMPERATURES = "Temperatures";
        private static final String SPLTEMPERATURES = "Special";
        private static final String SPLTIMES = "Times";
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +TITLE+" VARCHAR(255), "+STEPS+" VARCHAR(5000), "+TEMPERATURES+" VARCHAR(5000), "+
                SPLTEMPERATURES+" VARCHAR(255), "+SPLTIMES+" VARCHAR(255));";

        private static final String DROP_TABLE = "DROP TABLE IF EXISTS "+TABLE_NAME;

        private Context context;
        public DBHelper(Context context){
            super(context, DATABASE_NAME, null, VERSION_NUMBER);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //CREATE TABLE RECIPELIST (_id INTEGER PRIMARY KEY AUTOINCREMENT, Title VARCHAR(255), Steps VARCHAR(5000), Temperatures VARCHAR(255));

            try {
                db.execSQL(CREATE_TABLE);
            } catch (SQLException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(DROP_TABLE);
                onCreate(db);
            } catch (SQLException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    DBHelper dbHelper;

    public DBAdapter(Context context){
        dbHelper = new DBHelper(context);
    }
    public long insertData(String title, String steps, String temps, String stemps,String stimes){
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.TITLE,title);
        cv.put(DBHelper.STEPS,steps);
        cv.put(DBHelper.TEMPERATURES,temps);
        cv.put(DBHelper.SPLTEMPERATURES, stemps);
        cv.put(DBHelper.SPLTIMES,stimes);

        SQLiteDatabase sqldb = dbHelper.getWritableDatabase();
        long id = sqldb.insert(DBHelper.TABLE_NAME, null, cv);
        return id;
    }

    public long updateTempData(String temp, int _id){
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.TEMPERATURES,temp);
        SQLiteDatabase sq = dbHelper.getWritableDatabase();
        String where = DBHelper.UID + "="+_id;
        long id = sq.update(DBHelper.TABLE_NAME, cv, where, null);
        return id;
    }

    public long updateSplTemperatures(String temp, int _id){
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.SPLTEMPERATURES,temp);
        SQLiteDatabase sq = dbHelper.getWritableDatabase();
        String where = DBHelper.UID + "="+_id;
        long id = sq.update(DBHelper.TABLE_NAME, cv, where, null);
        return id;
    }

    public long updateSplTime(String temp, int _id) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.SPLTIMES,temp);
        SQLiteDatabase sq = dbHelper.getWritableDatabase();
        String where = DBHelper.UID + "="+_id;
        long id = sq.update(DBHelper.TABLE_NAME,cv,where,null);
        return id;
    }

    public List<String> getData(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.TITLE};
        Cursor c = db.query(DBHelper.TABLE_NAME, columns, null, null, null, null, null);
        StringBuffer sb = new StringBuffer();
        while(c.moveToNext()){
            int cid = c.getInt(c.getColumnIndex(DBHelper.UID));
            String ctitle = c.getString(c.getColumnIndex(DBHelper.TITLE));
            sb.append(cid + " " + ctitle +"\n");
        }

        List<String> myList = new ArrayList<String>(Arrays.asList(sb.toString().split("\n")));
        return myList;
    }

    public String getAllSteps(String title, int id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DBHelper.STEPS};
        String where = DBHelper.UID + "="+id+" AND "+ DBHelper.TITLE+"='"+title+"'";
        Cursor c = db.query(DBHelper.TABLE_NAME, columns,where,null,null,null,null);
        StringBuffer sb = new StringBuffer();
        while(c.moveToNext()){
            String ctitle = c.getString(c.getColumnIndex(DBHelper.STEPS));
            sb.append(ctitle);
        }
        return sb.toString();
    }


    public String getAllTemps(int _id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DBHelper.TEMPERATURES};
        String where = DBHelper.UID + "="+_id;
        Cursor c = db.query(DBHelper.TABLE_NAME, columns,where,null,null,null,null);
        StringBuffer sb = new StringBuffer();
        while(c.moveToNext()){
            String ctemp = c.getString(c.getColumnIndex(DBHelper.TEMPERATURES));
            sb.append(ctemp);
        }
        return sb.toString();
    }

    public String getSplTemperatures(int _id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DBHelper.SPLTEMPERATURES};
        String where = DBHelper.UID + "="+_id;
        Cursor c = db.query(DBHelper.TABLE_NAME, columns,where,null,null,null,null);
        StringBuffer sb = new StringBuffer();
        while(c.moveToNext()){
            String ctemp = c.getString(c.getColumnIndex(DBHelper.SPLTEMPERATURES));
            sb.append(ctemp);
        }
        return sb.toString();
    }

    public String getSplTime(int _id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DBHelper.SPLTIMES};
        String where = DBHelper.UID + "="+_id;
        Cursor c = db.query(DBHelper.TABLE_NAME, columns,where,null,null,null,null);
        StringBuffer sb = new StringBuffer();
        while(c.moveToNext()){
            String ctemp = c.getString(c.getColumnIndex(DBHelper.SPLTIMES));
            sb.append(ctemp);
        }
        return sb.toString();
    }
}