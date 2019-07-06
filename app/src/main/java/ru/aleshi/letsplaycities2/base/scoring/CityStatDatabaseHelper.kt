package ru.aleshi.letsplaycities2.base.scoring

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.*

class CityStatDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DB_NAME = "cities_frq_stat"
        private const val TABLE_NAME = "cities"
        private const val CITY = "city"
        private const val FRQ = "frq"
    }

    @ExperimentalCoroutinesApi
    fun updateFrequentWords(city: String, mostFrequent: ScoringGroup, manager: ScoreManager) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                val cursor = writableDatabase.rawQuery("SELECT $FRQ FROM $TABLE_NAME WHERE $CITY='$city'", null)
                var frq = 0
                if (cursor != null && cursor.moveToFirst()) {
                    frq = cursor.getInt(cursor.getColumnIndex(FRQ))
                    cursor.close()
                }

                if (frq != 0) {
                    val value = ContentValues()
                    value.put(FRQ, ++frq)
                    writableDatabase.update(TABLE_NAME, value, "$CITY='$city'", null)
                } else {
                    val value = ContentValues()
                    value.put(CITY, city)
                    value.put(FRQ, 1)
                    writableDatabase.insert(TABLE_NAME, null, value)
                }

                val top10 =
                    writableDatabase.rawQuery("SELECT $CITY, $FRQ FROM $TABLE_NAME ORDER BY $FRQ DESC LIMIT 10;", null)
                if (top10 != null) {
                    val frqInd = top10.getColumnIndex(FRQ)
                    val cityInd = top10.getColumnIndex(CITY)

                    var cIndex = 0
                    while (top10.moveToNext()) {
                        val c = top10.getString(cityInd)
                        val f = top10.getInt(frqInd)

                        mostFrequent.child[cIndex++].set("$c=$f")
                    }
                    top10.close()
                }
            }
            manager.saveStats()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE " + TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CITY + " TEXT, " +
                    FRQ + " INTEGER);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //Nothing
    }
}