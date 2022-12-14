package kr.ac.hallym.prac13

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MyService : Service() {

    class MyBinder: Binder(){

        fun funA(arg:Int){
            Log.d("kkang","funA.....$arg")
        }
        fun funB(arg:Int){
            Log.d("kkang","funB.....$arg")
        }

    }


//코루틴 쓰는 이유 anr오류를 해결하기 위해 사용
    override fun onBind(intent: Intent): IBinder {
        Log.d("kkang","service onBind....")
        return MyBinder()
    }

}