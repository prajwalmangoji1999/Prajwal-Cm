package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    /**
     * Generates a native Excel Spreadsheet XML (.xls) file with:
     * - Employee details (Name, ID, Joining Date, Date of Birth, Current & Permanent Address)
     * - Login time
     * - Logged time (Logout time)
     * - Net login hours
     * - Status as requested: "P" for Present, "A" for Absent, "HD" for Half Day
     */
    suspend fun generateExcelFile(
        context: Context,
        records: List<AttendanceRecordEntity>,
        monthTitle: String,
        employeesMap: Map<String, UserEntity> = emptyMap()
    ): File = withContext(Dispatchers.IO) {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val sanitizedMonth = monthTitle.replace(" ", "_").replace("/", "-")
        val file = File(reportsDir, "Attendance_Report_${sanitizedMonth}_${System.currentTimeMillis()}.xls")

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val xmlBuilder = StringBuilder()
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        xmlBuilder.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
        xmlBuilder.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        xmlBuilder.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
        xmlBuilder.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
        xmlBuilder.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n")

        // Styles
        xmlBuilder.append(" <Styles>\n")
        xmlBuilder.append("  <Style ss:ID=\"Default\" ss:Name=\"Normal\">\n")
        xmlBuilder.append("   <Alignment ss:Vertical=\"Center\"/>\n")
        xmlBuilder.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"10\" ss:Color=\"#000000\"/>\n")
        xmlBuilder.append("  </Style>\n")

        // Main Header style
        xmlBuilder.append("  <Style ss:ID=\"HeaderStyle\">\n")
        xmlBuilder.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        xmlBuilder.append("   <Borders>\n")
        xmlBuilder.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#CBD5E1\"/>\n")
        xmlBuilder.append("   </Borders>\n")
        xmlBuilder.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/>\n")
        xmlBuilder.append("   <Interior ss:Color=\"#1E3A8A\" ss:Pattern=\"Solid\"/>\n")
        xmlBuilder.append("  </Style>\n")

        // Present Code "P" Style
        xmlBuilder.append("  <Style ss:ID=\"CodePStyle\">\n")
        xmlBuilder.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        xmlBuilder.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"12\" ss:Bold=\"1\" ss:Color=\"#065F46\"/>\n")
        xmlBuilder.append("   <Interior ss:Color=\"#D1FAE5\" ss:Pattern=\"Solid\"/>\n")
        xmlBuilder.append("  </Style>\n")

        // Half Day Code "HD" Style
        xmlBuilder.append("  <Style ss:ID=\"CodeHDStyle\">\n")
        xmlBuilder.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        xmlBuilder.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"12\" ss:Bold=\"1\" ss:Color=\"#92400E\"/>\n")
        xmlBuilder.append("   <Interior ss:Color=\"#FEF3C7\" ss:Pattern=\"Solid\"/>\n")
        xmlBuilder.append("  </Style>\n")

        // Absent Code "A" Style
        xmlBuilder.append("  <Style ss:ID=\"CodeAStyle\">\n")
        xmlBuilder.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        xmlBuilder.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"12\" ss:Bold=\"1\" ss:Color=\"#991B1B\"/>\n")
        xmlBuilder.append("   <Interior ss:Color=\"#FEE2E2\" ss:Pattern=\"Solid\"/>\n")
        xmlBuilder.append("  </Style>\n")

        // Clocked In / Pending Style
        xmlBuilder.append("  <Style ss:ID=\"PendingStyle\">\n")
        xmlBuilder.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        xmlBuilder.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"10\" ss:Bold=\"1\" ss:Color=\"#1E40AF\"/>\n")
        xmlBuilder.append("   <Interior ss:Color=\"#DBEAFE\" ss:Pattern=\"Solid\"/>\n")
        xmlBuilder.append("  </Style>\n")

        xmlBuilder.append(" </Styles>\n")

        // Worksheet
        xmlBuilder.append(" <Worksheet ss:Name=\"Attendance Report\">\n")
        xmlBuilder.append("  <Table>\n")

        // Column widths
        xmlBuilder.append("   <Column ss:Width=\"90\"/>\n")  // Emp ID
        xmlBuilder.append("   <Column ss:Width=\"130\"/>\n") // Name
        xmlBuilder.append("   <Column ss:Width=\"85\"/>\n")  // Date
        xmlBuilder.append("   <Column ss:Width=\"90\"/>\n")  // Month Folder
        xmlBuilder.append("   <Column ss:Width=\"85\"/>\n")  // Login Time
        xmlBuilder.append("   <Column ss:Width=\"85\"/>\n")  // Logged Time (Logout)
        xmlBuilder.append("   <Column ss:Width=\"95\"/>\n")  // Net Login Hours
        xmlBuilder.append("   <Column ss:Width=\"80\"/>\n")  // Status Code (P / HD / A)
        xmlBuilder.append("   <Column ss:Width=\"100\"/>\n") // Status Full Name
        xmlBuilder.append("   <Column ss:Width=\"90\"/>\n")  // Joining Date
        xmlBuilder.append("   <Column ss:Width=\"85\"/>\n")  // DOB
        xmlBuilder.append("   <Column ss:Width=\"160\"/>\n") // Current Address
        xmlBuilder.append("   <Column ss:Width=\"140\"/>\n") // Login GPS
        xmlBuilder.append("   <Column ss:Width=\"180\"/>\n") // Login Address
        xmlBuilder.append("   <Column ss:Width=\"140\"/>\n") // Logout GPS
        xmlBuilder.append("   <Column ss:Width=\"180\"/>\n") // Logout Address

        // Header Row
        xmlBuilder.append("   <Row ss:Height=\"28\">\n")
        val headers = listOf(
            "Employee ID",
            "Employee Name",
            "Date",
            "Month Folder",
            "Login Time",
            "Logged Time",
            "Net Login Hours",
            "Attendance (P/HD/A)",
            "Status Description",
            "Joining Date",
            "Date of Birth",
            "Current Address",
            "Login GPS Coordinates",
            "Login Location Address",
            "Logout GPS Coordinates",
            "Logout Location Address"
        )
        for (h in headers) {
            xmlBuilder.append("    <Cell ss:StyleID=\"HeaderStyle\"><Data ss:Type=\"String\">$h</Data></Cell>\n")
        }
        xmlBuilder.append("   </Row>\n")

        // Rows
        for (record in records) {
            val employee = employeesMap[record.empId]

            val loginTimeStr = timeFormat.format(Date(record.loginTime))
            val logoutTimeStr = record.logoutTime?.let { timeFormat.format(Date(it)) } ?: "Not Logged Out"
            val durationStr = if (record.logoutTime != null) {
                AttendanceRecordEntity.formatDuration(record.netMinutes)
            } else {
                "In Progress"
            }

            // Requirements:
            // 1. Present Means its shows "P"
            // 2. Absent Means its shows "A"
            // 3. Half Day Means its shows "HD"
            val (code, styleId) = when (record.status) {
                AttendanceRecordEntity.STATUS_PRESENT -> Pair("P", "CodePStyle")
                AttendanceRecordEntity.STATUS_HALF_DAY -> Pair("HD", "CodeHDStyle")
                AttendanceRecordEntity.STATUS_ABSENT -> Pair("A", "CodeAStyle")
                else -> Pair("--", "PendingStyle")
            }

            val joiningDate = employee?.joiningDate?.ifBlank { "--" } ?: "--"
            val dob = employee?.dob?.ifBlank { "--" } ?: "--"
            val currentAddress = employee?.currentAddress?.ifBlank { "--" } ?: "--"

            val loginGps = String.format(Locale.US, "%.5f, %.5f", record.loginLat, record.loginLng)
            val logoutGps = if (record.logoutLat != null && record.logoutLng != null) {
                String.format(Locale.US, "%.5f, %.5f", record.logoutLat, record.logoutLng)
            } else {
                "--"
            }

            xmlBuilder.append("   <Row ss:Height=\"22\">\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${escapeXml(record.empId)}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${escapeXml(record.employeeName)}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${record.dateString}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${escapeXml(record.monthYear)}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">$loginTimeStr</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">$logoutTimeStr</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">$durationStr</Data></Cell>\n")
            xmlBuilder.append("    <Cell ss:StyleID=\"$styleId\"><Data ss:Type=\"String\">$code</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${record.status}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${escapeXml(joiningDate)}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${escapeXml(dob)}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${escapeXml(currentAddress)}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">$loginGps</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${escapeXml(record.loginAddress)}</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">$logoutGps</Data></Cell>\n")
            xmlBuilder.append("    <Cell><Data ss:Type=\"String\">${escapeXml(record.logoutAddress ?: "--")}</Data></Cell>\n")
            xmlBuilder.append("   </Row>\n")
        }

        xmlBuilder.append("  </Table>\n")
        xmlBuilder.append(" </Worksheet>\n")
        xmlBuilder.append("</Workbook>\n")

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write(xmlBuilder.toString())
            }
        }

        file
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    /**
     * Creates an Intent to open or share the Excel spreadsheet file.
     */
    fun getShareExcelIntent(context: Context, file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.ms-excel"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "GeoAttend Monthly Attendance Report")
            putExtra(Intent.EXTRA_TEXT, "Attached is the monthly employee attendance Excel spreadsheet with employee details, login/logged times, net hours, and P/A/HD status.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
