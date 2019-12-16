package com.hololo.app.dnschanger.dnschanger

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.text.InputFilter
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.hololo.app.dnschanger.DNSChangerApp
import com.hololo.app.dnschanger.R
import com.hololo.app.dnschanger.about.AboutActivity
import com.hololo.app.dnschanger.dnschanger.DNSPresenter.SERVICE_OPEN
import com.hololo.app.dnschanger.model.DNSModel
import com.hololo.app.dnschanger.model.DNSModelJSON
import com.hololo.app.dnschanger.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import javax.inject.Inject

class MainActivity : AppCompatActivity(), IDNSView, DialogInterface.OnClickListener {


    @Inject
    lateinit var presenter: DNSPresenter
    @Inject
    lateinit var gson: Gson


    private var dnsList: List<DNSModel>? = null

    private val dnsModel: DNSModel
        get() {
            val dnsModel = DNSModel(getString(R.string.custom_dns), "0.0.0.0", "0.0.0.0")
            val first = firstDnsEdit!!.text.toString()
            val second = secondDnsEdit!!.text.toString()


            if (dnsList != null)
                for (model in dnsList!!) {
                    if (model.firstDns == first && model.secondDns == second) {
                        dnsModel.name = model.name
                    }
                }

            dnsModel.firstDns = first
            dnsModel.secondDns = second

            return dnsModel
        }

    private val isValid: Boolean
        get() {
            var result = true
            firstDnsEdit!!.error = null
            secondDnsEdit!!.error = null

            if (!IP_PATTERN.matcher(firstDnsEdit!!.text).matches()) {
                firstDnsEdit!!.error = getString(R.string.enter_valid_dns)
                result = false
            }

            if (!IP_PATTERN.matcher(secondDnsEdit!!.text).matches()) {
                secondDnsEdit!!.error = getString(R.string.enter_valid_dns)
                result = false
            }

            return result
        }

