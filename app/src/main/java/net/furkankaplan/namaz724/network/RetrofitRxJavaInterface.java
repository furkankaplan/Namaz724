package net.furkankaplan.namaz724.network;

import net.furkankaplan.namaz724.network.model.City;
import net.furkankaplan.namaz724.network.model.SubAdminArea;
import net.furkankaplan.namaz724.network.model.Time;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitRxJavaInterface {

    @GET("/sehirler")
    Observable<List<City>> getCities(@Query("ulke") String countryID);

    @GET("/ilceler")
    Observable<List<SubAdminArea>> getSubAdminAreas(@Query("sehir") String cityID);

    @GET("/vakitler")
    Observable<List<Time>> getTimes(@Query("ilce") String subAdminAreaID);

}
