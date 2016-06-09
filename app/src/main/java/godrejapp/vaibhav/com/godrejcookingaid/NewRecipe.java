package godrejapp.vaibhav.com.godrejcookingaid;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class NewRecipe extends AppCompatActivity {

    DBAdapter dbAdapter;
    EditText edIngredient, edQuantity, edTitle;
    Button btn,Savebutton;
    ArrayList<String> recipes_steps = new ArrayList<String>();
    ArrayList<Recipe> recipeBook = new ArrayList<Recipe>();
    ListView recipe_list;
    LinearLayout ll;
    int stepCount = 1;

    TextView rtext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_recipe);
        boolean vis = false;

        rtext = (TextView) findViewById(R.id.rtxt);
        edIngredient = (EditText) findViewById(R.id.edittxtIng);
        edQuantity = (EditText) findViewById(R.id.edittxtQty);
        edTitle = (EditText) findViewById(R.id.title);
        FloatingActionButton Savebutton = (FloatingActionButton) findViewById(R.id.btnSave);
        btn = (Button) findViewById(R.id.btnEnter);
        recipe_list = (ListView) findViewById(R.id.listview_steps);
        ll = (LinearLayout) findViewById(R.id.LLview);
        final ArrayAdapter<String> x = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, recipes_steps);
        recipe_list.setAdapter(x);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String ing = edIngredient.getText().toString();
                String qty = edQuantity.getText().toString();
                String step = ing + "-" + qty; //"Step" + stepCount + ": " +
                if (TextUtils.isEmpty(qty) || TextUtils.isEmpty(ing)) {
                    //EditText is empty
                    Toast.makeText(getApplicationContext(), "Enter a valid step", Toast.LENGTH_LONG).show();
                } else {
                    recipes_steps.add(step);
                    stepCount++;
                    recipe_list.setAdapter(x);

                    try {
                        recipeBook.add(new Recipe(ing, qty, new Time(0, 0), 0));
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                edIngredient.setText("");
                edQuantity.setText("");
                edIngredient.requestFocus();
            }
        });

        Savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "";
                String steps = "";

                title = edTitle.getText().toString();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(getBaseContext(), "Enter a title", Toast.LENGTH_SHORT).show();
                }
                else {
                    steps = recipes_steps.toString();
                    try {
                        dbAdapter = new DBAdapter(getApplicationContext());
                        long id = dbAdapter.insertData(title, steps,"","","");

                        if(id < 0){
                            Toast.makeText(getBaseContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getBaseContext(), "Successful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                finish();
            }
        });

    }

}
/*
        btn2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(llviewvisibility ==  View.VISIBLE){
                    llviewvisibility = View.GONE;
                    ll.setVisibility(llviewvisibility);
                    btn2.setText("Add Recipe Steps");
                    btn3.setVisibility(View.VISIBLE);
                }
                else {
                    llviewvisibility = View.VISIBLE;
                    ll.setVisibility(llviewvisibility);
                    btn2.setText("Lock Recipe");
                    btn3.setVisibility(View.INVISIBLE);
                }
            }
        });

        btn3.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
              if(strtV == View.VISIBLE){
                  strtV = View.GONE;
                  btn2.setVisibility(strtV);
                  btn3.setText("Next Step");
              }
              else {
                  strtV = View.VISIBLE;
                  btn2.setVisibility(strtV);
              }

            }
        });


    }
}
*/