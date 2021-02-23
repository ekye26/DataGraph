package com.example.datagraph

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.sql.DriverManager
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val load: Button = findViewById(R.id.load)

        load.setOnClickListener {
            connect_avg()
            connect_minmax()
        }
    }

    private fun connect_avg() {

        //이 부분 없으면 오류 이유 파익 x
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val jdbcURL = "jdbc:postgresql://:5432/" //서버 주소
        val username = "" // 유저 이름
        val password = "0812" // 비번

        try {
            val connection = DriverManager.getConnection(jdbcURL, username, password)
            println("Connected to PostgreSQL server")

            //현재시간  // 년도
            val time = System.currentTimeMillis()
            val dateFormatY = SimpleDateFormat("yyyy")
            val timeY = dateFormatY.format(Date(time)).toString()
            //달
            val dateFormatM = SimpleDateFormat("MM")
            val timeM = dateFormatM.format(Date(time)).toInt()
            //일
            val dateFormatD = SimpleDateFormat("dd")
            val timeD_ = dateFormatD.format(Date(time)).toInt()
            val timeD = dateFormatD.format(Date(time)).toString()
            //년도-달-일-00시00분
            val date1 = timeY + "-" + timeM + "-" + timeD + "-" + "00-00"
            val sdf1 = SimpleDateFormat("yyyy-MM-dd-kk-mm")
            val midnight_today: Long = sdf1.parse(date1).time // 00시 00분
            //일 + 1 = 내일
            val dateFormatD1 = SimpleDateFormat("dd")
            val timeD_p_1 = dateFormatD1.format(Date(time)).toInt() + 1
            val timeD_p1 = timeD_p_1.toString()
            // 내일 00시 00분
            val date2 = timeY + "-" + timeM + "-" + timeD_p1 + "-" + "00-00"
            val sdf2 = SimpleDateFormat("yyyy-MM-dd-kk-mm")
            val next_midnignt: Long = sdf2.parse(date2).time

            val sql = "SELECT long_v FROM ts_kv WHERE key = 24 ;"

            val statement = connection.createStatement()
            val result = statement.executeQuery(sql)

            while (result.next()) {

                connection.close()

                val connection = DriverManager.getConnection(jdbcURL, username, password)
                val sql_today =
                    "SELECT AVG(long_v) AS avg FROM ts_kv WHERE key = 24 AND ts >= $midnight_today AND ts < $next_midnignt ;"

                val statement_today = connection.createStatement()
                val result_today = statement_today.executeQuery(sql_today)

                while (result_today.next()) {
                    val today = result_today.getFloat("avg")

                    // 어제
                    val timeD_m_1 = dateFormatD.format(Date(time)).toInt() - 1
                    val timeD_yester = timeD_m_1.toString()

                    val date_yester = timeY + "-" + timeM + "-" + timeD_yester + "-" + "00-00"
                    val sdf_yester = SimpleDateFormat("yyyy-MM-dd-kk-mm")
                    val midnignt_yester: Long = sdf_yester.parse(date_yester).time

                    connection.close()

                    val connection = DriverManager.getConnection(jdbcURL, username, password)
                    val sql_yester =
                        "SELECT AVG(long_v) AS avg FROM ts_kv WHERE key = 24 AND ts >= $midnignt_yester AND ts < $midnight_today ;"

                    val statement_yester = connection.createStatement()
                    val result_yester = statement_yester.executeQuery(sql_yester)

                    while (result_yester.next()) {
                        val yesterday = result_yester.getFloat("avg") //어제 평균값 출력
                        //2일전
                        val timeD_m_2 = dateFormatD.format(Date(time)).toInt() - 2
                        val timeD_2yester = timeD_m_2.toString()

                        val date_2yester = timeY + "-" + timeM + "-" + timeD_2yester + "-" + "00-00"
                        val sdf_2yester = SimpleDateFormat("yyyy-MM-dd-kk-mm")
                        val midnignt_2yester: Long = sdf_2yester.parse(date_2yester).time

                        connection.close()

                        val connection = DriverManager.getConnection(jdbcURL, username, password)
                        val sql_2yester =
                            "SELECT AVG(long_v) AS avg FROM ts_kv WHERE key = 24 AND ts >= $midnignt_2yester AND ts < $midnignt_yester ;"

                        val statement_2yester = connection.createStatement()
                        val result_2yester = statement_2yester.executeQuery(sql_2yester)

                        while (result_2yester.next()) {
                            val yesterday_2 = result_2yester.getFloat("avg")//2일전 평균값 출력

                            val one: TextView = findViewById(R.id.textView2)
                            one.setText("2일 전 : $yesterday_2")
                            val two: TextView = findViewById(R.id.textView3)
                            two.setText("어제 : $yesterday")
                            val three: TextView = findViewById(R.id.textView4)
                            three.setText("오늘 : $today")

                            val mpLineChart: BarChart = findViewById(R.id.BarChart)

                            val dataVals = ArrayList<BarEntry>()

                            dataVals.add(BarEntry(timeD_.toFloat(), yesterday_2))
                            dataVals.add(BarEntry(timeD_m_1.toFloat(), yesterday))
                            dataVals.add(BarEntry(timeD_m_2.toFloat(), today))

                            val lineDataSet = BarDataSet(dataVals, "Data Set 1")
                            //val dataSets = ArrayList<ILineDataSet>()
                            val data = BarData(lineDataSet)
                            mpLineChart.data = data
                            mpLineChart.invalidate()
                            mpLineChart.animateY(2000)

//                            val lineChart : LineChart = findViewById(R.id.Chart)
//                            val visitors = ArrayList<Entry>()
//
//                            visitors.add(Entry(timeD_.toFloat(), yesterday_2))
//                            visitors.add(Entry(timeD_m_1.toFloat(), yesterday))
//                            visitors.add(Entry(timeD_m_2.toFloat(), today))
//
//                            val lineDataSet = LineDataSet(visitors, "평균값")
//
//                            lineDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
//                            lineDataSet.valueTextColor = Color.BLACK
//                            lineDataSet.valueTextSize = 16f
//
//                            val lineData = LineData(lineDataSet)
//
//                            lineChart.data = lineData
//                            lineChart.invalidate()
//                            lineChart.animateY(2000)
//                            lineChart.axisLeft.axisMinimum = 93.0f
//                            lineChart.axisLeft.axisMaximum = 95.0f

                            break
                        }
                        connection.close()
                        break
                    }
                    connection.close()
                    break
                }
                connection.close()
                break
            }
            connection.close()

        } catch (e: SQLException) {
            println("Error in connected to PostgreSQL server")
            Toast.makeText(this, "불러오기 실패", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun connect_minmax() {

        //이 부분 없으면 오류 이유 파익 x
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val jdbcURL = "jdbc:postgresql://203.255.56.50:5432/thingsboard" //서버 주소
        val username = "postgres" // 유저 이름
        val password = "0812" // 비번

        try {
            val connection = DriverManager.getConnection(jdbcURL, username, password)
            println("Connected to PostgreSQL server")

            //현재시간  // 년도
            val time = System.currentTimeMillis()
            val dateFormatY = SimpleDateFormat("yyyy")
            val timeY = dateFormatY.format(Date(time)).toString()
            //달
            val dateFormatM = SimpleDateFormat("MM")
            val timeM = dateFormatM.format(Date(time)).toInt()
            //일
            val dateFormatD = SimpleDateFormat("dd")
            val timeD_ = dateFormatD.format(Date(time)).toInt()
            val timeD = dateFormatD.format(Date(time)).toString()
            //년도-달-일-00시00분
            val date1 = timeY + "-" + timeM + "-" + timeD + "-" + "00-00"
            val sdf1 = SimpleDateFormat("yyyy-MM-dd-kk-mm")
            val midnight_today: Long = sdf1.parse(date1).time // 00시 00분
            //일 + 1 = 내일
            val dateFormatD1 = SimpleDateFormat("dd")
            val timeD_p_1 = dateFormatD1.format(Date(time)).toInt() + 1
            val timeD_p1 = timeD_p_1.toString()
            // 내일 00시 00분
            val date2 = timeY + "-" + timeM + "-" + timeD_p1 + "-" + "00-00"
            val sdf2 = SimpleDateFormat("yyyy-MM-dd-kk-mm")
            val next_midnignt: Long = sdf2.parse(date2).time

            val sql = "SELECT long_v FROM ts_kv WHERE key = 24 ;"

            val statement = connection.createStatement()
            val result = statement.executeQuery(sql)

            while (result.next()) {

                connection.close()

                val connection = DriverManager.getConnection(jdbcURL, username, password)
                val sql_avg =
                    "SELECT AVG(long_v) AS avg FROM ts_kv WHERE key = 24 AND ts >= $midnight_today AND ts < $next_midnignt ;"

                val statement_avg = connection.createStatement()
                val result_avg = statement_avg.executeQuery(sql_avg)

                while (result_avg.next()) {
                    val avg = result_avg.getFloat("avg")

                    connection.close()

                    val connection = DriverManager.getConnection(jdbcURL, username, password)
                    val sql_max = "select * from ts_kv WHERE key = 24 AND ts >= $midnight_today AND ts < $next_midnignt  order by long_v  desc limit 1 ;"
                    val statement_max = connection.createStatement()
                    val result_max = statement_max.executeQuery(sql_max)

                    while (result_max.next()) {
                        val max = result_max.getFloat("long_v") //어제 평균값 출력
                        val max_time = result_max.getLong("ts")

                        val date_m = Date(max_time)
                        val datef = SimpleDateFormat("kk:mm", Locale.getDefault())
                        val date_max = datef.format(date_m)

                        connection.close()

                        val connection = DriverManager.getConnection(jdbcURL, username, password)
                        val sql_min = "select * from ts_kv WHERE key = 24 AND ts >= $midnight_today AND ts < $next_midnignt  order by long_v  asc limit 1 ;"

                        val statement_min = connection.createStatement()
                        val result_min = statement_min.executeQuery(sql_min)


                        while (result_min.next()) {
                            val min = result_min.getFloat("long_v")//2일전 평균값 출력
                            val min_time = result_min.getLong("ts")

                            val date_i = Date(min_time)
                            val datef = SimpleDateFormat("kk:mm", Locale.getDefault())
                            val date_min = datef.format(date_i)


                            val one: TextView = findViewById(R.id.textView6)
                            one.setText("평균 : $avg")
                            val two: TextView = findViewById(R.id.textView7)
                            two.setText("최소 : $min || 시간 : $date_max")
                            val three: TextView = findViewById(R.id.textView8)
                            three.setText("최고 : $max || 시간 : $date_min")


                            val lineChart : LineChart = findViewById(R.id.LineChart)
                            val visitors = ArrayList<Entry>()

                            visitors.add(Entry(1.0f, avg))
                            visitors.add(Entry(2.0f, min))
                            visitors.add(Entry(3.0f, max))

                            val lineDataSet = LineDataSet(visitors, "평균값")

                            lineDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
                            lineDataSet.valueTextColor = Color.BLACK
                            lineDataSet.valueTextSize = 16f

                            val lineData = LineData(lineDataSet)

                            lineChart.data = lineData
                            lineChart.invalidate()
                            lineChart.animateY(2000)
//                            lineChart.axisLeft.axisMinimum = 40.0f
//                            lineChart.axisLeft.axisMaximum = 150.0f

                            break
                        }
                        connection.close()
                        break
                    }
                    connection.close()
                    break
                }
                connection.close()
                break
            }
            connection.close()

        } catch (e: SQLException) {
            println("Error in connected to PostgreSQL server")
            Toast.makeText(this, "불러오기 실패", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }


    }
}