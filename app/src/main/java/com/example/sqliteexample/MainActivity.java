// SQLite поддерживают все андроид-устройства, не зависимо от версии
// поддерживаются следуюшие типы даных:
// NULL
// INTEGER - аналог Long
// REAL - аналог Double
// TEXT - аналог String
// NUMERIC- булевы значения,а также время и дату
// BLOB - бинарные данные
// фактические типы данных не зависят оттого, какой тип был указан при создании таблицы,
// в поле могут сохраняться зачения разных типов
// при этом SQLite сам определяеттип данных для сохранения значения поля
// задание типа данных лишь указывает SQLite на предпочтительный тип даннных

// SQLite не поддерживает BOOLEAN, лучше хранить INTEGER 0 или 1

// файлы лучше хранить не в виде BLOB, а в виде файлов на лиске, а в БД сохранять пить к файлу

//сделаем простое приложение, записывающее, читающее и стирающее запись.
// создадим зоготовку: 2 текстовых поля и 3 кнопки.

package com.example.sqliteexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DateTimePatternGenerator;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;

//унаследум MainActivity от View.OnClickListener и реализуем метод onClick
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnAdd, btnRead,btnClear;
    EditText etQuantity, etDate; //, etTask;
    TextView tvOutput;
    // ADDED:
    AutoCompleteTextView  etTask;
// --->>>
// работу с БД реализуем в MainActivity
//      объявим перпменную класса DBHelper
//          созадим его экземпляр в методе onCreate;
//              в методе onClick() содаем объект SQLiteDatabase

    SharedPreferences sPref;

    String todayValue;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onClick(v);
            }
        });

        //etTask = (EditText) findViewById(R.id.etTask);
        // ADDED:
        etTask = (AutoCompleteTextView) findViewById(R.id.etTask);
        // ЧТОБЫ ЗАПОЛНИТЬ ВАРИАНТАМИ, НУЖЕН АДАПТОР

        etQuantity = (EditText) findViewById(R.id.etQuantity);
        tvOutput = (TextView) findViewById(R.id.output);
        // для прокрутки TextView
        // также нужно установить аттрибут android:scrollbars="vertical" в XML
        tvOutput.setMovementMethod(ScrollingMovementMethod.getInstance());
//TODO: SINGLE SimpleDateFormat

//        Calendar today = Calendar.getInstance();
        todayValue = new SimpleDateFormat("yyyy-MM-dd")
                .format(new Date().getTime())
                .substring(0,10);
        etDate = (EditText) findViewById(R.id.etDate);
        etDate.setText(todayValue);

        dbHelper = new DBHelper(this);

        this.read("");
    }

