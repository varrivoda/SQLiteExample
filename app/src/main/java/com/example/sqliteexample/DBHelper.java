package com.example.sqliteexample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
// унаследуем этот класс от абстрактного класса SQLiteOpenHelper
// здесь нужно обязательно реализовать 2абстрактных метода
// onCreate - вызывается при создании БД
// onUpgrade - вслучае,если номер Бд выше,чем .... имеет место при обновлении прилодения,
//          ...когданужно заменить старую версию сртуктуры БД

// также SQLiteOpenHelper имеет необязательные методы:
// onDowngrade
// onOpen - при открытии
// getReadableDatabase - возвращает экземплят БД,доступный для чтения,
// getWritableDatabase - вовращает экземпляр, достурпный для чтения и записи

public class DBHelper extends SQLiteOpenHelper {

    // ---4---
    // нам понадобятся константы для версии БД, (начиная с номера 1)
    //      имени
    //      имени таблицы
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "routinesDb";
    public static final String TABLE_ROUTINES = "routines";

    // добавим также константы для заголовков столбцов
    // при этом идентификатор _id должен обязательно начинаться с нижнего подчеркивания
    //          ..это связано с особенностями работы android
    public static final String KEY_ID = "_id";
    public static final String KEY_TASK = "task";
    public static final String KEY_QUANTITY = "quantity";
    public static final String TIMESTAMP = "timestamp";

//---3---
// также в DBHelper нужно реализовать конструктор
// в нём мы вызываем конструктор суперкласса, и в негопередаем 4 пкраметра:
//      -контекст,
//      -имя БД,
//      -объект класса CursorFactory (который расширяет стандартный Cursor)
//
//      -версию БД
// В нашем примере мы не будем использовать CursorFactory, просто занулим его.

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // в методе onCreate реализуем логику создания таблиц и заполнения их начальными данными при помощи команд SQL
    // метод onCreate используется, если базаданых не существует, и ее надо создать.
    // используем метод execSQL() объекта SQLDatabase, который выполняет SQL-запрос.
    // в нашем случае мысоздаем таблицу с именем и колонками из наших констант - см выше.
    // по запросу видно, что мы создаем таблицу со столбцами id, task, quantity, time_stamp
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ TABLE_ROUTINES + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "                 // AUTOINCREMENT NOT NULL ?
                + KEY_TASK + " TEXT, "
                + KEY_QUANTITY + " TEXT, "
                + TIMESTAMP + " INTEGER)"); // DEFAULT CURRENT_TIMESTAMP NOT NULL)");
    }

    // В методе onUpgrade, который сработает при изменении номера версии БД можно вызвать запрос на уничтожение таблицы
    // после чего вызвать onCreate с обновленной структурой.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTINES);

        onCreate(db);
    }
    // еще момент - в данном классе принято объявлять открытые строковые константы для названия
    // таблиц и полей БД, чтобы их можно было использовать в других классах для определения
    // названия таблиц и полей. Поэтому справим модификаторы констант на public

}
// работу с БД реализуем в MainActivity --->
//      объявим перпменную класса DBHelper
//          созадим его экземпляр в методе onCreate;
//              в методе onClick() содаем объект SQLiteDatabase ---->>>
