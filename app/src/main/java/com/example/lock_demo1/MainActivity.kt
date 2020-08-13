package com.example.lock_demo1

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

//    懒加载必须为val
//    对象创建的顺序︰构造方法→>init代码块、属性的创建→>onCreate( setContentView(R. layout.activity_main))
//    private val dotS = arrayOf(yk1,yk2,yk3,yk4,yk5,yk6,yk7,yk8,yk9)

//      在onCreate方法上面调用布局的控件要注意
    private val dotS:Array<ImageView> by lazy {
        return@lazy arrayOf(sDot1,sDot2,sDot3,sDot4,sDot5,sDot6,sDot7,sDot8,sDot9)
    }

    private var rPassward:String? = null
    private var firstParssward:String? = null

    private val barHeight:Int by lazy {
        //获取屏幕的尺⼨
        val display = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(display)
        //获取操作区域的尺⼨
        val drawingRect = Rect()
        window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT).getDrawingRect(drawingRect)
        display.heightPixels - drawingRect.height()
    }

//    记录被点亮的点
    private var selectedpotin = mutableListOf<ImageView>()
    //    保存线的tag值
    private var lineTagArray = arrayOf(12,23,45,56,78,89,14,47,25,58,36,69,15,26,48,59,24,35,57,68)
    private var parward = StringBuffer()
    private var lastSelectedView:ImageView? = null
//    图片获取码
    private val IMAGE_CODE = 123



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rPassward = sharePrefre.getInstance(this).getPassward().also {
            if (it == null){
                tigView.text = "请设置密码"
            }else{
                tigView.text = "请输入密码"
            }
        }
        Log.v("yk", "原始密码 $rPassward")

//        从手机获取头像
        headerView.setOnClickListener(){
            Intent().also {
                it.action = Intent.ACTION_PICK
                it.type = "image/*"
                startActivityForResult(it,IMAGE_CODE)
            }
        }

//        获取头像图片
        File(filesDir,"header.jpg").also {
            if (it.exists()){
                BitmapFactory.decodeFile(it.path).also { yk ->
                    headerView.setImageBitmap(yk)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            IMAGE_CODE ->{
                if (requestCode != Activity.RESULT_CANCELED){
                    val uri:Uri? = data?.data
                    uri?.let {
                        contentResolver.openInputStream(uri).use {
                            BitmapFactory.decodeStream(it).apply {
//                                显示图片
                                headerView.setImageBitmap(this)
//                                缓存图片
//                                创建保存图片的文件
                                var file = File(filesDir,"header.jpg")
                                FileOutputStream(file).also {fors ->
//                                    将图片保存到对应的路径中
                                    compress(Bitmap.CompressFormat.JPEG,50,fors)
                                }
                            }
                        }
                    }

                }
            }
        }
    }




    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val laction = getTouchPoint(event!!)
        if (laction.x >= 0 && laction.x <= ykContainer.width && laction.y >= 0 && laction.y <= ykContainer.height){
            when(event?.action){
                MotionEvent.ACTION_DOWN ->{
                    tigView.text = "请输入密码"

                    findViewByPoint(laction)?.also {
                        if (it.visibility == View.INVISIBLE){
                            it.visibility = View.VISIBLE
                            selectedpotin.add(it)
                            parward.append(it.tag)
                            lastSelectedView = it
                        }
                    }
                }
                MotionEvent.ACTION_MOVE ->{
                    findViewByPoint(laction)?.also {
                        if (it.visibility == View.INVISIBLE){
                            it.visibility = View.VISIBLE
                            selectedpotin.add(it)
                            parward.append(it.tag)

                            if (lastSelectedView == null){
                                lastSelectedView = it
                            }else{
                                var a = lastSelectedView?.tag.toString().toInt()
                                var b = it.tag.toString().toInt()
                                var resultTag = compare(a,b)
                                lastSelectedView = it
                                if (lineTagArray.contains(resultTag)){
                                    ykContainer.findViewWithTag<ImageView>(resultTag.toString()).also {
                                        it.visibility = View.VISIBLE
                                        selectedpotin.add(it)
                                    }
                                }
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    for (item:ImageView in selectedpotin){
                        item.visibility = View.INVISIBLE
                    }
                    setTig()
                    selectedpotin.clear()
                    Log.v("yk", "滑动输入 $parward")
                    sharePrefre.getInstance(this).savePassward(parward.toString())
                    parward.delete(0,parward.length)
                    lastSelectedView = null
                }
            }
        }

        return true
    }

    private fun setTig(){
        if (rPassward == null){
//            设置密码
                if (firstParssward == null){
                    tigView.text = "请确认密码"
                    firstParssward = parward.toString()
                }else{
                    if (firstParssward == parward.toString()){
                        tigView.text = "密码设置成功"
                        sharePrefre.getInstance(this).savePassward(firstParssward!!)
                        rPassward = firstParssward
                    }else{
                        tigView.text = "两次密码不一致"
                    }
                    firstParssward = null
                }
        }else{
//            以有密码，检查输入的是否正确
            if (parward.toString() == rPassward){
                tigView.text = "密码正确"
            }else{
                tigView.text = "密码错误，请重新输入"
            }
        }
    }

//    比较两个数大小
    private fun compare(a:Int, b:Int) = if (a > b) b * 10 + a else a * 10 + b
    
//    获取触摸点坐标
    private fun getTouchPoint(event: MotionEvent):Point{
    var point = Point()
    point.x = event.x.toInt() - ykContainer.x.toInt()
    point.y = event.y.toInt() - 100 - ykContainer.y.toInt()
    return point
   }

//    判断触摸点是否在图中
    private fun findViewByPoint(point: Point):ImageView?{
    for (item:ImageView in dotS){
        if (getViewRect(item).contains(point.x,point.y)){
            return item
        }
    }
    return null
}
    private fun getViewRect(v: View) = Rect(v.left,v.top,v.right,v.bottom)
}
