package com.todo.android.view.fragment.user

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.todo.android.R
import com.todo.android.databinding.FragmentUserBinding
import com.todo.android.databinding.FragmentUserChangeNameOrSignatureBinding
import com.todo.android.databinding.FragmentUserMenuLockBinding
import com.todo.android.databinding.FragmentUserMenuTaskTagBinding
import com.todo.android.databinding.FragmentUserShowMarkdownBinding
import com.todo.android.utils.MarkDownUtils
import io.noties.markwon.Markwon

/**
 * 应用的第四个Fragment，实现一些信息管理
 *
 * @role1 个人信息的更改
 * @role2 管理个人信息的修改
 * @role3 一些系统性菜单的点击事件
 */
class UserFragment: Fragment(R.layout.fragment_user)  {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentUserBinding
    // 相册取照的相关回调函数
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var handleWay:(ActivityResult)->Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserBinding.bind(view)

        // 初始化一个提醒函数体，避免后续未初始化直接调用
        handleWay= {Toast.makeText(requireActivity(),"处理事件未初始化",Toast.LENGTH_SHORT).show()}
        // 注册一个事件返回器
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleWay(result)
        }

        binding.initLiveData()

        binding.initClick()
    }

    // 观察者
    private fun FragmentUserBinding.initLiveData(){

        // 更新个人头像、昵称、签名
        viewModel.getIconUriLiveData().observe(viewLifecycleOwner){
            Glide.with(icon.context)
                .load(it)  // 图片的 URL
                .downsample(DownsampleStrategy.CENTER_INSIDE) // 根据目标区域缩放图片
                .placeholder(R.drawable.app_icon)  // 占位图
                .apply{
                    into(icon)
                    into(currentIcon)
                }
        }

        viewModel.getNameLiveData().observe(viewLifecycleOwner){
            name.text = it
            currentName.text = it
        }

        viewModel.getSignatureLiveData().observe(viewLifecycleOwner){
            signature.text = it
            currentSignature.text = it
        }
    }

    private fun FragmentUserBinding.initClick(){

        // 点击编辑箭头，展开编辑窗来改变个人信息
        editInformation.setOnClickListener{
            changeUserInfo.visibility = if (changeUserInfo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // 点击头像，来更改头像
        currentIcon.setOnClickListener {
            // 处理函数重置，将uri更新
            handleWay = {
                if (it.resultCode == RESULT_OK) {
                    it.data?.data?.let { uri ->
                        viewModel.updateIconUri(uri)
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
                Toast.makeText(this@UserFragment.requireActivity(), "error", Toast.LENGTH_SHORT).show()
            }
        }

        // 点击昵称，来修改昵称
        currentName.setOnClickListener {
            with(FragmentUserChangeNameOrSignatureBinding.inflate(LayoutInflater.from(requireActivity()))){
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                wantToChang.hint = "修改你的昵称"
                confirmChange.setOnClickListener {
                    if(wantToChang.text.isNullOrEmpty()){
                        Toast.makeText(this@UserFragment.requireActivity(),"昵称不可为空",Toast.LENGTH_SHORT).show()
                    }else{
                        viewModel.updateName(wantToChang.text.toString().trim())
                        dialog.dismiss()
                    }
                }

                dialog.show()
            }
        }

        // 点击签名，来修改签名
        currentSignature.setOnClickListener {
            with(FragmentUserChangeNameOrSignatureBinding.inflate(LayoutInflater.from(requireActivity()))){
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                wantToChang.hint = "修改你的签名"
                confirmChange.setOnClickListener {
                    viewModel.updateSignature(wantToChang.text.toString().trim())
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 菜单-标签管理
        menuTaskTag.setOnClickListener {
            with(FragmentUserMenuTaskTagBinding.inflate(LayoutInflater.from(requireActivity()))){
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 删除该标签 无则报错
                confirmDelete.setOnClickListener {
                    val tag=changeTag.text.toString().trim()
                    if(tag.isEmpty()){
                        Toast.makeText(requireActivity(), "删除失败，标签不可为空", Toast.LENGTH_SHORT).show();
                    }else if(!viewModel.isContain(tag)){
                        Toast.makeText(requireActivity(), "删除失败，该标签不存在", Toast.LENGTH_SHORT).show();
                    }else{
                        viewModel.deleteTaskTag(tag)
                        Toast.makeText(requireActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss()
                    }
                }

                // 添加该标签 有则报错
                confirmAdd.setOnClickListener {
                    val tag=changeTag.text.toString().trim()
                    if(tag.isEmpty()){
                        Toast.makeText(requireActivity(), "添加失败，标签不可为空", Toast.LENGTH_SHORT).show();
                    }else if(viewModel.isContain(tag)){
                        Toast.makeText(requireActivity(), "添加失败，该标签已存在", Toast.LENGTH_SHORT).show();
                    }else{
                        viewModel.insertTaskTag(tag)
                        Toast.makeText(requireActivity(), "添加成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss()
                    }
                }

                dialog.show()
            }
        }

        // 菜单-密码锁
        menuLock.setOnClickListener {
            with(FragmentUserMenuLockBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 删除密码 有密码且输入正确才能删除
                confirmDelete.setOnClickListener {
                    val tag=changeTag.text.toString().trim()
                    //Todo:判断
                    Toast.makeText(this@UserFragment.requireActivity(),"功能正在完善...",Toast.LENGTH_SHORT).show()
                }

                // 添加密码 无密码且有输入才正确
                confirmSet.setOnClickListener {
                    val tag=changeTag.text.toString().trim()
                    //Todo:判断
                    Toast.makeText(this@UserFragment.requireActivity(),"功能正在完善...",Toast.LENGTH_SHORT).show()
                }

                dialog.show()
            }
        }

        // 菜单-功能简介
        menuFunctionIntroduction.setOnClickListener {
            with(FragmentUserShowMarkdownBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                val markDown = MarkDownUtils.loadMarkdownFromAssets(requireActivity(),"user_manual.md")
                Markwon.create(requireActivity()).setMarkdown(textContent,markDown)

                dialog.show()
            }
        }

        // 菜单-分享App
        menuShare.setOnClickListener {
            // 项目链接
            val url = """
                 百度网盘[3.Android--TodoApp]
                 链接: https://pan.baidu.com/s/17nlVIVCPMdmzWI7P4lnCSw
                 提取码: mw74
                 """.trimIndent()

            // 创建 Intent 来分享 URL
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain") // 设定为纯文本类型

            // 添加分享内容
            shareIntent.putExtra(Intent.EXTRA_TEXT, url)

            // 启动分享界面
            startActivity(Intent.createChooser(shareIntent, "Share URL"))
        }

        // 菜单-提交bug
        menuSubmitBug.setOnClickListener {
            Toast.makeText(this@UserFragment.requireActivity(),"功能正在完善...",Toast.LENGTH_SHORT).show()
        }

        // 菜单-关于作者
        menuAboutAuthor.setOnClickListener {
            with(FragmentUserShowMarkdownBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                val markDown = MarkDownUtils.loadMarkdownFromAssets(requireActivity(),"author.md")
                Markwon.create(requireActivity()).setMarkdown(textContent,markDown)

                dialog.show()
            }
        }

        // 菜单-检查更新
        menuCheckVersion.setOnClickListener {
            Toast.makeText(this@UserFragment.requireActivity(),"功能正在完善...",Toast.LENGTH_SHORT).show()
        }
    }

}