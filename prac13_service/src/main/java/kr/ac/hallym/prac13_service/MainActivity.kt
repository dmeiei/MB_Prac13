package kr.ac.hallym.prac13_service

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import kotlinx.coroutines.*
import kr.ac.hallym.prac13_outer.IMyAidlInterface
import kr.ac.hallym.prac13_service.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var messenger: Messenger
    lateinit var replyMessenger: Messenger
    var messengerJob: Job? = null
    var connectionMode = "none"
    var aidlService: IMyAidlInterface? = null
    var aidJob:Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Messenger
        onCreateMessengerService()

        //AIDL
        onCreateAIDLService()

        //JobScheduler
        onCreateJobScheduler()

    }

    override fun onStop() {
        super.onStop()
        if(connectionMode == "messenger"){
            onStopMessengerService()
        } else if (connectionMode == "aidl"){
            onStopAIDLService()
        }
        connectionMode="none"
        changeViewEnabled()
    }

    val messengerConnection:ServiceConnection = object:ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d("kkang","onServiceConnected...")
            messenger = Messenger(p1)
            val msg = Message()
            msg.replyTo = replyMessenger
            msg.what = 10
            messenger.send(msg)
            connectionMode = "messenger"

        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d("kkang","onServiceDisconnected...")
        }
    }

    inner class HandlerReplyMsg: Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                10->{
                    val bundle = msg.obj as Bundle
                    bundle.getInt("duration")?.let{
                        when{
                            it>0 ->{
                                binding.messengerProgress.max = it
                                val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
                                messengerJob = backgroundScope.launch {
                                    while (binding.messengerProgress.progress < binding.messengerProgress.max){
                                        delay(1000)
                                        binding.messengerProgress.incrementProgressBy(1000)
                                    }
                                }
                                changeViewEnabled()
                            }
                            else ->{
                                connectionMode = "none"
                                unbindService(messengerConnection)
                                changeViewEnabled()
                            }
                        }
                    }
                }
            }
        }
    }

    fun changeViewEnabled() = when (connectionMode){
        "messenger" ->{
            binding.messengerPlay.isEnabled = false
            binding.aidPlay.isEnabled = false
            binding.messengerStop.isEnabled = true
            binding.aidStop.isEnabled = false
        }
        "aidl" ->{
            binding.messengerPlay.isEnabled = false
            binding.aidPlay.isEnabled = false
            binding.messengerStop.isEnabled = false
            binding.aidStop.isEnabled = true
        }
        else ->{
            binding.messengerPlay.isEnabled = true
            binding.aidPlay.isEnabled = true
            binding.messengerStop.isEnabled = false
            binding.aidStop.isEnabled = false

            binding.messengerProgress.progress = 0
            binding.aidlProgress.progress = 0
        }
    }

    private fun onCreateMessengerService(){
        replyMessenger = Messenger(HandlerReplyMsg())
        binding.messengerPlay.setOnClickListener {
            val intent = Intent("ACTION_SERVICE_Messenger")
            intent.setPackage("kr.ac.hallym.prac13_outer")
            bindService(intent,messengerConnection,Context.BIND_AUTO_CREATE)
        }
        binding.messengerStop.setOnClickListener {
            val msg = Message()
            msg.what = 20
            messenger.send(msg)
            unbindService(messengerConnection)
            messengerJob?.cancel()
            connectionMode = "none"
            changeViewEnabled()
        }
    }
    private fun onStopMessengerService(){
        val msg = Message()
        msg.what = 20
        messenger.send(msg)
        unbindService(messengerConnection)
    }

    val aidlConnection: ServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            aidlService = IMyAidlInterface.Stub.asInterface(p1)
            aidlService!!.start()
            binding.aidlProgress.max = aidlService!!.maxDuration
            val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
            aidJob = backgroundScope.launch {
                while(binding.aidlProgress.progress < binding.aidlProgress.max){
                    delay(1000)
                    binding.aidlProgress.incrementProgressBy(1000)
                }
            }
            connectionMode="aidl"
            changeViewEnabled()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            aidlService = null
        }
    }

    private fun onCreateAIDLService(){
        binding.aidPlay.setOnClickListener {
            val intent = Intent("ACTION_SERVICE_AIDL")
            intent.setPackage("kr.ac.hallym.prac13_outer")
            bindService(intent, aidlConnection,Context.BIND_AUTO_CREATE)
        }
        binding.aidStop.setOnClickListener {
            Log.d("kkang","stop....")
            aidlService!!.stop()
            unbindService(aidlConnection)
            aidJob?.cancel()
            connectionMode = "none"
            changeViewEnabled()
        }
    }

    private fun onStopAIDLService(){
        unbindService(aidlConnection)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onCreateJobScheduler(){
        var jobScheduler:JobScheduler?=getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val builder = JobInfo.Builder(1, ComponentName(this,MyJobService::class.java))
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
        val jobInfo = builder.build()
        jobScheduler!!.schedule(jobInfo)
    }

}