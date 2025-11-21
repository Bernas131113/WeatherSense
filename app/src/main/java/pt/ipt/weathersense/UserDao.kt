package pt.ipt.weathersense

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User)


    @Query("SELECT * FROM users WHERE email = :email AND pass = :password LIMIT 1")
    suspend fun checkLogin(email: String, password: String): User?
}