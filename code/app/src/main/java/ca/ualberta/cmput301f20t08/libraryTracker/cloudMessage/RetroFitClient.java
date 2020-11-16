package ca.ualberta.cmput301f20t08.libraryTracker.cloudMessage;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Configure retrofit client
 */
public class RetroFitClient {

    private static Retrofit retrofit =null;
    public static Retrofit getClient(String baseURL){
        if (retrofit==null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
