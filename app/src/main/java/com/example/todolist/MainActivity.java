package com.example.todolist;
/*
 * @author Chris Yang Li
 */

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.todolist.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView listView;
    private Button button;
    EditText mEditText;
    private static final String FILE_NAME = "ToDoList.txt";
    private static final String TEMP_NAME = "TempList.txt";
    public static String itemName;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        button = findViewById(R.id.button);
        mEditText = findViewById(R.id.editTextText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(v);
            }
        });

        items = load();
        itemsAdapter =  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        setUpListViewListener();

    }

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getApplicationContext();
                Toast.makeText(context, "Items Removed", Toast.LENGTH_LONG).show();
                itemName = items.get(position);
                items.remove(position);
                itemsAdapter.notifyDataSetChanged();

                FileOutputStream fos = null;
                FileInputStream fis = null;
                try {
                    fis = getApplicationContext().openFileInput(FILE_NAME);
                    fos = getApplicationContext().openFileOutput(TEMP_NAME, Context.MODE_PRIVATE);

                    int ch;
                    int commaCount = 0;
                    while ((ch = fis.read()) != -1) {
                        if (ch == ',') commaCount++;
                        if (commaCount-1 != position)
                            fos.write(ch);
                    }

                    fos.close();
                    fis.close();

                    FileInputStream tempToDoFile = getApplicationContext().openFileInput(TEMP_NAME);
                    FileOutputStream newFile = getApplicationContext().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                    while ((ch = tempToDoFile.read()) != -1) {
                        newFile.write(ch);
                    }

                    tempToDoFile.close();
                    newFile.close();

                    items = load();
                    itemsAdapter =  new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, items);
                    listView.setAdapter(itemsAdapter);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            }
        });
    }

    private void addItem(View view) {
        EditText inputName = findViewById(R.id.editTextText);
        String itemText = inputName.getText().toString();
        //Toast.makeText(this, "Name entry cannot be empty", Toast.LENGTH_LONG).show();

        if (!(itemText.equals(""))) {
            if (items.contains(itemText)) {
                Toast.makeText(this, "Repeated Name Not Allowed!", Toast.LENGTH_LONG).show();
                return;
            }
            itemsAdapter.add(itemText);
            save();
            inputName.setText("");
        } else {
            Toast.makeText(this, "Name entry cannot be empty", Toast.LENGTH_LONG).show();
        }
    }

    public void save() {
        String text = mEditText.getText().toString();
        //Toast.makeText(this, "Saving...", Toast.LENGTH_LONG).show();
        FileOutputStream fos = null;
        FileInputStream fis = null;

        try {
            File f = getApplicationContext().getFileStreamPath(FILE_NAME);
            if(!f.exists()) {
                //Log.d("Create", "Triggered");
                FileOutputStream createfile = getApplicationContext().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                createfile.close();
            }
            fis = getApplicationContext().openFileInput(FILE_NAME);
            fos = getApplicationContext().openFileOutput(TEMP_NAME, Context.MODE_PRIVATE);
            int ch;
            while ((ch = fis.read()) != -1) {
                fos.write(ch);
            }

            char[] tempArray = text.toCharArray();
            fos.write(',');
            for (int i = 0; i < tempArray.length; i++) {
                fos.write(tempArray[i]);
            }

            fis.close();
            fos.close();

            FileInputStream tempToDoFile = getApplicationContext().openFileInput(TEMP_NAME);
            FileOutputStream newFile = getApplicationContext().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);

            while ((ch = tempToDoFile.read()) != -1) {
                newFile.write(ch);
            }

            tempToDoFile.close();
            newFile.close();

            items = load();
            mEditText.getText().clear();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<String> load() {
        FileInputStream fis = null;
        ArrayList<String> tempitems = new ArrayList<>();
        ArrayList<Character> charArrayList = new ArrayList<Character>();

        try {
            fis = getApplicationContext().openFileInput(FILE_NAME);
            int ch;
            boolean firstcomma = false;

            while ((ch = fis.read()) != -1) {
                if (ch == ',' && !firstcomma) {
                    firstcomma = true;
                    continue;
                } else if (ch == ',') {
                    StringBuilder builder = new StringBuilder(charArrayList.size());
                    for(Character c: charArrayList) {
                        builder.append(c);
                    }
                    tempitems.add(builder.toString());
                    charArrayList.clear();
                } else {
                    charArrayList.add((char)ch);
                }
            }

            StringBuilder builder = new StringBuilder(charArrayList.size());
            for(Character c: charArrayList) {
                builder.append(c);
            }
            if (!builder.toString().equals(""))
                tempitems.add(builder.toString());
            charArrayList.clear();

            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempitems;
    }

}