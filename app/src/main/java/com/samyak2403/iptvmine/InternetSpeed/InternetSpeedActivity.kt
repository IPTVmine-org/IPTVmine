package com.samyak2403.iptvmine.InternetSpeed

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.ekn.gruzer.gaugelibrary.Range

import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.databinding.ActivityInternetSpeedBinding
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import java.text.DecimalFormat
import kotlin.math.floor

class InternetSpeedActivity : AppCompatActivity(), ISpeedTestListener {

    private lateinit var binding: ActivityInternetSpeedBinding
    private lateinit var context: Context

    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInternetSpeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the Toolbar as the ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        context = this


        init()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun init() {
        setupGauge()


        binding.startTestBT.setOnClickListener {

                binding.startTestBT.visibility = View.GONE
                Thread { getNetSpeed() }.start()

        }
    }

    private fun setupGauge() {
        val coolPink = Range().apply {
            color = ContextCompat.getColor(context, R.color.cool_pink)
            from = 0.0
            to = 50.0
        }

        val yellowRange = Range().apply {
            color = ContextCompat.getColor(context, R.color.yellow)
            from = 50.0
            to = 100.0
        }

        val greenRange = Range().apply {
            color = ContextCompat.getColor(context, R.color.cool_green)
            from = 100.0
            to = 150.0
        }

        with(binding.speedGauge) {
            addRange(coolPink)
            addRange(yellowRange)
            addRange(greenRange)
            minValue = 0.0
            maxValue = 150.0
            value = 0.0
            valueColor = ContextCompat.getColor(context, android.R.color.transparent)
        }
    }

    private fun getNetSpeed() {
        val speedTestSocket = SpeedTestSocket()
        binding.speedGauge.valueColor = ContextCompat.getColor(context, R.color.white)

        startTime = System.currentTimeMillis()
        speedTestSocket.addSpeedTestListener(this)
        speedTestSocket.startDownload("http://ipv4.appliwave.testdebit.info/50M.iso", 100)
    }

    override fun onCompletion(report: SpeedTestReport) {
        val downloadSpeedMbps = report.transferRateBit.toFloat() / 1_000_000
        val latencyMs = (System.currentTimeMillis() - startTime) / 600

        runOnUiThread {
            binding.speedGauge.value = floor(downloadSpeedMbps.toDouble())
            binding.speedTxt.text = String.format("%s MB/s", DecimalFormat("##").format(downloadSpeedMbps))
            binding.latencyTxt.text = String.format("%s ms", latencyMs)
            binding.startTestBT.apply {
                visibility = View.VISIBLE
                text = getString(R.string.start)
            }
            binding.speedGauge.valueColor = ContextCompat.getColor(context, android.R.color.transparent)
        }
        Log.d("SpeedTest", "onCompletion: $downloadSpeedMbps Mbps")
    }

    override fun onProgress(percent: Float, report: SpeedTestReport) {
        val downloadSpeedMbps = report.transferRateBit.toFloat() / 1_000_000
        runOnUiThread { binding.speedGauge.value = floor(downloadSpeedMbps.toDouble()) }
        Log.d("SpeedTest", "onProgress: $downloadSpeedMbps Mbps")
    }

    override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
        Log.e("SpeedTest", "Error: $errorMessage")
    }
}
