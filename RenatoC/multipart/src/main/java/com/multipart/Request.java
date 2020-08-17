package com.multipart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.StringBuilder;



public class Request
{

	public HttpURLConnection httpURLConnetion;
	public byte[] streamByteArray;
	public static final String method = "POST";
	public String urlString;
	private final String boundaryMultipart = "*****12345";
	private String lineJumpToMultipart = "\r\n";
	private String twoHyphens = "--";


	//Sample request
	public String[] request = new String[]
	{
		"user", "A",
		"password", "12345"
	};

	//Sample file
	public String[] fileRequest = new String[]
	{
		"pic", "Charlize.jpg"
	};

	private static final Logger log = LogManager.getLogger( Request.class );

	public Request( String urlString )
	{
		this.urlString = urlString;
		log.debug( "Request constructor: {}", urlString );
		try
		{
			makeHttpURLConnectionReady( new URL( this.urlString ) );
			make();
		}
		catch( Exception exception )
		{
			exception.printStackTrace();
		}
	};

	public static void main( String[] args )
	{
		new Request( args[0] );
	}

	private void makeHttpURLConnectionReady( URL url ) 
		throws Exception
	{
		log.debug( String.format("(%s) Preparing connection...)", url.toString() ));
		this.httpURLConnetion = (HttpURLConnection) url.openConnection();
		httpURLConnetion.setUseCaches( false );
		httpURLConnetion.setRequestMethod( this.method );
		httpURLConnetion.setDoOutput( true );
		httpURLConnetion.setRequestProperty("Connection", "Keep-Alive");
		httpURLConnetion.setRequestProperty("Cache-Control", "no-cache");

		httpURLConnetion.setRequestProperty
		(
			"Content-Type", 
			String.format
			(
				"multipart/form-data;boundary=%s", 
				boundaryMultipart 
			)
		);

		//This is the default anyway
		httpURLConnetion.setDoInput( true );
	}

	private DataOutputStream buildHttpMultipartQuery()
		throws Exception
	{
		DataOutputStream dataOutputStream = 
			new DataOutputStream
			(
				this.httpURLConnetion.getOutputStream()
			);


		for ( int i = 0 ; i < request.length ; i += 2 )
		{
			dataOutputStream.writeBytes
			( 
				String.format("%s%s%s", twoHyphens, boundaryMultipart, lineJumpToMultipart ) 
			);

			dataOutputStream.writeBytes
			(
				String.format
				( 
					"Content-Disposition: form-data; name=\"%s\"%s%s",
					request[ i ], lineJumpToMultipart, lineJumpToMultipart
				) 
			);
			dataOutputStream.writeBytes
			(
				String.format
				( 
					"%s%s",
					request[ i + 1 ], lineJumpToMultipart
				) 
			);
		}

		for ( int i = 0 ; i < fileRequest.length ; i += 2 )
		{
			dataOutputStream.writeBytes
			( 
				String.format("%s%s%s", twoHyphens, boundaryMultipart, lineJumpToMultipart ) 
			);

			dataOutputStream.writeBytes
			(
				String.format
				( 
					"Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"; %s%s",
					fileRequest[ i ], fileRequest[ i + 1 ], lineJumpToMultipart, lineJumpToMultipart
				) 
			);
			File file = new File( fileRequest[ i + 1 ] );
			dataOutputStream.write
			(
				FileUtils.readFileToByteArray( file )
			);
			dataOutputStream.writeBytes( lineJumpToMultipart );
		}

		dataOutputStream.writeBytes( twoHyphens );
		dataOutputStream.writeBytes( boundaryMultipart );
		dataOutputStream.writeBytes( twoHyphens );
		return dataOutputStream;
	}

	public DataInputStream readResponse()
		throws Exception
	{
		DataInputStream dataInputStream = 
			new DataInputStream
			( 
				this.httpURLConnetion.getInputStream()
			);

		StringBuilder stringBuilder = new StringBuilder();

		int b;

		while( ( b = dataInputStream.read() ) != -1 )
		{
//			log.debug( String.valueOf( b ) );
			stringBuilder.append( ( char ) b );
		}

		log.debug( "StringBuilt: {}", stringBuilder );

		return dataInputStream;
	}

	//\actionCore
	public String make(  ) 
		throws Exception
	{
		//Write to outputstream
		DataOutputStream dataOutputStream = buildHttpMultipartQuery( );

		dataOutputStream.flush();
		dataOutputStream.close();
		
		log.debug( "ResponseCode: {}", String.valueOf( this.httpURLConnetion.getResponseCode() ) );

		//Read from inputstream
		DataInputStream dataInputStream = readResponse();

		dataInputStream.close();

		return null;
	}

}
