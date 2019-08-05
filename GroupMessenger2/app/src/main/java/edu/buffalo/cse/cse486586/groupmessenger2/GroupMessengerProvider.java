package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import static edu.buffalo.cse.cse486586.groupmessenger2.GroupMessengerActivity.TAG;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    //private DatabaseHandler dbhelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         *
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        //Reference : PA 1
        //https://developer.android.com/training/data-storage/files#WriteInternalStorage


        Context context = getContext();
        String filename = values.getAsString("key");
        String string = values.getAsString("value");
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "File write failed");
        }
        Log.v("insert", values.toString());
        return uri;

    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        //dbhelper = new DatabaseHandler(getContext());
        //return true;
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        /*final SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor result;

        String mSelection = DatabaseHandler.COLUMN_KEY+"=?";
        String[] mSelectionArgs = new String[]{selection};

        result = db.query(DatabaseHandler.TABLE_NAME, projection, mSelection,
                mSelectionArgs, null, null, sortOrder);

        result.moveToNext();

        Log.v("query", selection);
        return result;*/

        //MatrixCursor cursor = new MatrixCursor(new String[] {"key","value"});
        Context context = getContext();
        String line = "" ;
        MatrixCursor cursor = null;
        //Refernece: https://stackoverflow.com/questions/12421814/how-can-i-read-a-text-file-in-android
        try
        {
            //Thread.sleep(500);
            FileInputStream instream = context.openFileInput(selection);
            //if (instream != null)
            //{
            InputStreamReader inputreader = new InputStreamReader(instream);
            BufferedReader buffreader = new BufferedReader(inputreader);
                /*String line,line1 = "";
                try
                {
                    while ((line = buffreader.readLine()) != null)
                        line1+=line;
                }catch (Exception e)
                {
                    e.printStackTrace();
                }*/
            line = buffreader.readLine();
            //String rec [] = {selection , line};
            cursor = new MatrixCursor(new String[] {"key","value"});
            cursor.addRow(new String[]{  selection, line });
            //return cursor;
            //}
        }
        catch (Exception e)
        {
            String error="";
            error=e.getMessage();
        }



        Log.v("query", selection + " = " + line);
        return cursor;
    }
}