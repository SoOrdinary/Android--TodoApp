package com.soordinary.todo.view.fragment.user

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.soordinary.todo.R
import com.soordinary.todo.data.network.socket.DataTransferNew
import com.soordinary.todo.data.network.socket.DataTransferOld
import com.soordinary.todo.data.network.xml.AppVersionInfo
import com.soordinary.todo.data.network.xml.VersionXml
import com.soordinary.todo.databinding.DialogUserChangeNameOrSignatureBinding
import com.soordinary.todo.databinding.DialogUserDataMigrationBinding
import com.soordinary.todo.databinding.DialogUserDataMigrationNewBinding
import com.soordinary.todo.databinding.DialogUserDataMigrationOldBinding
import com.soordinary.todo.databinding.DialogUserMenuLockBinding
import com.soordinary.todo.databinding.DialogUserMenuSubmitBugBinding
import com.soordinary.todo.databinding.DialogUserMenuTaskTagBinding
import com.soordinary.todo.databinding.DialogUserShowMarkdownBinding
import com.soordinary.todo.databinding.FragmentUserBinding
import com.soordinary.todo.utils.MarkDownUtil
import com.soordinary.todo.utils.NetworkUtil
import com.soordinary.todo.utils.encryption.MD5Util
import com.soordinary.todo.view.foreground.download.DownloadService
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * 应用的第四个Fragment，实现一些信息管理
 *
 * @role1 个人信息的更改
 * @role2 管理个人信息的修改
 * @role3 一些系统性菜单的点击事件
 */
class UserFragment : Fragment(R.layout.fragment_user) {

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentUserBinding

    // 相册取照的相关回调函数
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var handleWay: (ActivityResult) -> Unit

