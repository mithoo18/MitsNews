package com.example.mitsnews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mitsnews.Model.Articles;
import com.example.mitsnews.Model.Headlines;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Adapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    Button btnSearch;
    EditText etQuery;

    final String API_KEY= "YOUR API KEY";
    List<Articles> articles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //binding
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        btnSearch = findViewById(R.id.btnSearch);
        etQuery = findViewById(R.id.etQuery);

        //recycler
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final String country = getCountry();

        //when you swipe it will refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrieveJson("", country, API_KEY);//without search query
            }
        });

        //it just show the data
        retrieveJson("", country, API_KEY);//bec query is empty

        //when you click on search and pass query
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //query should not be null
                    if (!etQuery.getText().toString().equals("")) {
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                            //query pass
                            retrieveJson(etQuery.getText().toString(), country, API_KEY);
                             }
                            });
                        retrieveJson(etQuery.getText().toString(), country, API_KEY);
                    }else{
                        //when query is null
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            retrieveJson("", country, API_KEY);
                            //we have not send any query
                        }
                        });
                    retrieveJson("", country, API_KEY);
                }
            }
        });
    }
    public void retrieveJson(String query,String country,String apiKey){
        swipeRefreshLayout.setRefreshing(true);
        Call<Headlines> call;

        //not null - everything
        if(!etQuery.getText().toString().equals(""))
        {
            call = ApiClient.getIntance().getApi().getSpecificData(query,apiKey);
        }else{
            //null - top-trending
            call = ApiClient.getIntance().getApi().getHeadlines(country,apiKey);
        }

        call.enqueue(new Callback<Headlines>() {
            @Override
            public void onResponse(Call<Headlines> call, Response<Headlines> response) {
            if(response.isSuccessful() && response.body().getArticles()!=null)
            {
                swipeRefreshLayout.setRefreshing(false);
                articles.clear();
                articles = response.body().getArticles();
                adapter = new Adapter(MainActivity.this,articles);
                recyclerView.setAdapter(adapter);
            }
            }
            @Override
            public void onFailure(Call<Headlines> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this,t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public String getCountry()
    {
        Locale locale = Locale.getDefault();
        String country = locale.getCountry();
        return country.toLowerCase();
    }

}
