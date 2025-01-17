package com.todo.android.view.fragment.task

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.todo.android.BaseActivity
import com.todo.android.R
import com.todo.android.data.room.entity.Task
import com.todo.android.databinding.ActivityTaskAddBinding
import com.todo.android.utils.DateTimeUtils
import com.todo.android.utils.DateTimeUtils.getSeparatedStringFromTimestamp
import com.todo.android.utils.DateTimeUtils.timestampToString
import com.todo.android.utils.SizeUnits

/**
 * 增加Task的界面,目的是短按用于快速添加任务，长按进入更多设置的页面，方便添加描述性内容
 *
 * @role1 设置并新增Task
 *
 * @improve1 Todo：进入和退出动画
 */
class TaskAddActivity : BaseActivity<ActivityTaskAddBinding>() {

    private val taskViewModel: TaskViewModel by viewModels()
    // 相册取照的相关回调函数
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var handleWay:(ActivityResult)->Unit

    companion object{
        // 静态打开方法，指明打开该类需要哪些参数
        fun actionStart(context: Context){
            val intent = Intent(context, TaskAddActivity::class.java).apply{
                // putExtra()
            }
            // 设置进入动画和退出动画
            val options = ActivityOptions.makeCustomAnimation(context, R.anim.activity_add_in, R.anim.activity_add_out)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun getBindingInflate()=ActivityTaskAddBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 初始化一个提醒函数体，避免后续未初始化直接调用
        handleWay= {Toast.makeText(this,"处理事件未初始化",Toast.LENGTH_SHORT).show()}
        // 注册一个事件返回器
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleWay(result)
        }

        // 绑定UI样式与默认值
        with(binding){
            // 分隔时间
            val parts = getSeparatedStringFromTimestamp(timestampToString(System.currentTimeMillis()))
            val day = parts[0]
            val hour = parts[1]
            val minute = parts[2]
            // 绑定标签
            val taskTags = listOf("default") + (taskViewModel.getNowTaskTagsLiveData().value?.toList() ?: emptyList())
            val adapter = ArrayAdapter<String>(this@TaskAddActivity, android.R.layout.simple_spinner_dropdown_item, taskTags)
            taskTag.adapter = adapter


            // 绑定时间
            taskDueDateDay.setText(day)
            taskDueDateHour.setText(hour)
            taskDueDateMinute.setText(minute)
            // 默认选中 "default"
            taskTag.setSelection(0)

            binding.initClick()
        }
    }

    override fun finish() {
        super.finish()
        // Todo:设置退出动画
    }

    // 扩展函数绑定点击事件
    private fun ActivityTaskAddBinding.initClick(){

        // 点击返回退出该活动
        taskReturn.setOnClickListener {
            finish()
        }

        // 点击添加照片
        taskPhoto.setOnClickListener {
            // 处理函数重置，将uri存下来,并将图片控件设置
            handleWay = {
                if (it.resultCode == RESULT_OK) {
                    it.data?.data?.let { uri ->
                        taskPhotoUri.text = uri.toString()
                        val bitmap = BitmapFactory.decodeStream(this@TaskAddActivity.contentResolver.openInputStream(uri))
                        val size15dp=SizeUnits.dpToPx(15)
                        taskPhoto.setImageBitmap(bitmap)
                        taskPhoto.setPadding(size15dp,size15dp,size15dp,size15dp)
                    }
                }
            }
            // 开启相册
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            // 打开事件回调
            try {
                launcher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TaskAddActivity, "error", Toast.LENGTH_SHORT).show()
            }
        }

        // 点击提交task
        submitTask.setOnClickListener {
            // 校验输入，错误则直接返回
            if (!checkInput(taskTitle, taskDueDateDay, taskDueDateHour, taskDueDateMinute)) return@setOnClickListener
            // 成功则插入数据
            val dueTimestamp = DateTimeUtils.stringToTimestamp("${taskDueDateDay.text.toString().trim()}  ${taskDueDateHour.text.toString().trim()}:${taskDueDateMinute.text.toString().trim()}")
            // 生成最终task
            val newTask= Task(
                title = taskTitle.text.toString().trim(),
                subtitle = taskSubtitle.text.toString().trim(),
                details = taskDetails.text.toString().trim(),
                voice = null,
                image = taskPhotoUri.text.toString().trim(),
                dueDate = dueTimestamp,
                isFinish = false,
                tag = taskTag.selectedItem.toString()
            )

            taskViewModel.insertTask(newTask)
            finish()
        }

    }

    // 校验函数(每次修改要保持和TaskFragment中的checkInput同步)
    private fun checkInput(taskTitle: EditText, taskDueDateDay: EditText, taskDueDateHour: EditText, taskDueDateMinute: EditText): Boolean {

        val title = taskTitle.text.toString().trim()
        val day = taskDueDateDay.text.toString().trim()
        val hour = taskDueDateHour.text.toString().trim()
        val minute = taskDueDateMinute.text.toString().trim()

        // 校验任务标题
        if (title.isEmpty()) {
            Toast.makeText(this, "标题不可为空", Toast.LENGTH_SHORT).show()
            return false // 如果标题为空，返回 false
        }

        // 校验日期输入
        if (day.isEmpty() || hour.isEmpty() || minute.isEmpty()) {
            Toast.makeText(this, "日期不可为空", Toast.LENGTH_SHORT).show()
            return false // 如果日期不完整，返回 false
        }
        // Todo:检测具体时间是否规范

        // 所有校验通过，返回 true
        return true
    }


}