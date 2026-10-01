package com.focusforge.app.work
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
class HeartbeatWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params){
 override suspend fun doWork():Result = Result.success()
}
