package com.example.dongseonambook;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiServices {

    public class FeelModel {
        private String text;

        public FeelModel(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public class KakaoBookResponse {
        public List<Document> documents;

        public static class Document {
            public String title;
            public List<String> authors;
            public String publisher;
            public String thumbnail;
            public String contents;
        }
    }
    @POST("/feel")
    Call<List<String>> uploadText(@Body FeelModel feelModel);

    @GET("v3/search/book")
    Call<KakaoBookResponse> searchBooks(
            @Header("Authorization") String apiKey,
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size
    );
}
