package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    public double toCelsius (int kelvin){

        double celsius = kelvin-273;

        return celsius;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    long prevtime = 0;
    int prevbutt=0;

    public void buttonClicked(View v) {
        int id = v.getId();
        //long bkkTime = 0,nonTime = 0, pathumTime = 0;
        //long nextTime = 0;

        long currenttime = System.currentTimeMillis();


        WeatherTask w = new WeatherTask();
        switch (id) {
            case R.id.btBangkok:
                //nextTime = System.currentTimeMillis();
                    if (currenttime - prevtime > 60000 || id != prevbutt) {
                        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                        prevtime = currenttime;
                        prevbutt = id;
                    }
                break;
            case R.id.btNon:
                    //nextTime = System.currentTimeMillis();
                    if (currenttime - prevtime > 60000 || id != prevbutt) {
                        w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nontaburi Weather");
                        prevtime = currenttime;
                        prevbutt = id;
                    }
                break;
            case R.id.btPathum:
                    //nextTime = System.currentTimeMillis();0
                    if (currenttime - prevtime > 60000 || id != prevbutt) {
                        w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                        prevtime = currenttime;
                        prevbutt = id;
                    }
                break;
        }
        //currentTime = nextTime;

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

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "", sky;
        ProgressDialog pDialog;
        String title;

        double windSpeed;
        int humidity , currentTemp, maxTemp, minTemp;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    JSONObject jMain = jWeather.getJSONObject("main");
                    JSONArray jsonArray = jWeather.getJSONArray("weather");
                    JSONObject jSky = jsonArray.getJSONObject(0);


                    sky = jSky.getString("main");
                    windSpeed = jWind.getDouble("speed");
                    currentTemp = jMain.getInt("temp");
                    maxTemp = jMain.getInt("temp_max");
                    minTemp = jMain.getInt("temp_min");
                    humidity = jMain.getInt("humidity");
                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind, tvHumid, tvTemp;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvHumid = (TextView)findViewById(R.id.tvHumid);

            if (result) {
                tvTitle.setText(title);
                tvWeather.setText(sky);
                tvTemp.setText(toCelsius(currentTemp) + " (max = " + toCelsius(maxTemp) + ", min = " + toCelsius(minTemp) + ")");
                tvWind.setText(String.format("%.1f", windSpeed));
                tvHumid.setText(humidity + "%");
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
            }
        }
    }
}
