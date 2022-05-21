package org.dieschnittstelle.mobile.android.skeleton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailviewBindingImpl;
import org.dieschnittstelle.mobile.android.skeleton.model.ToDo;
import org.dieschnittstelle.mobile.android.skeleton.model.ToDoCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.ToDoCRUDOperationsImpl;

public class DetailViewActivity extends AppCompatActivity implements DetailViewModel {
    public static String ARG_ITEM_ID = "itemId";
    private ToDo item;
    private ActivityDetailviewBindingImpl binding;
    private ToDoCRUDOperations crudOperations;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_detailview);
        this.crudOperations = ToDoCRUDOperationsImpl.getInstance();
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_detailview);
        long itemId = getIntent().getLongExtra(ARG_ITEM_ID, -1);
        if(itemId != -1){
            this.item = this.crudOperations.readToDo(itemId);
        }

        if (this.item == null){
            this.item = new ToDo();
        }
        this.binding.setViewmodel(this);
    }

    public ToDo getItem(){
        return this.item;
    }

    public void onSaveItem() {
        Intent returnIntent = new Intent();
        if(item.getId() > 0){
            this.item = this.crudOperations.updateToDo(this.item);
        }else{
            this.item = this.crudOperations.createToDo(this.item);
        }
        returnIntent.putExtra(ARG_ITEM_ID, this.item.getId());

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
