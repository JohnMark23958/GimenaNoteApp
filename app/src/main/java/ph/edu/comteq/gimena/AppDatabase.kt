package ph.edu.comteq.gimena

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room




@Database(
    entities = [
        Note::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            return instance ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_database"
                ).build()
                this.instance = instance
                instance
            }
        }
    }
}