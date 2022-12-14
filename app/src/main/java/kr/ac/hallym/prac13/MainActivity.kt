package kr.ac.hallym.prac13

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import kr.ac.hallym.prac13.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var serviceBinder: MyService.MyBinder

    val connection:ServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("kkang","onServiceConnected...")
            serviceBinder = service as MyService.MyBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("kkang","onServiceDisconnected...")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.bindServiceButton.setOnClickListener {
            val intent = Intent(this,MyService::class.java)
            bindService(intent,connection,Context.BIND_AUTO_CREATE)
        }

        binding.unbindServiceButton.setOnClickListener {
            unbindService(connection)
        }

        binding.bindFunACallButton.setOnClickListener {
            serviceBinder.funA(10)
        }

        binding.bindFunBCallButton.setOnClickListener {
            Log.d("kkang","${serviceBinder.funB(20)}")
        }

    }
}