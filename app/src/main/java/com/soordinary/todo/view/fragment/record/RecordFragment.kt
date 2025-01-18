package com.soordinary.todo.view.fragment.record

import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.soordinary.todo.R
import com.soordinary.todo.data.room.entity.RecordSo
import com.soordinary.todo.databinding.DialogRecordAddBinding
import com.soordinary.todo.databinding.DialogRecordDateBinding
import com.soordinary.todo.databinding.DialogRecordDeleteBinding
import com.soordinary.todo.databinding.FragmentRecordBinding
import com.soordinary.todo.utils.DateTimeUtils

/**
 * 应用的第二个Fragment--record，将用户的各种事件记录下来
 */
class RecordFragment:Fragment(R.layout.fragment_record) {


    // 适配器需要使用该viewModel
    val viewModel: RecordViewModel by activityViewModels()
    private lateinit var binding: FragmentRecordBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecordBinding.bind(view)

        // 更新UI
        val timeString = DateTimeUtils.timestampToString(System.currentTimeMillis())
        binding.date.text=DateTimeUtils.getSeparatedStringFromTimestamp(timeString)[0]
        // 初始化RecycleView的配置
        binding.recordList.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL, false
            )
            adapter = RecordAdapter(this@RecordFragment, viewModel.recordList, 0)
        }

        binding.initLiveData()

        binding.initClick()
    }

    // 观察事件
    private fun FragmentRecordBinding.initLiveData(){
        // 观察数据变化实时更新ViewModel的缓存并通知列表更新
        viewModel.recordLiveData.observe(viewLifecycleOwner){
            viewModel.recordList.clear()
            viewModel.recordList.addAll(it)
            // Todo:优化更新方式
            binding.recordList.adapter?.notifyDataSetChanged()
        }
    }

    // 点击事件
    private fun FragmentRecordBinding.initClick() {
        date.setOnClickListener {
            with(DialogRecordDateBinding.inflate(layoutInflater)){
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
                    viewModel.getRecordsByDateRange(dateStart,dateStart+86399999L)
                    // 改变UI
                    val formattedTime = String.format("%04d.%02d.%02d", year, month+1,dayOfMonth)
                    binding.date.text = formattedTime
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }


    inner class ListenRecordItemClick{

        // 长按日志时删除
        fun onLongClickItem(recordSo: RecordSo){
            with(DialogRecordDeleteBinding.inflate(layoutInflater)) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                confirmDelete.setOnClickListener {
                    viewModel.deleteRecord(recordSo)
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 单击底部加号时添加自定义日志
        fun onClickAdd(){
            with(DialogRecordAddBinding.inflate(layoutInflater)) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                confirm.setOnClickListener {
                    val time = System.currentTimeMillis()
                    val record = RecordSo(
                        content=recordContent.text.toString().trim(),
                        planTime = time,
                        finishTime = time
                    )
                    viewModel.insertRecord(record)
                    Toast.makeText(requireActivity(),"新增日志: ${DateTimeUtils.timestampToString(time)}",Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 长按底部加号时添加自定义日志
        fun onLongClickAdd(){
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
                        content=recordContent.text.toString().trim(),
                        planTime = time-timeout.text.toString().toLong()*60000,
                        finishTime = time
                    )
                    viewModel.insertRecord(record)
                    Toast.makeText(requireActivity(),"新增日志: ${DateTimeUtils.timestampToString(time)}",Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }
}