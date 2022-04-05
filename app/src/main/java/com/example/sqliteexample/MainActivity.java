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

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//унаследум MainActivity от View.OnClickListener и реализуем метод onClick
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnAdd, btnRead,btnClear;
    EditText etTask, etQuantity;

// --->>>
// работу с БД реализуем в MainActivity
//      объявим перпменную класса DBHelper
//          созадим его экземпляр в методе onCreate;
//              в методе onClick() содаем объект SQLiteDatabase

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

        btnRead= (Button) findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this::onClick);

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this::onClick);

        etTask = (EditText) findViewById(R.id.etTask);
        etQuantity = (EditText) findViewById(R.id.etQuantity);

        dbHelper = new DBHelper(this);
    }

//  реализуем метод onClick, в котором запишем в строки значения текстовых полей
// а затем для разделения действийпо отдельным кнопкам пропишем конструкцию SWITCH

                        // ***
                        // затем создадим отдельный класс для работы с БД- DBHelper

    @Override
    public void onClick(View v){
        String task = etTask.getText().toString();
        String quantity = etQuantity.getText().toString();

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



        switch (v.getId()){
            case R.id.btnAdd:
                // объект contentValues заполняется парами:
                // метод .put() с аргументами ИМЯ ПОЛЯ, ЗНАЧЕНИЕ
                // и при ppfgbcb в таблицу в указанные поля будут вставлены соответствукеющие значения
                contentValues.put(DBHelper.KEY_TASK, task);
                contentValues.put(DBHelper.KEY_QUANTITY, quantity);
                // мы заполняем только поля task и quantity, а id будет добавлено автоматически.
                                    // ( посмотрим,будет ли добавлен sqltime? )

                // далее, подготовленные строки вставим в таблицу методом .insert()
                database.insert(DBHelper.TABLE_ROUTINES, null, contentValues);
                // этот метод принимает объект имя таблицы и объект contentValues со вставляемыми значениями
                                    // ( второй аргумент испольхуется при вставке пустой строки -
                                    // - а нам сейчас это не нужно, верно?
                break;
            case R.id.btnRead:
                //todo
                break;
            case R.id.btnClear:
                //
                break;
        }
    }
}