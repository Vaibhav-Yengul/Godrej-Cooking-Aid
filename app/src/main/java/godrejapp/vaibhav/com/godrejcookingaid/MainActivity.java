package godrejapp.vaibhav.com.godrejcookingaid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    DBAdapter dbAdapter;
    List<String> myList;
    MyListAdapter ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                Intent intent = new Intent(getBaseContext(),NewRecipe.class);
                startActivity(intent);
            }
        });


        final ListView lvRecipe = (ListView) findViewById(R.id.list_recipes);
        dbAdapter = new DBAdapter(this);
        ad = new MyListAdapter(this, dbAdapter.getData());
        lvRecipe.setAdapter(ad);

        lvRecipe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object listItem = lvRecipe.getItemAtPosition(position);
                //Toast.makeText(getBaseContext(),listItem.toString(),Toast.LENGTH_SHORT).show();

                String listitem = listItem.toString();
                int spaceIndex = listitem.indexOf(" ");
                int intentID = Integer.parseInt(listitem.substring(0,spaceIndex));
                String intentTitle = listitem.substring(spaceIndex+1, listitem.length());
                Intent I = new Intent(getBaseContext(), RecipeName.class);
                I.putExtra("Title",intentTitle);
                I.putExtra("UID",intentID);
                startActivity(I);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ad.swapItems(dbAdapter.getData());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
