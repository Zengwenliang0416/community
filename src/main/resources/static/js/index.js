$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	console.info("已经点击了")

	$("#publishModal").modal("hide");
	// 发送AJAX请求之前，将CSRF令牌设置到请求的消息头中


	// 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发送异步请求（POST）
	$.post(
		// 三个条件
		// 访问路径
		CONTEXT_PATH + "/discuss/add",
		// 需要传入的数据
		{"title":title,"content":content},
		// 回调函数
		function (data){
			// 返回的结果是一个字符串，需要将其转成对象
			data = $.parseJSON(data);
			// 返回对象之后会得到一个状态——提示消息，在提示框中显示返回的消息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2s后自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 数据添加完了之后怎么办？把当前页面刷新让用户可以看到页面数据
				if (data.code==0){
					window.location.reload();
				}
			}, 2000);
		}

	)
}