//  реализуем метод onClick, в котором запишем в строки значения текстовых полей
// а затем для разделения действийпо отдельным кнопкам пропишем конструкцию SWITCH

                        // ***
                        // затем создадим отдельный класс для работы с БД- DBHelper

    @Override
    public void onClick(View v){
        this.add(v);
    }


    public void add(View v){

        String task = etTask.getText().toString();
        String quantity = etQuantity.getText().toString();
        String etDateString = etDate.getText().toString();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date inputDate;



        // НАЧНЕМ С ПРОВЕРКИ ДАТЫ.
        String currentDateStr = dateFormat.format(new Date()).substring(0,10);
//        currentDateStr = new SimpleDateFormat("yyyy-MM-dd")
//                .format(Calendar.getInstance()
//                        .getTime()).substring(0,10);


        // БУДУЩУЮ ДАТУ ЗАПИСЫВАТЬ НЕЛЬЗЯ.
        if(etDateString.compareTo(currentDateStr)>0){
            Toast.makeText(this, "You cannot add future date!", Toast.LENGTH_SHORT).show();
        }else {
            //--->>>
            // создадим экземпляр класса SQLiteDatabase
            // этот класс предназначен для управления БД SQLite
            // в нем определены следующие методы:
            //      query() - для чтения данных из БД
            //      insert()- для добавлеия данных в БД
            //      delete() для удаления данных из БД
            //      update() для изменения данных в БД
            // кроме того,
            //      execSQL() выполняет любой SQL запрос

            // чтобы создать объект SQLiteDatabase
            // вызовем метод getWritableDatabase() вспомогательного класса DBHelper
            // чтобы открыть и вернуть экземпляр БД, с котороый мы дудем рабобтать
            // этот экземпляр будет доступен как для чтения, так и для записи
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            // если БД не существует, вспомогательтный класс DBHelper вызовет свой метод onCreate()
            // если версия БД изменилась, вызовет свой метод onUpgrade()
            //      - в любом случае,
            //      методы getWritableDatabase() и getReadableDatabase()
            //      вернут существующую, или только что созанную, или обновленную БД

            // создадим объект класса ContentValues
            // этокласс используется для добавления нровых строк в таблицу.
            // каждый объект этогокласса представляет собой одну строку таблицы,
            // и выглядит укак массив с именами столбцов и значениями, которые им соответствуют
            ContentValues contentValues = new ContentValues();

            // далее, по нажтию кнопки ADD будем заполнять наш contentValues строками
            // -->> перейдём в обработки нажатия кнопки add ->>
            // объект contentValues заполняется парами:
            // метод .put() с аргументами ИМЯ ПОЛЯ, ЗНАЧЕНИЕ
            // и при ppfgbcb в таблицу в указанные поля будут вставлены соответствукеющие значения
            contentValues.put(DBHelper.KEY_TASK, task);
            contentValues.put(DBHelper.KEY_QUANTITY, quantity);

            // ЕСЛИ ДАТА РАНЕЕ ЧЕМ СЕГОДНЯ, ЕЕ ТОЖЕ ЗАПИСЫВАЕМ В БД
        // IF FALSE -- DEPRECATED, ИЗБАВЛЯЕМСЯ ОТ КАЛЕНДАРЯ
            if(false){//inputDate.compareTo(currentDateStr)<0) {
                int year = Integer.parseInt(etDateString.substring(0,4));
                int month = Integer.parseInt(etDateString.substring(0,4));
                int day = Integer.parseInt(etDateString.substring(0,4));
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                contentValues.put(DBHelper.TIMESTAMP, calendar.getTimeInMillis());
            }
        // IF TRUE

            try {
                inputDate = dateFormat.parse(etDateString);
                if(etDateString.compareTo(currentDateStr)<0){
                    contentValues.put(DBHelper.TIMESTAMP, inputDate.getTime());
                }else{
                    contentValues.put(DBHelper.TIMESTAMP, new Date().getTime());
                }

                this.dbInsert(database, contentValues, task, quantity);
            } catch (ParseException parseException) {
                //inputDate = new Date();
                Log.d("ERROR", parseException.toString());


                //ALERT
                AlertDialog dateParseAlert = new AlertDialog.Builder(this)
                        .setTitle("Parse error")
                        .setMessage("Create new record for current date?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                contentValues.put(DBHelper.TIMESTAMP, new Date().getTime());
                                dbInsert(database, contentValues, task, quantity);
                            }
                        })
                        .setNegativeButton("NO", null)
                        .create();
                dateParseAlert.show();
            }

            // мы заполняем только поля task и quantity, а id будет добавлено автоматически.
            // ( посмотрим,будет ли добавлен sqltime? )

            // далее, подготовленные строки вставим в таблицу методом .insert()
            //database.insert(DBHelper.TABLE_ROUTINES, null, contentValues);
            // этот метод принимает объект имя таблицы и объект contentValues со вставляемыми значениями
            // ( второй аргумент испольхуется при вставке пустой строки -
            // - а нам сейчас это не нужно, верно?
        } //    END ELSE
    }

    public void dbInsert(SQLiteDatabase database, ContentValues contentValues,
                         String task, String quantity){

        // мы заполняем только поля task и quantity, а id будет добавлено автоматически.
        // ( посмотрим,будет ли добавлен sqltime? )

        // далее, подготовленные строки вставим в таблицу методом .insert()
        database.insert(DBHelper.TABLE_ROUTINES, null, contentValues);
        // этот метод принимает объект имя таблицы и объект contentValues со вставляемыми значениями
        // ( второй аргумент испольхуется при вставке пустой строки -
        // - а нам сейчас это не нужно, верно?


        //добавим всплывающую подсказку о добавлениии записи
        Toast.makeText(this, "Record \""
                + task + " - "
                + quantity + " \" is successfully added", Toast.LENGTH_SHORT).show();


        // очищаем поля ввода, курсор на etTask
        etTask.setText("");
        etQuantity.setText("");
        etTask.requestFocus();

        this.read();
    }

    public void read(){
        this.read(DBHelper.TABLE_ROUTINES);
    }

    public void read(String tableName){

        //if(sPref.contains("tables"))
        ArrayList<String> routinesList = new ArrayList<>();
        tableName = DBHelper.TABLE_ROUTINES;

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(tableName, null, null, null,null, null, DBHelper.TIMESTAMP);
        //Cursor cursor2 = database.
        // метод query возвращает объект класса Cursor.
        // его можно рассматривать как набюор строк с данными.
        // класс Cursor имеет следубщие методы:
        //      moveToFirst(), moveToLast() -> boolean - перемещает курсор на первую (последнюю) строку в результате запроса
        //      moveToNext(), moveToPrevious()
        //      getCount() - воззвращает количество строк в наборе даннных
        //      getColumnIndexOrThrow() - возвращает индекс указанногостолбца, бросая исключение если такого не сущ
        //      getColumnName() - имя столбца по индексу
        //      getColimnNames() - массив всехимен столбцов в объекте Cursor
        //      moveToPosition() - перемещает курсор в указаннную строку
        //      getPosition() - вощзвращает текущуб позоътциб курсора

        // также Android предоставляет свои дополнителные методы:
        //      isBeforeFirst() - указывает ли курсорна позтцию перед первой строкой
        //      isAfterLast() - сигнализирует о достищ=жении конца запроса
        //      isClosed() - возвращает true если курсор закрыт
        // ...и это еще не все методы,см. документацию. . .

        // метод moveToFirst() делает первую запист в курсор активной,
        // и заодно проверяет, есть ли вообще в нем запись? те выбиралось ли что-нибудь в методе query()

        //StringBuilder sb = new StringBuilder();

        // тестовый вывод
        tvOutput.setText("");   //TimeZone.getDefault().toString() + " \n\n");

        if(cursor.moveToFirst()){
            //получаем порядковые номера столбцов по их именам
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int taskIndex = cursor.getColumnIndex(DBHelper.KEY_TASK);
            int quantityIndex = cursor.getColumnIndex(DBHelper.KEY_QUANTITY);
            int dateIndex = cursor.getColumnIndex(DBHelper.TIMESTAMP);
            String date;

            // для того чтобы сгруппировать записи по датам
            String lastRecordDate = "";
            long sqlDateInMillis;
            // DEPRECATED Calendar sqlCalendar = Calendar.getInstance();

            // создадим формат даты с часовым поясом,чтобы даты группировались согласно часовому поясу
            SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");
            dateFormat.setTimeZone(TimeZone.getDefault());

            do {
                // ГРУППИРУЕМ ПО ДАТЕ
                // получим дату текущей записи в мс
                sqlDateInMillis = cursor.getLong(dateIndex);
                // запишем в календарь время из записи БД
                //DEPRECATED КАЛЕНДАРЬ sqlCalendar.setTimeInMillis(sqlDateInMillis);
        //в date запишем yyyy-MM-dd записи в БД,
        // НО!! ФОРМАТИРОВАТЬ БУДЕМ НЕ ИЗ ОБЪЕКТА Date{} А ИЗ ОБЪЕКТА КАЛЕНДАРЯ
        // ТАК МЫ ПОЛУЧИМ ДАТУ С УЧЁТОМ ЧАСОВЫХ ПОЯСОВ

        //                String date = new SimpleDateFormat("yyyy-MM-dd")
        //                        .format(sqlCalendar);//new Date(sqlDateInMillis));

        // почему-то не получается форматировать объект календаря:
        //      logcat> java.lang.IllegalArgumentException: Cannot format given Object as a Date

                //*** ОКАЗЫВАЕТСЯ, ПРИМЕНЯТЬ ЧАСОВОЙ ПОЯС НУЖНО ПРИ КОНВЕРИТИРОВАНИИ ДАТЫ В String:
                //*     <<When you format a Date object into a string, for example by using SimpleDateFormat,
                //*     then you can set the time zone on the DateFormat object
                //*     to let it know in which time zone you want to display the date and time>>

                // поэтому ВЫШЕ объявим переменную SimpleDateFormat
                // и установили ей TimeZoneb формат вывода.

                date = dateFormat.format(sqlDateInMillis);//new Date(sqlDateInMillis));

                // если она НЕ совпадает с lastRecordDate, выводим ее
                // и обновляем последнюю дату
                if(!(date.substring(0, 10)).equals(lastRecordDate)){
                    tvOutput.append(" --- "
                            // DEPRECATED + sqlCalendar.get(Calendar.DATE)
                            + date.substring(0, 10)
                            + " --- \n");
                    lastRecordDate = date.substring(0, 10);
                }
                // ВЫВОДИМ прочее В TEXTVIEW
                tvOutput.append(date.substring(11,16)
                                + ", task: "
                                + cursor.getString(taskIndex)
                                + " - "
                                + cursor.getString(quantityIndex) + "\n"
                        );
                if(!routinesList.contains(cursor.getString(taskIndex)))
                    routinesList.add(cursor.getString(taskIndex));

//* DEPRECATED
//*                tvOutput.append(
//*                                sqlCalendar.get(Calendar.HOUR_OF_DAY)
//*                                + ":" + sqlCalendar.get(Calendar.MINUTE)  //cursor.getString(dateIndex).substring(11,16)
//*                                +", task: " + cursor.getString(taskIndex)
//*                                + " - " + cursor.getString(quantityIndex) + "\n"
//*                );
                {
                    Log.d("mLog", "ID = " + cursor.getInt(idIndex)
                            + ", --- " + date + " --- " + cursor.getString(dateIndex)
                            + ", Task = " + cursor.getString(taskIndex)
                            + " - " + cursor.getString(quantityIndex) + " times"
                    );
                }   //LOG EVERYTHING
            } while (cursor.moveToNext());
        }else {
            Log.d("mLog", "0 rows");
            tvOutput.append("0 rows");
        }
        // Cursor обязательно надощакрывать методом close() для освобождения памяти(?)
        //tvOutput.append(sb.toString());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, routinesList);
        etTask.setAdapter(adapter);


        cursor.close();
    } // *** END READ

    public String readEverything(String table){
        StringBuilder sb = new StringBuilder("");
        ArrayList<String> line = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + table, null);
        if(cursor.moveToFirst()) {
            ArrayList<String> columns = new ArrayList<String>(
                    Arrays.asList(cursor.getColumnNames()));

            for(String colunm: columns)
                sb.append("|" + colunm);

            sb.append("\n");

            do{
                for(String column: columns)
                    sb.append("|" + cursor.getString(cursor.getColumnIndexOrThrow(column)));

                sb.append("\n");

            }while (cursor.moveToNext());
        }

        cursor.close();
        return sb.toString();
    }

    public void clearCurrentTable(){
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Clear database?")
                .setMessage("It will erase all data")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                        database.delete(DBHelper.TABLE_ROUTINES, null, null);
                        Toast.makeText(MainActivity.this,"Database erased",Toast.LENGTH_SHORT).show();
                        MainActivity.this.read();
                    }
                })
                .setNegativeButton("NO", null)
                .create();
        alert.show();
    }

    public ArrayList<String> allTablesList(){
        ArrayList<String> allTablesList = new ArrayList<>();
        allTablesList.add("TestLine \n");
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor =  database.rawQuery("SELECT * FROM sqlite_master WHERE type='table'", null);

        while(cursor.moveToNext()){
            allTablesList.add(cursor.getString(1)+"\n");
        }

        cursor.close();
        return allTablesList;
    }

    //Calendar dialogDate = Calendar.getInstance();



    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
            etDate.setText(year+"-"+month+"-"+dayOfMonth);
        }
    };

    public void setDate(View v){
        new DatePickerDialog(MainActivity.this, d,
                Integer.valueOf(todayValue.substring(0,4)),
                Integer.valueOf(todayValue.substring(5,7)),
                Integer.valueOf(todayValue.substring(8,10)))
                .show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }
