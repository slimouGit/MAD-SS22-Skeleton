package org.dieschnittstelle.mobile.android.skeleton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityMainListitemViewBinding;
import org.dieschnittstelle.mobile.android.skeleton.model.RetrofitRemoteDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.RoomLocalTodoCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.ToDo;
import org.dieschnittstelle.mobile.android.skeleton.model.ToDoCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.util.MADAsyncOperationRunner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String LOGGER = "OverviewActivity";
    private ViewGroup viewRoot;
    private FloatingActionButton addNewItemButton;
    public static final Comparator<ToDo> NAME_COMPARATOR = Comparator.comparing(ToDo::getName);
    public static final Comparator<ToDo> CHECKED_AND_NAME_COMPARATOR = Comparator.comparing(ToDo::isChecked).thenComparing(ToDo::getName);

    private ListView listView;
    private ArrayAdapter<ToDo> listViewAdapter;
    private List<ToDo> listViewItems = new ArrayList<>();

    private ToDoCRUDOperations crudOperations;
    private ProgressBar progressBar;
    private MADAsyncOperationRunner operationRunner;
    private Comparator<ToDo> currentComparator = NAME_COMPARATOR;
    private RoomLocalTodoCRUDOperations localTodoCRUDOperations;
    public static String ARG_ITEM_ID = "itemId";

    private ActivityResultLauncher<Intent> detailviewActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewRoot = findViewById(R.id.viewRoot);
        listView = findViewById(R.id.listView);
        addNewItemButton = findViewById(R.id.fab_add);
        addNewItemButton.setOnClickListener(v -> onAddNewItemButton());
        progressBar = findViewById(R.id.progressBar);
        operationRunner = new MADAsyncOperationRunner(this, progressBar);
        listViewAdapter = initialiseListViewAdapter();
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                ToDo selectedItem = listViewAdapter.getItem(pos);
                onListitemSelected(selectedItem);
            }
        });

        initialiseActivityResultLauncher();

//        crudOperations = ToDoCRUDOperationsImpl.getInstance();
//        crudOperations = new RoomLocalTodoCRUDOperations(this.getApplicationContext());
        crudOperations =  ((ToDoApplication)getApplication()).getCrudOperations();


        operationRunner.run(
                () -> crudOperations.readAllToDos(),
                items -> {
//                    items.forEach(item -> this.addListItemView(item));
                    items.forEach(this::addListItemView);
                    sortItems();
                });
    }

    @NonNull
    private ArrayAdapter<ToDo> initialiseListViewAdapter() {
        return new ArrayAdapter<>(this, R.layout.activity_main_listitem_view, listViewItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View existingListitemView, @NonNull ViewGroup parent) {
                ToDo item = super.getItem(position);

                ActivityMainListitemViewBinding itemBinding = existingListitemView != null
                        ? (ActivityMainListitemViewBinding) existingListitemView.getTag()
                        : DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_main_listitem_view, null, false);

                itemBinding.setItem(item);
                itemBinding.setController(MainActivity.this);
                View itemView = itemBinding.getRoot();
                itemView.setTag(itemBinding);
                return itemView;
            }
        };
    }

    private void initialiseActivityResultLauncher() {
        detailviewActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (result) -> {
                    if (result.getResultCode() == DetailViewActivity.STATUS_CREATED || result.getResultCode() == DetailViewActivity.STATUS_UPDATED) {
                        long itemId = result.getData().getLongExtra(DetailViewActivity.ARG_ITEM_ID, -1);
                        operationRunner.run(
                                () -> crudOperations.readToDo(itemId),
                                item -> {
                                    if (result.getResultCode() == DetailViewActivity.STATUS_CREATED) {
                                        onDataItemCreated(item);
                                    }
                                    if (result.getResultCode() == DetailViewActivity.STATUS_UPDATED) {
                                        onDataItemUpdated(item);
                                    }
                                }
                        );
                    }
                }
        );
    }

    private void onDataItemCreated(ToDo item) {
        this.addListItemView(item);
        sortItems();
    }

    private void onDataItemUpdated(ToDo item) {
        ToDo itemToBeUpdated = this.listViewAdapter.getItem(this.listViewAdapter.getPosition(item));
        itemToBeUpdated.setName(item.getName());
        itemToBeUpdated.setDescription(item.getDescription());
        itemToBeUpdated.setChecked(item.isChecked());
        this.listViewAdapter.notifyDataSetChanged();
    }

    private void addListItemView(ToDo item) {
//        TextView listItemView = (TextView) getLayoutInflater().inflate(R.layout.activity_main_listitem_view, null);
//        listItemView.setText(item);
//        listView.addView(listItemView);
//        listItemView.setOnClickListener(v -> onListitemSelected(((TextView) v).getText().toString()));
        listViewAdapter.add(item);
        listView.setSelection(listViewAdapter.getPosition(item));
        sortItems();
    }

    private void onListitemSelected(ToDo item) {
        Intent detailviewIntent = new Intent(this, DetailViewActivity.class);
        detailviewIntent.putExtra(DetailViewActivity.ARG_ITEM_ID, item.getId());
//        startActivity(detailviewIntent);
        detailviewActivityLauncher.launch(detailviewIntent);
    }

    private void showMessage(String msg) {
//        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();
        Snackbar.make(viewRoot, msg, Snackbar.LENGTH_SHORT).show();
    }

    private static int CALL_DETAILVIEW_FOR_NEW_ITEM = 1;

    private void onAddNewItemButton() {
        Intent detailviewIntentForAddItem = new Intent(this, DetailViewActivity.class);
//        startActivityForResult(detailviewIntentForAddItem, CALL_DETAILVIEW_FOR_NEW_ITEM);
        detailviewActivityLauncher.launch(detailviewIntentForAddItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(LOGGER, "requestCode: " + requestCode);
        Log.i(LOGGER, "resultCode: " + resultCode);
        Log.i(LOGGER, "data: " + data);
        if (requestCode == CALL_DETAILVIEW_FOR_NEW_ITEM) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra(DetailViewActivity.ARG_ITEM_ID);
//                showMessage("revieved: " + name);
//                addListItemView(name);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sortList) {
            showMessage("SORT LIST");
            this.currentComparator = CHECKED_AND_NAME_COMPARATOR;
            sortItems();
            return true;
        } else if (item.getItemId() == R.id.deleteAllItemsLocally) {
            showMessage("DELETE ALL ITEMS LOCALLY");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void sortItems() {
        this.listViewItems.sort(this.currentComparator);
        this.listViewAdapter.notifyDataSetChanged();
    }

    public void onCheckedChangedInListview(ToDo item) {
        this.operationRunner.run(
                () -> crudOperations.updateToDo(item),
                updateditem -> {
                    onDataItemUpdated(updateditem);
                    showMessage("checked changed " + updateditem.getName());
                }
        );
    }
}
