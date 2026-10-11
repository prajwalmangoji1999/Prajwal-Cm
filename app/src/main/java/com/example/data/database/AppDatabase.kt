package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.LeaveRequestDao
import com.example.data.dao.UserDao
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.LeaveRequestEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [UserEntity::class, AttendanceRecordEntity::class, LeaveRequestEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveRequestDao(): LeaveRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                lateinit var instance: AppDatabase
                val appContext = context.applicationContext
                instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "geoattend_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope, appContext) { instance })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope,
            private val context: Context,
            private val databaseProvider: () -> AppDatabase
        ) : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                scope.launch(Dispatchers.IO) {
                    try {
                        val database = databaseProvider()
                        if (database.userDao().getUserByEmpId("ADMIN001") == null) {
                            populateInitialData(context, database.userDao(), database.attendanceDao(), database.leaveRequestDao())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        private fun createSamplePunchBitmap(file: java.io.File, label: String, headerColor: Int) {
            try {
                val bitmap = android.graphics.Bitmap.createBitmap(400, 400, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                canvas.drawColor(android.graphics.Color.rgb(15, 23, 42)) // Dark Navy/Slate background

                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                }

                // Draw header bar
                paint.color = headerColor
                canvas.drawRect(0f, 0f, 400f, 60f, paint)

                paint.color = android.graphics.Color.WHITE
                paint.textSize = 20f
                paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
                paint.textAlign = android.graphics.Paint.Align.CENTER
                canvas.drawText(label, 200f, 40f, paint)

                // Draw face silhouette
                paint.color = android.graphics.Color.rgb(51, 65, 85)
                canvas.drawCircle(200f, 180f, 65f, paint)
                val path = android.graphics.Path()
                path.moveTo(90f, 320f)
                path.quadTo(200f, 230f, 310f, 320f)
                path.lineTo(310f, 340f)
                path.lineTo(90f, 340f)
                path.close()
                canvas.drawPath(path, paint)

                // Draw verified badge circle
                paint.color = headerColor
                canvas.drawCircle(270f, 215f, 24f, paint)
                paint.color = android.graphics.Color.WHITE
                paint.textSize = 26f
                canvas.drawText("✓", 270f, 224f, paint)

                // Draw bottom watermark
                paint.color = android.graphics.Color.rgb(148, 163, 184)
                paint.textSize = 14f
                paint.textAlign = android.graphics.Paint.Align.CENTER
                canvas.drawText("LIVE GPS CAMERA VERIFIED", 200f, 375f, paint)

                java.io.FileOutputStream(file).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        suspend fun populateInitialData(
            context: Context? = null,
            userDao: UserDao,
            attendanceDao: AttendanceDao,
            leaveRequestDao: LeaveRequestDao? = null
        ) {
            var sampleLoginPath = ""
            var sampleLogoutPath = ""
            if (context != null) {
                try {
                    val loginFile = java.io.File(context.filesDir, "sample_punch_in.jpg")
                    createSamplePunchBitmap(loginFile, "CHECK IN - VERIFIED", android.graphics.Color.rgb(16, 185, 129))
                    sampleLoginPath = loginFile.absolutePath

                    val logoutFile = java.io.File(context.filesDir, "sample_punch_out.jpg")
                    createSamplePunchBitmap(logoutFile, "CHECK OUT - VERIFIED", android.graphics.Color.rgb(244, 63, 94))
                    sampleLogoutPath = logoutFile.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // Seed Admin (Primary Admin ID 1/5)
            val admin = UserEntity(
                empId = "ADMIN001",
                name = "Operations Admin",
                password = "admin",
                role = UserEntity.ROLE_ADMIN,
                joiningDate = "2023-01-10",
                dob = "1990-05-12",
                permanentAddress = "HQ Corporate Tower, Metro City",
                currentAddress = "742 Evergreen Terrace, Metro City"
            )
            userDao.insertUser(admin)

            // Seed Sample Employees with realistic routes & travelled distance
            val employees = listOf(
                UserEntity(
                    empId = "EMP101",
                    name = "Alex Johnson",
                    password = "password",
                    role = UserEntity.ROLE_EMPLOYEE,
                    joiningDate = "2024-02-15",
                    dob = "1996-08-22",
                    permanentAddress = "12 Oakwood Lane, Seattle, WA",
                    currentAddress = "455 Pine Street, Seattle, WA",
                    lastKnownLat = 37.7749,
                    lastKnownLng = -122.4194,
                    lastKnownAddress = "Financial District Office, San Francisco",
                    lastActiveTimestamp = System.currentTimeMillis() - (15 * 60 * 1000),
                    travelDistanceKm = 14.6,
                    routeWaypoints = "37.7650,-122.4300;37.7690,-122.4250;37.7720,-122.4210;37.7749,-122.4194"
                ),
                UserEntity(
                    empId = "EMP102",
                    name = "Sarah Miller",
                    password = "password",
                    role = UserEntity.ROLE_TEAM_LEAD,
                    joiningDate = "2024-03-01",
                    dob = "1994-11-04",
                    permanentAddress = "88 Maple Ave, Portland, OR",
                    currentAddress = "210 Broadway Blvd, San Francisco, CA",
                    lastKnownLat = 37.7833,
                    lastKnownLng = -122.4167,
                    lastKnownAddress = "Tech Hub Campus, San Francisco",
                    lastActiveTimestamp = System.currentTimeMillis() - (35 * 60 * 1000),
                    canViewAllEmployees = true,
                    canTrackLocations = true,
                    travelDistanceKm = 9.8,
                    routeWaypoints = "37.7780,-122.4220;37.7805,-122.4190;37.7833,-122.4167"
                ),
                UserEntity(
                    empId = "EMP103",
                    name = "David Chen",
                    password = "password",
                    role = UserEntity.ROLE_SITE_MANAGER,
                    joiningDate = "2024-04-12",
                    dob = "1998-03-17",
                    permanentAddress = "300 Cedar Ridge, Austin, TX",
                    currentAddress = "78 Mission St, San Francisco, CA",
                    lastKnownLat = 37.7915,
                    lastKnownLng = -122.3985,
                    lastKnownAddress = "Port Logistics Center, San Francisco",
                    lastActiveTimestamp = System.currentTimeMillis() - (5 * 60 * 1000),
                    canExportExcel = true,
                    canViewAllEmployees = true,
                    canTrackLocations = true,
                    travelDistanceKm = 21.4,
                    routeWaypoints = "37.7700,-122.4150;37.7780,-122.4080;37.7850,-122.4020;37.7915,-122.3985"
                ),
                UserEntity(
                    empId = "EMP104",
                    name = "Priya Sharma",
                    password = "password",
                    role = UserEntity.ROLE_EMPLOYEE,
                    joiningDate = "2024-05-20",
                    dob = "1995-12-30",
                    permanentAddress = "55 Elm St, Denver, CO",
                    currentAddress = "102 Howard St, San Francisco, CA",
                    lastKnownLat = 37.7685,
                    lastKnownLng = -122.4467,
                    lastKnownAddress = "Twin Peaks Field Operations, SF",
                    lastActiveTimestamp = System.currentTimeMillis() - (55 * 60 * 1000),
                    travelDistanceKm = 11.2,
                    routeWaypoints = "37.7820,-122.4200;37.7750,-122.4330;37.7685,-122.4467"
                ),
                UserEntity(
                    empId = "EMP105",
                    name = "Marcus Vance",
                    password = "password",
                    role = UserEntity.ROLE_EMPLOYEE,
                    joiningDate = "2024-06-18",
                    dob = "1993-07-09",
                    permanentAddress = "99 Sunset Blvd, Los Angeles, CA",
                    currentAddress = "500 Howard St, San Francisco, CA",
                    lastKnownLat = 37.7890,
                    lastKnownLng = -122.4010,
                    lastKnownAddress = "Downtown Client Site, San Francisco",
                    lastActiveTimestamp = System.currentTimeMillis() - (120 * 60 * 1000),
                    travelDistanceKm = 6.5,
                    routeWaypoints = "37.7820,-122.4090;37.7855,-122.4050;37.7890,-122.4010"
                )
            )
            userDao.insertUsers(employees)

            // Seed historical attendance across recent 60 days
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

            val sampleRecords = mutableListOf<AttendanceRecordEntity>()

            // Generate past records
            for (dayOffset in 1..25) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)

                // Skip Sundays
                if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) continue

                val dateStr = dateFormat.format(calendar.time)
                val monthStr = monthFormat.format(calendar.time)

                employees.forEachIndexed { index, emp ->
                    calendar.set(Calendar.HOUR_OF_DAY, 8)
                    calendar.set(Calendar.MINUTE, 30 + (index * 5))
                    calendar.set(Calendar.SECOND, 0)
                    val loginMillis = calendar.timeInMillis

                    val durationHours = when ((dayOffset + index) % 7) {
                        0 -> 4.5 // Half Day (4h 30m >= 4:15)
                        1 -> 3.0 // Absent (< 4:15)
                        else -> 9.0 // Present (9h >= 8:45)
                    }

                    val durationMillis = (durationHours * 60 * 60 * 1000).toLong()
                    val logoutMillis = loginMillis + durationMillis
                    val (netMins, status) = AttendanceRecordEntity.calculateAttendanceStatus(loginMillis, logoutMillis)

                    val pastDistance = 8.0 + (dayOffset % 15) * 1.2
                    val roundedPastDist = Math.round(pastDistance * 10.0) / 10.0

                    sampleRecords.add(
                        AttendanceRecordEntity(
                            empId = emp.empId,
                            employeeName = emp.name,
                            dateString = dateStr,
                            monthYear = monthStr,
                            loginTime = loginMillis,
                            logoutTime = logoutMillis,
                            loginPhotoPath = sampleLoginPath,
                            logoutPhotoPath = sampleLogoutPath,
                            loginLat = emp.lastKnownLat ?: 37.7749,
                            loginLng = emp.lastKnownLng ?: -122.4194,
                            loginAddress = emp.lastKnownAddress ?: "Field Office",
                            logoutLat = (emp.lastKnownLat ?: 37.7749) + 0.008,
                            logoutLng = (emp.lastKnownLng ?: -122.4194) + 0.006,
                            logoutAddress = "Client Base Station, San Francisco",
                            netMinutes = netMins,
                            status = status,
                            createdAt = loginMillis,
                            travelDistanceKm = roundedPastDist,
                            routeWaypoints = "${emp.lastKnownLat},${emp.lastKnownLng};${(emp.lastKnownLat ?: 37.7749) + 0.004},${(emp.lastKnownLng ?: -122.4194) + 0.003};${(emp.lastKnownLat ?: 37.7749) + 0.008},${(emp.lastKnownLng ?: -122.4194) + 0.006}"
                        )
                    )
                }
            }

            // Also seed today's in-progress login
            val todayCal = Calendar.getInstance()
            val todayStr = dateFormat.format(todayCal.time)
            val thisMonthStr = monthFormat.format(todayCal.time)

            todayCal.set(Calendar.HOUR_OF_DAY, 8)
            todayCal.set(Calendar.MINUTE, 45)
            val todayLoginMillis = todayCal.timeInMillis

            sampleRecords.add(
                AttendanceRecordEntity(
                    empId = "EMP102",
                    employeeName = "Sarah Miller",
                    dateString = todayStr,
                    monthYear = thisMonthStr,
                    loginTime = todayLoginMillis,
                    logoutTime = null,
                    loginPhotoPath = sampleLoginPath,
                    loginLat = 37.7780,
                    loginLng = -122.4220,
                    loginAddress = "Tech Hub Campus, San Francisco",
                    status = AttendanceRecordEntity.STATUS_CLOCKED_IN,
                    createdAt = todayLoginMillis,
                    travelDistanceKm = 9.8,
                    routeWaypoints = "37.7780,-122.4220;37.7805,-122.4190;37.7833,-122.4167"
                )
            )

            todayCal.set(Calendar.HOUR_OF_DAY, 9)
            todayCal.set(Calendar.MINUTE, 0)
            val emp103Login = todayCal.timeInMillis

            sampleRecords.add(
                AttendanceRecordEntity(
                    empId = "EMP103",
                    employeeName = "David Chen",
                    dateString = todayStr,
                    monthYear = thisMonthStr,
                    loginTime = emp103Login,
                    logoutTime = null,
                    loginPhotoPath = sampleLoginPath,
                    loginLat = 37.7700,
                    loginLng = -122.4150,
                    loginAddress = "Port Logistics Center, San Francisco",
                    status = AttendanceRecordEntity.STATUS_CLOCKED_IN,
                    createdAt = emp103Login,
                    travelDistanceKm = 21.4,
                    routeWaypoints = "37.7700,-122.4150;37.7780,-122.4080;37.7850,-122.4020;37.7915,-122.3985"
                )
            )

            attendanceDao.insertRecords(sampleRecords)

            if (leaveRequestDao != null) {
                leaveRequestDao.insertLeaveRequest(
                    LeaveRequestEntity(
                        empId = "EMP101",
                        employeeName = "Alex Johnson",
                        leaveType = LeaveRequestEntity.LEAVE_TYPE_PL,
                        startDate = todayStr,
                        endDate = todayStr,
                        daysCount = 1,
                        reason = "Personal family event",
                        documentPath = null,
                        status = LeaveRequestEntity.STATUS_PENDING
                    )
                )
            }
        }
    }
}
