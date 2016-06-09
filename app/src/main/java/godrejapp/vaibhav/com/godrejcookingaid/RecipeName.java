package godrejapp.vaibhav.com.godrejcookingaid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class RecipeName extends AppCompatActivity {

    DBAdapter dbAdapter;
    ListView lv;
    ArrayList<String> rsteps;
    int uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_name);
        String title = getIntent().getStringExtra("Title");
//        getActionBar().setTitle(title);
        lv = (ListView) findViewById(R.id.content_recipe_name_lv);
        uid = getIntent().getIntExtra("UID", -1);

        TextView tv = (TextView) findViewById(R.id.content_recipe_name_tv);
        tv.setText(title);
        dbAdapter = new DBAdapter(this);
        if (uid > 0) {
            String steps = dbAdapter.getAllSteps(title, uid);
            steps = steps.replace("[", "").replace("]", "");

            rsteps = new ArrayList<String>(Arrays.asList(steps.split(",")));

            for (int i = 0; i < rsteps.size(); i++) {
                rsteps.set(i, (i + 1) + " " + rsteps.get(i));
            }
            ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rsteps);
            lv.setAdapter(ad);
        } else {
            Toast.makeText(getBaseContext(), "UID not received", Toast.LENGTH_SHORT).show();
        }
    }

    public void RecordButtonClick(View view) {
        Intent intent = new Intent(getBaseContext(), RecordRecipe.class);
        String senddata = rsteps.toString();
        intent.putExtra("Steps", senddata);
        intent.putExtra("UID", uid);
        startActivity(intent);
    }

    public void  PlayButtonClick(View view){
        Intent intent = new Intent(getBaseContext(), PlayRecipe.class);
        String senddata = rsteps.toString();
        intent.putExtra("Steps", senddata);
        intent.putExtra("UID",uid);
        startActivity(intent);
    }
}
