package com.witheyjr.TheTweetsOf;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import com.witheyjr.TheTweetsOf.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	final String URL = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=tradewavenet&include_rts=1";
	final String picURL = "https://api.twitter.com/1.1/users/show.json?screen_name=tradewavenet";
	final String APIKEY = "INSERT YOUR API KEY HERE";
	final String APISECRET = "INSERT YOUR API SECRET HERE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button buttonBearerToken = (Button)findViewById(R.id.btn_bearer_token);
		buttonBearerToken.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new GetBearerTokenTask().execute();
			}
		});
		
		Button buttonGetFeed = (Button)findViewById(R.id.btn_get_feed);
		buttonGetFeed.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView textBearerToken = (TextView)findViewById(R.id.txt_bearer_token);
				String bearerToken = textBearerToken.getText().toString();
				new GetFeedTask().execute(bearerToken, URL);
				new GetPicTask().execute(bearerToken, picURL);
			}
		});
	}

	protected class GetBearerTokenTask extends AsyncTask<Void, Void, String> {
        @Override
		protected String doInBackground(Void... params) {
			try {
				DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
				HttpPost httpPost = new HttpPost("https://api.twitter.com/oauth2/token");
				
				String apiString = APIKEY + ":" + APISECRET;
				String authorization = "Basic " + Base64.encodeToString(apiString.getBytes(), Base64.NO_WRAP);
		
				httpPost.setHeader("Authorization", authorization);
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
		
				InputStream inputStream = null;
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
		
				inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();
		
				String line = null;
				while ((line = reader.readLine()) != null) {
				    sb.append(line + "\n");
				}
				return sb.toString();
			} catch (Exception e) {
				Log.e("GetBearerTokenTask", "Error:" + e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(String jsonText) {
			try {
				JSONObject root = new JSONObject(jsonText);
				String bearerToken = root.getString("access_token");			
				TextView txt = (TextView)findViewById(R.id.txt_bearer_token);
				txt.setText(bearerToken);
			} catch (Exception e) {
				Log.e("GetBearerTokenTask", "Error:" + e.getMessage());
			}
		}
    }
	
	protected class GetFeedTask extends AsyncTask<String, Void, String> {
	    @Override
		protected String doInBackground(String... params) {
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
				HttpGet httpget = new HttpGet(params[1]);
				httpget.setHeader("Authorization", "Bearer " + params[0]);
				httpget.setHeader("Content-type", "application/json");

				InputStream inputStream = null;
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				
				StringBuilder sb2 = new StringBuilder();
				JSONArray jsonArray = new JSONArray(sb.toString());
				
				Log.i("bleugh", "Number of entries " + jsonArray.length());
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject2 = jsonArray.getJSONObject(i);
					sb2.append(jsonObject2.getString("text") + "\n\n");
				}
				
				return sb2.toString();
			} catch (Exception e) {
				Log.e("GetFeedTask", "Error:" + e.getMessage());
				return null;
			}
		}
		
		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(String jsonText){
			try {
				TextView txt = (TextView)findViewById(R.id.txt_feed);
				txt.setText(jsonText);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					txt.setTextIsSelectable(true);
				}
			} catch (Exception e) {
				Log.e("GetFeedTask", "Error:" + e.getMessage());
			}
		}
	}
	
	protected class GetPicTask extends AsyncTask<String, Void, Bitmap> {
	    @Override
		protected Bitmap doInBackground(String... params) {
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
				HttpGet httpget = new HttpGet(params[1]);
				httpget.setHeader("Authorization", "Bearer " + params[0]);
				httpget.setHeader("Content-type", "application/json");

				InputStream inputStream = null;
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				
				JSONObject jsonObject = new JSONObject(sb.toString());
				// String picLink = jsonObject.getJSONObject("profile_image_url").toString();
				String picLink = jsonObject.getString("profile_image_url");
				picLink = picLink.replace("_normal", "");
				Log.d("bleugh", picLink);
				Bitmap bitmap = null;
				try {
					bitmap = BitmapFactory.decodeStream((InputStream)new URL(picLink).getContent());
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
				return bitmap;
			} catch (Exception e) {
				Log.e("GetPicTask", "Error:" + e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Bitmap image){
			try {
				ImageView img = (ImageView)findViewById(R.id.pic);
				if (image != null)
					img.setImageBitmap(image);
			} catch (Exception e) {
				Log.e("GetPicTask", "Error:" + e.getMessage());
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;	
	}
}









