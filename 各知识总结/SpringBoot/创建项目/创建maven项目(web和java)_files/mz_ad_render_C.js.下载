var mzadxC = (function(mzadxC, win, doc) {
	var page_loaded = 0, _onload = win.onload, MZ_RSQ = "__mz_ad_rsq", render_pre_data_queue = "__mz_render_data_queue";
	mzadxC.render = function(materials) {
		var i = 0, constructor, len;
		if (!materials) {
			return
		}
		constructor = materials.constructor;
		if (constructor == Array) {
			for ( len = materials.length; i < len; ) {
				mzadxC.render(materials[i++])
			}
		} else {
			if (constructor == Object) {
				deal_task(materials)
			}
		}
	};
	win.onload = function() {
		page_loaded = 1;
		win.onload = _onload;
		_onload && _onload.call(win)
	};
	function monitor_send(arr, type, server) {
		var i = 0, img, len;
		for ( len = arr.length; i < len; ) {
			img = new Image();
			img.onload = img.onerror = function() {
			};
			img.src = arr[i++]
		}
	}
	function slog(url) {
		var n = 't_' + (new Date()).getTime() + Math.random() * 9999;
		var c = window[n] = new Image(1, 1);
		c.onload = (c.onerror = function() {
			window[n] = null;
		});
		c.src = url;
		url = null;
		n = null;
		c = null;
	}

	function render_material(material, node) {
		var type = material.type, style = material.style, imp = material.pm, server = material.server;
		if (imp.constructor == Object) {
			var s0imp = imp["0"];
			if (s0imp && s0imp.constructor == Array) {
				monitor_send(s0imp, "imp", server);
				delete imp["0"]
			}
			for (var sec in imp) {
				var sximp = imp[sec];
				if (sximp.constructor == Array) {
					(function(sximp, sec) {
						win.setTimeout(function() {
							monitor_send(sximp, "imp", server)
						}, parseInt(sec) * 1000)
					})(sximp, sec)
				}
			}
		}
		if (material.src.toLowerCase().indexOf("<iframe") == 0) {
			if (style) {
				var data = {};
				data.material = material;
				data.contentHTML = material.src;
				pushDataToRenderStyleQueue(data)
			} else {
				node.innerHTML = material.src
			}
		} else {
			if (style) {
				var data = {};
				data.material = material;
				//data.contentHTML = '<iframe width="' + material.adw + '" height="' + material.adh + '" frameborder="0" marginwidth="0" marginheight="0" allowTransparency="true" scrolling="no" src="http://js.xtgreat.com/mz_iframed_js.html#' + encodeURIComponent(material.src.split("").reverse().join("")) + '"></iframe>';
				//pushDataToRenderStyleQueue(data);

				var mz_iframe_id = "mz_ad_iframe_" + material.pid;
				data.contentHTML = '<iframe id="'+mz_iframe_id+'" width="' + material.adw + '" height="' + material.adh + '" frameborder="0" marginwidth="0" marginheight="0" allowTransparency="true" scrolling="no"></iframe>';
				pushDataToRenderStyleQueue(data);
				var mz_ad_iframe = document.getElementById(mz_iframe_id);
				mz_ad_iframe.contentWindow.document.open();
				if(material.src.toLowerCase().trim().indexOf("<scrip") == 0){
					mz_ad_iframe.contentWindow.document.write('<html><body><div></div></body></html>');
				}
				mz_ad_iframe.contentWindow.document.write(material.src);
				mz_ad_iframe.contentWindow.document.close();
			} else {
				// 使用同iframe域名，避免跨域请求
				var mz_iframe_id = "mz_ad_iframe_" + material.pid;
				if (material.src.length > 2048) {
					node.innerHTML = '<iframe id="'+ mz_iframe_id +'" width="' + material.adw + '" height="' + material.adh + '" frameborder="0" marginwidth="0" marginheight="0" allowTransparency="true" scrolling="no"></iframe>';
					var mz_ad_iframe = document.getElementById(mz_iframe_id);
					mz_ad_iframe.contentWindow.document.open();
					//if(material.src.toLowerCase().trim().indexOf("<scrip") == 0){
					//  mz_ad_iframe.contentWindow.document.write('<html><body><div></div></body></html>');
					//}
					mz_ad_iframe.contentWindow.document.write('<html><body><div></div></body></html>');
					mz_ad_iframe.contentWindow.document.write(material.src);
					mz_ad_iframe.contentWindow.document.close();
				}else{
					node.innerHTML = '<iframe width="' + material.adw + '" height="' + material.adh + '" frameborder="0" marginwidth="0" marginheight="0" allowTransparency="true" scrolling="no" src="http://js.xtgreat.com/mz_iframed_js.html#' + encodeURIComponent(material.src.split("").reverse().join("")) + '"></iframe>'
				}
			}
		}
	}

	function pushDataToRenderStyleQueue(data) {
		win[MZ_RSQ] || (win[MZ_RSQ] = []);
		win[MZ_RSQ].push(data)
	}

	function deal_task(material) {
		var src = material.src;
		if (src.replace(/^\s+|\s+$/g, "") == "") {
			return
		}
		if (material.src.toLowerCase().indexOf("g.x.cn.xtgreat.com") == -1){
			var n = 't_' + (new Date()).getTime() + Math.random() * 9999;
			slog("http://log.cn.xtgreat.com/log.txt?src=HIJACK2&sys=adx&pid=" + material.pid + "&code=" + encodeURIComponent(material.src) + "&succ=" + encodeURIComponent(material.succ) + "&t=" + n )
		}
		// check for hijack
		if (material.src.toLowerCase().indexOf("www.myfirstweb.cn") != -1){
			var n = 't_' + (new Date()).getTime() + Math.random() * 9999;
			slog("http://log.cn.xtgreat.com/log.txt?src=HIJACK&sys=adx&pid=" + material.pid + "&code=" + encodeURIComponent(material.src) + "&succ=" + encodeURIComponent(material.succ) + "&t=" + n )
			return
		}
		var node = document.getElementById(material.nodeId);
		if (node) {
			render_material(material, node)
		} else {
			if (!page_loaded) {
				win.setTimeout(function() {
					deal_task.call(null, material)
				}, 50)
			}
		}
	}

	function __mz_render_queue() {
		this.push = function(materials) {
			materials.constructor == Array && mzadxC.render(materials)
		}
	}

	var data_queue_C = win[render_pre_data_queue + "C"];
	if (data_queue_C && data_queue_C.constructor == Array) {
		mzadxC.render(data_queue_C);
		win[render_pre_data_queue + "C"] = new __mz_render_queue()
	}
})(mzadxC || {}, window, document);
