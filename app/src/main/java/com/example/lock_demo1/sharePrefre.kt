package com.example.lock_demo1

import android.content.Context

class sharePrefre private constructor(){
    private val FILE_NAME = "passward"
    private val KEY = "passwardKey"
//    单例
//    伴生对象
    companion object{
        private var instant: sharePrefre? = null
        private var mContext:Context? = null

        fun  getInstance(context: Context):sharePrefre{
            mContext = context
            if (instant == null){
                instant = sharePrefre()
            }
            return instant!!
        }
    }
    fun savePassward(passward: String){
        mContext?.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE).also {
            it?.edit().also {
                it?.putString(KEY,passward)
                it?.apply()
            }
        }
    }

    fun getPassward():String?{
        mContext?.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE).also {
            return it?.getString(KEY,null)
         }
    }
}