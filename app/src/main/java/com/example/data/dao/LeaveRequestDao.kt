package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LeaveRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveRequestDao {
    @Query("SELECT * FROM leave_requests ORDER BY appliedAt DESC")
    fun getAllLeaveRequestsFlow(): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE empId = :empId ORDER BY appliedAt DESC")
    fun getEmployeeLeaveRequestsFlow(empId: String): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE status = :status ORDER BY appliedAt DESC")
    fun getLeaveRequestsByStatusFlow(status: String): Flow<List<LeaveRequestEntity>>

    @Query("SELECT COUNT(*) FROM leave_requests WHERE status = 'PENDING'")
    fun getPendingLeaveCountFlow(): Flow<Int>

    @Query("SELECT * FROM leave_requests WHERE id = :id LIMIT 1")
    suspend fun getLeaveRequestById(id: Long): LeaveRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequestEntity): Long

    @Update
    suspend fun updateLeaveRequest(request: LeaveRequestEntity)

    @Query("DELETE FROM leave_requests WHERE id = :id")
    suspend fun deleteLeaveRequest(id: Long)
}
