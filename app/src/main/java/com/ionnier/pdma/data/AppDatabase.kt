package com.ionnier.pdma.data

import android.content.Context
import androidx.room.*
import com.ionnier.pdma.ui.fragments.Ingredient
import kotlinx.coroutines.flow.Flow

@Database(entities = [IngredientEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "database-name"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}


@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients order by addedAt desc")
    fun getAll(): Flow<List<IngredientEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: IngredientEntry)

    @Delete
    suspend fun delete(ingredient: IngredientEntry)
}


@Entity(tableName = "ingredients")
data class IngredientEntry(
    @PrimaryKey var addedAt: Long,
    var photo: String?,
    @Embedded val ingredient: Ingredient,
)