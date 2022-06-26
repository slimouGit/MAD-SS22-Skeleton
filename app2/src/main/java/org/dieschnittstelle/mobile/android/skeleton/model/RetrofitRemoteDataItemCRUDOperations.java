package org.dieschnittstelle.mobile.android.skeleton.model;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class RetrofitRemoteDataItemCRUDOperations implements ToDoCRUDOperations{

    public interface ToDoWebAPI{

        @POST("/api/todos")
        public Call<ToDo> createToDo(@Body ToDo item);

        @GET("/api/todos")
        public Call<List<ToDo>> readAllToDos();

        @GET("/api/todos/{todoId}")
        public Call<ToDo> readToDo(@Path("todoId") long id);

        @PUT("/api/todos/{todoId}")
        public Call<ToDo> updateToDo(@Path("todoId") long id, @Body ToDo item);

        @DELETE("/api/todos/{todoId}")
        public Call<Boolean> deleteToDo(@Path("todoId") long id);
    }

    private ToDoWebAPI toDoWebAPI;

    public RetrofitRemoteDataItemCRUDOperations() {
        Retrofit apibase = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        toDoWebAPI = apibase.create(ToDoWebAPI.class);
    }

    @Override
    public ToDo createToDo(ToDo item) {
        try {
            return toDoWebAPI.createToDo(item).execute().body();
        } catch (Exception e) {
            throw new RuntimeException("got exception" + e, e);
        }
    }

    @Override
    public List<ToDo> readAllToDos() {
        try {
            return toDoWebAPI.readAllToDos().execute().body();
        } catch (Exception e) {
            throw new RuntimeException("got exception" + e, e);
        }
    }

    @Override
    public ToDo readToDo(long id) {
        try {
            return toDoWebAPI.readToDo(id).execute().body();
        } catch (Exception e) {
            throw new RuntimeException("got exception" + e, e);
        }
    }

    @Override
    public ToDo updateToDo(ToDo itemToBeUpdated) {
        try {
            return toDoWebAPI.updateToDo(itemToBeUpdated.getId(), itemToBeUpdated).execute().body();
        } catch (Exception e) {
            throw new RuntimeException("got exception" + e, e);
        }
    }

    @Override
    public boolean deleteToDo(long id) {
        try {
            return toDoWebAPI.deleteToDo(id).execute().body();
        } catch (Exception e) {
            throw new RuntimeException("got exception" + e, e);
        }
    }

    @Override
    public boolean deleteAll() {
        return false;
    }
}
