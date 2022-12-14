package kr.ac.hallym.prac13

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.ac.hallym.prac13.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityMain2Binding
    lateinit var messenger: Messenger
    lateinit var replyMessenger: Messenger

    val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("kkang","onServiceConnected...")
            messenger = Messenger(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("kkang","onServiceDisconnected...")
        }
    }

    internal class HandlerReplyMsg : Handler(Looper.getMainLooper()){

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main2)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bindServiceButton.setOnClickListener {
            val intent = Intent(this, MyService2::class.java)
            bindService(intent,connection,Context.BIND_AUTO_CREATE)
        }

        binding.unbindServiceButton.setOnClickListener {
            unbindService(connection)
        }
        binding.sendButton1.setOnClickListener {
            val msg = Message()
            msg.what = 10
            msg.obj = "hello"
            messenger.send(msg)
        }
        binding.sendButton2.setOnClickListener {
            val msg = Message()
            msg.what = 20
            msg.obj = "world"
            messenger.send(msg)
        }
    }
}