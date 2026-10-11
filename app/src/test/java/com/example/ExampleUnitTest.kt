package com.example

import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAttendanceRules_present() {
        // >= 8 hours 45 mins (8.75 hours = 525 mins)
        val loginTime = 1000000000000L
        val logoutTime = loginTime + (8L * 60 + 45) * 60 * 1000L // 8h 45m exactly
        val (netMinutes, status) = AttendanceRecordEntity.calculateAttendanceStatus(loginTime, logoutTime)

        assertEquals(525L, netMinutes)
        assertEquals(AttendanceRecordEntity.STATUS_PRESENT, status)

        val record = AttendanceRecordEntity(
            empId = "EMP101",
            employeeName = "Alex",
            dateString = "2026-09-30",
            monthYear = "September 2026",
            loginTime = loginTime,
            logoutTime = logoutTime,
            netMinutes = netMinutes,
            status = status
        )
        assertEquals("P", record.statusCode)
    }

    @Test
    fun testAttendanceRules_halfDay() {
        // >= 4 hours 15 mins (4.25 hours = 255 mins) and < 8 hours 45 mins
        val loginTime = 1000000000000L
        val logoutTime = loginTime + (4L * 60 + 30) * 60 * 1000L // 4h 30m
        val (netMinutes, status) = AttendanceRecordEntity.calculateAttendanceStatus(loginTime, logoutTime)

        assertEquals(270L, netMinutes)
        assertEquals(AttendanceRecordEntity.STATUS_HALF_DAY, status)

        val record = AttendanceRecordEntity(
            empId = "EMP101",
            employeeName = "Alex",
            dateString = "2026-09-30",
            monthYear = "September 2026",
            loginTime = loginTime,
            logoutTime = logoutTime,
            netMinutes = netMinutes,
            status = status
        )
        assertEquals("HD", record.statusCode)
    }

    @Test
    fun testAttendanceRules_absent() {
        // < 4 hours 15 mins
        val loginTime = 1000000000000L
        val logoutTime = loginTime + (3L * 60) * 60 * 1000L // 3h
        val (netMinutes, status) = AttendanceRecordEntity.calculateAttendanceStatus(loginTime, logoutTime)

        assertEquals(180L, netMinutes)
        assertEquals(AttendanceRecordEntity.STATUS_ABSENT, status)

        val record = AttendanceRecordEntity(
            empId = "EMP101",
            employeeName = "Alex",
            dateString = "2026-09-30",
            monthYear = "September 2026",
            loginTime = loginTime,
            logoutTime = logoutTime,
            netMinutes = netMinutes,
            status = status
        )
        assertEquals("A", record.statusCode)
    }

    @Test
    fun testUserRolesAndPermissions() {
        val admin = UserEntity(
            empId = "ADMIN001",
            name = "Super Admin",
            role = UserEntity.ROLE_ADMIN
        )
        assertTrue(admin.isAdmin)

        val teamLead = UserEntity(
            empId = "EMP201",
            name = "Team Lead User",
            role = UserEntity.ROLE_TEAM_LEAD,
            canExportExcel = true,
            canTrackLocations = true
        )
        assertFalse(teamLead.isAdmin)
        assertEquals(UserEntity.ROLE_TEAM_LEAD, teamLead.role)
        assertTrue(teamLead.canExportExcel)
        assertTrue(teamLead.canTrackLocations)
        assertFalse(teamLead.canViewAllEmployees)
    }
}
