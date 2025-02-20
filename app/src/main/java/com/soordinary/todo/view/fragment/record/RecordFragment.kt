package com.soordinary.todo.view.fragment.record

import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.soordinary.todo.R
import com.soordinary.todo.component.ItemSlideDeleteCallback
import com.soordinary.todo.data.room.entity.RecordSo
import com.soordinary.todo.databinding.DialogRecordAddBinding
import com.soordinary.todo.databinding.DialogRecordDateBinding
import com.soordinary.todo.databinding.FragmentRecordBinding
import com.soordinary.todo.utils.DateTimeUtils
import com.soordinary.todo.utils.Diff

/**
 * 应用的第二个Fragment--record，将用户的各种事件记录下来
 */
class RecordFragment : Fragment(R.layout.fragment_record) {


    // 适配器需要使用该viewModel
    val viewModel: RecordViewModel by activityViewModels()
    private lateinit var binding: FragmentRecordBinding
    private lateinit var itemSlideDeleteCallback: ItemSlideDeleteCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecordBinding.bind(view)

        // 更新UI
        val timeString = DateTimeUtils.timestampToString(System.currentTimeMillis())
        binding.date.text = DateTimeUtils.getSeparatedStringFromTimestamp(timeString)[0]
        // 初始化RecycleView的配置
        binding.recordList.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL, false
            )
            adapter = RecordAdapter(this@RecordFragment, viewModel.recordList)
            itemSlideDeleteCallback = ItemSlideDeleteCallback(adapter as ItemSlideDeleteCallback.SlideDeleteListener)
            ItemTouchHelper(itemSlideDeleteCallback).attachToRecyclerView(this)
        }

        binding.initLiveData()

        binding.initClick()
    }

    // 观察事件
    private fun FragmentRecordBinding.initLiveData() {
        // 观察数据变化实时更新ViewModel的缓存并通知列表更新
        with(viewModel) {
            recordLiveData.observe(viewLifecycleOwner) {
                val oldRecord = ArrayList<RecordSo>(recordList)
                recordList.clear()
                recordList.addAll(it)
                val diffTask = Diff<RecordSo> { old, new -> old.id == new.id }
                diffTask.buildCRD(oldRecord, recordList, { binding.recordList.adapter?.notifyItemRemoved(it) }, { binding.recordList.adapter?.notifyItemInserted(it) })
            }
        }
    }

    // 点击事件
    private fun FragmentRecordBinding.initClick() {
        date.setOnClickListener {
            with(DialogRecordDateBinding.inflate(layoutInflater)) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                calendarSet.date = viewModel.getNowDay()

                calendarSet.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    // 计算选中日期的时间戳
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, dayOfMonth, 0, 0, 0) // 月份减 1
                    calendar.set(Calendar.MILLISECOND, 0)
                    val dateStart = calendar.timeInMillis
                    // 查询
                    viewModel.getRecordsByDateRange(dateStart, dateStart + 86399999L)
                    // 改变UI
                    val formattedTime = String.format("%04d.%02d.%02d", year, month + 1, dayOfMonth)
                    binding.date.text = formattedTime
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }


    inner class ListenRecordItemClick {

        // 长按日志时显示该日志完成情况
        fun onLongClickItem(recordSo: RecordSo) {
            val timeDiff = recordSo.finishTime - recordSo.planTime
            val diffStr = formatTimeDifference(timeDiff)
            val content = recordSo.content
            val message = when {
                content.startsWith("##") -> {
                    when (recordSo.isTimeout) {
                        false -> {
                            "🎉   提前 $diffStr 完成"
                        }

                        true -> {
                            "\uD83D\uDE22   超时 $diffStr 搞定"
                        }
                    }
                }

                content.startsWith("&&") -> {
                    "⏰    3--2--1-----"
                }

                else -> {
                    "📝    TodoTodoTodo...."
                }
            }
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        }

        private fun formatTimeDifference(diff: Long): String {
            val absDiff = kotlin.math.abs(diff)
            if (absDiff < 60 * 1000) {
                return "${absDiff / 1000} 秒"
            }
            val converted = DateTimeUtils.convertFromTimestamp(absDiff)
            var result = ""
            if (converted[0] > 0) {
                result += "${converted[0]} 天 "
            }
            if (converted[1] > 0) {
                result += "${converted[1]} 小时 "
            }
            if (converted[2] > 0) {
                result += "${converted[2]} 分钟"
            }
            return result.trim()
        }

        // 单击底部加号时添加自定义日志
        fun onClickAdd() {
            with(DialogRecordAddBinding.inflate(layoutInflater)) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                confirm.setOnClickListener {
                    val time = System.currentTimeMillis()
                    val record = RecordSo(
                        content = recordContent.text.toString().trim(),
                        planTime = time,
                        finishTime = time
                    )
                    viewModel.insertRecord(record)
                    Toast.makeText(requireActivity(), "新增日志: ${DateTimeUtils.timestampToString(time)}", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 长按底部加号时添加自定义日志
        fun onLongClickAdd() {
            with(DialogRecordAddBinding.inflate(layoutInflater)) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // UI绑定
                customizeTime.visibility = View.VISIBLE
                recordDate.setText(DateTimeUtils.timestampToString(System.currentTimeMillis()))
                timeout.setText("0")

                confirm.setOnClickListener {
                    val time = DateTimeUtils.stringToTimestamp(recordDate.text.toString().trim())
                    val record = RecordSo(
                        content = recordContent.text.toString().trim(),
                        planTime = time - timeout.text.toString().toLong() * 60000,
                        finishTime = time
                    )
                    viewModel.insertRecord(record)
                    Toast.makeText(requireActivity(), "新增日志: ${DateTimeUtils.timestampToString(time)}", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 左右滑动日志时删除
        fun onSwipedItem(recordSo: RecordSo) {
            viewModel.deleteRecord(recordSo)
        }
    }
}