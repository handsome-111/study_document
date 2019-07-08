1.将时间戳转换为时间：
timestampToTime : function (timestamp) {
	var date = new Date(timestamp);
	var Y = date.getFullYear() + '-';
	var M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
	var D = date.getDate() + ' ';
	var h = date.getHours() + ':';
	var m = date.getMinutes() + ':';
	var s = date.getSeconds();
	console.log(timestamp);
	return Y+M+D+h+m+s;
}
2.将时间转换为时间戳:
	var date = new Date('2014-04-23 18:55:49:123');
    // 有三种方式获取
    var time1 = date.getTime();
