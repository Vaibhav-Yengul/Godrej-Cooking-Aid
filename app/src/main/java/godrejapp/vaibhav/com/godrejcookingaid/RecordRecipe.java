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

public class RecordRecipe extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String SERIAL_PORT_SERVICE = "00001101-0000-1000-8000-00805f9b34fb";
    int recipe_id;
    Button btnStar,btnConnect;
    TextView textView;
    Spinner devicesSpinner;
    ArrayList<Float> listOftemperatures;
    ArrayList<Float> listOfSpltemperatures;
    ArrayList<Integer> listOfSplTime;
    float lastTemperature;
    int lastTime;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    String arduinoData;
    int readBufferPosition;
    ArrayList<String> rec_steps;
    int counter;
    volatile boolean stopWorker;
    ListView lv;
    LineChart lc;
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
        setContentView(R.layout.record_recipe);

        String step = getIntent().getStringExtra("Steps");
        step = step.replace("[", "").replace("]", "");
        recipe_id = getIntent().getIntExtra("UID", 0);

        listOftemperatures = new ArrayList<Float>();
        listOfSpltemperatures = new ArrayList<Float>();
        listOfSplTime = new ArrayList<Integer>();
        devicesSpinner = (Spinner) findViewById(R.id.bluetooth_devices);
        textView = (TextView) findViewById(R.id.textViewlist);
        btnConnect = (Button)findViewById(R.id.connect);
        btnStar = (Button) findViewById(R.id.btnstar);
        // step number, ingredient,quantity,time,temperature
        rec_steps = new ArrayList<String>(Arrays.asList(step.split(",")));
        Toast.makeText(this, rec_steps.toString(),Toast.LENGTH_SHORT).show();


        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Button btnConnect = (Button) findViewById(R.id.connect);
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

        btnStar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                String selectedFromList = "r;" + rec_steps.size() +";" + rec_steps.toString() + "*";
                Toast t = Toast.makeText(getApplicationContext(), selectedFromList, Toast.LENGTH_SHORT);
                t.show();
                try {
                    mmOutputStream.write(selectedFromList.getBytes());
                } catch (IOException ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                } catch (NullPointerException ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                }
                textView.setText("Step 1 started..");

            }
        });


        lc = (LineChart) findViewById(R.id.linechartTempvsTime);
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
        Button btnConnect = (Button)findViewById(R.id.connect);

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
                                                addtoList(arduinoData);
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

    private void addtoList(String mydata) {

        String stepdata = textView.getText() + " Step Temperature = " + lastTemperature + "\n" + mydata;
        textView.setText(stepdata);

        listOftemperatures.add(lastTemperature);
        listOfSpltemperatures.add(lastTemperature);
        listOfSplTime.add(lastTime);

        if("recipe finished".equals(mydata.toLowerCase())){
            DBAdapter dbAdapter;
            try {
                dbAdapter = new DBAdapter(getApplicationContext());
                long id = dbAdapter.updateTempData(listOftemperatures.toString(), recipe_id);

                if(id < 0){
                    Toast.makeText(getBaseContext(), "Unsuccessful temp transfer", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getBaseContext(), "Successful temp transfer", Toast.LENGTH_SHORT).show();
                }

                long id2 = dbAdapter.updateSplTemperatures(listOfSpltemperatures.toString(), recipe_id);
                if(id2 < 0){
                    Toast.makeText(getBaseContext(), "Unsuccessful spl  temp transfer", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getBaseContext(), "Successful spl temp transfer", Toast.LENGTH_SHORT).show();
                }

                long id3 = dbAdapter.updateSplTime(listOfSplTime.toString(), recipe_id);
                if(id3 < 0){
                    Toast.makeText(getBaseContext(), "Unsuccessful spl  time transfer", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getBaseContext(), "Successful spl time transfer", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addEntry(String mydata) {
        int commalocation = mydata.indexOf(",");
        String tempData = mydata.substring(0, commalocation);
        String time = mydata.substring(commalocation+1,mydata.length());

        LineData data = lc.getData();
        lastTemperature = Float.parseFloat(tempData);
        lastTime = Integer.parseInt(time);
        try {
            listOftemperatures.add(lastTemperature);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        if(data != null){
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            if(set == null){
                set = createSet();
                data.addDataSet(set);
            }
            data.addXValue(time+"s");
            data.addEntry(new Entry(Float.parseFloat(tempData), set.getEntryCount()), 0);

            lc.notifyDataSetChanged();
            lc.setVisibleXRange(0,6);
            lc.moveViewToX(data.getXValCount() - 7);
        }
    }


    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Real Time Temperature Value");
        set.setLineWidth(2.5f);
        set.setCubicIntensity(0.5f);
        set.setCircleRadius(4.5f);
        set.setDrawFilled(true);
        set.setColor(Color.rgb(240, 99, 99));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);

        return set;
    }

}
