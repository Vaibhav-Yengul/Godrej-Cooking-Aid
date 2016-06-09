package godrejapp.vaibhav.com.godrejcookingaid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayRecipe extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String SERIAL_PORT_SERVICE = "00001101-0000-1000-8000-00805f9b34fb";
    int recipe_id;
    Button btnStar,btnConnect;
    TextView textView;
    Spinner devicesSpinner;
    float lastTemperature;
    ArrayList<String> temperatures;
    ArrayList<String> times;
    DBAdapter dbadapter;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    TextView ardStatus;
    String arduinoData;
    int readBufferPosition;
    ArrayList<String> rec_steps;
    int counter;
    volatile boolean stopWorker;
    ListView lv;
    LineChart lc;
    String temp,time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If we have no Bluetooth adapter we are done.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Toast.makeText(this, R.string.bluetooth_not_available, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.play_recipe);

        String step = getIntent().getStringExtra("Steps");
        step = step.replace("[", "").replace("]", "");
        recipe_id = getIntent().getIntExtra("UID", 0);
        ardStatus = (TextView) findViewById(R.id.arduinoStatus);
        devicesSpinner = (Spinner) findViewById(R.id.bluetooth_devices2);
        textView = (TextView) findViewById(R.id.textViewlist2);
        btnConnect = (Button)findViewById(R.id.connect2);
        btnStar = (Button) findViewById(R.id.btnstar2);
        // step number, ingredient,quantity,time,temperature
        rec_steps = new ArrayList<String>(Arrays.asList(step.split(",")));
        trimArray(rec_steps);
        Toast.makeText(this, rec_steps.toString(),Toast.LENGTH_SHORT).show();

        dbadapter = new DBAdapter(this);
        temp = dbadapter.getSplTemperatures(recipe_id);
        time = dbadapter.getSplTime(recipe_id);
        temp = temp.replace("[","").replace("]", "");
        time = time.replace("[","").replace("]","");

        temperatures = new ArrayList<String>(Arrays.asList(temp.split(",")));
        times = new ArrayList<String>(Arrays.asList(time.split(",")));
        trimArray(temperatures);
        trimArray(times);

        textView.setText("");
        for(int i=0;i<rec_steps.size(); i++){
            // Add + quantity + of + ingridient + and maintain + temp + for time + time
            String stepX = rec_steps.get(i);
            int dash = stepX.indexOf("-");
            String ingredient = stepX.substring(2, dash);
            String qty = stepX.substring(dash + 1, stepX.length());
            String makeStep = "Add " + qty + " of " + ingredient + " and maintain " + temperatures.get(i)
                    +" C tempearture for time " + times.get(i) + "s";
            textView.setText(textView.getText() + "Step " + (i+1) + " " + makeStep+ "\n\n");
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Button btnConnect = (Button) findViewById(R.id.connect2);
                if (btnConnect.getText() == getResources().getString(R.string.connect)) {
                    String[] deviceSplit = devicesSpinner.getSelectedItem().toString().split("\n");
                    mmDevice = mBluetoothAdapter.getRemoteDevice(deviceSplit[1].toString());
                    openBT();
                } else {
                    if (mmSocket.isConnected()) {
                        btnConnect.setText(R.string.connect);
                        try {
                            closeBT();
                        } catch (IOException closeException) {
                        }

                    }
                }
            }
        });

        btnStar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String selectedFromList = "p;" + rec_steps.size() + ";" + rec_steps.toString() + ";" + temperatures.toString() +
                        ";" + times.toString() +"*";
                Toast t = Toast.makeText(getApplicationContext(), selectedFromList, Toast.LENGTH_LONG);
                t.show();
                try {
                    mmOutputStream.write(selectedFromList.getBytes());
                } catch (IOException ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                } catch (NullPointerException ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                }


            }
        });

        lc = (LineChart) findViewById(R.id.linechartTempvsTime2);
        initializeGraph();
    }

    private void trimArray(ArrayList<String> listArray) {
        for(int i =1 ; i < listArray.size() ;i++){
            int len = listArray.get(i).length();
            listArray.set(i, listArray.get(i).substring(1,len));
        }
    }

    private void initializeGraph() {
        //customize line chart
        lc.setDescription("");

        //enable vale highlighting
        lc.setHighlightFullBarEnabled(true);

        //enabel touch gestures
        lc.setTouchEnabled(true);

        lc.setDragEnabled(true);
        lc.setScaleEnabled(true);
        lc.setDrawGridBackground(false);

        //enable pinch zoom to avoid scaling x and y values seperately
        lc.setPinchZoom(true);
        LineData data = new LineData();
        lc.setData(data);

        Legend l = lc.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.GREEN);

        XAxis xa = lc.getXAxis();
        YAxis ya = lc.getAxisLeft();
        ya.setAxisMaxValue(50f);
        ya.setDrawGridLines(true);

        YAxis ya2 = lc.getAxisRight();
        ya2.setEnabled(false);

        lc.setVisibleXRange(0, 6);
        lc.moveViewToX(data.getXValCount() - 7);
    }

    protected void onStart() {
        super.onStart();
        // If Bluetooth is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            initialize();
        }
    }

    /**
     * Ensures user has turned on Bluetooth on the Android device.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    initialize();
                } else {
                    finish();
                    return;
                }
        }
    }


    void initialize(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            List<String> s = new ArrayList<String>();
            for(BluetoothDevice device : pairedDevices){
                s.add(device.getName() + "\n" + device.getAddress());
            }
            ArrayAdapter<String> devicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, s);
            devicesSpinner.setAdapter(devicesAdapter);
        }
    }

    void openBT(){
        Button btnConnect = (Button)findViewById(R.id.connect2);

        try{
            UUID uuid = UUID.fromString(SERIAL_PORT_SERVICE); //Standard SerialPortService ID
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            btnConnect.setText(R.string.disconnect);
        }
        catch(IOException ex)
        {
            Toast.makeText(this, R.string.bluetooth_connection_failed, Toast.LENGTH_LONG).show();
            btnConnect.setText(R.string.connect);
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        beginListenForData();
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    void beginListenForData()
    {

        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            arduinoData = data;
                                            if(arduinoData.substring(0,1).equals("#")) {
                                                arduinoData = arduinoData.substring(1,arduinoData.length());
                                                addEntry(arduinoData);
                                                arduinoData = "";
                                            }
                                            else {
                                                arduinoData = arduinoData.substring(1,arduinoData.length());
                                                ardStatus.setText(arduinoData);
                                                arduinoData = "";
                                            }
                                        }
                                    });

                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }



    private void addEntry(String mydata) {
        int commalocation = mydata.indexOf(",");
        String tempData = mydata.substring(0, commalocation);
        String time = mydata.substring(commalocation+1,mydata.length());

        LineData data = lc.getData();
        if(data != null){
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            if(set == null){
                set = createSet();
                data.addDataSet(set);
            }
            data.addXValue(time);
            data.addEntry(new Entry(Float.parseFloat(tempData), set.getEntryCount()), 0);

            lc.notifyDataSetChanged();
            lc.setVisibleXRange(0, 6);
            lc.moveViewToX(data.getXValCount() - 7);

        }
    }


    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Real Time Temperature Value");
        set.setLineWidth(2.5f);
        set.setCubicIntensity(0.5f);
        set.setCircleRadius(4.5f);
        set.setColor(Color.rgb(240, 99, 99));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);

        return set;
    }
}