    // 下载版本号及请求权限回调
    private var versionCode: Int = 0
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startDownloadService()
        } else {
            Toast.makeText(requireActivity(), "权限被拒绝，无法下载", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserBinding.bind(view)

        // 初始化一个提醒函数体，避免后续未初始化直接调用
        handleWay = { Toast.makeText(requireActivity(), "处理事件未初始化", Toast.LENGTH_SHORT).show() }
        // 注册一个事件返回器
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleWay(result)
        }

        binding.initLiveData()

        binding.initClick()
    }

    // 观察者
    private fun FragmentUserBinding.initLiveData() {

        // 更新个人头像、昵称、签名
        viewModel.getIconUriLiveData().observe(viewLifecycleOwner) {
            Glide.with(icon.context)
                .load(it)  // 图片的 URL
                .downsample(DownsampleStrategy.CENTER_INSIDE) // 根据目标区域缩放图片
                .placeholder(R.drawable.app_icon)  // 占位图
                .apply {
                    into(icon)
                    into(currentIcon)
                }
        }

        viewModel.getNameLiveData().observe(viewLifecycleOwner) {
            name.text = it
            currentName.text = it
        }

        viewModel.getSignatureLiveData().observe(viewLifecycleOwner) {
            signature.text = it
            currentSignature.text = it
        }
    }

    private fun FragmentUserBinding.initClick() {

        // 点击编辑箭头，展开编辑窗来改变个人信息
        editInformation.setOnClickListener {
            changeUserInfo.visibility = if (changeUserInfo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // 点击头像，来更改头像
        currentIcon.setOnClickListener {
            // 处理函数重置，将uri更新
            handleWay = {
                if (it.resultCode == RESULT_OK) {
                    it.data?.data?.let { uri ->
                        // 将图片保存至应用缓存目录
                        try {
                            // 获取图片流
                            val inputStream = requireActivity().contentResolver.openInputStream(uri)
                            // 获取缓存目录
                            val cacheDir = requireContext().cacheDir
                            // 定义要创建的子文件夹名称
                            val imageFolderName = "user_icon_cache"
                            // 创建子文件夹的 File 对象
                            val imageFolder = File(cacheDir, imageFolderName)
                            // 直接创建文件夹，如果已存在则不做任何操作
                            imageFolder.mkdirs()
                            val fileName = "user_icon${System.currentTimeMillis()}.jpg"
                            val file = File(imageFolder, fileName)
                            // 判断原来是否有头像 Todo:若不删除历史图片，可写界面，选择直接切换历史头像
//                            val oldIconUri = viewModel.getIconUriLiveData().value
//                            if (!oldIconUri.isNullOrEmpty()) {
//                                val oldIconFile = File(oldIconUri)
//                                if (oldIconFile.exists()) {
//                                    oldIconFile.delete() // 删除原有图
//                                }
//                            }
                            // 将图片流保存到缓存目录
                            val outputStream = FileOutputStream(file, false)
                            inputStream?.copyTo(outputStream)
                            // 保存为缓存目录中的文件路径
                            viewModel.updateIconUri(file.absolutePath)
                            // 关闭流
                            outputStream.flush()
                            outputStream.close()
                            inputStream?.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(requireActivity(), "图片保存失败", Toast.LENGTH_SHORT).show()
                        }
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
                Toast.makeText(requireActivity(), "error", Toast.LENGTH_SHORT).show()
            }
        }
        // 长按头像可恢复默认头像
        currentIcon.setOnLongClickListener {
            viewModel.updateIconUri("")
            true
        }

        // 点击昵称，来修改昵称
        currentName.setOnClickListener {
            with(DialogUserChangeNameOrSignatureBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                wantToChang.hint = "修改你的昵称"
                confirmChange.setOnClickListener {
                    if (wantToChang.text.isNullOrEmpty()) {
                        Toast.makeText(requireActivity(), "昵称不可为空", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.updateName(wantToChang.text.toString().trim())
                        dialog.dismiss()
                    }
                }

                dialog.show()
            }
        }

        // 点击签名，来修改签名
        currentSignature.setOnClickListener {
            with(DialogUserChangeNameOrSignatureBinding.inflate(LayoutInflater.from(requireActivity()))) {
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
            with(DialogUserMenuTaskTagBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 删除该标签 无则报错
                confirmDelete.setOnClickListener {
                    val tag = changeTag.text.toString().trim()
                    if (tag.isEmpty()) {
                        Toast.makeText(requireActivity(), "删除失败，标签不可为空", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (!viewModel.isContain(tag)) {
                        Toast.makeText(requireActivity(), "删除失败，该标签不存在", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.deleteTaskTag(tag)
                    Toast.makeText(requireActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                    dialog.dismiss()
                }

                // 添加该标签 有则报错
                confirmAdd.setOnClickListener {
                    val tag = changeTag.text.toString().trim()
                    if (tag.isEmpty()) {
                        Toast.makeText(requireActivity(), "添加失败，标签不可为空", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (viewModel.isContain(tag)) {
                        Toast.makeText(requireActivity(), "添加失败，该标签已存在", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.insertTaskTag(tag)
                    Toast.makeText(requireActivity(), "添加成功", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 菜单-密码锁 [添加的密码是MD5形式]
        menuLock.setOnClickListener {
            with(DialogUserMenuLockBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                val currentPassword = viewModel.getPasswordLiveData().value

                // 删除密码 有密码且输入正确才能删除
                confirmDelete.setOnClickListener {
                    val inputPasswordToMD5 = MD5Util.encryptByMD5(changePassword.text.toString().trim())
                    // 判断
                    if (currentPassword.isNullOrEmpty()) {
                        Toast.makeText(requireActivity(), "删除失败，暂无密码", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (inputPasswordToMD5 != currentPassword) {
                        Toast.makeText(requireActivity(), "删除失败，输入密码错误", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.updatePassword("")
                    Toast.makeText(requireActivity(), "删除成功", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                // 添加密码 无密码且有输入才正确
                confirmSet.setOnClickListener {
                    val newPasswordToMD5 = MD5Util.encryptByMD5(changePassword.text.toString().trim())
                    // 判断
                    if (!currentPassword.isNullOrEmpty()) {
                        Toast.makeText(requireActivity(), "添加失败，已有密码", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (changePassword.text.toString().trim().isNullOrEmpty()) {
                        Toast.makeText(requireActivity(), "添加失败，密码不可为空", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.updatePassword(newPasswordToMD5)
                    Toast.makeText(requireActivity(), "添加成功,密码为${changePassword.text.toString().trim()}", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 菜单-功能简介
        menuFunctionIntroduction.setOnClickListener {
            with(DialogUserShowMarkdownBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                val markDown = MarkDownUtil.loadMarkdownFromAssets(requireActivity(), "user_manual.md")
                Markwon.create(requireActivity()).setMarkdown(textContent, markDown)

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
                 
                 gitee链接
                 https://gitee.com/ly0919/todo/releases/download/latest/Todo.zip
                 
                 github链接
                 https://github.com/SoOrdinary/Android--TodoApp/releases/download/latest/Todo.zip
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
            with(DialogUserMenuSubmitBugBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                sendEmail.setOnLongClickListener {
                    // 设置发送邮件的相关API
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:soordinary@foxmail.com") // 指定邮件地址
                        putExtra(Intent.EXTRA_SUBJECT, "Bug Report or Improve Feedback About AndroidApp--Todo") // 邮件标题
                        putExtra(Intent.EXTRA_TEXT, "在此填写内容") // 邮件内容
                    }
                    try {
                        startActivity(Intent.createChooser(emailIntent, "发送邮件"))
                    } catch (e: android.content.ActivityNotFoundException) {
                        Toast.makeText(requireActivity(), "发送失败\uD83D\uDE2D，可借助电脑发送邮件", Toast.LENGTH_SHORT).show()
                        Toast.makeText(requireActivity(), "\uD83D\uDC81soordinary@foxmail.com", Toast.LENGTH_LONG).show()
                    }
                    true
                }

                dialog.show()
            }
        }

        // 菜单-关于作者
        menuAboutAuthor.setOnClickListener {
            with(DialogUserShowMarkdownBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                val markDown = MarkDownUtil.loadMarkdownFromAssets(requireActivity(), "author.md")
                Markwon.create(requireActivity()).setMarkdown(textContent, markDown)

                dialog.show()
            }
        }

        // 菜单-数据迁移
        menuDataMigration.setOnClickListener {
            val currentPassword = viewModel.getPasswordLiveData().value
            if (currentPassword.isNullOrEmpty()) {
                Toast.makeText(requireActivity(), "该操作需先为本机设置密码", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            with(DialogUserDataMigrationBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                if (!NetworkUtil.isValid(requireActivity())) {
                    confirmToSend.isEnabled = false
                    confirmToReceive.isEnabled = false
                    internetHint.text = "该操作需打开WIFI"
                }

                confirmToSend.setOnClickListener {
                    dialog.dismiss()
                    // 唤起转出设置界面
                    transferOld(requireActivity())
                }

                confirmToReceive.setOnClickListener {
                    dialog.dismiss()
                    // 唤起转入设置界面
                    transferNew(requireActivity())
                }

                dialog.show()
            }
        }

        // 菜单-检查更新
        menuCheckVersion.setOnClickListener {
            Toast.makeText(requireActivity(), "正在查询最新版本", Toast.LENGTH_SHORT).show()
            val activity = requireActivity()
            CoroutineScope(Dispatchers.IO).launch {
                val xmlUrl = "https://gitee.com/ly0919/todo/releases/download/lastest/version_info.xml"
                val oldVersion = getAppCurVersionInfo()
                val newVersion = VersionXml.getAppVersionFromXml(xmlUrl)
                // 有新版则提示更新
                withContext(Dispatchers.Main) {
                    if (newVersion == null) {
                        Toast.makeText(activity, "无法请求，请检查网络", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }
                    if ((oldVersion?.versionCode?.toLong() ?: 9999) >= (newVersion.versionCode?.toLong() ?: 0)) {
                        Toast.makeText(activity, "当前已是最新版", Toast.LENGTH_SHORT).show()
                    }
                    val dialogBuilder = AlertDialog.Builder(activity)
                    dialogBuilder.setCancelable(false)
                        .setTitle("更新内容可前往github查看")
                        .setMessage("当前版本: ${oldVersion!!.versionName}\n最新版本: ${newVersion!!.versionName}")
                        .setPositiveButton("更新") { dialog, _ ->
                            versionCode = newVersion.versionCode!!.toInt()
                            if (checkStoragePermission()) {
                                startDownloadService()
                            } else {
                                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("取消") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                }
            }
        }
    }

    // 旧设备转出、新设备转入界面
    private fun transferOld(context: Context) {
        with(DialogUserDataMigrationOldBinding.inflate(LayoutInflater.from(context))) {
            val dialog = Dialog(context)
            dialog.setContentView(root)
            dialog.setCancelable(true)

            oldIp.text = "${NetworkUtil.getLocalIpAddress(context)} -- 本机IP"

            confirm.setOnClickListener {
                if (newPassword.text.isNullOrEmpty()) {
                    Toast.makeText(requireActivity(), "密码不可为空", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dialog.setCancelable(false)
                oldIp.visibility = View.GONE
                newPassword.visibility = View.GONE
                confirm.visibility = View.GONE
                oldLogParent.visibility = View.VISIBLE
                tip.text = "传输数据过程日志"

                val old = DataTransferOld(requireActivity(), 8888, MD5Util.encryptByMD5(newPassword.text.toString()), oldLog) {
                    dialog.setCancelable(true)
                }
                Thread {
                    old.start(false)
                }.start()

            }

            dialog.show()
        }
    }

    private fun transferNew(context: Context) {
        with(DialogUserDataMigrationNewBinding.inflate(LayoutInflater.from(context))) {
            val dialog = Dialog(context)
            dialog.setContentView(root)
            dialog.setCancelable(true)

            confirm.setOnClickListener {
                if (oldIp.text.isNullOrEmpty() || oldPassword.text.isNullOrEmpty()) {
                    Toast.makeText(requireActivity(), "IP与密码不可为空", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dialog.setCancelable(false)
                oldIp.visibility = View.GONE
                oldPassword.visibility = View.GONE
                confirm.visibility = View.GONE
                newLogParent.visibility = View.VISIBLE
                tip.text = "接收数据过程日志"

                val new = DataTransferNew(requireActivity(), oldIp.text.toString(), 8888, MD5Util.encryptByMD5(oldPassword.text.toString()), newLog) {
                    dialog.setCancelable(true)
                }
                Thread {
                    new.start(false)
                }.start()
            }

            dialog.show()
        }
    }

    // 获取当前的版本号
    private fun getAppCurVersionInfo(): AppVersionInfo? {
        try {
            // 获取 PackageManager 实例
            val packageManager = requireActivity().packageManager
            // 获取当前应用的包名
            val packageName = requireActivity().packageName

            // 获取 PackageInfo 对象
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            // 兼容不同 Android 版本获取 versionCode
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            // 获取 versionName
            val versionName = packageInfo.versionName
            return AppVersionInfo(versionCode.toString(), versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    // 检查存储权限
    private fun checkStoragePermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            true
        else
            ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    // 启动下载的服务
    private fun startDownloadService() {
        DownloadService.serviceStart(
            requireActivity(),
            "https://gitee.com/ly0919/todo/releases/download/lastest/Todo.apk",
            // todo：发版后新增改动只能继续往上加code再发，否则用户下载断点续传会合并出bug
            "Todo_Version${versionCode}.apk",
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath.toString()
        )
    }

}