//      СЮДА ПЕРЕНЕСЕМДЕЙСТВИЯ ПО ОЧИСТКЕ ТАБЛИЦЫ
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // CLEAR TABLE
        if(item.getItemId()==R.id.menu_clear){
            this.clearCurrentTable();
        }

        // СПИСОК ТАБЛИЦ
        if(item.getItemId()==R.id.menu_viewAll){
            Dialog dlgViewAll = new Dialog(this, R.style.Dialog);
            dlgViewAll.setTitle("Please select table");
            dlgViewAll.setContentView(R.layout.layout_view_all);


            ListView lvAllTablesList;
            lvAllTablesList = (ListView) dlgViewAll.findViewById(R.id.lvAllTablesList);


            /*ArrayList<String> allTablesList = new ArrayList<>();
            allTablesList.add("TestLine \n");
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            Cursor cursor =  database.rawQuery("SELECT * FROM sqlite_master WHERE type='table'", null);

            while(cursor.moveToNext()){
                tvAllTablesList.append(cursor.getString(1)+"\n");
            }

            cursor.close();*/
            //new ArrayAdapter()

            ArrayAdapter<String> adapter = new ArrayAdapter(
                    this, R.layout.support_simple_spinner_dropdown_item,// android.R.layout.simple_spinner_dropdown_item,
                    allTablesList());
            //adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            lvAllTablesList.setAdapter(adapter);

            lvAllTablesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)                {
                    //Toast.makeText(adapter.getContext(), String.valueOf(position) ,Toast.LENGTH_SHORT).show();
                    tvOutput.setText(readEverything(allTablesList().get(position)));
                }
            });


            Button btnChangeTable = dlgViewAll.findViewById(R.id.btnChahgeTable);
            btnChangeTable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlgViewAll.dismiss();
                }
            });

            dlgViewAll.show();
        }

        if(item.getItemId()==R.id.menu_addNewList){
            AlertDialog newListDialog = new AlertDialog.Builder(this)
                    .setTitle("Create new list")
                    .setMessage("New list will be marke for current date")
                    .setPositiveButton("YES, FOR TODAY", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            SQLiteDatabase database = dbHelper.getWritableDatabase();
                            dbHelper.newList(database, "t"+todayValue.replace("-","_"));//(DBHelper.TABLE_ROUTINES, null, null);
                            Toast.makeText(MainActivity.this,"Table" + todayValue +" created",Toast.LENGTH_SHORT).show();
                            MainActivity.this.read();
                        }
                    })
                    .setNegativeButton("NO", null)
                    .create();
            newListDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }
}