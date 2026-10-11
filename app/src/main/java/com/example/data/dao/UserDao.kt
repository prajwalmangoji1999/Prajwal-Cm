package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE empId = :empId LIMIT 1")
    suspend fun getUserByEmpId(empId: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(empId) = LOWER(:empId) AND password = :password LIMIT 1")
    suspend fun authenticate(empId: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(role) != 'admin' ORDER BY name ASC")
    fun getAllEmployeesFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE LOWER(role) != 'admin' ORDER BY name ASC")
    suspend fun getAllEmployees(): List<UserEntity>

    // Admin Credentials Management (Max 5 Admins)
    @Query("SELECT * FROM users WHERE LOWER(role) = 'admin' ORDER BY name ASC")
    fun getAllAdminsFlow(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(role) = 'admin'")
    fun getAdminCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(role) = 'admin'")
    suspend fun getAdminCount(): Int

    @Query("SELECT * FROM users ORDER BY role ASC, name ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(role) != 'admin'")
    fun getEmployeeCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(role) != 'admin'")
    suspend fun getEmployeeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Password change for any ID
    @Query("UPDATE users SET password = :newPassword WHERE empId = :empId")
    suspend fun updatePassword(empId: String, newPassword: String)

    @Query("UPDATE users SET role = :role, canExportExcel = :canExport, canViewAllEmployees = :canViewAll, canTrackLocations = :canTrack WHERE empId = :empId")
    suspend fun updatePermissions(empId: String, role: String, canExport: Boolean, canViewAll: Boolean, canTrack: Boolean)

    @Query("UPDATE users SET lastKnownLat = :lat, lastKnownLng = :lng, lastKnownAddress = :address, lastActiveTimestamp = :timestamp, travelDistanceKm = :distanceKm, routeWaypoints = :waypoints WHERE empId = :empId")
    suspend fun updateLocationAndRoute(empId: String, lat: Double, lng: Double, address: String, timestamp: Long, distanceKm: Double, waypoints: String)

    @Query("UPDATE users SET lastKnownLat = :lat, lastKnownLng = :lng, lastKnownAddress = :address, lastActiveTimestamp = :timestamp WHERE empId = :empId")
    suspend fun updateLocation(empId: String, lat: Double, lng: Double, address: String, timestamp: Long)

    @Query("DELETE FROM users WHERE empId = :empId")
    suspend fun deleteUser(empId: String)

    @Query("UPDATE users SET paidLeaveBalance = :pl, sickLeaveBalance = :sl, compOffBalance = :co, lastLeaveCreditMonthYear = :monthYear WHERE empId = :empId")
    suspend fun updateLeaveBalances(empId: String, pl: Int, sl: Int, co: Int, monthYear: String)

    @Query("UPDATE users SET paidLeaveBalance = :pl, sickLeaveBalance = :sl, compOffBalance = :co WHERE empId = :empId")
    suspend fun setLeaveBalances(empId: String, pl: Int, sl: Int, co: Int)

    @Query("UPDATE users SET paidLeaveBalance = CASE WHEN (paidLeaveBalance + :delta) < 0 THEN 0 ELSE (paidLeaveBalance + :delta) END WHERE empId = :empId")
    suspend fun adjustPaidLeaveBalance(empId: String, delta: Int)

    @Query("UPDATE users SET sickLeaveBalance = CASE WHEN (sickLeaveBalance + :delta) < 0 THEN 0 ELSE (sickLeaveBalance + :delta) END WHERE empId = :empId")
    suspend fun adjustSickLeaveBalance(empId: String, delta: Int)

    @Query("UPDATE users SET compOffBalance = CASE WHEN (compOffBalance + :delta) < 0 THEN 0 ELSE (compOffBalance + :delta) END WHERE empId = :empId")
    suspend fun adjustCompOffBalance(empId: String, delta: Int)
}
