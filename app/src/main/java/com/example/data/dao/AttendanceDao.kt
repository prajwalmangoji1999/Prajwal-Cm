package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE createdAt >= :cutoffMillis ORDER BY loginTime DESC")
    fun getAllRecordsWithin90DaysFlow(cutoffMillis: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE createdAt >= :cutoffMillis ORDER BY loginTime DESC")
    suspend fun getAllRecordsWithin90Days(cutoffMillis: Long): List<AttendanceRecordEntity>

    @Query("SELECT DISTINCT monthYear FROM attendance_records WHERE createdAt >= :cutoffMillis ORDER BY monthYear DESC")
    fun getDistinctMonthsFlow(cutoffMillis: Long): Flow<List<String>>

    @Query("SELECT * FROM attendance_records WHERE monthYear = :monthYear AND createdAt >= :cutoffMillis ORDER BY loginTime DESC")
    fun getRecordsByMonthFlow(monthYear: String, cutoffMillis: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE monthYear = :monthYear AND createdAt >= :cutoffMillis ORDER BY loginTime DESC")
    suspend fun getRecordsByMonth(monthYear: String, cutoffMillis: Long): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE empId = :empId AND createdAt >= :cutoffMillis ORDER BY loginTime DESC")
    fun getEmployeeRecordsFlow(empId: String, cutoffMillis: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE empId = :empId AND dateString = :dateString LIMIT 1")
    suspend fun getTodayRecord(empId: String, dateString: String): AttendanceRecordEntity?

    @Query("SELECT * FROM attendance_records WHERE empId = :empId AND dateString = :dateString LIMIT 1")
    fun getTodayRecordFlow(empId: String, dateString: String): Flow<AttendanceRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>)

    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity)

    // Data Deletion (Requirement 4)
    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM attendance_records WHERE monthYear = :monthYear")
    suspend fun deleteRecordsByMonth(monthYear: String): Int

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllAttendanceRecords(): Int

    @Query("DELETE FROM attendance_records WHERE createdAt < :cutoffMillis")
    suspend fun deleteRecordsOlderThan(cutoffMillis: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE createdAt >= :cutoffMillis")
    fun getTotalRecordCountFlow(cutoffMillis: Long): Flow<Int>
}
