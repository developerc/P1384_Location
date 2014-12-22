package ru.example.p1384_location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    EditText etResponse;
    EditText etStatus;
    TextView tvIsConnected;
    Button btnPost;
    String resultPOST;
    String matchtemper = "";
    String authtok = ""; 
    String Lat;
    String Lon;
    private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        // get reference to the views
        etResponse = (EditText) findViewById(R.id.etResponse);
        etStatus = (EditText) findViewById(R.id.etStatus);
        
        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
        btnPost = (Button) findViewById(R.id.btnPost);
 
        // check if you are connected or not
        if(isConnected()){
            tvIsConnected.setBackgroundColor(0xFF00CC00);
            tvIsConnected.setText("You are connected");
        }
        else{
            tvIsConnected.setText("You are NOT conncted");
        }
 
        // show response on the EditText etResponse 
       // etResponse.setText("http://hmkcode.com/examples/index.php");
       // new HttpAsyncTask().execute("http://192.168.28.19:3000");
        new HttpAsyncTask().execute("http://pchelka.teleknot.ru");
     // add click listener to Button "POST"
        btnPost.setOnClickListener(this);	
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	}
	
	   public static String GET(String url){
	        InputStream inputStream = null;
	        String result = "";
	        try {
	 
	            // create HttpClient
	            HttpClient httpclient = new DefaultHttpClient();
	 
	            // make GET request to the given URL
	            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
	 
	            // receive response as inputStream
	            inputStream = httpResponse.getEntity().getContent();
	 
	            // convert inputstream to string
	            if(inputStream != null)
	                result = convertInputStreamToString(inputStream);
	            else
	                result = "Did not work!";
	 
	        } catch (Exception e) {
	            Log.d("InputStream", e.getLocalizedMessage());
	        }
	 
	        return result;
	    }
	 
	    // convert inputstream to String
	    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
	        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
	        String line = "";
	        String result = "";
	        while((line = bufferedReader.readLine()) != null)
	            result += line;
	 
	        inputStream.close();
	        return result;
	 
	    }
	 
	    // check network connection
	    public boolean isConnected(){
	        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	            if (networkInfo != null && networkInfo.isConnected()) 
	                return true;
	            else
	                return false;   
	    }
	    
	    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
	        @Override
	        protected String doInBackground(String... urls) {
	 
	            return GET(urls[0]);
	        }
	        // onPostExecute displays the results of the AsyncTask.
	        @Override
	        protected void onPostExecute(String result) {
	            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
	         // работаем с регулярками первый проход
	           Pattern pattern = Pattern.compile("(meta content)(.*?)(csrf-token\")");
	           Matcher matcher = pattern.matcher(result);
	          matcher.find();
	        	   matchtemper = matcher.group();       
	        	  // etResponse.setText(matchtemper);     	   
	                     
	        // второй проход
	            pattern = Pattern.compile("(\")(.*?)(\")");
	            matcher = pattern.matcher(matchtemper);
	            matcher.find();
	            matcher.find();
	            matcher.find();
	            authtok = matcher.group(2);
	            etResponse.setText(authtok);
	   
	       }
	    } 
	    
	    @Override
	    public void onClick(View view) {
	 
	        switch(view.getId()){
	            case R.id.btnPost:
	              //  if(!validate())
	                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
	                // call AsynTask to perform network operation on separate thread
	                Lat="45.654";
	  	  	        Lon="52.987";
	                new HttpAsyncTaskPost().execute("http://pchelka.teleknot.ru/api/user1/x11unkde/setcoord");  
	            break;
	        }
	    }	
	    
	    private class HttpAsyncTaskPost extends AsyncTask<String, Void, String> {
	        @Override
	        protected String doInBackground(String... urls) {
	 
	           HttpClient client = 
	  	              new DefaultHttpClient();
	  	          HttpPost post = 
	  	             new HttpPost(urls[0]);

	  	          List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();

	  	        pairs.add(new BasicNameValuePair("lon", Lon));
	  	        pairs.add(new BasicNameValuePair("lat", Lat));
	  	        
	  	          try {
					post.setEntity(new UrlEncodedFormEntity(pairs));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		          try {
					HttpResponse response = client.execute(post);
					 BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"windows-1251"));
			          StringBuilder sb = new StringBuilder();
			          String line = null;
			          while ((line = reader.readLine()) != null) {
			          sb.append(line + System.getProperty("line.separator"));
			          } 
			          
			                 resultPOST = sb.toString(); //получили ответ
			               //  etResponse.setText(resultPOST);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		          
	        	return null;
	        }
	        // onPostExecute displays the results of the AsyncTask.
	        @Override
	        protected void onPostExecute(String result) {
	            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
	       }
	    }   	    
//-------------------- Otobrazchaem koordinaty -----------------------------------------------
	    @Override
	    protected void onResume() {
	      super.onResume();
	      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, locationListener);
	      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,locationListener);
	      checkEnabled();
	    }
	    
	    @Override
	    protected void onPause() {
	      super.onPause();
	      locationManager.removeUpdates(locationListener);
	    }
	    
	    private LocationListener locationListener = new LocationListener() {

	        @Override
	        public void onLocationChanged(Location location) {
	          showLocation(location);
	        }

	        @Override
	        public void onProviderDisabled(String provider) {
	          checkEnabled();
	        }

	        @Override
	        public void onProviderEnabled(String provider) {
	          checkEnabled();
	          showLocation(locationManager.getLastKnownLocation(provider));
	        }

	        @Override
	        public void onStatusChanged(String provider, int status, Bundle extras) {
	          if (provider.equals(LocationManager.GPS_PROVIDER)) {
	            etStatus.setText("Status GPS: " + String.valueOf(status));
	          } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
	            etStatus.setText("Status NET: " + String.valueOf(status));
	          }
	        }
	      };
	    
	      private void showLocation(Location location) {
	    	    if (location == null)
	    	      return;
	    	    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
	    	      etResponse.setText(formatLocation(location));
	    	      Lat=formatLat(location);
	    	      Lon=formatLon(location);
	    	      new HttpAsyncTaskPost().execute("http://pchelka.teleknot.ru/api/user1/x11unkde/setcoord"); 
	    	    } else if (location.getProvider().equals(
	    	        LocationManager.NETWORK_PROVIDER)) {
	    	      etResponse.setText(formatLocation(location));
	    	      Lat=formatLat(location);
	    	      Lon=formatLon(location);
	    	      new HttpAsyncTaskPost().execute("http://pchelka.teleknot.ru/api/user1/x11unkde/setcoord"); 
	    	    }
	    	  }
	      
	      private String formatLat(Location location) {
	    	  if (location == null)
	    	      return "";
	    	  return String.format("%1$.10f",location.getLatitude());
	      }
	      
	      private String formatLon(Location location) {
	    	  if (location == null)
	    	      return "";
	    	  return String.format("%1$.10f",location.getLongitude());
	      }
	    
	      private String formatLocation(Location location) {
	    	    if (location == null)
	    	      return "";
	    	    return String.format(
	    	        "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
	    	        location.getLatitude(), location.getLongitude(), new Date(
	    	            location.getTime()));
	    	  }
	      
	      private void checkEnabled() {
	    	    etStatus.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
	    	//    tvEnabledNet.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
	    	  }	      
	    
	    
//---------------------------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
