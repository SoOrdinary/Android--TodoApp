package com.soordinary.todo.utils

/**
 * 公共Diff类，需要传入equals方法，用于比较两个T是否相同
 *
 * @role shortestPath：返回更新序列Node，查询当前Node的getStep，可知该步怎样得来的，一直向前索引最终得到全部流程
 * @role 可直接通过build构建,传入新旧列表与增删规则
 * Todo：双向迭代
 */
class Diff<T>(val equals: (T, T) -> Boolean) {

    // 路径节点类定义：坐标值、前驱节点、如何得到的[null--起点  0--Snack的ij均加1  1--增加j(down)  -1--删除i(right)]
    inner class Node(val i: Int, val j: Int, val pre: Node?, var getStep: Int? = null) {
        // toString打印该节点坐标
        override fun toString(): String {
            return "(${i},${j})"
        }
    }

    fun shortestPath(oldList: List<T>, newList: List<T>): Node {
        // kotlin语法规则不需要判断可能为空
        val N = oldList.size
        val M = newList.size
        val MAX = N + M
        val size = 1 + 2 * MAX
        val middle = size / 2
        // 滚动数组，用于更新每一步下所有k的最优进度
        val curK: Array<Node?> = Array(size) { null }
        curK[middle] = searchSnackEnd(oldList, newList, Node(0, 0, null))
        // 判断是否到达了终点
        if (curK[middle]!!.i >= N && curK[middle]!!.j >= M) {
            return curK[middle]!!
        }
        // 从第一步遍历到理论最大步
        for (d in 1..MAX) {
            // 寻找第d步的所有k
            for (k in -d..d step 2) {
                // 当前d步的某一个k，寻找上一步到下一步能够达到的最小距离
                val toK = middle + k
                val toDownK = toK + 1
                val toRightK = toK - 1
                // 当前k可以由上一步下移，也能右移(i+1)，根据移动后谁的i大决定从谁那边移动，不过要排除边界情况，因为边界只能下移或右移
                // 右移i会加1，所以满足下前i>右前i时，实际移动后是下后i>=右前i，此时也优先选择下，也是有道理的，因为我们要先右后下，所以需要下而不是右，说明再往前已经右过了才需要下
                if ((k == -d) || (k != d && curK[toDownK]!!.i > curK[toRightK]!!.i)) {
                    val lastNode = Node(curK[toDownK]!!.i, curK[toDownK]!!.j + 1, curK[toDownK], 1)
                    curK[toK] = searchSnackEnd(oldList, newList, lastNode)
                } else {
                    val lastNode = Node(curK[toRightK]!!.i + 1, curK[toRightK]!!.j, curK[toRightK], -1)
                    curK[toK] = searchSnackEnd(oldList, newList, lastNode)
                }
                // 判断是否到达了终点
                if (curK[toK]!!.i >= N && curK[toK]!!.j >= M) {
                    return curK[toK]!!
                }
            }
        }
        throw Exception("could not find a diff path");
    }

    // 搜索一步的结尾并返回该点
    private fun searchSnackEnd(oldList: List<T>, newList: List<T>, node: Node): Node {
        var finalNode = node
        var i = node.i
        var j = node.j
        // 如果索引均不超出范围且当前ij下值相等，跃进到对角线
        while (i < oldList.size && j < newList.size && equals(oldList[i], newList[j])) {
            finalNode = Node(++i, ++j, finalNode, 0)
        }
        return finalNode
    }

    // 构建结果，增删
    fun buildCRD(oldList: List<T>, newList: List<T>, deleteMethod: (oldIndex: Int) -> Any?, addMethod: (newIndex: Int) -> Any?) {
        var processNode = shortestPath(oldList, newList)
        while (processNode.pre != null) {
            when (processNode.getStep) {
                // "删除 ${oldList[processNode.i - 1]}"
                -1 -> deleteMethod(processNode.pre!!.i)
                // "保留 ${oldList[processNode.i - 1]}"
                0 -> {}
                // "增加 ${newList[processNode.j - 1]}"
                1 -> addMethod(processNode.pre!!.i)
            }
            processNode = processNode.pre!!
        }
    }

    // 单项更新：移动加修改，同时多项不可用
    fun buildU(oldList: List<T>, newList: List<T>, changeMethod: (oldIndex: Int) -> Any?, deleteMethod: (oldIndex: Int) -> Any?, addMethod: (newIndex: Int) -> Any?) {
        var processNode = shortestPath(oldList, newList)
        while (processNode.pre != null) {
            when (processNode.getStep) {
                // "删除
                -1 -> deleteMethod(processNode.pre!!.i)
                // "保留
                0 -> {}
                // "增加
                1 -> {
                    // 同一位置则用更新，必定先删后增，倒着判断的，所以只需要关心增加前面是不是删就行了
                    if (processNode.pre != null && processNode.pre!!.getStep == -1) {
                        changeMethod(processNode.pre!!.i - 1)
                        return
                    } else {
                        addMethod(processNode.pre!!.i)
                    }
                }
            }
            processNode = processNode.pre!!
        }
    }
}