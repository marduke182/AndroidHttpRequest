package com.example.requesterexample;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.example.connection.HttpClientHandler;
import com.example.model.StatusJson;
import com.google.gson.Gson;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	AsyncTask<Long, Void, Long> myAsyncTask;
	private HttpClientHandler httpClientHandlerApp;
	private ProgressDialog pd;
	private Button send;
	private String errorConexion;

	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send = (Button) findViewById(R.id.button1);
    	httpClientHandlerApp = (HttpClientHandler) getApplication();
        
        
        send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				myAsyncTask = new MyAsyncTask().execute();
			}
		});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void ensenarPd() {
		pd = new ProgressDialog(this);
		pd.setCancelable(true);
		pd.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (myAsyncTask.cancel(true)) {
					Toast.makeText(
							getApplicationContext(),
							getApplication().getString(R.string.progress_dialog_enviando_informacion_cancel),
							Toast.LENGTH_SHORT).show();
				} else {
					if (myAsyncTask != null) {
						if (!(myAsyncTask.getStatus()
								.equals(AsyncTask.Status.FINISHED))) {
							ensenarPd();
						}
					}
				}
			}
		});
		pd.setIndeterminate(true);
		// pd.setIndeterminateDrawable(getResources().getDrawable(
		// R.anim.progress_dialog_icon_drawable_animation));
		pd.setMessage(getApplication()
				.getString(R.string.progress_dialog_enviando_informacion));
		pd.show();
	}
    
    private class MyAsyncTask extends AsyncTask<Long, Void, Long> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ensenarPd();
		}

		
		@Override
		protected Long doInBackground(Long... arg0) {
			Gson gson = new Gson();

//			String jsonInma = gson.toJson(a.getInmaest());// gson.toJson(sesion.getAjustesActual().getInmaest());
//			String user = sesion.getUsuario().getUsername();

			InputStream is = httpClientHandlerApp.pruebaRequest("2");
			if (is != null) {
				Reader reader = new InputStreamReader(is);
				StatusJson ca = gson.fromJson(reader,
						StatusJson.class);
				if (ca.getCode().equals("OK")) {
					return 1l;
				} else {
					
					return -1l;
				}
			}
			errorConexion = httpClientHandlerApp.getErrorType();
			return -1l;
		}

		@Override
		protected void onPostExecute(Long id) {
			super.onPostExecute(id);

			pd.dismiss();


		}

	}
    
}
