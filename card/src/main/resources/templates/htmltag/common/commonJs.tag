<script>
    /** 错误消息提示框 */
    function showError(message) {
        bs4pop.alert(message, {type : "error"});
    }

    /** 提示消息弹出框 */
    function showInfo(message) {
        bs4pop.alert(message, {type : "info"});
    }

    /** 警示消息框 */
    function showWarning(message) {
        bs4pop.alert(message, {type : "warning"});
    }

    function getNowFormatDate(nian,yue,ti) {
        var date = new Date();
        var year = date.getFullYear();
        var month = date.getMonth() + 1;
        var strDate = date.getDate();
        if (month >= 1 && month <= 9) {
            month = "0" + month;
        }
        if (strDate >= 0 && strDate <= 9) {
            strDate = "0" + strDate;
        }
        var currentdate = year + nian + month + yue + strDate + ti;
        return currentdate;
    }
    
    /**打印信息 tableId展示的表格id  templateName 打印模板名称  需要向晓辉索取   url 加载打印数据的接口  自定义 */
    function print(tableId, templateName, url){
    	var id = $.common.isEmpty(table.options.uniqueId) ? $.table.selectFirstColumns() : $.table.selectColumns(table.options.uniqueId);
   	 	if (id.length == 0) {
            $.modal.alertWarning("请至少选择一条记录");
            return;
        }
        if(typeof callbackObj != 'undefined'){
            window.printFinish=function(){
            }
            var paramStr ="";
            var data=loadPrintData(id, url);
            debugger;
            if(!data){
                return;
            }
            paramStr = JSON.stringify(data);
            console.log("打印信息--:"+paramStr);
            if(paramStr==""){
                return;
            }
            callbackObj.printDirect(paramStr,templateName);
        }else{
            bs4pop.alert("请检查打印的设备是否已连接", { type: "error" });
        }
    }
    //加载打印数据
    function loadPrintData(id,url){
        console.log("调用打印信息："+id);
        data = {};
    	data.id = id;
        $.ajax({
    	    type: "POST",
    	    url:url,
    	    data:JSON.stringify(data),
    	    contentType: "application/json; charset=utf-8",
    	    traditional:true,
    	    dataType:'json',
    	    async: false,
    	    error: function(result) {
    	    	$.modal.alertError(result.message);
    	    	return "";
    	    },
    	    success: function(result) {
    	    	debugger;
    	    	if(result.code=="200"){
    	    		return result.data;
    	    	}else{
    	    		$.modal.alertError(result.message);
    	    		return "";
    	    	}
    	    }
    	});	
    }

    function randomString(len) {
    	len = len || 16;
	  　　var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
	 　　var maxPos = $chars.length;
	  　　var pwd = '';
	  　　for (i = 0; i < len; i++) {
	  　　　　pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
	 　　}
	 　　return pwd;
    }

</script>
