package com.tc.nutriyou.activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tc.nutriyou.R;
import com.tc.nutriyou.config.ConfigFirebase;
import com.tc.nutriyou.model.Cliente;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SearchAdress extends AppCompatActivity {

    private static final String TAG = "SearchAdress";
    String API_KEY = "AIzaSyD6CbCT6KwGwZPPiL-TJJ9e9UFCNCih8Hw";
    PlacesClient placesClient;
    Cliente cliente;
    private DatabaseReference firebase_user;
    TextView end_atual;

    public void SearchAdress() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_adress);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            startActivity(new Intent(SearchAdress.this, Login.class));

        end_atual = findViewById(R.id.end_text_rua_atual);
        placesClient = Places.createClient(this);
        firebase_user = ConfigFirebase.getFirebase().child("cliente").child(user.getUid());

        firebase_user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {//TODO
                }
                cliente = dataSnapshot.getValue(Cliente.class);
                if (cliente.getEndereco() != null) {
                    end_atual.setText(cliente.getEndereco());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

		/* 
		 * Olá Julia,
		 * eu mexi, pesquisei, porém nao testei. 
		 * esse link ajudou, https://stackoverflow.com/questions/33340286/restrict-autocomplete-search-to-a-particular-country-in-google-places-android-ap
		 * esse tbm, https://developers.google.com/places/android-sdk/reference/com/google/android/libraries/places/widget/AutocompleteSupportFragment.html#setLocationBias(com.google.android.libraries.places.api.model.LocationBias)
		 * esse tbm https://developers.google.com/places/android-sdk/autocomplete
		 *
		 */

        AutocompleteSupportFragment autocompleteFragment = 
			(AutocompleteSupportFragment) 
				getSupportFragmentManager()
				.findFragmentById
				(
					R.id.autocomplete_fragment
				);

		autocompleteFragment.setLocationRestriction
		(
			RectangularBounds.newInstance
			( 
				//Aqui o northeast e southwest que descrevem o retangulo de SaoPaulo
				new LatLng( -23.503214, -46.488869),
				new LatLng( -23.634843, -46.731102)
			)
		);

        autocompleteFragment.setCountry("BR");
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener
		(
			new PlaceSelectionListener() 
			{
	            @Override
	            public void onPlaceSelected(Place place) 
				{
	                cliente = new Cliente();
	
	                cliente.setId(user.getUid());
	                cliente.setEndereco(place.getName());
	                cliente.salvarEndereco();
	                // TODO: Get info about the selected place.
	                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
	                Toast.makeText(SearchAdress.this, "Place: " + place.getName() + ", " + place.getId(), Toast.LENGTH_LONG);
		        }
		
	            @Override
	            public void onError(Status status) 
				{
	                Log.i(TAG, "An error occurred: " + status);
	            }
			}
		);
    }

    private String getCityNameByCoordinates(double lat, double lon) throws IOException {
        Geocoder mGeocoder = new Geocoder(SearchAdress.this, Locale.getDefault());

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }
}