    private val dnsItems: Array<CharSequence?>
        get() {
            val result = arrayOfNulls<CharSequence>(18)

            try {
                val `is` = assets.open("dns_servers.json")
                val size = `is`.available()
                val buffer = ByteArray(size)
                `is`.read(buffer)
                `is`.close()
                val json = String(buffer, Charsets.UTF_8)
                val dnsModels = gson.fromJson(json, DNSModelJSON::class.java)
                dnsList = dnsModels.modelList
                for ((counter, dnsModel) in dnsList!!.withIndex()) {
                    result[counter] = dnsModel.name
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return result
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DaggerDNSComponent.builder().applicationComponent(
                DNSChangerApp.getApplicationComponent())
                .dNSModule(DNSModule(this)).build().inject(this)
        initViews()
        getServiceStatus()
        parseIntent()
    }

    private fun parseIntent() {
        if (intent != null && intent.extras != null) {
            val dnsModelJSON = intent.extras!!.getString("dnsModel", "")
            if (dnsModelJSON.isNotEmpty()) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(1903)
                if (dnsList == null)
                    dnsItems
                val model = gson.fromJson(dnsModelJSON, DNSModel::class.java)
                if (model.name == getString(R.string.custom_dns)) {
                    firstDnsEdit!!.setText(model.firstDns)
                    secondDnsEdit!!.setText(model.secondDns)
                } else {
                    for (i in dnsList!!.indices) {
                        val dnsModel = dnsList!![i]
                        if (dnsModel.name == model.name) {
                            onClick(null, i)
                        }
                    }
                }
                runOnUiThread {
                    makeSnackbar(getString(R.string.dns_starting))
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    startButton!!.performClick()
                }
            }
        }
    }

    private fun getServiceStatus() {
        if (presenter.isWorking) {
            serviceStarted()
            presenter.getServiceInfo()
        } else {
            serviceStopped()
        }
    }

    override fun changeStatus(serviceStatus: Int) {
        if (serviceStatus == SERVICE_OPEN) {
            serviceStarted()
            makeSnackbar(getString(R.string.service_started))
        } else {
            serviceStopped()
            makeSnackbar(getString(R.string.service_stoppped))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> openAboutActivity()
            R.id.settings -> openSettingsActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openAboutActivity() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    override fun setServiceInfo(model: DNSModel) {
        chooseButton!!.text = model.name
        firstDnsEdit!!.setText(model.firstDns)
        secondDnsEdit!!.setText(model.secondDns)
    }

    private fun serviceStopped() {
        startButton!!.setText(R.string.start)
        startButton!!.setBackgroundResource(R.drawable.button)
        firstDnsEdit!!.isEnabled = true
        secondDnsEdit!!.isEnabled = true
        firstDnsEdit!!.setText("")
        secondDnsEdit!!.setText("")
        chooseButton!!.isEnabled = true
        chooseButton!!.setText(R.string.choose_dns_server)
        chooseButton!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    private fun serviceStarted() {
        startButton!!.setText(R.string.stop)
        startButton!!.setBackgroundResource(R.drawable.button_red)
        firstDnsEdit!!.isEnabled = false
        secondDnsEdit!!.isEnabled = false
        chooseButton!!.isEnabled = false
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_black_24dp)
        drawable!!.setBounds(40, 0, drawable.intrinsicHeight + 40, drawable.intrinsicWidth)
        chooseButton!!.setCompoundDrawables(drawable, null, null, null)
    }

    private fun makeSnackbar(message: String) {
        Snackbar.make(activity_main!!, message, Snackbar.LENGTH_LONG).show()
    }

    private fun initViews() {
        setSupportActionBar(tool_bar)
        supportActionBar!!.title = ""


        val filters = arrayOfNulls<InputFilter>(1)
        filters[0] = InputFilter { source, start, end, dest, dstart, dend ->
            if (end > start) {
                val destTxt = dest.toString()
                val resultingTxt = destTxt.substring(0, dstart) +
                        source.subSequence(start, end) +
                        destTxt.substring(dend)
                if (!resultingTxt.matches(("^\\d{1,3}(\\." + "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?").toRegex())) {
                    return@InputFilter ""
                } else {
                    val splits = resultingTxt.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in splits.indices) {
                        if (Integer.valueOf(splits[i]) > 255) {
                            return@InputFilter ""
                        }
                    }
                }
            }
            null
        }
        firstDnsEdit!!.filters = filters
        secondDnsEdit!!.filters = filters

        chooseButton.setOnClickListener {
            openChooser()
        }

        startButton.setOnClickListener {
            startDNS()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == -1) {
            presenter.startService(dnsModel)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun openChooser() {
        val items = dnsItems
        val dialog = AlertDialog.Builder(this)
                .setItems(items, this)
                .setTitle(R.string.choose_dns_server)
                .setNegativeButton(R.string.cancel)
                { dialog, _ -> dialog.dismiss() }
                .create()
        val listView = dialog.listView
        listView.divider = ContextCompat.getDrawable(this, R.drawable.divider) // set color
        listView.dividerHeight = 1
        listView.setPadding(16, 16, 16, 16)
        dialog.show()
    }

    private fun startDNS() {
        if (presenter.isWorking) {
            presenter.stopService()
        } else if (isValid) {
            val intent = VpnService.prepare(this)
            if (intent != null) {
                startActivityForResult(intent, REQUEST_CONNECT)
            } else {
                onActivityResult(REQUEST_CONNECT, Activity.RESULT_OK, null)
            }
        } else {
            makeSnackbar(getString(R.string.enter_valid_dns))
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val model = dnsList!![which]
        firstDnsEdit!!.setText(model.firstDns)
        secondDnsEdit!!.setText(model.secondDns)
        chooseButton!!.text = model.name
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()
    }

    companion object {

        private const val REQUEST_CONNECT = 21
        private val IP_PATTERN = Patterns.IP_ADDRESS
    }
}
