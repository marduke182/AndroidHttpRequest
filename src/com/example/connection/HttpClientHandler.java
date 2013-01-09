package com.example.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.requesterexample.R;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HttpClientHandler extends Application {

	private String baseURL;
	public DefaultHttpClient httpClient;
	String errorType;
	int responseCode = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		httpClient = createHttpClient();
		baseURL = this.getString(R.string.urlBase);
	}

	public String getBaseURL() {
		return baseURL;
	}



	public DefaultHttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 150000);
		HttpConnectionParams.setSoTimeout(params, 150000);
		
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
		HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		//HttpProtocolParams.setUserAgent(params,"Android "+Build.VERSION.RELEASE+" - "+Build.MODEL);
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
		.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
		SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
		params, schReg);
		return new DefaultHttpClient(conMgr, params);
		}

	public InputStream pruebaRequest(String id) {
		InputStream resEntityPostStream = null;
		String URL = baseURL + this.getString(R.string.sendPrueba);
		try {
			JSONObject json = new JSONObject();
			json.put("idConfiguracion", id);
			StringEntity se = new StringEntity(json.toString());
			Log.i("EL JSON", json.toString());
			se.setContentType("application/json");
			resEntityPostStream = obtenerDatosServidor(new URI(URL), se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return resEntityPostStream;
	}
	
	public InputStream sincronizarUsuarios(String id) {
		InputStream resEntityPostStream = null;
		String URL = baseURL + this.getString(R.string.sincUsuarios) + "/" + id;
		Log.i("URL", URL);
		try {
			resEntityPostStream = obtenerDatosServidorGet(new URI(URL));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return resEntityPostStream;
	}

	public DefaultHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * Se conecta al servicio web para obtener la configuración que corresponda
	 * al id proporcionado
	 * 
	 * @param id
	 *            Identificador de la configuración que se desea para el
	 *            dispositivo
	 * @return InputStream que contine el json con todas las propiedades de la
	 *         configuración que se desea
	 */
	public InputStream buscarConfiguracion(String id) {
		InputStream resEntityPostStream = null;
		String URL = baseURL + this.getString(R.string.getConfiguration);
		try {
			JSONObject json = new JSONObject();
			json.put("idConfiguracion", id);
			StringEntity se = new StringEntity(json.toString());
			Log.i("EL JSON", json.toString());
			se.setContentType("application/json");
			resEntityPostStream = obtenerDatosServidor(new URI(URL), se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return resEntityPostStream;
	}

	public InputStream login(String usuario, String password) {
		InputStream resEntityPostStream = null;
		String URL = baseURL + this.getString(R.string.setLogin);
		try {
			JSONObject json = new JSONObject();
			json.put("tipo_id", "S");
			json.put("id", usuario);
			json.put("pass", password);
			json.put("version", "1");
			json.put("plataforma", "bb");
			json.put("login", "true");
			json.put("tipo_us", "P");
			StringEntity se = new StringEntity(json.toString());
			Log.i("EL JSON", json.toString());
			se.setContentType("application/json");
			resEntityPostStream = obtenerDatosServidor(new URI(URL), se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return resEntityPostStream;
	}

	
	public boolean conexionDisponible() {
		boolean disponible = false;
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		if (info != null) {
			return (info.isConnected());
		}
		return disponible;
	}

	public InputStream obtenerDatosServidor(URI URL, HttpEntity postEntity) {
		DefaultHttpClient client = getHttpClient();
		HttpPost request = new HttpPost();
		InputStream resEntityPostStream = null;
		if (!conexionDisponible()) {
			errorType = this.getString(R.string.networkErrorNoNetowork);
		} else {
			try {
				request.setURI(URL);
				if (postEntity != null)
					request.setEntity(postEntity);
				HttpResponse response = client.execute(request);
				responseCode = response.getStatusLine().getStatusCode();
				if (responseCode == 200) {
					HttpEntity resEntityPost = response.getEntity();
					resEntityPostStream = resEntityPost.getContent();
					errorType = "";
				} else if (responseCode == 401) {
					System.out.println("401");
					errorType = this
							.getString(R.string.networkErrorUnauthorized);
				} else if ((responseCode == 302) || (responseCode == 304)) {
					String url_nueva = response.getHeaders("Location")[0]
							.getValue();
					URI uri = null;
					try {
						uri = new URI(url_nueva);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					return obtenerDatosServidor(uri, postEntity);
				} else if (responseCode == 408) {
					errorType = this.getString(R.string.networkErrorTimeout);
				} else if (responseCode == 500) {
					errorType = this
							.getString(R.string.networkErrorInternalServerError);
				} else {
					errorType = "";
				}
			} catch (IllegalStateException e) {
				Log.i("Error", e.toString());
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				Log.i("Error", e.toString());
				errorType = this.getString(R.string.networkErrorTimeout);
				return null;
			}

		}
		return resEntityPostStream;
	}

	public InputStream obtenerDatosServidorGet(URI URL) {
		DefaultHttpClient client = getHttpClient();
		HttpGet request = new HttpGet();
		InputStream resEntityPostStream = null;
		if (!conexionDisponible()) {
			errorType = this.getString(R.string.networkErrorNoNetowork);
		} else {
			try {
				request.setURI(URL);
				HttpResponse response = client.execute(request);
				responseCode = response.getStatusLine().getStatusCode();
				if (responseCode == 200) {
					Log.i("RESPONSE CODE", "200");
					HttpEntity resEntityPost = response.getEntity();
					resEntityPostStream = resEntityPost.getContent();
					errorType = "";
				} else if (responseCode == 401) {
					System.out.println("401");
					errorType = this
							.getString(R.string.networkErrorUnauthorized);
				} else if ((responseCode == 302) || (responseCode == 304)) {
					String url_nueva = response.getHeaders("Location")[0]
							.getValue();
					URI uri = null;
					try {
						uri = new URI(url_nueva);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					return obtenerDatosServidorGet(uri);
				} else if (responseCode == 408) {
					errorType = this.getString(R.string.networkErrorTimeout);
				} else if (responseCode == 500) {
					errorType = this
							.getString(R.string.networkErrorInternalServerError);
				} else {
					errorType = "";
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				errorType = this.getString(R.string.networkErrorTimeout);
				return null;
			}

		}
		return resEntityPostStream;
	}

	public InputStream obtenerDatosServidorPut(URI URL, HttpEntity postEntity) {
		DefaultHttpClient client = getHttpClient();
		HttpPut request = new HttpPut();
		InputStream resEntityPostStream = null;
		if (!conexionDisponible()) {
			errorType = this.getString(R.string.networkErrorNoNetowork);
		} else {
			try {
				request.setURI(URL);
				if (postEntity != null)
					request.setEntity(postEntity);
				HttpResponse response = client.execute(request);
				responseCode = response.getStatusLine().getStatusCode();
				if (responseCode == 200) {
					HttpEntity resEntityPost = response.getEntity();
					resEntityPostStream = resEntityPost.getContent();
					errorType = "";
				} else if (responseCode == 401) {
					System.out.println("401");
					errorType = this
							.getString(R.string.networkErrorUnauthorized);
				} else if ((responseCode == 302) || (responseCode == 304)) {
					String url_nueva = response.getHeaders("Location")[0]
							.getValue();
					URI uri = null;
					try {
						uri = new URI(url_nueva);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					return obtenerDatosServidor(uri, postEntity);
				} else if (responseCode == 408) {
					errorType = this.getString(R.string.networkErrorTimeout);
				} else if (responseCode == 500) {
					errorType = this
							.getString(R.string.networkErrorInternalServerError);
				} else {
					errorType = "";
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				errorType = this.getString(R.string.networkErrorTimeout);
				return null;
			}

		}
		return resEntityPostStream;
	}

	/**
	 * @return the errorType
	 */
	public String getErrorType() {
		return errorType;
	}

	/**
	 * @param errorType
	 *            the errorType to set
	 */
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

}
